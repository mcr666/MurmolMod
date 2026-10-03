package mcr.richi.game.riichi;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import mcr.richi.block.FengPanBlock;
import mcr.richi.game.MahjongGameLog;
import mcr.richi.game.MahjongTileNotation;
import mcr.richi.game.RichiTableState;
import mcr.richi.game.RichiTableSync;
import mcr.richi.network.MahjongSettlementPayload;

/**
 * 立直麻雀完整对局状态机（服务端行为拥有者）。
 * 持有 4 家 RiichiPlayer + RiichiWall，行为执行后写回 RichiTableState 字符串并按需
 * 实体级渲染同步（摸打）或整桌重建（吃碰杠/结算）。东风战单局，庄家 = 座位 0。
 *
 * <p>回合流：beginTurn 摸牌 → 打牌 → 响应窗口 60t（荣和 &gt; 碰/明杠 &gt; 吃，先响应先生效）
 * → 无人响应下家摸牌。杠后岭上摸牌 + 揭新宝。立直需门清听牌且 ≥1000 点。</p>
 */
public class RiichiGame {
	/** 响应窗口保底时长（刻，= 满长考银行 20s；实际结束由各待副露家银行耗尽驱动） */
	private static final int CLAIM_WINDOW_TICKS = 400;

	private final ServerLevel level;
	private final BlockPos origin;
	private final RichiTableState st;
	private final RiichiPlayer[] ps = new RiichiPlayer[4];
	private RiichiWall wall = new RiichiWall();

	private int turn = -1;
	/** 当前局（0 起，庄家 = handIndex % 4；场风 = handIndex / 4） */
	private int handIndex;
	/** 总局数（对局类型：一局 1 / 东风战 4 / 半庄战 8） */
	private final int totalHands;
	/** 和牌/终局结算数据缓存（finishRound 前构造；多家荣和按座次每 2s 轮流推送一个） */
	private final List<MahjongSettlementPayload.Settlement> pendingSettlements = new ArrayList<>();
	/** 结算确认：全员（人类）确认或 10s 倒计时结束才清桌续局/终局 */
	private final boolean[] settleConfirmed = new boolean[4];
	/** 是否处于结算等待窗口（防重复推进） */
	private boolean settleWaiting;
	/** 结算阶段：0=无 1=和牌结算界面（播报+确认） 2=点数变化界面（5s 自动确认） */
	private int settlePhase;
	/** 本局开始时点数快照（点数变化界面展示全场收支） */
	private int[] pointsBeforeRound = new int[4];
	/** 待结算界面全部关闭后播报的聊天行（和牌/流局满贯播报延后到点数变化界面结束） */
	private String pendingChat = "";
	/** 本局结算后是否轮庄（庄家输牌 = true；连庄/流局 = false，本场累加） */
	private boolean roundAdvance;
	/** 九种九牌宣言窗口（本回合首次摸牌有效；确认后途中流局） */
	private boolean kyuushuAvailable;
	/** 本局已完成的打牌巡数（天地和判定；每局重置） */
	private int discardsThisHand;
	/** 本局是否已有人鸣牌（双立直判定） */
	private boolean anyCall;
	/** 摸进的是牌山最后一张（海底捞月判定） */
	private boolean lastTileHand;
	/** 打出的是牌山最后一张（河底捞鱼判定） */
	private boolean houteiFlag;
	/** 岭上摸牌后（岭上开花判定） */
	private boolean rinshanFlag;
	/** 抢杠窗口激活（加杠的牌可被荣和） */
	private boolean chankanWindow;
	/** 抢杠荣和标记（计分用） */
	private boolean chankanFlag;
	/** 抢杠窗口对应的加杠家座位 */
	private int chankanKongSeat = -1;
	/** 响应窗口中已宣言荣和的座位（多家荣和，窗口收口统一结算） */
	private final List<Integer> pendingRons = new ArrayList<>();
	/** 当前回合家已摸牌（或吃碰杠后）等待打牌 */
	private boolean waitingDiscard;
	/** 本回合是否刚摸牌（决定能否自摸） */
	private boolean justDrew;
	/** 本回合是否宣言立直（打牌时生效） */
	private boolean pendingRiichi;
	/** 一发标记（立直宣言巡内无人鸣牌前有效） */
	private final boolean[] ippatsu = new boolean[4];

	// ---- 响应窗口（别人打出的牌） ----
	private long claimWindowEnd;
	/** 响应窗口短考截止时刻：短考（思考档位每巡时限）走完才开始消耗长考银行（与打牌回合同款长短考） */
	private long claimShortEnd;
	private int claimDiscarder = -1;
	private int claimTile = -1;
	/** 各家可用响应选项（null = 无） */
	private final List<String>[] claimOptions = new List[4];
	/** 吃的组合缓存：选项 "chi:<idx>" → 手牌两张 code */
	private final List<int[]> chiCombos = new ArrayList<>();

	// ---- 延迟任务（多条并存：结算轮播 / AI 荣和延迟宣言 / 10s 续局） ----
	private record DelayedTask(long at, Runnable run) {
	}

	private final List<DelayedTask> tasks = new ArrayList<>();
	/** 10s 续局任务引用（全员结算确认后取消） */
	private Runnable settleTask;
	/** 待延迟宣言的 AI 荣和数（AI 和牌停 0.5s 再宣布，期间响应窗口不收口） */
	private int aiRonPending;
	/** 待延迟决策的 AI 副露数（速度档中/慢时决策延后，期间响应窗口不收口） */
	private int aiMeldPending;
	/** 当前鸣牌窗口的打牌者座位（-1 = 无窗口）；延迟决策任务用 */
	private int discarderSeat = -1;

	@SuppressWarnings("unchecked")
	public RiichiGame(ServerLevel level, BlockPos origin, RichiTableState st) {
		this.level = level;
		this.origin = origin;
		this.st = st;
		this.totalHands = switch (st.gameType) {
			case 1 -> 4; // 东风战
			case 2 -> 8; // 半庄战
			default -> 1; // 一局
		};
		for (int seat = 0; seat < 4; seat++) {
			boolean ai = st.aiSeat[seat];
			String uuid = st.players[seat] == null ? RiichiBot.aiUuid(seat) : st.players[seat];
			ps[seat] = new RiichiPlayer(seat, uuid, ai ? aiSeatName(seat) : nameOf(uuid), ai, st.points[seat]);
		}
	}

	/** AI 结算显示名：统一走 RiichiBot.seatDisplayName（与 Marker 名牌/管理界面形态名同源）。
	 *  "AI:" + 形象名；同形态出现多个 AI 时才编号 AI:乘黄1 / AI:乘黄2；Murmol NPC 直接叫 "Murmol" */
	private String aiSeatName(int seat) {
		return RiichiBot.seatDisplayName(st, seat);
	}

	private String nameOf(String uuid) {
		try {
			ServerPlayer p = level.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
			if (p != null)
				return p.getGameProfile().getName();
		} catch (IllegalArgumentException ignored) {
		}
		return uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
	}

	/** NPC 座位动作反馈：Murmol NPC 执行操作（打牌/鸣牌/和牌等）时挥手 */
	private void swingIfNpc(int seat) {
		if (!ps[seat].ai)
			return;
		String u = st.players[seat];
		if (u == null || !st.npcUuids.contains(u))
			return;
		if (level.getEntity(java.util.UUID.fromString(u)) instanceof net.minecraft.world.entity.LivingEntity living)
			living.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
	}

	// ==================================================================
	// 开局
	// ==================================================================

	/** 当前局庄家座位（東1局=座位0，此后每局轮转） */
	private int dealerSeat() {
		return handIndex % 4;
	}

	/** 开局/下一局：重建牌山、清各家手牌区、发 4×13 理牌（点数与立直棒跨局保留，长考银行回满）。
	 *  首局（deal）同时为 AI 座位生成随机形态形象。 */
	public void deal() {
		handIndex = 0;
		resetHand();
		FengPanBlock.resolveAvatars(level, st); // 形象解析同时分配各 AI 流派（客户端按同步包渲染假玩家）
		// 形象重解析后刷新 AI 结算名——否则名字停留在构造时的旧形态（随机模式重掷后错位）
		for (int s = 0; s < 4; s++)
			if (ps[s].ai)
				ps[s].name = aiSeatName(s);
		String[] names = new String[4];
		for (int i = 0; i < 4; i++)
			names[i] = ps[i].name + (ps[i].ai && !"npc".equals(st.aiAvatarForms[i])
					? "(AI:" + RiichiBot.FLOW_NAMES[Math.floorMod(st.aiFlows[i], 7)] + ")" : "");
		MahjongGameLog.gameStart(level, origin, totalHands, names, st.points);
	}

	/** 发一局的公共流程（deal 首局 / nextHand 续局） */
	private void resetHand() {
		wall = new RiichiWall();
		pointsBeforeRound = st.points.clone(); // 本局起点点数（点数变化界面用）
		for (RiichiPlayer p : ps) {
			p.hand.clear();
			p.river.clear();
			p.melds.clear();
			p.riichi = false; // 立直不跨局（此前漏重置：下一局每巡被锁定自动摸切）
			p.doubleRiichi = false;
			p.riichiFuriten = false;
			p.tempFuriten = false;
			p.firstDrawDone = false;
			p.discardCount = 0;
		}
		discardsThisHand = 0;
		anyCall = false;
		st.banner = "";
		lastTileHand = false;
		houteiFlag = false;
		rinshanFlag = false;
		chankanFlag = false;
		java.util.Arrays.fill(ippatsu, false);
		java.util.Arrays.fill(st.riichiRiverIdx, -1);
		java.util.Arrays.fill(st.riichiPending, false);
		java.util.Arrays.fill(st.handsExposed, RichiTableState.HAND_STAND);
		java.util.Arrays.fill(st.lastTedashi, 0);
		st.drawnSeat = -1;
		// 读秒库按思考时限档位重置（此前硬编码 20s，覆盖开局套用的档位）
		for (int i = 0; i < 4; i++)
			st.longBank[i] = RichiTableState.THINK_PRESETS[st.thinkIdx][1] * 20;
		for (RiichiPlayer p : ps) {
			for (int i = 0; i < 13; i++) {
				int code = wall.draw();
				if (code < 0)
					break;
				p.hand.add(code);
			}
			p.sortHand();
		}
		st.round = handIndex % 4 + 1;
		st.roundWind = handIndex / 4;
		st.revealedIndicators = 1;
		// S 行：开局快照（回放用，末段 = 王牌区岭上 4 张）
		{
			String[] hs = new String[4];
			for (int i = 0; i < 4; i++)
				hs[i] = MahjongTileNotation.format(ps[i].hand);
			MahjongGameLog.handStart(level, origin, hs, MahjongTileNotation.format(wall.remainingCodes()),
					MahjongTileNotation.format(wall.doraStackTiles()),
					MahjongTileNotation.format(wall.rinshanStackTiles()));
		}
		writeBack();
		st.phase = RichiTableState.PHASE_PLAYING;
		int dealTicks = FengPanBlock.startDealAnimation(level, origin, st);
		messageTable("message.richi.hand_start",
				(st.roundWind == 0 ? "東" : "南") + " " + st.round + "局");
		// 座位切换（轮庄）只调换牌局中的东南西北，不再把玩家传送归位——世界位置保持不动
		broadcastRoundStart();
		schedule(dealTicks, this::beginFirstTurn);
	}

	/** 每局开始向参与玩家聊天播报局目/本场数及各自座位（自风字黄色），如：[東1局-1本场] 您的座位：西 */
	private void broadcastRoundStart() {
		String[] winds = { "東", "南", "西", "北" };
		for (int seat = 0; seat < 4; seat++) {
			net.minecraft.server.level.ServerPlayer p = st.playerOf(level, seat);
			if (p == null)
				continue;
			String selfWind = winds[Math.floorMod(seat - dealerSeat() + 4, 4)];
			p.displayClientMessage(Component.translatable("message.richi.round_start",
					(st.roundWind == 0 ? "東" : "南"), st.round, st.honba,
					Component.literal(selfWind).withStyle(net.minecraft.ChatFormatting.YELLOW)), false);
		}
	}

	private void beginFirstTurn() {
		beginTurn(dealerSeat());
	}

	// ==================================================================
	// 回合推进
	// ==================================================================

	/** 摸牌回合开始：牌尾摸一张，短考重置，检查九种九牌/自摸/立直/杠选项 */
	private void beginTurn(int seat) {
		int drawn = wall.draw();
		if (drawn < 0) {
			ryuukyoku();
			return;
		}
		lastTileHand = wall.isEmpty(); // 海底判定：摸进的是最后一张
		rinshanFlag = false;
		ps[seat].hand.add(drawn);
		// 九种九牌（本局首次摸牌且幺九字牌 ≥9 种）：确认制——本回合可宣言，按数字键确认，不宣则继续
		if (!ps[seat].firstDrawDone) {
			ps[seat].firstDrawDone = true;
			kyuushuAvailable = countTerminalKinds(ps[seat].hand) >= 9;
		}
		startDiscardPhase(seat, true, drawn);
	}

	/** 幺九字牌种类数（九种九牌判定） */
	private static int countTerminalKinds(List<Integer> hand) {
		java.util.Set<Integer> kinds = new java.util.HashSet<>();
		for (int c : hand) {
			int d = c < 30 ? c % 10 : c - 29;
			if (c >= 30 || d == 1 || d == 9)
				kinds.add(c);
		}
		return kinds.size();
	}

	/** 打牌阶段开始（beginTurn 摸牌后 / 吃碰后不摸 / 杠后岭上摸牌） */
	private void startDiscardPhase(int seat, boolean drew, int drawnCode) {
		turn = seat;
		waitingDiscard = true;
		justDrew = drew;
		st.drawnSeat = drew ? seat : -1; // 刚摸的牌在手牌末尾（渲染与原手牌隔开一个牌位）
		pendingRiichi = false;
		st.turnSeat = seat;
		st.turnInLong = false;
		st.turnLimit = RichiTableState.THINK_PRESETS[st.thinkIdx][0] * 20; // 每巡时限按思考时限档位
		st.turnDeadline = level.getGameTime() + st.turnLimit;
		if (drew) {
			writeBack(); // 广播含刚摸的牌（客户端整桌重建）
			// 摸到的牌补交互代理（发牌时只有 13 张常驻代理，否则摸牌点不中）
			FengPanBlock.spawnDrawnInteractor(level, origin, seat, ps[seat].hand.size() - 1);
		}
		// 自家选项（自摸/立直/杠）推给本人
		sendSelfOptions(seat);
		// 立直后摸切（日麻规则：立直者打牌锁定）：无自摸/暗杠机会则自动打出刚摸的牌（AI 与人类一致）
		if (ps[seat].riichi && !(justDrew && canWinNow(seat, true)) && findAnkan(seat) < 0) {
			int last = ps[seat].hand.size() - 1;
			schedule((8 + level.random.nextInt(6)) * aiMul(), () -> playerDiscard(seat, last));
			return;
		}
		if (ps[seat].ai)
			schedule((8 + level.random.nextInt(8)) * aiMul(), this::aiTakeTurn);
	}

	/** AI 打牌速度倍率（快=1 中=2 慢=3）：所有 AI 动作延迟统一乘该系数 */
	private int aiMul() {
		return RichiTableState.AI_SPEED_MUL[Math.floorMod(st.aiSpeedIdx, RichiTableState.AI_SPEED_MUL.length)];
	}

	/** 自家回合可用动作：tsumo / kyuushu / riichi / ankan:code / kakan:code（和牌 > 流局 > 宣言 > 副露） */
	private void sendSelfOptions(int seat) {
		List<String> opts = new ArrayList<>();
		// 自摸（含天和/地和——第一巡且确实构成和牌形时无需常规役）
		if (justDrew && (canWinNow(seat, true) || firstTurnShapeWin(seat)))
			opts.add("tsumo:" + ps[seat].hand.get(ps[seat].hand.size() - 1)); // 带摸牌 code（HUD 画牌面）
		if (kyuushuAvailable)
			opts.add("kyuushu");
		if (canRiichi(seat))
			opts.add("riichi");
		int ankan = findAnkan(seat);
		if (ankan >= 0)
			opts.add("ankan:" + ankan);
		int kakan = findKakan(seat);
		if (kakan >= 0)
			opts.add("kakan:" + kakan);
		if (ps[seat].ai)
			return;
		ServerPlayer p = st.playerOf(level, seat);
		if (p != null) {
			mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin, String.join(";", opts));
			if (!opts.isEmpty())
				playOptionSound(p);
		}
	}

	/** 有可选动作时对本人播放一声拾取经验球音效（仅该玩家听得见，不公开） */
	private void playOptionSound(ServerPlayer p) {
		p.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.0f);
	}

	/**
	 * 推给客户端的选项 token 追加荣和牌 code（HUD 画牌面用）：
	 * ron/pon/minkan → "ron:&lt;claimTile&gt;"。chi 的 token 在构建时已带完整三张。
	 * 内部 claimOptions 保持原 token（contains 匹配不受影响）。
	 */
	private List<String> withClaimTile(List<String> opts) {
		List<String> out = new ArrayList<>(opts.size());
		for (String t : opts) {
			if (t.equals("ron") || t.equals("pon") || t.equals("minkan"))
				out.add(t + ":" + claimTile);
			else
				out.add(t);
		}
		return out;
	}

	/** 清除所有玩家的选项 HUD */
	private void clearAllOptions() {
		for (int seat = 0; seat < 4; seat++) {
			if (ps[seat].ai)
				continue;
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null)
				mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin, "");
		}
	}

	/**
	 * 打出（玩家与 AI 共用）：理牌 → 牌河末尾；实体级同步；立直宣言在此生效；
	 * 随后开响应窗口。
	 */
	public void playerDiscard(int seat, int index) {
		if (!waitingDiscard || turn != seat)
			return;
		RiichiPlayer p = ps[seat];
		if (index < 0 || index >= p.hand.size())
			return;
		swingIfNpc(seat); // NPC 打牌挥手
		// 立直宣言待选：校验所选牌打出后仍听牌（防"宣言后打出破坏听牌的牌仍成立立直"漏洞）
		if (pendingRiichi) {
			int removed = p.hand.remove(index);
			boolean keepsTenpai = Mahjong4jBridge.isTenpai(p.hand, p.melds);
			p.hand.add(index, removed);
			if (!keepsTenpai) {
				cancelRiichi();
				ServerPlayer cp = st.playerOf(level, seat);
				if (cp != null)
					cp.displayClientMessage(Component.translatable("message.richi.riichi_invalid"), true);
				return;
			}
		}
		// 一发过期：立直后再次打牌（一巡已过）；宣言当巡不受影响（此时 p.riichi 尚未置位）
		if (p.riichi && ippatsu[seat])
			ippatsu[seat] = false;
		p.discardCount++;
		discardsThisHand++;
		kyuushuAvailable = false; // 打出后九种九牌机会消失
		houteiFlag = lastTileHand; // 河底判定：打出的是牌山最后一张
		lastTileHand = false;
		rinshanFlag = false;
		// 长考阶段打出：结算本回合实际消耗的长考银行
		if (st.turnInLong)
			st.longBank[seat] = (int) Math.max(0, st.turnDeadline - level.getGameTime());
		waitingDiscard = false;
		justDrew = false;
		// 手切/摸切标记（隐藏手牌的牌背动画 key 错位用）：摸切 = 本巡刚摸牌且打出最右一张
		st.lastTedashi[seat] = (st.drawnSeat == seat && index == p.hand.size() - 1) ? 0 : 1;
		st.drawnSeat = -1; // 摸牌已打出/摸切
		// 摸牌位的交互代理随打出移除（其余 13 张代理与理牌后的固定槽位对齐）
		FengPanBlock.removeHandInteractor(level, origin, seat, p.hand.size() - 1);
		st.clearSelection(); // 选中牌随广播降下（客户端处理升降）
		p.tempFuriten = false; // 同巡振听到自己的打牌为止
		int code = p.hand.remove(index);
		boolean riichiDeclared = pendingRiichi; // 牌谱事件旗（置位在下方立直块消费）
		p.sortHand();
		p.river.add(code);
		// 立直宣言牌被鸣后：下一张切牌改为横摆，补回立直标记
		if (st.riichiPending[seat]) {
			st.riichiPending[seat] = false;
			st.riichiRiverIdx[seat] = p.river.size() - 1;
		}
		if (pendingRiichi) {
			p.riichi = true;
			p.doubleRiichi = p.discardCount == 1 && !anyCall; // 第一巡立直且无人鸣牌 = 双立直
			st.riichiSticks[seat]++;
			st.riichiRiverIdx[seat] = p.river.size() - 1; // 宣言牌横摆渲染
			st.points[seat] -= 1000;
			p.points -= 1000;
			ippatsu[seat] = true;
			pendingRiichi = false;
			// 宣言牌特效：旋转扩散的 astral_burst 粒子环
			FengPanBlock.spawnRiichiBurst(level, origin,
					FengPanBlock.riverTilePos(level, origin, seat, st.riichiRiverIdx[seat]));
			broadcast(Component.translatable("message.richi.riichi_chat", p.name).getString());
			// 立直音效：敲铁砧
			Vec3 c = FengPanBlock.tableCenter(level, origin);
			level.playSound(null, c.x, c.y, c.z, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.6f, 1.3f);
		}
		st.clearSelection();
		writeBack();
		// T 行：切牌事件（旗 r = 本巡宣言立直）
		MahjongGameLog.turnEvent(level, origin, seat, "d",
				MahjongTileNotation.format(List.of(code)) + (riichiDeclared ? "|r" : ""));
		Vec3 riverPos = FengPanBlock.riverTilePos(level, origin, seat, st.riverCodes(seat).size() - 1);
		level.playSound(null, riverPos.x, riverPos.y, riverPos.z,
				SoundEvents.DECORATED_POT_HIT, SoundSource.PLAYERS, 0.9f, 1.25f);
		// 开响应窗口（无人可选 → 直接下家）
		openClaimWindow(seat, code);
	}

	/** 超时自动处理：可自摸则自动胡牌（倒计时结束自动胡），否则摸切（打最右一张）。
	 *  超时若处于立直宣言待选：取消宣言（不替玩家自动成立立直），再常规摸切。 */
	public void autoDiscard() {
		if (!waitingDiscard || turn < 0)
			return;
		if (pendingRiichi)
			pendingRiichi = false;
		if (justDrew && (canWinNow(turn, true) || firstTurnShapeWin(turn))) {
			clearAllOptions();
			doTsumo(turn);
			return;
		}
		playerDiscard(turn, ps[turn].hand.size() - 1);
	}

	// ==================================================================
	// 响应窗口
	// ==================================================================

	/** 打出后开响应窗口：荣和 > 碰/明杠 > 吃（下家），先响应先生效；多家可同时荣和 */
	private void openClaimWindow(int discarder, int tile) {
		claimDiscarder = discarder;
		claimTile = tile;
		discarderSeat = discarder; // 延迟副露决策任务引用
		claimWindowEnd = level.getGameTime() + CLAIM_WINDOW_TICKS;
		claimShortEnd = 0; // 选项收集完由 armClaimTiming 统一设定
		chiCombos.clear();
		pendingRons.clear();
		boolean anyHumanOption = false;
		for (int seat = 0; seat < 4; seat++) {
			claimOptions[seat] = null;
			if (seat == discarder)
				continue;
			List<String> opts = new ArrayList<>();
			// 地和：闲家荣和本局第一张打出的牌（须构成和牌形）
			if (canRon(seat) || isChihoWin(seat)) {
				opts.add("ron");
				if (ps[seat].ai)
					scheduleAiRon(seat); // AI 停 0.5s 再宣布和牌
			}
			if (!ps[seat].riichi) {
				// 红五与普通五等价计数（如手里 1 红5 + 2 普通5 也可碰红5声明牌）
				int equiv = 0;
				for (int c : ps[seat].hand)
					if (sameTile(c, tile))
						equiv++;
				if (equiv >= 2)
					opts.add("pon");
				if (equiv >= 3)
					opts.add("minkan");
				// 吃：仅下家、数牌
				if (seat == (discarder + 1) % 4 && tile < 30) {
						int t = tile == 0 ? 5 : tile;
						if (t / 10 < 3) {
							for (int[] combo : chiCandidates(ps[seat].hand, t)) {
								chiCombos.add(combo);
								// token 携带完整三张（声明牌 + 手牌两张），HUD 画组合区分不同吃法
								opts.add("chi:" + (chiCombos.size() - 1) + ":" + tile + ","
										+ combo[0] + "," + combo[1]);
							}
						}
					}
			}
			if (!opts.isEmpty()) {
				claimOptions[seat] = opts;
				if (!ps[seat].ai) {
					anyHumanOption = true;
					ServerPlayer p = st.playerOf(level, seat);
					if (p != null) {
						mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin,
								String.join(";", withClaimTile(opts)));
						playOptionSound(p); // 待副露提示音：仅该玩家听得见（不公开谁可副露）
					}
					// 等待倒计时只推给该家（不公开）：短考秒数 + 长考银行
					sendClaimCountdown(seat,
							RichiTableState.THINK_PRESETS[st.thinkIdx][0],
							(st.longBank[seat] + 19) / 20);
				}
			}
		}
		// AI 副露决策（按流派：0/1/4/6 可副露，2 门清流/3 御无双/5 闪电流不副露）：
		// 荣和优先级最高（已在 pendingRons/延迟宣言）；有人可荣和时 AI 不副露（不抢人类荣和机会），
		// 有人可碰/杠时 AI 不吃。决策延迟按速度档执行（快=立即，中/慢停顿后再吃碰）
		armClaimTiming();
		if (pendingRons.isEmpty() && aiRonPending == 0) {
			aiMeldPending++;
			schedule(10 * aiMul(), () -> {
				aiMeldPending--;
				if (claimWindowEnd <= 0 || discarderSeat < 0)
					return;
				int dsc = discarderSeat;
				boolean humanRon = false, humanMeld = false;
				for (int s = 0; s < 4; s++) {
					if (ps[s].ai || claimOptions[s] == null)
						continue;
					if (claimOptions[s].contains("ron"))
						humanRon = true;
					if (claimOptions[s].contains("pon") || claimOptions[s].contains("minkan"))
						humanMeld = true;
				}
				if (humanRon)
					return;
				for (int seat = 0; seat < 4; seat++) {
					if (!ps[seat].ai || seat == dsc || claimOptions[seat] == null)
						continue;
					int flow = Math.floorMod(st.aiFlows[seat], 7);
					if (flow != 0 && flow != 1 && flow != 4 && flow != 6)
						continue; // 该流派不副露
					List<String> opts = claimOptions[seat];
					if (opts.contains("minkan")) {
						execAiMeld(seat, Fuuro.Type.MINKAN, null);
						return;
					}
					boolean canChi = !humanMeld && opts.stream().anyMatch(t -> t.startsWith("chi:"));
					if (opts.contains("pon") || canChi) {
						// CalculateMeld 鸣牌评估：与不鸣牌基线分比较（场况：巡目/手内宝牌/场风/自风）
						int turns = ps[0].river.size() + ps[1].river.size() + ps[2].river.size()
								+ ps[3].river.size();
						List<Integer> doraInds = wall.revealedDoras();
						int ownDora = 0;
						for (int c : ps[seat].hand) {
							if (c == 0 || c == 10 || c == 20)
								ownDora++; // 赤5
							for (int ind : doraInds)
								if (c == RiichiWall.doraTileOf(ind)) {
									ownDora++;
									break;
								}
						}
						int selfWind = 27 + Math.floorMod(seat - dealerSeat() + 4, 4);
						TileEfficiency.MeldOption m = RiichiBot.bestMeld(ps[seat], buildVisible34(seat),
								claimTile, canChi, turns, ownDora, 27 + st.roundWind, selfWind, flow);
						if (m != null && (!m.pon || opts.contains("pon"))) {
							execAiMeld(seat, m.pon ? Fuuro.Type.PON : Fuuro.Type.CHII,
									m.pon ? null : m.selfCodes);
							return;
						}
					}
				}
				// 无 AI 副露：若窗口本应由本方法收口且人类已全部表态，则收口
				boolean anyHuman = false;
				for (int s = 0; s < 4; s++)
					if (!ps[s].ai && claimOptions[s] != null)
						anyHuman = true;
				if (!anyHuman && pendingRons.isEmpty() && aiRonPending == 0) {
					claimWindowEnd = 0;
					advanceTurn();
				}
			});
			return;
		}
		if (!anyHumanOption && pendingRons.isEmpty() && aiRonPending == 0 && aiMeldPending == 0) {
			claimWindowEnd = 0;
			advanceTurn();
		} else {
			resolveWindow(); // 有 AI 荣和宣言且人类均已表态 → 立即多家结算
		}
	}

	/** AI 荣和延迟宣言（按速度档制造"宣布和牌"节奏；期间响应窗口不收口） */
	private void scheduleAiRon(int seat) {
		aiRonPending++;
		schedule(10 * aiMul(), () -> {
			aiRonPending--;
			if (claimWindowEnd <= 0 || claimOptions[seat] == null || !claimOptions[seat].contains("ron"))
				return;
			pendingRons.add(seat);
			claimOptions[seat] = null;
			swingIfNpc(seat); // NPC 荣和宣言挥手
			resolveWindow();
		});
	}

	/** AI 吃决策已并入 RiichiBot.bestMeld（CalculateMeld 鸣牌评估，综合分取舍） */

	/** AI 执行副露（等价 tryClaim 的碰/杠/吃路径） */
	private void execAiMeld(int seat, Fuuro.Type type, int[] chiPair) {
		swingIfNpc(seat); // NPC 副露挥手
		claimWindowEnd = 0;
		clearAllOptions();
		doOpenMeld(seat, type, claimTile, chiPair);
	}

	/**
	 * 响应窗口收口判定：人类全部表态后——有多家荣和 → 统一结算；
	 * 抢杠窗口无人荣和 → 杠家继续岭上；否则正常推进下家。
	 */
	private void resolveWindow() {
		if (claimWindowEnd <= 0)
			return;
		if (aiRonPending > 0)
			return; // 有 AI 荣和待宣言（0.5s 延迟），窗口保持开放
		for (int s = 0; s < 4; s++)
			if (!ps[s].ai && claimOptions[s] != null)
				return; // 还有未表态的人类
		claimWindowEnd = 0;
		if (!pendingRons.isEmpty()) {
			List<Integer> winners = new ArrayList<>(pendingRons);
			pendingRons.clear();
			boolean wasChankan = chankanWindow;
			chankanWindow = false;
			chankanFlag = wasChankan; // 结算时保持抢杠标记
			doMultiRon(winners);
			chankanFlag = false;
		} else if (chankanWindow) {
			chankanWindow = false;
			chankanFlag = false;
			finishKong(chankanKongSeat);
		} else {
			closeWindowAndAdvance();
		}
	}

	/** 吃组合枚举：hand 中能与 tile 组成顺子的两两组合（tile 可为最小/中间/最大张） */
	private List<int[]> chiCandidates(List<Integer> hand, int tile) {
		List<int[]> result = new ArrayList<>();
		int suit = tile / 10, d = tile % 10 == 0 ? 5 : tile % 10; // 红五(0/10/20)按 5 计
		// 组合 1：tile 为最小（d+1, d+2）；组合 2：中间（d-1, d+1）；组合 3：最大（d-2, d-1）
		int[][] wants = { { d + 1, d + 2 }, { d - 1, d + 1 }, { d - 2, d - 1 } };
		for (int[] w : wants) {
			if (w[0] < 1 || w[1] > 9)
				continue;
			int a = findCode(hand, suit * 10 + w[0]);
			int b = findCode(hand, suit * 10 + w[1], a);
			if (a >= 0 && b >= 0)
				result.add(new int[] { hand.get(a), hand.get(b) });
		}
		return result;
	}

	private int findCode(List<Integer> hand, int code) {
		return findCode(hand, code, -1);
	}

	private int findCode(List<Integer> hand, int code, int exclude) {
		for (int i = 0; i < hand.size(); i++)
			if (i != exclude && hand.get(i) == code)
				return i;
		// 找 5 时可用红五（code-5）替代
		if (code % 10 == 5 && code < 30) {
			for (int i = 0; i < hand.size(); i++)
				if (i != exclude && hand.get(i) == code - 5)
					return i;
		}
		return -1;
	}

	/** 窗口超时：清窗口 → 下家 */
	private void closeWindowAndAdvance() {
		claimWindowEnd = 0;
		claimDiscarder = -1;
		claimTile = -1;
		clearAllOptions();
		advanceTurn();
	}

	/** 无人鸣牌 → 下家回合（一发标记保留：立直一巡内有效，直到该家再次打牌才过期） */
	private void advanceTurn() {
		beginTurn((turn + 1) % 4);
	}

	// ==================================================================
	// 响应处理（吃/碰/明杠/荣和）
	// ==================================================================

	/** 玩家响应（先响应先生效；选项必须仍在窗口内且属于该玩家） */
	private void tryClaim(int seat, String action, int comboIdx) {
		if (claimWindowEnd <= 0 || seat == claimDiscarder)
			return;
		List<String> opts = claimOptions[seat];
		if (opts == null)
			return;
		switch (action) {
				case "ron" -> {
					if (!opts.contains("ron"))
						return;
					// 多家荣和：先记录，待全员表态后统一结算
					pendingRons.add(seat);
					claimOptions[seat] = null;
					ServerPlayer rp = st.playerOf(level, seat);
					if (rp != null)
						mcr.richi.network.MahjongGamePayloads.sendOptions(rp, origin, "");
					resolveWindow();
				}
			case "pon", "minkan" -> {
				if (!opts.contains(action))
					return;
				claimWindowEnd = 0;
				clearAllOptions();
				doOpenMeld(seat, action.equals("pon") ? Fuuro.Type.PON : Fuuro.Type.MINKAN, claimTile, null);
			}
			case "chi" -> {
				// token 为完整三张格式（"chi:<idx>:<tile>,<a>,<b>"），须按前缀匹配组合索引
				// （全串 contains 永远失配 → 数字键吃牌静默无效；与 bestAiChi 的解析方式一致）
				String key = "chi:" + comboIdx + ":";
				if (comboIdx < 0 || comboIdx >= chiCombos.size() || opts.stream().noneMatch(t -> t.startsWith(key)))
					return;
				int[] combo = chiCombos.get(comboIdx);
				claimWindowEnd = 0;
				clearAllOptions();
				doOpenMeld(seat, Fuuro.Type.CHII, claimTile, combo);
			}
		}
	}

	/** 执行吃/碰/明杠：手牌移除对应牌 + 声明牌移出打牌者牌河 → 副露 → 打牌阶段（不摸牌） */
	private void doOpenMeld(int seat, Fuuro.Type type, int calledTile, int[] chiPair) {
		RiichiPlayer p = ps[seat];
		RiichiPlayer from = ps[claimDiscarder];
		int[] tiles;
		switch (type) {
			case CHII -> {
				tiles = new int[] { chiPair[0], chiPair[1], calledTile };
				for (int t : new int[] { chiPair[0], chiPair[1] })
					p.hand.remove(Integer.valueOf(t));
			}
			case PON -> tiles = takeMeldTiles(p, calledTile, 2);
			default -> tiles = takeMeldTiles(p, calledTile, 3); // MINKAN
		}
		// 声明牌移出打牌者牌河（渲染随整桌重建更新）
		if (!from.river.isEmpty())
			from.river.remove(from.river.size() - 1);
		// 被取走的若是该家立直宣言牌 → 取消横摆标记，该家下一张切牌改为横摆补回
		if (st.riichiRiverIdx[claimDiscarder] >= from.river.size()) {
			st.riichiRiverIdx[claimDiscarder] = -1;
			st.riichiPending[claimDiscarder] = true;
		}
		// 有人鸣牌 → 全部一发失效；双立直判定失效
		java.util.Arrays.fill(ippatsu, false);
		anyCall = true;
		st.drawnSeat = -1; // 副露家未摸牌（声明牌不在手牌中）
		p.melds.add(new Fuuro(type, tiles, calledTile, claimDiscarder));
		// T 行：吃(c)/碰(p)/明杠(k)
		MahjongGameLog.turnEvent(level, origin, seat,
				type == Fuuro.Type.CHII ? "c" : type == Fuuro.Type.PON ? "p" : "k",
				MahjongTileNotation.format(java.util.Arrays.stream(tiles).boxed().toList())
						+ "|" + claimDiscarder);
		writeBack();
		clearAllOptions();
		if (type == Fuuro.Type.MINKAN)
			finishKong(seat); // 明杠：与暗杠/加杠一致，岭上摸牌 + 揭新宝
		else
			startDiscardPhase(seat, false, -1);
		messageTable("message.richi.called", p.name);
		broadcast(Component.translatable("message.richi.called_chat", p.name, tileName(calledTile)).getString());
		// 鸣牌音效：吃 = 吃完食物；碰 = 末影珍珠触地；明杠 = 铁砧放置（finishKong 内已放，避免双重）
		Vec3 c = FengPanBlock.tableCenter(level, origin);
		if (type != Fuuro.Type.MINKAN) {
			var sound = switch (type) {
				case CHII -> SoundEvents.PLAYER_BURP;
				case PON -> SoundEvents.ENDERMAN_TELEPORT;
				default -> SoundEvents.ANVIL_PLACE;
			};
			level.playSound(null, c.x, c.y, c.z, sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}

	/**
	 * 碰/明杠取牌：手牌中与声明牌同种（红五与普通五等价）的 count 张移入副露，
	 * 红五优先（红宝牌立即可见），避免声明牌为红五时按原码匹配凑不齐三张。
	 */
	private int[] takeMeldTiles(RiichiPlayer p, int calledTile, int count) {
		int[] tiles = new int[count + 1];
		tiles[0] = calledTile;
		int n = 1;
		for (int i = 0; i < p.hand.size() && n < tiles.length; i++)
			if (sameTile(p.hand.get(i), calledTile)) {
				tiles[n++] = p.hand.get(i);
				p.hand.remove(i--);
			}
		return tiles;
	}

	// ==================================================================
	// 杠（暗杠/加杠，明杠走 doOpenMeld）
	// ==================================================================

	/** 同种牌判定（红五万 0 与五万 5 视为同牌） */
	private static boolean sameTile(int a, int b) {
		int na = a == 0 ? 5 : a, nb = b == 0 ? 5 : b;
		return na == nb;
	}

	/** 暗杠候选：手牌中某牌恰有 4 张，返回该牌 code */
	private int findAnkan(int seat) {
		int[] count = new int[37];
		for (int c : ps[seat].hand)
			count[c]++;
		for (int c = 0; c < 37; c++)
			if (count[c] == 4)
				return c;
		return -1;
	}

	/** 加杠候选：已有碰副露 + 手牌中有同种牌（红五/普通五互通），返回要加入的 code */
	private int findKakan(int seat) {
		RiichiPlayer p = ps[seat];
		if (p.riichi)
			return -1;
		for (Fuuro f : p.melds) {
			if (f.type != Fuuro.Type.PON)
				continue;
			int code = f.calledTile == 0 ? 5 : f.calledTile; // 红五万归一化为五万
			if (p.hand.contains(code))
				return code;
			// 手里是红五也可加杠普通五
			if (code % 10 == 5 && code < 30 && p.hand.contains(code - 5))
				return code - 5;
		}
		return -1;
	}

	private void doKong(int seat, String kind, int code) {
		swingIfNpc(seat);
		if (code < 0)
			return;
		RiichiPlayer p = ps[seat];
		st.drawnSeat = -1; // 摸进的牌被杠消耗（岭上摸牌后由 startDiscardPhase 重新置位）
		Fuuro fuuro;
		if (kind.equals("ankan")) {
			int n = 0;
			int[] tiles = new int[4];
			for (int i = p.hand.size() - 1; i >= 0 && n < 4; i--) {
				if (p.hand.get(i) == code) {
					tiles[n++] = code;
					p.hand.remove(i);
				}
			}
			if (n < 4)
				return;
			fuuro = new Fuuro(Fuuro.Type.ANKAN, tiles, code, seat);
		} else { // kakan：找同 code 的碰副露升级
			Fuuro pon = null;
			for (Fuuro f : p.melds) {
				if (f.type == Fuuro.Type.PON && sameTile(f.calledTile, code)) {
					pon = f;
					break;
				}
			}
			if (pon == null || !p.hand.contains(code))
				return;
			p.hand.remove(Integer.valueOf(code));
			int[] tiles = new int[4];
			System.arraycopy(pon.tiles, 0, tiles, 0, 3);
			tiles[3] = code;
			p.melds.remove(pon);
			fuuro = new Fuuro(Fuuro.Type.KAKAN, tiles, code, pon.fromSeat);
		}
		p.melds.add(fuuro);
		anyCall = true;
		// T 行：暗杠(a 四张)/加杠(g 一张)
		MahjongGameLog.turnEvent(level, origin, seat, kind.equals("ankan") ? "a" : "g",
				MahjongTileNotation.format(java.util.Arrays.stream(fuuro.tiles).boxed().toList()));
		broadcast(Component.translatable("message.richi.kong_chat", p.name, tileName(code)).getString());
		// 加杠 → 抢杠检查：他家可荣和该张则开抢杠窗口，无人抢才岭上摸牌
		if (kind.equals("kakan")) {
			List<Integer> claimers = new ArrayList<>();
			for (int s = 0; s < 4; s++)
				if (s != seat && canRonTile(s, code))
					claimers.add(s);
			if (!claimers.isEmpty()) {
				openChankanWindow(seat, code, claimers);
				return;
			}
		}
		finishKong(seat);
	}

	/** 抢杠窗口开启：仅允许荣和该加杠张；全员响应后统一结算 */
	private void openChankanWindow(int kongSeat, int tile, List<Integer> claimers) {
		chankanWindow = true;
		chankanFlag = true;
		chankanKongSeat = kongSeat;
		claimDiscarder = kongSeat;
		claimTile = tile;
		claimWindowEnd = level.getGameTime() + CLAIM_WINDOW_TICKS;
		claimShortEnd = 0;
		pendingRons.clear();
		chiCombos.clear();
		for (int s : claimers) {
			claimOptions[s] = List.of("ron");
			if (ps[s].ai) {
				scheduleAiRon(s); // AI 抢杠同样停 0.5s 再宣布
			} else {
				ServerPlayer p = st.playerOf(level, s);
				if (p != null) {
					mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin, "ron:" + tile);
					playOptionSound(p);
					sendClaimCountdown(s, RichiTableState.THINK_PRESETS[st.thinkIdx][0],
							(st.longBank[s] + 19) / 20);
				}
			}
		}
		armClaimTiming();
		writeBack();
		resolveWindow();
	}

	/** 杠收尾：岭上摸牌 + 揭新宝（明牌）+ 进入打牌阶段 */
	private void finishKong(int seat) {
		RiichiPlayer p = ps[seat];
		int rinshan = wall.rinshanDraw();
		if (rinshan >= 0)
			p.hand.add(rinshan);
		wall.revealNextDora();
		st.revealedIndicators = wall.revealedIndicatorCount();
		rinshanFlag = true;
		lastTileHand = wall.isEmpty();
		writeBack();
		clearAllOptions();
		startDiscardPhase(seat, true, rinshan);
		// 杠音效：铁砧放置
		Vec3 c = FengPanBlock.tableCenter(level, origin);
		level.playSound(null, c.x, c.y, c.z, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6f, 1.0f);
	}

	// ==================================================================
	// 荣和 / 自摸 / 流局 / 结算
	// ==================================================================

	/** 荣和结算（多家荣和支持）：放铳者按各家点数支付，本场费每家 300×本场，立直棒给最靠右的和牌者 */
	private void doMultiRon(List<Integer> winners) {
		winners.sort(Integer::compareTo); // 结算按座次顺序展示
		int sticks = totalRiichiSticks();
		// 立直棒归最靠近放铳者右边的和牌者（头跳）
		int stickWinner = -1;
		for (int k = 1; k <= 4 && stickWinner < 0; k++)
			if (winners.contains((claimDiscarder + k) % 4))
				stickWinner = (claimDiscarder + k) % 4;
		winEffect(FengPanBlock.riverTilePos(level, origin, claimDiscarder, ps[claimDiscarder].river.size() - 1));
		var resultBySeat = new java.util.LinkedHashMap<Integer, Mahjong4jBridge.WinResult>();
		String names = "", details = "", bannerDetails = "";
		int total = 0;
		for (int w : winners) {
			RiichiPlayer p = ps[w];
			List<Integer> full = new ArrayList<>(p.hand);
			full.add(claimTile);
			// 地和：闲家荣和本局第一张打出的牌（须构成和牌形）
			boolean chiho = isChihoWin(w);
			var r = chiho
					? tenhouChihoResult(w, false, false, p, full)
					: Mahjong4jBridge.score(full, p.melds, w, dealerSeat(), st.roundWind, false,
							p.riichi, ippatsu[w], ronFlags(p), wall.actualDoras());
			if (!r.win())
				continue;
			resultBySeat.put(w, r);
			int gain = r.score().getRon() + 300 * st.honba;
			st.points[claimDiscarder] -= gain;
			st.points[w] += gain + (w == stickWinner ? sticks : 0);
			ps[claimDiscarder].points -= gain;
			p.points += gain + (w == stickWinner ? sticks : 0);
			total += gain;
			names = names.isEmpty() ? p.name : names + "、" + p.name;
			details = details.isEmpty() ? describeResult(r) : details + "；" + describeResult(r);
			bannerDetails = bannerDetails.isEmpty() ? shortDescribe(r) : bannerDetails + "；" + shortDescribe(r);
		}
		if (resultBySeat.isEmpty())
			return; // 无人真正和牌：不清立直棒、不结束本局（防软锁与棒凭空消失）
		java.util.Arrays.fill(st.riichiSticks, 0);
		// 各和牌家结算缓存（finishRound 延迟 2s 起按座次每 2s 轮流推送）
		for (var e : resultBySeat.entrySet()) {
			RiichiPlayer p = ps[e.getKey()];
			List<Integer> full = new ArrayList<>(p.hand);
			full.add(claimTile);
			// 结算界面显示的 gain 不含场供/立直棒（点数变化界面展示全场收支）
			int gain = e.getValue().score().getRon();
			pendingSettlements.add(new MahjongSettlementPayload.Settlement(
					MahjongSettlementPayload.MODE_HAND_WIN, origin.asLong(), p.name, "ron",
					describeResult(e.getValue()), csv(full), csvMelds(p), claimTile,
					p.riichi ? csv(wall.uraIndicatorTiles()) : "", csv(wall.revealedDoras()),
					buildYakuList(p, full, e.getValue()), e.getValue().han(), e.getValue().fu(), gain,
					seatNames(), st.points.clone()));
			MahjongGameLog.win(level, origin, handIndex + 1, true, e.getKey(), claimDiscarder,
					String.join("+", e.getValue().yakuNames().stream().map(YakuNames::zh).toList()),
					e.getValue().han(), e.getValue().fu(),
					e.getValue().score().getRon() + 300 * st.honba,
					MahjongTileNotation.format(p.hand), csvMelds(p));
		}
		String pay = Component.translatable("message.richi.pay", ps[claimDiscarder].name, total).getString();
		finishRound("message.richi.win_ron", names, details, bannerDetails, pay,
				resultBySeat.keySet().stream().mapToInt(Integer::intValue).toArray());
		// 聊天播报延后：结算界面（含点数变化）全部关闭后再发
		pendingChat = Component.translatable("message.richi.win_ron", names, details).getString() + " " + pay;
		// 击飞放铳者：原地垂直向上弹起约 2 格
		ServerPlayer loser = st.playerOf(level, claimDiscarder);
		if (loser != null) {
			loser.setDeltaMovement(0, 0.9, 0);
			loser.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(loser));
		}
	}

	/** 自摸计分 flags：双立直/岭上开花/海底捞月 + 里宝牌 */
	private Mahjong4jBridge.Flags tsumoFlags(RiichiPlayer p) {
		return new Mahjong4jBridge.Flags(p.doubleRiichi, false, rinshanFlag, lastTileHand, false, wall.uraDoras());
	}

	/** 荣和计分 flags：双立直/抢杠/河底捞鱼 + 里宝牌 */
	private Mahjong4jBridge.Flags ronFlags(RiichiPlayer p) {
		return new Mahjong4jBridge.Flags(p.doubleRiichi, chankanFlag, false, false, houteiFlag, wall.uraDoras());
	}

	/** 天和：庄家在本局无任何打牌时自摸 */
	private boolean isTenhou(int seat) {
		return discardsThisHand == 0 && seat == dealerSeat();
	}

	/** 第一巡（天地和窗口）且手里 14 张确实构成和牌形——散牌第一巡不能白吃役满 */
	private boolean firstTurnShapeWin(int seat) {
		return discardsThisHand == 0 && Mahjong4jBridge.isWinnableShape(ps[seat].hand, ps[seat].melds);
	}

	/** 地和：闲家荣和本局第一张打出的牌且能构成和牌形 */
	private boolean isChihoWin(int seat) {
		if (discardsThisHand != 1 || seat == dealerSeat())
			return false;
		List<Integer> full = new ArrayList<>(ps[seat].hand);
		full.add(claimTile);
		return Mahjong4jBridge.isWinnableShape(full, ps[seat].melds);
	}

	private void doTsumo(int seat) {
		swingIfNpc(seat);
		if (!waitingDiscard || !justDrew)
			return;
		RiichiPlayer p = ps[seat];
		// 天和：庄家开局第一摸即和；地和：闲家在第一巡（无任何打牌）自摸——都必须先构成和牌形
		boolean tenhou = isTenhou(seat);
		boolean chihoTsumo = discardsThisHand == 0 && seat != dealerSeat();
		boolean shapeWin = Mahjong4jBridge.isWinnableShape(new ArrayList<>(p.hand), p.melds);
		if (!(tenhou || chihoTsumo) && (!shapeWin || !canWinNow(seat, true)))
			return;
		var r = tenhou || chihoTsumo
				? tenhouChihoResult(seat, true, tenhou, p, new ArrayList<>(p.hand))
				: Mahjong4jBridge.score(new ArrayList<>(p.hand), p.melds, seat, dealerSeat(), st.roundWind, true,
						p.riichi, ippatsu[seat], tsumoFlags(p), wall.actualDoras());
		if (!r.win())
			return;
		winEffect(FengPanBlock.handTilePos(level, origin, seat, p.hand.size() - 1));
		boolean parent = seat == dealerSeat();
		int[] pays = new int[4];
		if (parent) {
			int each = r.score().getParentTsumo();
			// 庄家自摸：其余三家各付亲分（庄家可能不在 0 号位，不能只遍历 1..3）
			for (int i = 0; i < 4; i++)
				if (i != seat)
					pays[i] = each;
		} else {
			// 闲家自摸：庄家付亲分，其余闲家付子分（含 0 号位庄家，不能只遍历 1..3）
			for (int i = 0; i < 4; i++)
				if (i != seat)
					pays[i] = i == dealerSeat() ? r.score().getParent() : r.score().getChild();
		}
		int baseTotal = 0;
		for (int i = 0; i < 4; i++) {
			if (i == seat)
				continue;
			baseTotal += pays[i];
		}
		// 自摸本场费：其余三家各付 100×本场数（不计入结算界面 gain，只计入实际转帐）
		if (st.honba > 0)
			for (int i = 0; i < 4; i++)
				if (i != seat)
					pays[i] += 100 * st.honba;
		int total = 0;
		for (int i = 0; i < 4; i++) {
			if (i == seat)
				continue;
			st.points[i] -= pays[i];
			ps[i].points -= pays[i];
			total += pays[i];
		}
		st.points[seat] += total + totalRiichiSticks();
		p.points += total;
		java.util.Arrays.fill(st.riichiSticks, 0);
		StringBuilder payInfo = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			if (i != seat && pays[i] > 0)
				payInfo.append(ps[i].name).append(" -").append(pays[i]).append(' ');
		}
		pendingSettlements.add(new MahjongSettlementPayload.Settlement(
				MahjongSettlementPayload.MODE_HAND_WIN, origin.asLong(), p.name, "tsumo", describeResult(r),
				csv(p.hand), csvMelds(p), p.hand.isEmpty() ? -1 : p.hand.get(p.hand.size() - 1),
				p.riichi ? csv(wall.uraIndicatorTiles()) : "", csv(wall.revealedDoras()),
				buildYakuList(p, new ArrayList<>(p.hand), r), r.han(), r.fu(), baseTotal, seatNames(),
				st.points.clone()));
		MahjongGameLog.win(level, origin, handIndex + 1, false, seat, -1,
				String.join("+", r.yakuNames().stream().map(YakuNames::zh).toList()),
				r.han(), r.fu(), 0, MahjongTileNotation.format(p.hand), csvMelds(p));
		finishRound("message.richi.win_tsumo", p.name, describeResult(r), shortDescribe(r),
				payInfo.toString().trim(), seat);
		pendingChat = Component.translatable("message.richi.win_tsumo", p.name, describeResult(r)).getString()
				+ " " + payInfo.toString().trim();
	}

	/** 和牌展示文案：役种（中文）+ 翻符（役满显示役名） */
	private String describeResult(Mahjong4jBridge.WinResult r) {
		String names = r.yakuNames().stream().map(YakuNames::zh)
				.reduce((a, b) -> a + "·" + b).orElse("");
		// 宝牌条目按总数追加（宝牌×N、里宝牌×K、红宝牌×M），不显示具体牌面
		List<String> extras = new ArrayList<>();
		appendDora(extras, "宝牌", r.doraHits());
		appendDora(extras, "红宝牌", r.redHits());
		appendDora(extras, "里宝牌", r.uraHits());
		if (!extras.isEmpty())
			names = (names.isEmpty() ? "" : names + "·") + String.join("·", extras);
		if (r.yakuman())
			return names + "（" + yakumanTier(r) + "）";
		return names + "　" + r.han() + "翻" + r.fu() + "符";
	}

	/** 天和/地和结算：+1 倍役满，并与手牌役满叠加（雀魂倍数累计；非役满牌型仍按 1 倍役满计）。fullHand 为含和牌张的 14 张手牌 */
	private Mahjong4jBridge.WinResult tenhouChihoResult(int seat, boolean tsumo, boolean tenhou, RiichiPlayer p,
			List<Integer> fullHand) {
		String flag = tenhou ? "TENHO" : "CHIHO";
		// 先按普通牌型打分：若本身成立役满则叠加倍数（雀魂：天地和与手牌役满累计）
		var inner = Mahjong4jBridge.score(fullHand, p.melds, seat, dealerSeat(), st.roundWind, tsumo,
				p.riichi, ippatsu[seat], tsumo ? tsumoFlags(p) : ronFlags(p), wall.actualDoras());
		int count = 1;
		List<String> names = new ArrayList<>(List.of(flag));
		if (inner.win() && inner.yakuman()) {
			count += inner.han(); // inner.han 编码役满倍数
			names.addAll(inner.yakuNames());
		}
		return new Mahjong4jBridge.WinResult(true, true, count, 0, names,
				Mahjong4jBridge.ScoreVal.yakuman(seat == dealerSeat(), count));
	}

	/** 役满段位文案：han 编码倍数（fu=0），1 倍显示"役满"，n 倍显示"n倍役满" */
	private String yakumanTier(Mahjong4jBridge.WinResult r) {
		return r.han() > 1 ? r.han() + "倍役满" : "役满";
	}

	/** 桌心横幅短描述：不含役种说明，只保留番数符数（役满显示倍数段位） */
	private String shortDescribe(Mahjong4jBridge.WinResult r) {
		if (r.yakuman())
			return "（" + yakumanTier(r) + "）";
		return "　" + r.han() + "翻" + r.fu() + "符";
	}

	/**
	 * 结算界面役种条目（";" 分隔，逐条播报）：役种（中文）→ 宝牌×N → 红宝牌×M → 里宝牌×K。
	 * full = 手牌（荣和含荣和牌）；副露牌面另行计入。
	 */
	private String buildYakuList(RiichiPlayer p, List<Integer> full, Mahjong4jBridge.WinResult r) {
		List<String> entries = new ArrayList<>();
		for (String n : r.yakuNames())
			if (!n.equals("RED_DORA") && !n.equals("URADORA"))
				entries.add(YakuNames.zh(n));
		List<Integer> doras = wall.actualDoras();
		List<Integer> uras = p.riichi ? wall.uraDoras() : List.of();
		// 宝牌逐张按牌面聚合（如：宝牌·發×2、里宝牌·白×1、红宝牌·5万×1），不堆叠多条"宝牌"
		List<Integer> reds = new ArrayList<>();
		List<Integer> doraHits = new ArrayList<>();
		List<Integer> uraHits = new ArrayList<>();
		for (int c : full) {
			if (c == 0 || c == 10 || c == 20) {
				reds.add(c);
				continue;
			}
			if (doras.contains(c))
				doraHits.add(c);
			if (uras.contains(c))
				uraHits.add(c);
		}
		for (Fuuro f : p.melds)
			for (int t : f.tiles) {
				if (t == 0 || t == 10 || t == 20) {
					reds.add(t);
					continue;
				}
				if (doras.contains(t))
					doraHits.add(t);
			}
		appendDora(entries, "宝牌", doraHits);
		appendDora(entries, "红宝牌", reds);
		appendDora(entries, "里宝牌", uraHits);
		return String.join(";", entries);
	}

	/** 宝牌条目只计总数（宝牌×2），不显示具体牌面 */
	private static void appendDora(List<String> entries, String label, List<Integer> codes) {
		if (!codes.isEmpty())
			entries.add(label + "×" + codes.size());
	}

	/** 和牌特效：烟花发射音 + 和牌位置 astral_burst 粒子圆圈（水平半径 0.2，桌面略上方） */
	private void winEffect(Vec3 tilePos) {
		level.playSound(null, tilePos.x, tilePos.y, tilePos.z,
				SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.0f);
		int points = 12;
		for (int i = 0; i < points; i++) {
			double a = Math.PI * 2 * i / points;
			level.sendParticles(mcr.murmol.init.MurmolModParticleTypes.ASTRAL_BURST.get(),
					tilePos.x + Math.cos(a) * 0.2, tilePos.y + 0.1, tilePos.z + Math.sin(a) * 0.2,
					1, 0, 0, 0, 0);
		}
	}

	private int totalRiichiSticks() {
		int sum = 0;
		for (int n : st.riichiSticks)
			sum += n * 1000;
		return sum;
	}

	/** 九种九牌宣言确认（C→S）：途中流局（本场+1、连庄、无听牌罚符） */
	private void doKyuushu(int seat) {
		swingIfNpc(seat);
		if (!kyuushuAvailable || turn != seat || !waitingDiscard)
			return;
		kyuushuAvailable = false;
		MahjongGameLog.turnEvent(level, origin, seat, "q", "");
		// 九种九牌途中流局：流局者亮牌，其余三家盖牌
		finishRound("message.richi.kyuushu", "", "", "", new int[0], new int[] { seat });
		broadcast(Component.translatable("message.richi.kyuushu", ps[seat].name).getString());
	}

	/** 流局满贯（幺九字牌宣言失败）：满贯自摸向其余三家收款，立直棒归流满家 */
	private void doNagashiMangan(java.util.List<Integer> winners) {
		int sticks = totalRiichiSticks();
		int stickWinner = winners.get(winners.size() - 1); // 多家成立归最后一家（简化）
		String names = "", pay = "";
		for (int seat : winners) {
			RiichiPlayer p = ps[seat];
			boolean parent = seat == dealerSeat();
			org.mahjong4j.Score score = org.mahjong4j.Score.calculateScore(parent, 5, 30); // 满贯
			int[] pays = new int[4];
			if (parent) {
				// 满贯自摸：其余三家各付亲分（庄家可能不在 0 号位，不能只遍历 1..3）
				for (int i = 0; i < 4; i++)
					if (i != seat)
						pays[i] = score.getParentTsumo();
			} else {
				// 庄家付亲分，其余闲家付子分（跳过和牌家自己）
				for (int i = 0; i < 4; i++)
					if (i != seat)
						pays[i] = i == dealerSeat() ? score.getParent() : score.getChild();
			}
			int total = 0;
			for (int i = 0; i < 4; i++) {
				if (i == seat)
					continue;
				st.points[i] -= pays[i];
				ps[i].points -= pays[i];
				total += pays[i];
			}
			int gain = total + (seat == stickWinner ? sticks : 0);
			st.points[seat] += gain;
			p.points += total;
			names = names.isEmpty() ? p.name : names + "、" + p.name;
			String payLine = Component.translatable("message.richi.pay", p.name, gain).getString();
			pay = pay.isEmpty() ? payLine : pay + " " + payLine;
			// 结算界面显示的 gain 不含立直棒
			pendingSettlements.add(new MahjongSettlementPayload.Settlement(
					MahjongSettlementPayload.MODE_HAND_WIN, origin.asLong(), p.name, "nagashi", "流局满贯",
					csv(p.hand), csvMelds(p), -1, "", csv(wall.revealedDoras()), "流局满贯",
					0, 0, total, seatNames(), st.points.clone()));
		}
		java.util.Arrays.fill(st.riichiSticks, 0);
		finishRound("message.richi.nagashi_mangan", names, "", pay,
				winners.stream().mapToInt(Integer::intValue).toArray());
		pendingChat = Component.translatable("message.richi.nagashi_mangan", names).getString() + " " + pay;
	}

	/** 流局：牌山摸尽 + 流局满贯检查 + 听牌罚符（不听向听牌者支付，共 3000：1-2-1 / 1.5-1.5 / 3×1000） */
	private void ryuukyoku() {
		// 流局满贯：手牌与牌河全部为幺九字牌 → 按自摸满贯向其余三家收款（多家成立各自结算）
		java.util.List<Integer> nagashi = new ArrayList<>();
		for (int seat = 0; seat < 4; seat++) {
			boolean all = !ps[seat].hand.isEmpty() && !ps[seat].river.isEmpty();
			for (int c : ps[seat].hand)
				if (!(c >= 30 || c % 10 == 1 || c % 10 == 9)) {
					all = false;
					break;
				}
			if (all)
				for (int c : ps[seat].river)
					if (!(c >= 30 || c % 10 == 1 || c % 10 == 9)) {
						all = false;
						break;
					}
			if (all)
				nagashi.add(seat);
		}
		if (!nagashi.isEmpty()) {
			doNagashiMangan(nagashi);
			return;
		}
		java.util.List<Integer> tenpai = new ArrayList<>();
		java.util.List<Integer> noten = new ArrayList<>();
		for (int seat = 0; seat < 4; seat++) {
			(Mahjong4jBridge.isTenpai(ps[seat].hand, ps[seat].melds) ? tenpai : noten).add(seat);
		}
		StringBuilder pay = new StringBuilder();
		if (!tenpai.isEmpty() && !noten.isEmpty()) {
			// 标准 1-2-1 拆分：总罚符 3000，每对（听牌×不听）支付 3000/(听牌数×不听数)
			int payPair = 3000 / (tenpai.size() * noten.size());
			for (int t : tenpai) {
				for (int n : noten) {
					st.points[n] -= payPair;
					ps[n].points -= payPair;
					st.points[t] += payPair;
					ps[t].points += payPair;
				}
			}
			pay.append(Component.translatable("message.richi.noten_pay").getString());
		}
		// 流局时亮出各家听牌：听牌家显示等待张，未听显示"未听牌"
		boolean[] logTenpai = new boolean[4];
		String[] logWaits = new String[4];
		for (int seat = 0; seat < 4; seat++) {
			String wait = tenpai.contains(seat) ? waitNotation(ps[seat].hand) : "";
			broadcast(ps[seat].name + " "
					+ Component.translatable(wait.isEmpty() ? "message.richi.noten" : "message.richi.tenpai_wait",
							wait.isEmpty() ? "" : wait).getString());
			logTenpai[seat] = !wait.isEmpty();
			logWaits[seat] = wait;
		}
		MahjongGameLog.draw(level, origin, handIndex + 1, logTenpai, logWaits);
		// 摊牌规则：听牌家推倒亮牌；四家全部立直时全部亮牌（含未听家）
		java.util.List<Integer> faceUp = new ArrayList<>(tenpai);
		boolean allRiichi = true;
		for (int s = 0; s < 4; s++)
			if (!ps[s].riichi) {
				allRiichi = false;
				break;
			}
		if (allRiichi)
			for (int s = 0; s < 4; s++)
				if (!faceUp.contains(s))
					faceUp.add(s);
		finishRound("message.richi.draw", "", "", pay.toString().trim(),
				new int[0], faceUp.stream().mapToInt(Integer::intValue).toArray());
		broadcast(Component.translatable("message.richi.draw").getString()
				+ (pay.length() > 0 ? " " + pay : ""));
	}

	/** 局终了：横幅展示结果 + 延迟 2s 推和牌结算界面（多家荣和按座次每 2s 轮流展示；流局不推）；
	 *  全员确认或 10s 后续局/终局。手牌展示：和牌者推倒（面朝上），其余三家仍立着；
	 *  流局听牌家推倒（面朝上，faceUpSeats）、未听家盖牌（四家立直时全部亮牌）；
	 *  九种九牌途中流局仅流局者亮牌。 */
	private void finishRound(String msgKey, String winner, String detail, String pay, int... winnerSeats) {
		finishRound(msgKey, winner, detail, detail, pay, winnerSeats, new int[0]);
	}

	/** 同上（bannerDetail：桌心横幅专用短描述，如不含役种） */
	private void finishRound(String msgKey, String winner, String detail, String bannerDetail, String pay,
			int... winnerSeats) {
		finishRound(msgKey, winner, detail, bannerDetail, pay, winnerSeats, new int[0]);
	}

	/** 同上（faceUpSeats：流局时听牌家列表，推倒亮牌面） */
	private void finishRound(String msgKey, String winner, String detail, String pay,
			int[] winnerSeats, int[] faceUpSeats) {
		finishRound(msgKey, winner, detail, detail, pay, winnerSeats, faceUpSeats);
	}

	/** 局终了主体：bannerDetail 专用于桌心横幅（可传不含役种的短描述），detail 用于聊天播报 */
	private void finishRound(String msgKey, String winner, String detail, String bannerDetail, String pay,
			int[] winnerSeats, int[] faceUpSeats) {
		st.phase = RichiTableState.PHASE_FINISHED;
		st.turnSeat = -1;
		st.turnDeadline = 0;
		st.turnInLong = false;
		waitingDiscard = false;
		st.drawnSeat = -1;
		clearAllOptions();
		for (int seat = 0; seat < 4; seat++) {
			if (contains(winnerSeats, seat) || contains(faceUpSeats, seat))
				st.handsExposed[seat] = RichiTableState.HAND_FACE_UP;
			else if (winnerSeats.length > 0)
				st.handsExposed[seat] = RichiTableState.HAND_STAND;
			else
				st.handsExposed[seat] = RichiTableState.HAND_FACE_DOWN;
		}
		// 结算横幅随牌局状态广播（客户端渲染 text_display）；役种说明只在聊天与结算界面展示
		st.banner = Component.translatable(msgKey, winner, bannerDetail).getString()
				+ (pay.isEmpty() ? "" : "\n" + pay);
		writeBack();
		messageTable(msgKey, winner, detail);
		Vec3 c = FengPanBlock.tableCenter(level, origin);
		if (winnerSeats.length > 0) {
			// 和牌升级音只发给和牌者本人（此前公开播在桌心，放铳者/旁观者也会听到"叮"）
			for (int w : winnerSeats) {
				ServerPlayer wp = st.playerOf(level, w);
				if (wp != null)
					wp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
			}
		} else
			level.playSound(null, c.x, c.y, c.z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
		// 和牌后等 2s 再弹结算 GUI；多家荣和按座次每 2s 轮流展示（settleWaiting 结束后不再推送）
		for (int i = 0; i < pendingSettlements.size(); i++) {
			var s = pendingSettlements.get(i);
			schedule(2 * 20 + i * 2 * 20, () -> {
				if (settleWaiting)
					sendSettlementToAll(s);
			});
		}
		mcr.richi.game.MahjongLobby.broadcastSync(level, origin, st);
		// 结算确认门控：全员（人类）确定或 10s 倒计时结束 → 清桌续局 / 终局
		// 连庄规则：庄家和牌或流局 → 本场+1、庄家连庄；庄家输牌 → 局数+1、本场清零
		roundAdvance = false;
		for (int w : winnerSeats)
			if (w != dealerSeat())
				roundAdvance = true;
		if (roundAdvance)
			st.honba = 0;
		else
			st.honba++;
		java.util.Arrays.fill(settleConfirmed, false);
		settleWaiting = true;
		settlePhase = 1;
		settleTask = this::showPointDelta;
		// 确认时限 = 推送延迟(2s×条数) + 役种逐条播报时长(15tick 起、间隔 7tick、尾条停留 1s) + 5s 确认窗口
		int announce = 0;
		for (var s : pendingSettlements) {
			int n = s.yakuList().isEmpty() ? 0 : s.yakuList().split(";").length;
			announce = Math.max(announce, n == 0 ? 20 : 15 + 7 * (n - 1) + 20);
		}
		schedule(2 * 20 * Math.max(1, pendingSettlements.size()) + announce + 5 * 20, settleTask);
	}

	private static boolean contains(int[] arr, int v) {
		for (int a : arr)
			if (a == v)
				return true;
		return false;
	}

	/** 向对局四家（人类）推送结算界面 */
	private void sendSettlementToAll(MahjongSettlementPayload.Settlement s) {
		for (int seat = 0; seat < 4; seat++) {
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null)
				MahjongSettlementPayload.sendTo(p, s);
		}
	}

	/** 结算阶段 1 结束 → 推送点数变化界面（阶段 2，5s 自动确认） */
	private void showPointDelta() {
		if (!settleWaiting || settlePhase != 1)
			return;
		settlePhase = 2;
		settleTask = this::proceedNext;
		for (int seat = 0; seat < 4; seat++) {
			if (ps[seat].ai)
				continue;
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null)
				mcr.richi.network.MahjongPointDeltaPayload.sendTo(p, origin.asLong(), seatNames(),
						pointsBeforeRound, st.points);
		}
		schedule(5 * 20, settleTask);
	}

	/** 结算确认后推进：连庄（局数不变、本场累加）或轮庄（局数+1）；终局不清桌，下一把开始前才清 */
	private void proceedNext() {
		settleWaiting = false;
		settlePhase = 0;
		pendingSettlements.clear();
		settleTask = null;
		// 关闭各家的结算界面与点数变化界面
		for (int seat = 0; seat < 4; seat++) {
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null) {
				MahjongSettlementPayload.sendClose(p);
				mcr.richi.network.MahjongPointDeltaPayload.sendClose(p);
			}
		}
		// 聊天播报在结算界面全部消失后发出
		if (!pendingChat.isEmpty()) {
			broadcast(pendingChat);
			pendingChat = "";
		}
		// 击飞（雀魂规则）：任一家点数 < 0 立即破产终局，不再续局
		int busted = -1;
		for (int i = 0; i < 4; i++)
			if (st.points[i] < 0) {
				busted = i;
				break;
			}
		if (busted >= 0) {
			broadcast(Component.translatable("message.richi.bankrupt", ps[busted].name).getString());
			endGame();
			return;
		}
		if (roundAdvance && handIndex + 1 >= totalHands)
			endGame();
		else
			nextHand();
	}

	/** 结算界面确认（C→S）：阶段 1 确认 → 进点数变化界面；阶段 2 确认 → 立即推进 */
	private void confirmSettle(int seat) {
		if (!settleWaiting)
			return;
		settleConfirmed[seat] = true;
		for (int i = 0; i < 4; i++) {
			if (!ps[i].ai && !settleConfirmed[i])
				return;
		}
		cancelSettleTask();
		if (settlePhase == 1)
			showPointDelta();
		else
			proceedNext();
	}

	/** 取消当前结算阶段的倒计时任务 */
	private void cancelSettleTask() {
		if (settleTask != null) {
			tasks.removeIf(t -> t.run() == settleTask);
			settleTask = null;
		}
	}

	/** 续局：连庄则局数不变（本场累加），轮庄则局数+1 重新发牌。
	 *  注意：轮庄只推进 handIndex（庄家 = handIndex%4，自风按相对庄家偏移计算），
	 *  玩家不换座位——此前额外做了一次玩家座位轮转，导致東2局起庄家判定/自风整体错位，
	 *  连庄与轮庄判断、庄家支付随之出错（终局尤甚）。 */
	private void nextHand() {
		if (roundAdvance)
			handIndex++;
		resetHand();
	}

	/** 投票结束：全员同意后强制终局（聊天播报 + 终局结算界面，不清桌） */
	public void forceEnd() {
		if (st.phase != RichiTableState.PHASE_PLAYING)
			return;
		st.turnSeat = -1;
		st.turnDeadline = 0;
		st.turnInLong = false;
		waitingDiscard = false;
		st.drawnSeat = -1;
		tasks.clear();
		aiRonPending = 0;
		aiMeldPending = 0;
		discarderSeat = -1;
		clearAllOptions();
		endGame();
	}

	/** 终局：聊天框向全场播报排名 + 风盘处挑战解锁音效 + 推终局结算界面，回到等待重开 */
	private void endGame() {
		st.phase = RichiTableState.PHASE_FINISHED;
		// 上一局人类玩家自动回到等待队列：管理界面（含清桌重置后）保留上一局玩家的队列座位，可直接再开
		for (int seat = 0; seat < 4; seat++)
			if (!st.aiSeat[seat] && st.players[seat] != null && !st.queue.contains(st.players[seat]))
				st.queue.add(st.players[seat]);
		// NPC 参战者终局回到等候队列：保留排队资格并原地钉定（在座位上等待下一局，不乱走）
		for (int seat = 0; seat < 4; seat++) {
			String u = st.players[seat];
			if (u != null && st.npcUuids.contains(u)
					&& level.getEntity(java.util.UUID.fromString(u)) instanceof mcr.murmol.entity.MurmolNpcEntity npc) {
				if (!st.queue.contains(u))
					st.queue.add(u);
				npc.pinAt(npc.position(), npc.getYRot()); // 继续排队等候，原地停驻
			}
		}
		st.turnSeat = -1;
		st.turnDeadline = 0;
		writeBack(); // phase=FINISHED：同步包不再携带形象信息，客户端假玩家随之移除
		mcr.richi.game.MahjongLobby.broadcastSync(level, origin, st);
		Integer[] order = { 0, 1, 2, 3 };
		java.util.Arrays.sort(order, (a, b) -> Integer.compare(st.points[b], st.points[a]));
		MahjongGameLog.gameEnd(level, origin, st.points);
		// 天凤算法段位结算：顺位马 + pt（AI 席只播报不入库；pt 增量 一局×0/东风×0.5/半庄×1；
		// 数据存存档 data/mahjong/ranks.json）
		String[] rankUuids = new String[4];
		for (int i = 0; i < 4; i++)
			rankUuids[i] = st.aiSeat[i] ? null : st.players[i];
		double[][] uma = mcr.richi.game.MahjongRankStore.recordEnd(level, seatNames(), rankUuids, st.points,
				st.gameType);
		// 聊天框播报（全场玩家）：顺位 + 点数 + 马 + pt
		var header = Component.translatable("message.richi.gameover");
		for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
			p.sendSystemMessage(header);
			for (int rank = 0; rank < 4; rank++) {
				int s = order[rank];
				p.sendSystemMessage(Component.translatable("message.richi.standing_pt",
						rank + 1, ps[s].name, st.points[s],
						String.format("%+d", (int) uma[s][1]), String.format("%+.1f", uma[s][2])));
			}
		}
		// 风盘位置挑战进度解锁音效
		Vec3 c = FengPanBlock.tableCenter(level, origin);
		level.playSound(null, c.x, c.y, c.z,
				net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
		// 终局结算界面（对局四家）
		MahjongSettlementPayload.Settlement finalS = new MahjongSettlementPayload.Settlement(
				MahjongSettlementPayload.MODE_FINAL, origin.asLong(), ps[order[0]].name, "", "",
				"", "", -1, "", "", "", 0, 0, 0, seatNames(), st.points.clone());
		for (int seat = 0; seat < 4; seat++) {
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null)
				MahjongSettlementPayload.sendTo(p, finalS);
		}
	}

	// ---- 结算数据工具 ----

	private String[] seatNames() {
		String[] names = new String[4];
		for (int i = 0; i < 4; i++)
			names[i] = ps[i].name;
		return names;
	}

	private static String csv(List<Integer> codes) {
		StringBuilder sb = new StringBuilder();
		for (int c : codes) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(c);
		}
		return sb.toString();
	}

	/** 副露组 → "a,b,c;d,e,f,g"（组间 ';'，声明牌在组末） */
	private static String csvMelds(RiichiPlayer p) {
		StringBuilder sb = new StringBuilder();
		for (Fuuro f : p.melds) {
			if (sb.length() > 0)
				sb.append(';');
			for (int t : f.tiles)
				sb.append(t).append(',');
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	// ==================================================================
	// 玩家操作入口
	// ==================================================================

	/** C→S 动作分发（MahjongGamePayloads 调用） */
	public static void handleAction(ServerPlayer sp, BlockPos origin, String action, String data) {
		RiichiGame g = mcr.richi.game.RichiTableManager.getGame(origin);
		if (g != null)
			g.onAction(sp, action, data);
	}

	private void onAction(ServerPlayer sp, String action, String data) {
		int seat = seatOf(sp.getUUID().toString());
		if (seat < 0)
			return;
		switch (action) {
			case "chi" -> {
				// data = "<组合索引>:<荣和牌>"（荣和牌仅客户端画牌面用）
				String idxPart = data.contains(":") ? data.substring(0, data.indexOf(':')) : data;
				tryClaim(seat, "chi", parseInt(idxPart, -1));
			}
			case "pon" -> tryClaim(seat, "pon", -1);
			case "minkan" -> tryClaim(seat, "minkan", -1);
			case "ron" -> tryClaim(seat, "ron", -1);
			case "tsumo" -> {
				if (turn == seat && waitingDiscard)
					doTsumo(seat);
			}
			case "riichi" -> {
				if (turn == seat && waitingDiscard && canRiichi(seat) && !pendingRiichi) {
					// 立直两段式：先宣言（清选项 HUD），玩家右键点选手牌确定打出哪张
					pendingRiichi = true;
					ServerPlayer p = st.playerOf(level, seat);
					if (p != null) {
						mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin, "");
						p.displayClientMessage(Component.translatable("message.richi.riichi_pick"), true);
					}
				}
			}
			case "skip" -> onSkip(seat);
			case "settle_confirm" -> confirmSettle(seat);
			case "kyuushu" -> doKyuushu(seat);
			case "ankan" -> {
				if (turn == seat && waitingDiscard)
					doKong(seat, "ankan", parseInt(data, -1));
			}
			case "kakan" -> {
				if (turn == seat && waitingDiscard)
					doKong(seat, "kakan", parseInt(data, -1));
			}
		}
	}

	/** 手牌点击（FengPanBlock.handleHandInteract 转发）：本家回合选中/打出；响应窗口期右键 = 无操作 */
	public void onTileClick(Player player, int seat, int index) {
		// 立直待选：本家右键手牌 = 确定立直打出该张
		if (pendingRiichi && seat == turn) {
			playerDiscard(seat, index);
			return;
		}
		// 响应窗口期右键：无操作（荣和走键盘数字键，左键 = 跳过）
		if (turn != seat && claimWindowEnd > 0)
			return;
		if (turn != seat || !waitingDiscard) {
			if (pendingRiichi) {
				// 点到错误的牌（别家手牌）→ 取消立直
				cancelRiichi();
				player.displayClientMessage(Component.translatable("message.richi.riichi_cancel"), true);
			} else {
				player.displayClientMessage(
						Component.translatable("message.richi.not_your_turn", turn >= 0 ? ps[turn].name : "-"), true);
			}
			return;
		}
		if (st.selectedSeat == seat && st.selectedIndex == index) {
			playerDiscard(seat, index);
		} else {
			st.selectedSeat = seat;
			st.selectedIndex = index;
			writeBack(); // 广播选中状态，客户端抬升对应手牌（±SELECT_LIFT）
		}
	}

	/** 左键手牌：立直待选时取消 / 回合家可自摸则确认自摸，否则摸切 / 窗口期可荣和则确认荣和，否则跳过 */
	public void onTileAttack(Player player, int seat) {
		if (pendingRiichi && seat == turn) {
			cancelRiichi();
			player.displayClientMessage(Component.translatable("message.richi.riichi_cancel"), true);
			return;
		}
		if (turn == seat && waitingDiscard) {
			// 可自摸时左键 = 确认自摸（防止误摸切跳过自摸）
			if (justDrew && canWinNow(seat, true)) {
				clearAllOptions();
				doTsumo(seat);
			} else {
				playerDiscard(seat, ps[seat].hand.size() - 1); // 摸切
			}
			return;
		}
		// 响应窗口：左键自己手牌 = 可荣和则确认荣和（防止误跳过；计入多家荣和），否则跳过
		if (claimWindowEnd > 0 && seat != claimDiscarder && claimOptions[seat] != null) {
			if (claimOptions[seat].contains("ron") && canRon(seat)) {
				claimOptions[seat] = null;
				pendingRons.add(seat);
				resolveWindow();
			} else {
				onSkip(seat);
			}
		}
	}

	/** 取消立直宣言（左键点/点错牌）：恢复本家选项 */
	private void cancelRiichi() {
		pendingRiichi = false;
		sendSelfOptions(turn);
	}

	/** 跳过：响应窗口中放弃鸣牌/荣和（所有人表态后提前收口）；自家回合放弃自摸/杠选项 */
	private void onSkip(int seat) {
		if (claimWindowEnd > 0 && seat != claimDiscarder && claimOptions[seat] != null) {
			// 放过可荣和的牌 → 振听（立直家永久振听，否则同巡振听到自己下次打牌）
			if (claimOptions[seat].contains("ron")) {
				if (ps[seat].riichi)
					ps[seat].riichiFuriten = true;
				else
					ps[seat].tempFuriten = true;
			}
			claimOptions[seat] = null;
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null) {
				mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin, "");
				p.displayClientMessage(Component.translatable("message.richi.skip_call"), true);
			}
			resolveWindow(); // 人类全部表态后收口（多家荣和 / 抢杠 / 正常推进）
			return;
		}
		// 自家回合：放弃自摸/暗杠/加杠（保留打牌，选项清空即可）
		if (turn == seat && waitingDiscard) {
			ServerPlayer p = st.playerOf(level, seat);
			if (p != null) {
				mcr.richi.network.MahjongGamePayloads.sendOptions(p, origin, "");
				p.displayClientMessage(Component.translatable("message.richi.skip_call"), true);
			}
		}
	}

	/** 管理员踢出 → AI 接管该座位 */
	public void makeAI(int seat) {
		if (seat < 0 || seat > 3)
			return;
		ps[seat].ai = true;
		ps[seat].name = aiSeatName(seat);
		st.aiSeat[seat] = true;
		st.players[seat] = RiichiBot.aiUuid(seat);
	}

	public RiichiPlayer player(int seat) {
		return ps[seat];
	}

	// ==================================================================
	// 判定辅助
	// ==================================================================

	private boolean canRon(int seat) {
		return canRonTile(seat, claimTile);
	}

	/** 是否可荣和指定牌（含地和/河底等荣和役判定；振听时不可荣和，自摸不受限） */
	private boolean canRonTile(int seat, int tile) {
		if (ps[seat].riichiFuriten || ps[seat].tempFuriten || riverFuriten(seat))
			return false;
		List<Integer> full = new ArrayList<>(ps[seat].hand);
		full.add(tile);
		return Mahjong4jBridge.score(full, ps[seat].melds, seat, dealerSeat(), st.roundWind, false,
				ps[seat].riichi, ippatsu[seat], ronFlags(ps[seat]), wall.actualDoras()).win();
	}

	/** 临时振听（河内和牌张）：自己的牌河中存在当前听牌时，荣和全部振听（按牌面去重逐张试和） */
	private boolean riverFuriten(int seat) {
		RiichiPlayer p = ps[seat];
		for (int t : new java.util.TreeSet<>(p.river)) {
			int code = t < 30 && t % 10 == 0 ? t + 5 : t; // 红五与普通五同判
			List<Integer> full = new ArrayList<>(p.hand);
			full.add(code);
			if (Mahjong4jBridge.score(full, p.melds, seat, dealerSeat(), st.roundWind, false,
					p.riichi, ippatsu[seat], ronFlags(p), wall.actualDoras()).win())
				return true;
		}
		return false;
	}

	private boolean canWinNow(int seat, boolean tsumo) {
		return Mahjong4jBridge.score(new ArrayList<>(ps[seat].hand), ps[seat].melds,
				seat, dealerSeat(), st.roundWind, tsumo, ps[seat].riichi, ippatsu[seat],
				tsumoFlags(ps[seat]), wall.actualDoras()).win();
	}

	private boolean canRiichi(int seat) {
		RiichiPlayer p = ps[seat];
		if (p.riichi || !p.isMenzen() || st.points[seat] < 1000 || p.hand.isEmpty())
			return false;
		// 立直宣言牌待定：打出任一张后仍听牌
		for (int i = 0; i < p.hand.size(); i++) {
			int removed = p.hand.remove(i);
			boolean tenpai = Mahjong4jBridge.isTenpai(p.hand, p.melds);
			p.hand.add(i, removed);
			if (tenpai)
				return true;
		}
		return false;
	}

	private static int countIn(List<Integer> hand, int code) {
		int n = 0;
		for (int c : hand)
			if (c == code)
				n++;
		return n;
	}

	// ==================================================================
	// AI / tick / 工具
	// ==================================================================

	/** 每服务端 tick（MahjongTicker 转发） */
	public void tick(long now) {
		if (!tasks.isEmpty()) {
			List<DelayedTask> due = null;
			for (DelayedTask t : tasks)
				if (now >= t.at())
					(due == null ? due = new ArrayList<>() : due).add(t);
			if (due != null) {
				tasks.removeAll(due);
				for (DelayedTask t : due)
					t.run().run();
			}
		}
		if (claimWindowEnd > 0)
			tickClaimWindow(now);
	}

	/**
	 * 响应窗口长短考计时：短考段 = 思考档位每巡时限；之后逐 tick 消耗各家长考银行（与打牌回合同款）。
	 * 全局保底 cap = 短考 + 可响应人类中最大的长考银行（正常由各家表态/耗尽提前关窗）。
	 */
	private void armClaimTiming() {
		long now = level.getGameTime();
		int shortTicks = RichiTableState.THINK_PRESETS[st.thinkIdx][0] * 20;
		claimShortEnd = now + shortTicks;
		int maxBank = 0;
		for (int s = 0; s < 4; s++)
			if (!ps[s].ai && claimOptions[s] != null)
				maxBank = Math.max(maxBank, st.longBank[s]);
		claimWindowEnd = claimShortEnd + maxBank + 40; // 40t 余量防边界竞态
	}

	/**
	 * 响应窗口倒计时：短考段先行（与打牌回合同款），耗尽后实时消耗各待副露家长考银行
	 * （不公开——倒计时只推给该家自己）。长短考均耗尽：可荣和则自动胡牌，否则自动跳过。
	 */
	private void tickClaimWindow(long now) {
		for (int seat = 0; seat < 4; seat++) {
			if (seat == claimDiscarder || ps[seat].ai || claimOptions[seat] == null)
				continue;
			if (now < claimShortEnd) {
				// 短考段：每秒推送（短考秒数 + 长考银行秒数）
				if ((claimShortEnd - now) % 20 == 0) {
					int sec = (int) ((claimShortEnd - now) / 20);
					sendClaimCountdown(seat, sec, (st.longBank[seat] + 19) / 20);
					if (sec <= 3 && st.longBank[seat] <= 0)
						playHarp(seat); // 最后 3 秒每秒一声竖琴提示（还有长考银行时不提示）
				}
				continue;
			}
			if (st.longBank[seat] > 0) {
				// 长考段：逐 tick 消耗银行
				st.longBank[seat]--;
				if (st.longBank[seat] % 20 == 0) {
					sendClaimCountdown(seat, 0, (st.longBank[seat] + 19) / 20);
					if (st.longBank[seat] <= 60)
						playHarp(seat);
				}
				continue;
			}
			// 短考与长考均耗尽：有荣和 → 自动荣和（计入多家荣和）；否则自动跳过
			if (claimOptions[seat].contains("ron") && canRon(seat)) {
				pendingRons.add(seat);
				claimOptions[seat] = null;
				resolveWindow();
				return;
			}
			onSkip(seat);
			if (claimWindowEnd == 0)
				return; // 全员表态关窗
		}
		if (claimWindowEnd > 0 && now >= claimWindowEnd)
			closeWindowAndAdvance(); // 保底（正常由各家表态/银行耗尽驱动结束）
	}

	/** 副露/荣和等待倒计时（短考白色 + 长考黄色，只发给该家） */
	private void sendClaimCountdown(int seat, int shortSec, int longSec) {
		ServerPlayer p = st.playerOf(level, seat);
		if (p != null)
			net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,
					new mcr.richi.network.MahjongCountdownPayload.CountdownMessage(
							Math.max(0, shortSec), Math.max(0, longSec)));
	}

	/** 倒计时末 3 秒提示音：音符盒竖琴 A 音，仅该家可闻 */
	private void playHarp(int seat) {
		ServerPlayer p = st.playerOf(level, seat);
		if (p != null)
			p.playNotifySound(SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.PLAYERS, 1.0f, 0.891f);
	}

	private void schedule(long delayTicks, Runnable task) {
		tasks.add(new DelayedTask(level.getGameTime() + delayTicks, task));
	}

	private void aiTakeTurn() {
		int seat = turn;
		if (!waitingDiscard || seat < 0 || !ps[seat].ai)
			return;
		if (kyuushuAvailable) {
			doKyuushu(seat); // AI 直接宣言九种九牌
			return;
		}
		if (justDrew && (canWinNow(seat, true) || firstTurnShapeWin(seat))) {
			// AI 自摸按速度档延迟后宣布（立直摸切家立即和，避免与自动摸切任务竞态）
			if (ps[seat].riichi)
				doTsumo(seat);
			else
				schedule(10 * aiMul(), () -> doTsumo(seat));
			return;
		}
		int flow = Math.floorMod(st.aiFlows[seat], 7);
		boolean defensiveFlow = flow == 2 || flow == 4;
		boolean opponentRiichi = defendActive(seat);
		// 开杠决策：防守型（门清流/魂天流）视情况——场上有立直家时不杠（不暴露新宝牌信息、
		// 加杠有被抢杠风险且延迟防守）；无立直威胁时照常开杠，其余流派恒开
		int ankan = findAnkan(seat);
		if (ankan >= 0 && !(defensiveFlow && opponentRiichi)) {
			doKong(seat, "ankan", ankan);
			return;
		}
		// 立直后打牌锁定（规则）：除自摸/暗杠外只能摸切，杜绝任何手切路径
		if (ps[seat].riichi) {
			playerDiscard(seat, ps[seat].hand.size() - 1);
			return;
		}
		int kakan = findKakan(seat);
		if (kakan >= 0 && !(defensiveFlow && opponentRiichi)) {
			doKong(seat, "kakan", kakan);
			return;
		}
		if (flow == 5) {
			// 闪电流：前期摸切蓄势（未听牌且总巡数 <12），听牌或中盘后全力速攻
			// （立直走通用决策：非防守流只要听牌必立；永不防守，见下方防守权重）
			int turns = ps[0].river.size() + ps[1].river.size() + ps[2].river.size()
					+ ps[3].river.size();
			if (turns < 12 && !Mahjong4jBridge.isTenpai(ps[seat].hand, ps[seat].melds)) {
				playerDiscard(seat, ps[seat].hand.size() - 1);
				return;
			}
		}
		// 鬼神境：知他家手牌——可见牌计入他家手牌（剩余枚数即真实牌山）
		int[] visible = flow == 6 ? buildOmniscient34(seat) : buildVisible34(seat);
		// 立直决策（原为门清听牌无条件立直）：
		// 听 0 张（无牌可和）绝不立直；早巡立直用于压制对手并提升打点（无役带宝牌尤其需要），
		// 好型（存活听 ≥4）同样立直——两者优先于防守流克制；打点够高（荣和 ≥5200）时听张少也立；
		// 仅终盘防守流（门清流/魂天流）面对别家立直、听张少（≤4）且打点不高时放弃立直
		if (canRiichi(seat)) {
			int waits = countWaits(seat, visible);
			boolean early = wall.remaining() >= 60;
			boolean goodShape = waits >= 4;
			boolean highValue = waits > 0 && estimateRiichiValue(seat, visible) >= 5200;
			if (waits > 0 && (early || goodShape || highValue
					|| !(defensiveFlow && opponentRiichi && waits <= 4)))
				pendingRiichi = true;
		}
		// 防守权重按流派：野猪流/闪电流 0（不防）；一般流/御无双 1.0；魂天流 1.2（略高）；
		// 门清流 1.5（高）；鬼神境全知他家手牌——不主动防守，唯一例外：打得出去的牌"确实是炮"绝不打
		double[] danger = null;
		double dangerWeight = 0;
		switch (flow) {
			case 1 -> dangerWeight = 0; // 野猪流：不防守
			case 3 -> dangerWeight = 1.0; // 御无双：防守适中
			case 4 -> dangerWeight = 1.2; // 魂天流：防守略高
			case 2 -> dangerWeight = 1.5; // 门清流：防守高
			case 6 -> {
				danger = omniscientDanger(seat);
				dangerWeight = 1.0;
			}
			case 5 -> dangerWeight = 0; // 闪电流：永不防守（速攻流）
			default -> dangerWeight = 1.0; // 一般流：标准防守
		}
		if (dangerWeight > 0 && flow != 6 && defendActive(seat)) {
			// 非鬼神境：场上有立直家才生成铳率表
			List<Integer> doraCodes = new ArrayList<>();
			for (int c : wall.revealedDoras())
				doraCodes.add(c);
			List<Integer> riichiSeats = new ArrayList<>();
			List<List<Integer>> rivers = new ArrayList<>();
			for (int s = 0; s < 4; s++)
				if (s != seat && ps[s].riichi) {
					riichiSeats.add(s);
					rivers.add(ps[s].river);
				}
			danger = RiichiBot.defenseDanger(riichiSeats, rivers, visible, doraCodes, 27 + st.roundWind,
					dealerSeat());
		}
		playerDiscard(seat, RiichiBot.chooseDiscard(ps[seat], visible, buildDora34(),
				level.random::nextDouble, pendingRiichi, danger, dangerWeight, 0));
	}

	/**
	 * 鬼神境铳率表（全知）：不主动防守（不算安全牌、不做终局电报权衡），唯一例外是
	 * "打得出去的牌确实是炮"——他家手牌可见，能直接和出的牌 = 100% 铳率绝不打；
	 * 其余牌铳率 0，评分自然按进攻价值选择。
	 */
	private double[] omniscientDanger(int seat) {
		double[] danger = new double[34];
		// 鬼神境不防守（不做安全牌/终局电报权衡），唯一的例外：打得出去的牌"确实是炮"
		// （他家手牌可见，能直接和出的牌）——这种牌 100 罚分绝不打，其余照常评分自然回落
		for (int s = 0; s < 4; s++) {
			if (s == seat)
				continue;
			for (int i = 0; i < 34; i++) {
				if (danger[i] > 0)
					continue;
				// 索引 → 牌面码：1m..9m=1..9、1p..9p=11..19、1s..9s=21..29、1z..7z=30..36
				int code = i < 27 ? (i / 9) * 10 + i % 9 + 1 : i - 27 + 30;
				List<Integer> test = new ArrayList<>(ps[s].hand);
				test.add(code);
				if (Mahjong4jBridge.isWinnableShape(test, ps[s].melds))
					danger[i] = 100;
			}
		}
		return danger;
	}

	/** 防守启用判定：场上有立直家（除自己）即防 */
	private boolean defendActive(int seat) {
		for (int s = 0; s < 4; s++)
			if (s != seat && ps[s].riichi)
				return true;
		return false;
	}

	/** 全知可见牌 34 数组（自己手牌 + 他家手牌 + 各家牌河/副露；鬼神境用，剩余枚数 = 真实牌山） */
	private int[] buildOmniscient34(int seat) {
		int[] visible = buildVisible34(seat);
		for (int s = 0; s < 4; s++)
			if (s != seat)
				for (int c : ps[s].hand)
					visible[TileEfficiency.codeToIndex(c)]++;
		return visible;
	}

	/** 全场可见牌 34 数组（自己手牌 + 各家牌河 + 各家副露；供 AI 推算剩余枚数） */
	private int[] buildVisible34(int seat) {
		int[] visible = new int[34];
		for (int c : ps[seat].hand)
			visible[TileEfficiency.codeToIndex(c)]++;
		for (int s = 0; s < 4; s++) {
			for (int c : ps[s].river)
				visible[TileEfficiency.codeToIndex(c)]++;
			for (Fuuro f : ps[s].melds)
				for (int c : f.tiles)
					visible[TileEfficiency.codeToIndex(c)]++;
		}
		return visible;
	}

	/** 立直打点估算：对每种打牌选择的每个存活听牌按“立直荣和（点闲家）”试算，取最大支付（打点高时听张少也值得立直） */
	private int estimateRiichiValue(int seat, int[] visible) {
		int[] own = new int[34];
		for (int c : ps[seat].hand)
			own[TileEfficiency.codeToIndex(c)]++;
		List<Integer> doraInd = new ArrayList<>();
		for (int c : wall.revealedDoras())
			doraInd.add(c);
		int best = 0;
		List<Integer> hand = ps[seat].hand;
		// 枚举每种打牌（14 张去掉任意一张 = 13 张基牌）——只试“打刚摸牌”会漏掉打别的牌才成立的听牌形
		for (int d = 0; d < hand.size(); d++) {
			int removed = hand.remove(d);
			for (int i = 0; i < 34; i++) {
				if (4 - (visible[i] - own[i]) <= 0)
					continue;
				int code = i < 27 ? (i / 9) * 10 + i % 9 + 1 : i - 27 + 30;
				List<Integer> test = new ArrayList<>(hand);
				test.add(code);
				if (!Mahjong4jBridge.isWinnableShape(test, ps[seat].melds))
					continue;
				var r = Mahjong4jBridge.score(test, ps[seat].melds, seat, dealerSeat(), st.roundWind,
						false, true, false, Mahjong4jBridge.Flags.NONE, doraInd);
				if (r.win())
					best = Math.max(best, r.score().getRon());
				if (best >= 5200)
					break; // 早停：打点门槛只有一档
			}
			hand.add(d, removed);
			if (best >= 5200)
				break;
		}
		return best;
	}

	/** 当前听牌的可和枚数：枚举每种打牌选择，对每张仍“存活”的候选牌试和，取最大活听
	 *  （剩余 4 - 场外可见数，自己手牌不计入场外；只试“打刚摸牌”会漏掉打别的牌才成立的听牌形 → waits=0 → 拖一巡才立） */
	private int countWaits(int seat, int[] visible) {
		int[] own = new int[34];
		for (int c : ps[seat].hand)
			own[TileEfficiency.codeToIndex(c)]++;
		int best = 0;
		List<Integer> hand = ps[seat].hand;
		for (int d = 0; d < hand.size(); d++) {
			int removed = hand.remove(d);
			int waits = 0;
			for (int i = 0; i < 34; i++) {
				int remain = 4 - (visible[i] - own[i]); // 场外可见 = visible - own
				if (remain <= 0)
					continue;
				int code = i < 27 ? (i / 9) * 10 + i % 9 + 1 : i - 27 + 30;
				List<Integer> test = new ArrayList<>(hand);
				test.add(code);
				if (Mahjong4jBridge.isWinnableShape(test, ps[seat].melds))
					waits += remain;
			}
			hand.add(d, removed);
			best = Math.max(best, waits);
		}
		return best;
	}

	/** 当前宝牌（指示牌下一张）34 布尔 */
	private boolean[] buildDora34() {
		boolean[] dora = new boolean[34];
		for (int c : wall.revealedDoras())
			dora[TileEfficiency.codeToIndex(c)] = true;
		return dora;
	}

	/** 听牌等待张记法（流局显示用，如 "37m1z"；无等待张返回 ""） */
	private String waitNotation(List<Integer> hand) {
		java.util.Set<Integer> waitIdx = new java.util.LinkedHashSet<>();
		for (int code = 0; code <= 36; code++) {
			int idx = TileEfficiency.codeToIndex(code);
			int cnt = 0;
			for (int c : hand)
				if (TileEfficiency.codeToIndex(c) == idx)
					cnt++;
			if (cnt >= 4)
				continue; // 该种牌已全可见，不可能等
			List<Integer> h = new ArrayList<>(hand);
			h.add(code);
			if (RiichiBot.shantenOf(h) == -1)
				waitIdx.add(idx);
		}
		StringBuilder sb = new StringBuilder();
		for (int idx : waitIdx) {
			// 34 索引 → code（红五按普通 5 显示）
			int code = idx < 9 ? idx + 1 : idx < 18 ? idx - 8 + 10 : idx < 27 ? idx - 17 + 20 : idx - 27 + 30;
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(MahjongTileNotation.format(List.of(code)));
		}
		return sb.toString();
	}

	/** 管理指令：替换某座位手牌（/murmol mahjong replace）。仅对局中有效；返回 false = 座位/张数非法 */
	public boolean replaceHand(int seat, List<Integer> codes) {
		if (seat < 0 || seat > 3 || codes.isEmpty() || codes.size() > 14)
			return false;
		RiichiPlayer p = ps[seat];
		p.hand.clear();
		p.hand.addAll(codes);
		p.sortHand();
		st.clearSelection();
		if (turn == seat && waitingDiscard) {
			// 该家正待打牌：14 张 = 含刚摸的牌（保留摸牌位渲染），否则视为无摸牌
			boolean hasDrawn = codes.size() % 3 == 2;
			justDrew = hasDrawn;
			st.drawnSeat = hasDrawn ? seat : -1;
			st.turnDeadline = level.getGameTime() + st.turnLimit; // 重置本巡思考时间
		} else {
			st.drawnSeat = -1;
		}
		writeBack();
		FengPanBlock.rerenderTable(level, origin); // 重建交互代理（手牌张数可能变化）
		broadcast(p.name + " 的手牌已被管理员替换为 " + MahjongTileNotation.format(codes));
		return true;
	}

	private int seatOf(String uuid) {
		for (int i = 0; i < 4; i++)
			if (ps[i].uuid.equals(uuid))
				return i;
		return -1;
	}

	/** 写回 RichiTableState（渲染层唯一数据源）并按观看者过滤广播整桌快照 */
	private void writeBack() {
		for (int i = 0; i < 4; i++) {
			st.hands[i] = ps[i].handNotation();
			st.melds[i] = ps[i].meldNotation();
			st.rivers[i] = ps[i].riverNotation();
			st.points[i] = ps[i].points;
		}
		st.wall = MahjongTileNotation.format(wall.remainingCodes());
		st.doraWall = MahjongTileNotation.format(wall.doraStackTiles());
		st.revealedIndicators = wall.revealedIndicatorCount();
		RichiTableSync.broadcast(level, origin);
	}

	private static int parseInt(String s, int def) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return def;
		}
	}

	private void messageTable(String key, Object... args) {
		FengPanBlock.messageTable(level, st, key, args);
	}

	/** 聊天框向桌心 16 格内所有玩家播报（副露/立直/和牌/流局等每次操作实时通知；个人提示走动作栏不受影响） */
	private void broadcast(String text) {
		Vec3 c = FengPanBlock.tableCenter(level, origin);
		var box = new net.minecraft.world.phys.AABB(c.x - 16, c.y - 16, c.z - 16,
				c.x + 16, c.y + 16, c.z + 16);
		for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, box))
			p.sendSystemMessage(Component.literal("§6<幻星麻雀>§r " + text));
	}

	/** 牌面显示名（播报用） */
	private static String tileName(int code) {
		return mcr.richi.MahjongTileItem.create(code).getHoverName().getString();
	}
}
