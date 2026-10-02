package mcr.richi.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import mcr.murmol.MurmolMod;
import mcr.richi.game.RichiTableState;
import mcr.richi.network.MahjongTablePayload;

/**
 * 牌谱回放（屏幕叠加层，非 Screen——不遮挡鼠标、不暂停游戏输入）：读取服务端发来的 .log 全文，
 * 逐事件回放到风盘实体牌上——每步快照合成 ViewMessage 走 {@link MahjongTableClient#applyReplay}
 * 同一渲染管线（增量 diff），回放期间该桌实时同步被屏蔽，退出（{@link #end}）时恢复真实状态。
 * 左侧半透明叠加层为控制/说明：局目/巡目/指示牌 + 四家手牌/副露/牌河 + 当前动作。
 * 键盘：↑↓ 巡目步进，PgUp/PgDn 切换局目，←→ 切换对局（同桌多局牌谱），P 播放/暂停，End/Esc 退出回放。
 * 回放推导：摸牌 = 牌山尾（吃/碰/明杠后该家不摸，暗杠/加杠后岭上摸）；指示牌数 = 1 + 暗杠/加杠次数。
 */
@EventBusSubscriber(modid = MurmolMod.MODID, value = Dist.CLIENT)
public final class PaipuReplay {
	private static final int PANEL_W = 132;
	private static final int PLAY_INTERVAL = 15; // 播放速度：每 15tick 一步

	private PaipuReplay() {
	}

	// ==================================================================
	// 会话状态（null session = 未在回放）
	// ==================================================================

	private static Session session;

	/** 一次回放会话（一张桌的 .log 全文解析结果 + 播放进度） */
	private static final class Session {
		final long originPacked;
		final boolean fromList; // 从牌谱列表打开：Esc 退出后返回列表
		final List<Game> games = new ArrayList<>();
		int gameIdx, handIdx, cursor; // cursor = 事件下标（0..events.size()，=size 时显示结果）
		List<Snap> frames; // 当前手牌 0..events.size() 的快照缓存
		boolean playing;
		int playCooldown;
		String errorMsg; // 解析失败/无记录提示

		Session(long originPacked, String content, boolean fromList) {
			this.originPacked = originPacked;
			this.fromList = fromList;
			PaipuReplay.parse(this, content);
		}
	}

	/** 从牌谱列表打开回放（点击条目，内容到达时调用；startGame = 列表点选的对局序号） */
	public static void open(long originPacked, String content, boolean fromList, int startGame) {
		end();
		session = new Session(originPacked, content, fromList);
		MahjongTableClient.beginReplay(originPacked); // 回放驱动风盘实体，实时同步暂时屏蔽
		if (startGame > 0 && !session.games.isEmpty())
			session.gameIdx = Math.min(startGame, session.games.size() - 1);
		prepareHand();
	}

	/** 列表屏点击条目时置位：下一次收到牌谱全文按"来自列表"处理（Esc 返回列表） */
	public static boolean pendingFromList;

	/** PaipuPayload.Data 到达时取用（消费式） */
	public static boolean consumePendingFromList() {
		boolean v = pendingFromList;
		pendingFromList = false;
		return v;
	}

	/** 结束回放，恢复桌面真实状态；fromList 时返回牌谱列表 */
	public static void end() {
		if (session == null)
			return;
		long origin = session.originPacked;
		boolean backToList = session.fromList;
		session = null;
		MahjongTableClient.endReplay();
		if (backToList)
			PaipuListScreen.open();
	}

	public static boolean active() {
		return session != null;
	}

	// ==================================================================
	// 解析
	// ==================================================================

	/** 一局的开局数据 + 事件序列 + 结果行 */
	private static final class Hand {
		final String[] init = new String[4];
		String wall = "", dora = "", rinshan = ""; // rinshan = 王牌区岭上 4 张（旧格式日志为空）
		final List<String[]> events = new ArrayList<>(); // T 行 token（不含 T）
		String result = ""; // H 行结果（展示文本）
		String drawResult = ""; // 流局听牌说明
		boolean ron; // 荣和（供终帧点数结算）
		int winSeat = -1, fromSeat = -1, pay; // 荣和支付（终帧点数）
	}

	private static final class Game {
		final String[] names = new String[4];
		final int[] startPoints = { 25000, 25000, 25000, 25000 };
		final List<Hand> hands = new ArrayList<>();
		String end = ""; // E 行
	}

	/** 某一步的整桌快照（含驱动风盘实体所需全部字段） */
	private static final class Snap {
		final List<Integer>[] hands = new List[4];
		final List<Integer>[] rivers = new List[4];
		final String[] melds = new String[4];
		final boolean[] riichi = new boolean[4];
		final int[] riichiRiverIdx = { -1, -1, -1, -1 };
		final int[] riichiSticks = new int[4];
		final int[] points = new int[4];
		int indicators = 1;
		int discards;
		int wallCount;
		int turnSeat = -1;
	}

	private static void parse(Session s, String content) {
		if (content == null || content.isEmpty()) {
			s.errorMsg = "gui.richi.paipu.none";
			return;
		}
		Game g = null;
		Hand h = null;
		for (String line : content.split("\n")) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			String[] t = line.split("\\|", -1);
			switch (t[0]) {
				case "G" -> {
					g = new Game();
					s.games.add(g);
					h = null;
					for (int i = 0; i < 4 && 3 + i < t.length; i++)
						g.names[i] = t[3 + i];
					for (int i = 0; i < 4 && 7 + i < t.length; i++)
						g.startPoints[i] = parseInt(t[7 + i]);
				}
				case "S" -> {
					if (g == null)
						continue;
					h = new Hand();
					for (int i = 0; i < 4 && 1 + i < t.length; i++)
						h.init[i] = t[1 + i];
					h.wall = t.length > 5 ? t[5] : "";
					h.dora = t.length > 6 ? t[6] : "";
					h.rinshan = t.length > 7 ? t[7] : "";
					g.hands.add(h);
				}
				case "T" -> {
					if (h != null && t.length >= 3)
						h.events.add(t);
				}
				case "H" -> {
					if (h == null || g == null)
						continue;
					if (t.length > 2 && t[2].equals("d")) {
						// 流局：掩码 + 待张（按席序）
						int mask = parseInt(t.length > 3 ? t[3] : "0");
						StringBuilder sb = new StringBuilder();
						int k = 0;
						for (int seat = 0; seat < 4; seat++)
							if ((mask & 1 << seat) != 0)
								sb.append(g.names[seat]).append(" 听牌 ")
										.append(t.length > 4 + k ? t[4 + k++] : "").append("  ");
						h.drawResult = sb.toString().trim();
					} else {
						// 荣和：H|n|r|和席|放铳席|翻|符|点数|役种|... / 自摸：H|n|t|和席|翻|符|役种|...
						boolean ron = t.length > 2 && t[2].equals("r");
						int win = parseInt(t.length > 3 ? t[3] : "0");
						String name = nameOf(g, win);
						String body;
						if (ron) {
							h.ron = true;
							h.winSeat = win;
							h.fromSeat = parseInt(t.length > 4 ? t[4] : "0");
							h.pay = parseInt(t.length > 7 ? t[7] : "0");
							String yaku = t.length > 8 ? t[8] : "";
							body = name + " 荣和自 " + nameOf(g, h.fromSeat) + "：" + yaku + "　"
									+ (t.length > 5 ? t[5] : "?") + "翻" + (t.length > 6 ? t[6] : "?") + "符　"
									+ (t.length > 7 ? t[7] : "?") + "点";
						} else {
							String yaku = t.length > 6 ? t[6] : "";
							body = name + " 自摸：" + yaku + "　" + (t.length > 4 ? t[4] : "?") + "翻"
									+ (t.length > 5 ? t[5] : "?") + "符";
						}
						h.result = h.result.isEmpty() ? body : h.result + "\n" + body;
					}
				}
				case "E" -> {
					if (g != null)
						g.end = "终局点数 " + (t.length > 1 ? t[1] : "");
				}
			}
		}
		if (s.games.isEmpty())
			s.errorMsg = "gui.richi.paipu.none";
	}

	private static int parseInt(String str) {
		try {
			return Integer.parseInt(str.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	private static String nameOf(Game g, int seat) {
		return g != null && seat >= 0 && seat < 4 && g.names[seat] != null && !g.names[seat].isEmpty()
				? g.names[seat]
				: "P" + (seat + 1);
	}

	// ==================================================================
	// 快照重建（切局时一次性构建全部帧，事件数 <150，开销可忽略）
	// ==================================================================

	private static void prepareHand() {
		Session s = session;
		if (s == null)
			return;
		s.frames = null;
		Game g = curGame();
		if (g == null)
			return;
		s.handIdx = Math.floorMod(s.handIdx, g.hands.size());
		Hand h = g.hands.get(s.handIdx);
		List<Snap> out = new ArrayList<>();
		Snap s0 = new Snap();
		for (int seat = 0; seat < 4; seat++) {
			s0.hands[seat] = tryParse(h.init[seat]);
			s0.rivers[seat] = new ArrayList<>();
			s0.melds[seat] = "";
			s0.points[seat] = g.startPoints[seat];
		}
		ArrayDeque<Integer> wall = new ArrayDeque<>(tryParse(h.wall));
		// 岭上牌（王牌区 4 张）：暗杠/加杠后的补牌从岭上取，与普通牌山无关。
		// 旧格式日志（无岭上段）退回旧规则：岭上 ≈ 牌山尾。
		ArrayDeque<Integer> rinshan = new ArrayDeque<>(tryParse(h.rinshan));
		s0.wallCount = wall.size();
		out.add(s0);
		String lastMeldNoDrawSeat = null; // 上次吃/碰/明杠的席位（该家切牌不摸）
		boolean lastKong = false; // 上一步是暗杠/加杠（下一步摸岭上）
		for (String[] t : h.events) {
			Snap prev = out.get(out.size() - 1);
			Snap snap = copy(prev);
			int seat = parseInt(t[1]);
			String action = t[2];
			switch (action) {
				case "d" -> {
					// 摸牌推导：吃/碰/明杠后该家不摸；暗杠/加杠后摸岭上；其余摸牌山尾
					boolean noDraw = String.valueOf(seat).equals(lastMeldNoDrawSeat);
					if (noDraw)
						lastMeldNoDrawSeat = null;
					else if (lastKong && !rinshan.isEmpty())
						snap.hands[seat].add(rinshan.pollLast());
					else if (!wall.isEmpty())
						snap.hands[seat].add(wall.pollLast());
					lastKong = false;
					List<Integer> disc = tryParse(t.length > 3 ? t[3] : "");
					if (!disc.isEmpty()) {
						int code = disc.get(0);
						if (!snap.hands[seat].remove(Integer.valueOf(code))) {
							// 红五与普通五等价兜底
							for (int i = snap.hands[seat].size() - 1; i >= 0; i--) {
								int c = snap.hands[seat].get(i);
								if (sameTile(c, code)) {
									snap.hands[seat].remove(i);
									break;
								}
							}
						}
						snap.rivers[seat].add(code);
					}
					snap.discards++;
					if (t.length > 4 && t[4].contains("r")) {
						snap.riichi[seat] = true;
						snap.riichiRiverIdx[seat] = snap.rivers[seat].size() - 1;
						snap.riichiSticks[seat] = 1;
						snap.points[seat] -= 1000;
					}
					snap.turnSeat = seat;
					snap.wallCount = wall.size();
				}
				case "c", "p", "k" -> {
					snap.melds[seat] = appendMeld(snap.melds[seat], t.length > 3 ? t[3] : "");
					int from = parseInt(t.length > 4 ? t[4] : "0");
					if (from >= 0 && from < 4 && !snap.rivers[from].isEmpty())
						snap.rivers[from].remove(snap.rivers[from].size() - 1);
					snap.turnSeat = seat;
					lastMeldNoDrawSeat = String.valueOf(seat);
				}
				case "a", "g" -> {
					for (int code : tryParse(t.length > 3 ? t[3] : ""))
						snap.hands[seat].remove(Integer.valueOf(code));
					snap.melds[seat] = appendMeld(snap.melds[seat], t.length > 3 ? t[3] : "");
					snap.indicators = Math.min(5, snap.indicators + 1); // 暗杠/加杠揭新宝
					snap.turnSeat = seat;
					lastKong = true; // 下一步摸岭上
				}
			}
			out.add(snap);
		}
		// 终帧点数：荣和收付（自摸/流局日志无分账，不推算）
		Snap lastFrame = out.get(out.size() - 1);
		if (h.ron && h.winSeat >= 0 && h.winSeat < 4 && h.fromSeat >= 0 && h.fromSeat < 4) {
			lastFrame.points[h.winSeat] += h.pay;
			lastFrame.points[h.fromSeat] -= h.pay;
		}
		s.frames = out;
		s.cursor = Math.min(s.cursor, out.size() - 1);
		syncToTable();
	}

	/** 当前快照合成 ViewMessage → 驱动风盘实体走同一渲染管线（增量 diff，只动变化的牌） */
	private static void syncToTable() {
		Session s = session;
		Game g = curGame();
		if (g == null || s.frames == null || s.frames.isEmpty())
			return;
		Snap snap = s.frames.get(s.cursor);
		Hand h = g.hands.get(s.handIdx);
		String[] hands = new String[4], rivers = new String[4];
		for (int i = 0; i < 4; i++) {
			snap.hands[i].sort(java.util.Comparator.comparingInt(PaipuReplay::handOrder)); // 回放自动理牌（真实对局手牌由服务端排序，回放侧补齐）
			hands[i] = mcr.richi.game.MahjongTileNotation.format(snap.hands[i]);
			rivers[i] = mcr.richi.game.MahjongTileNotation.format(snap.rivers[i]);
		}
		String[] names = new String[4];
		for (int i = 0; i < 4; i++)
			names[i] = g.names[i] == null ? "" : g.names[i];
		String banner = "";
		if (s.cursor == h.events.size()) {
			banner = !h.result.isEmpty() ? h.result.replace("\n", "  ")
					: (!h.drawResult.isEmpty() ? "【流局】" + h.drawResult : "【流局】");
		}
		MahjongTableClient.applyReplay(new MahjongTablePayload.ViewMessage(
				s.originPacked, 0, true, 0, // viewerSeat/openHand/dealAnim（回放全部明牌）
				hands, new int[4], new int[4], rivers, snap.melds.clone(),
				snap.riichiRiverIdx.clone(), snap.riichiSticks.clone(),
				new int[] { 1, 1, 1, 1 }, snap.points.clone(), // handsExposed=1：回放手牌全部摊开朝上
				names, new String[4], new String[4], new String[4], // 回放无 AI 形象
				h.dora, snap.indicators, snap.wallCount,
				snap.turnSeat, Math.floorMod(s.handIdx, 4) + 1, Math.floorMod(s.handIdx / 4, 4), 0,
				-1, -1, -1, RichiTableState.PHASE_PLAYING, banner));
	}

	private static boolean sameTile(int a, int b) {
		int na = a == 0 ? 5 : a, nb = b == 0 ? 5 : b;
		if (na == nb)
			return true;
		// 花色相同数字相同（10-19 与 11-19 等）
		return a / 10 == b / 10 && (a % 10 == 0 ? 5 : a % 10) == (b % 10 == 0 ? 5 : b % 10);
	}

	/** 手牌理牌序：红五（code 0/10/20）按数字 5 归位（原自然排序会使其排到花色最前） */
	private static int handOrder(int c) {
		int suit = c / 10, d = c % 10;
		int n = d == 0 ? 5 : d;
		return suit * 20 + n * 2 - (d == 0 ? 1 : 0); // 红五紧挨普通五之前
	}

	private static String appendMeld(String melds, String not) {
		return melds.isEmpty() ? not : melds + " " + not;
	}

	private static Snap copy(Snap p) {
		Snap s = new Snap();
		for (int i = 0; i < 4; i++) {
			s.hands[i] = new ArrayList<>(p.hands[i]);
			s.rivers[i] = new ArrayList<>(p.rivers[i]);
			s.melds[i] = p.melds[i];
			s.riichi[i] = p.riichi[i];
			s.riichiRiverIdx[i] = p.riichiRiverIdx[i];
			s.riichiSticks[i] = p.riichiSticks[i];
			s.points[i] = p.points[i];
		}
		s.indicators = p.indicators;
		s.discards = p.discards;
		s.wallCount = p.wallCount;
		s.turnSeat = p.turnSeat;
		return s;
	}

	private static List<Integer> tryParse(String not) {
		try {
			return mcr.richi.game.MahjongTileNotation.parse(not);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private static Game curGame() {
		Session s = session;
		return s != null && !s.games.isEmpty() ? s.games.get(Math.floorMod(s.gameIdx, s.games.size())) : null;
	}

	/** 当前手牌当前步骤的事件说明（overlay 当前动作行） */
	private static String eventText(Session s) {
		Game g = curGame();
		if (g == null || s.frames == null)
			return "";
		Hand h = g.hands.get(s.handIdx);
		if (s.cursor == 0)
			return "开局";
		if (s.cursor == h.events.size()) {
			if (!h.result.isEmpty())
				return "【和牌】" + h.result.replace("\n", "  ");
			if (!h.drawResult.isEmpty())
				return "【流局】" + h.drawResult;
			return "【流局】途中流局";
		}
		String[] t = h.events.get(s.cursor - 1);
		int seat = parseInt(t[1]);
		String name = nameOf(g, seat);
		String detail = t.length > 3 ? t[3] : "";
		return switch (t[2]) {
			case "d" -> name + " 切 " + detail + (t.length > 4 && t[4].contains("r") ? "（立直）" : "");
			case "c" -> name + " 吃 " + detail + "（← " + nameOf(g, parseInt(t.length > 4 ? t[4] : "0")) + "）";
			case "p" -> name + " 碰 " + detail + "（← " + nameOf(g, parseInt(t.length > 4 ? t[4] : "0")) + "）";
			case "k" -> name + " 明杠 " + detail + "（← " + nameOf(g, parseInt(t.length > 4 ? t[4] : "0")) + "）";
			case "a" -> name + " 暗杠 " + detail;
			case "g" -> name + " 加杠 " + detail;
			case "q" -> name + " 九种九牌途中流局";
			default -> "";
		};
	}

	// ==================================================================
	// 输入 / 播放（无 Screen，事件总线全局键盘）
	// ==================================================================

	private static void step(int dir) {
		Session s = session;
		Game g = curGame();
		if (g == null || s.frames == null)
			return;
		int end = g.hands.get(s.handIdx).events.size();
		s.cursor += dir;
		if (s.cursor < 0) {
			if (g.hands.size() > 1) {
				s.handIdx = Math.floorMod(s.handIdx - 1, g.hands.size());
				prepareHand();
				s.cursor = s.frames.size() - 1;
			} else
				s.cursor = 0;
		} else if (s.cursor > end) {
			if (g.hands.size() > 1) {
				s.handIdx = Math.floorMod(s.handIdx + 1, g.hands.size());
				s.cursor = 0;
				prepareHand();
			} else
				s.cursor = end;
		}
		syncToTable();
	}

	private static void switchHand(int dir) {
		Session s = session;
		Game g = curGame();
		if (s == null || g == null || g.hands.isEmpty())
			return;
		s.handIdx = Math.floorMod(s.handIdx + dir, g.hands.size());
		s.cursor = 0;
		prepareHand();
	}

	/** 切换对局（同桌多份牌谱按 G…E 段依次排列） */
	private static void switchGame(int dir) {
		Session s = session;
		if (s == null || s.games.size() <= 1)
			return;
		s.gameIdx = Math.floorMod(s.gameIdx + dir, s.games.size());
		s.handIdx = 0;
		s.cursor = 0;
		prepareHand();
	}

	@SubscribeEvent
	public static void onKey(InputEvent.Key event) {
		if (event.getAction() != GLFW.GLFW_PRESS || session == null)
			return;
		Session s = session;
		switch (event.getKey()) {
			case GLFW.GLFW_KEY_UP -> {
				s.playing = false;
				step(-1);
			}
			case GLFW.GLFW_KEY_DOWN -> {
				s.playing = false;
				step(1);
			}
			case GLFW.GLFW_KEY_PAGE_UP -> {
				s.playing = false;
				switchHand(-1);
			}
			case GLFW.GLFW_KEY_PAGE_DOWN -> {
				s.playing = false;
				switchHand(1);
			}
			case GLFW.GLFW_KEY_LEFT -> switchGame(-1);
			case GLFW.GLFW_KEY_RIGHT -> switchGame(1);
			case GLFW.GLFW_KEY_PAUSE, GLFW.GLFW_KEY_P -> {
				s.playing = !s.playing;
				s.playCooldown = 0;
			}
			case GLFW.GLFW_KEY_END, GLFW.GLFW_KEY_ESCAPE -> end();
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Session s = session;
		if (s == null || !s.playing || Minecraft.getInstance().isPaused())
			return;
		if (--s.playCooldown > 0)
			return;
		s.playCooldown = PLAY_INTERVAL;
		Game g = curGame();
		if (g != null && s.frames != null) {
			int end = g.hands.get(s.handIdx).events.size();
			if (s.cursor < end) {
				s.cursor++;
				syncToTable();
			} else if (g.hands.size() > 1) {
				s.handIdx = Math.floorMod(s.handIdx + 1, g.hands.size());
				s.cursor = 0;
				prepareHand();
			} else
				s.playing = false;
		} else
			s.playing = false;
	}

	// ==================================================================
	// 渲染：左侧半透明叠加层（HUD）
	// ==================================================================

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_replay"),
				(GuiGraphics gfx, DeltaTracker partialTick) -> render(gfx));
	}

	private static void render(GuiGraphics gfx) {
		Session s = session;
		if (s == null)
			return;
		var font = Minecraft.getInstance().font;
		int left = 6, top = 6;
		int screenH = gfx.guiHeight();

		if (s.errorMsg != null) {
			gfx.fill(left, top, left + 140, top + 24, 0x990E141C);
			gfx.drawString(font, Component.translatable(s.errorMsg).getString(), left + 6, top + 8, 0xFFCC66, false);
			return;
		}
		Game g = curGame();
		if (g == null || s.frames == null)
			return;
		Snap snap = s.frames.get(s.cursor);
		Hand h = g.hands.get(s.handIdx);

		// 面板底：先量高度（四家块 + 头尾），再画（超出屏幕截断）
		List<List<String>> seatBlocks = new ArrayList<>();
		for (int seat = 0; seat < 4; seat++)
			seatBlocks.add(seatLines(snap, seat));
		int bh = 30; // 标题+指示牌+动作标题
		for (List<String> b : seatBlocks)
			bh += 14 + b.size() * 11 + 6;
		bh += 38; // 动作说明 + 两行播放提示
		int bottom = Math.min(screenH - 8, top + bh);
		gfx.fill(left, top, left + PANEL_W, bottom, 0x990E141C);
		gfx.fill(left, top, left + PANEL_W, top + 1, 0xFF5588FF);
		gfx.fill(left, bottom - 1, left + PANEL_W, bottom, 0xFF5588FF);

		// 标题行：局目 / 巡目 / 步数（风/局与服务端一致：round=handIdx%4+1，wind=handIdx/4）
		String[] winds = { "東", "南", "西", "北" };
		String wind = winds[Math.floorMod(s.handIdx / 4, 4)];
		int roundNo = Math.floorMod(s.handIdx, 4) + 1;
		String title = wind + roundNo + "局　巡 " + (snap.discards / 4 + 1) + "　" + (s.cursor) + "/" + h.events.size()
				+ (s.playing ? " ▶" : " ‖");
		gfx.drawString(font, title, left + 6, top + 6, 0xFFFFA0, false);
		// 宝牌指示牌
		List<Integer> doraStack = tryParse(h.dora);
		StringBuilder ind = new StringBuilder("指示 ");
		for (int i = 0; i < snap.indicators && 2 * i + 1 < doraStack.size(); i++)
			ind.append(mcr.richi.game.MahjongTileNotation
					.format(List.of(doraStack.get(2 * i + 1)))).append(' ');
		gfx.drawString(font, ind.toString(), left + 6, top + 17, 0x88CCFF, false);

		int y = top + 30;
		for (int seat = 0; seat < 4 && y < bottom - 24; seat++) {
			List<String> lines = seatBlocks.get(seat);
			boolean riichi = snap.riichi[seat];
			gfx.drawString(font, (riichi ? "[立直] " : "") + nameOf(g, seat), left + 6, y,
					seat == Math.floorMod(s.handIdx, 4) ? 0xFFCC66 : 0xFFFFFF, false);
			y += 12;
			for (String l : lines) {
				gfx.drawString(font, l, left + 6, y, 0xE0E0E0, false);
				y += 11;
			}
			y += 5;
		}
		// 当前动作 / 结果
		if (y < bottom - 34) {
			if (s.cursor == h.events.size() && !h.result.isEmpty()) {
				for (String l : h.result.split("\n")) {
					gfx.drawString(font, l, left + 6, y, 0x7FFF7F, false);
					y += 11;
				}
				if (!h.drawResult.isEmpty()) {
					gfx.drawString(font, "【流局】" + h.drawResult, left + 6, y, 0xFFCC66, false);
					y += 11;
				}
				if (s.handIdx == g.hands.size() - 1 && !g.end.isEmpty())
					gfx.drawString(font, g.end, left + 6, y, 0x88CCFF, false);
			} else {
				gfx.drawString(font, eventText(s), left + 6, y, 0x7FFF7F, false);
			}
		}
		gfx.drawString(font, "↑↓ 巡目　PgUp/Dn 局目", left + 6, bottom - 23, 0x909090, false);
		gfx.drawString(font, "←→ 对局　P 播放　End 退出", left + 6, bottom - 12, 0x909090, false);
	}

	/** 单座位展示行：点数 + 副露（手牌/牌河由风盘实体渲染，不再显示文本） */
	private static List<String> seatLines(Snap s, int seat) {
		List<String> out = new ArrayList<>();
		out.add(s.points[seat] + " 点");
		if (!s.melds[seat].isEmpty())
			out.add(s.melds[seat].toString());
		return out;
	}
}
