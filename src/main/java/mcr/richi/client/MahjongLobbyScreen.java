package mcr.richi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import mcr.richi.network.MahjongLobbyPayloads;

/**
 * 麻雀大厅管理界面（潜行右击风盘打开）：可视化查看座位/等待队列，底部按钮加入或退出队列、
 * 开始对局（管理员或队首）。管理员（2 级权限）点击座位行：等待阶段 = 安排/取消该座位的 AI 预约，
 * 对局中 = 踢出该座位玩家（AI 接管继续打）。数据来自服务端广播的 Sync（操作后立即重播）。
 */
public class MahjongLobbyScreen extends Screen {
	private static final int PANEL_W = 230;
	private static final int ROW_H = 20;
	private static final int QUEUE_ROWS = 5;

	private static final int PHASE_WAITING = 0, PHASE_PLAYING = 1, PHASE_FINISHED = 2;

	/** AI 形象模式显示键（顺序与 MahjongLobby.AVATAR_MODES 一致：随机 > 人类 > 各形态） */
	private static final String[] AVATAR_KEYS = { "gui.richi.lobby.av_random", "gui.richi.lobby.av_human",
			"gui.richi.lobby.av_luohong", "gui.richi.lobby.av_chen_huang", "gui.richi.lobby.av_moss_beast",
			"gui.richi.lobby.av_silkmoth", "gui.richi.lobby.av_komainu", "gui.richi.lobby.av_wenyao",
			"gui.richi.lobby.av_ferocious", "gui.richi.lobby.av_dark_fury", "gui.richi.lobby.av_villager" };

	private final BlockPos origin;
	/** 最近一次服务端同步（未收到前显示加载中） */
	private MahjongLobbyPayloads.LobbySync sync;
	/** sync 首次到达时重建底部按钮（按钮语义取决于是否已在队列） */
	private boolean builtButtons;

	public MahjongLobbyScreen(BlockPos origin) {
		super(Component.translatable("gui.richi.lobby.title"));
		this.origin = origin;
	}

	public static void open(BlockPos origin) {
		Minecraft.getInstance().setScreen(new MahjongLobbyScreen(origin));
	}

	private void refresh() {
		boolean had = this.sync != null;
		int oldType = this.sync != null ? this.sync.gameType() : -1;
		int oldThink = this.sync != null ? this.sync.thinkIdx() : -1;
		int oldSpeed = this.sync != null ? this.sync.aiSpeedIdx() : -1;
		boolean oldOpen = this.sync != null && this.sync.openHand();
		boolean oldInQueue = this.sync != null && !this.sync.queueNames().isEmpty()
				&& this.minecraft != null && this.minecraft.player != null
				&& this.sync.queueNames().contains(this.minecraft.player.getGameProfile().getName());
		this.sync = MahjongLobbyPayloads.CLIENT_SYNC.get(origin.asLong());
		boolean newInQueue = this.sync != null && !this.sync.queueNames().isEmpty()
				&& this.minecraft != null && this.minecraft.player != null
				&& this.sync.queueNames().contains(this.minecraft.player.getGameProfile().getName());
		if (!had && this.sync != null || (this.sync != null && this.sync.gameType() != oldType)
				|| (this.sync != null && this.sync.thinkIdx() != oldThink)
				|| (this.sync != null && this.sync.aiSpeedIdx() != oldSpeed)
				|| (this.sync != null && newInQueue != oldInQueue)
				|| (this.sync != null && this.sync.openHand() != oldOpen))
			rebuildButtons();
	}

	private void rebuildButtons() {
		clearWidgets();
		init(this.minecraft, this.width, this.height);
	}

	private boolean inQueue() {
		var player = this.minecraft != null ? this.minecraft.player : null;
		if (player == null || this.sync == null)
			return false;
		String name = player.getGameProfile().getName();
		return this.sync.queueNames().stream().anyMatch(name::equals);
	}

	private void send(int action, int extra) {
		net.neoforged.neoforge.network.PacketDistributor.sendToServer(
				new MahjongLobbyPayloads.ActionMessage(origin.asLong(), action, extra));
	}

	// ==================================================================
	// 灰色半透明按钮（与座位/列表行同款样式，替代原版 Button）
	// ==================================================================

	private record UiBtn(int x, int y, int w, int h, java.util.function.Supplier<String> label, Runnable action) {
	}

	private final java.util.List<UiBtn> btns = new java.util.ArrayList<>();

	private void addBtn(int x, int y, int w, int h, java.util.function.Supplier<String> label, Runnable action) {
		this.btns.add(new UiBtn(x, y, w, h, label, action));
	}

	@Override
	protected void init() {
		refresh();
		this.btns.clear();
		if (this.sync == null)
			return;
		int left = this.width / 2 - PANEL_W / 2;
		int top = this.height / 2 - 100;
		int colW = (PANEL_W - 12) / 2; // 左右两列等宽（列间隙 12）
		int colLX = left + 4, colRX = left + PANEL_W - 4 - colW;
		int queueTop = top + 4 * ROW_H + 6;
		// 两列内容起点：队列头行下方
		int y = queueTop + 12 + QUEUE_ROWS * 12 + 2;
		if (this.sync.phase() == PHASE_PLAYING) {
			// 对局中：投票结束对局（旧操作全部无效）
			addBtn(colLX, y, 130, 18,
					() -> Component.translatable("gui.richi.lobby.btn_vote_end").getString(),
					() -> {
						send(MahjongLobbyPayloads.ACT_VOTE_END, 0);
						onClose(); // 投票后关闭界面，避免挡住桌面
					});
			addBtn(colLX + 134, y, colW - 26, 18,
					() -> Component.translatable("gui.richi.lobby.btn_close").getString(), this::onClose);
			return;
		}
		// 左列：加入/退出队列、开始对局、查看牌谱
		addBtn(colLX, y, colW, 18,
				() -> Component.translatable(inQueue() ? "gui.richi.lobby.btn_leave" : "gui.richi.lobby.btn_join")
						.getString(),
				() -> send(MahjongLobbyPayloads.ACT_JOIN_QUEUE, 0));
		addBtn(colLX, y + 20, colW, 18,
				() -> Component.translatable("gui.richi.lobby.btn_start").getString(),
				() -> {
					send(MahjongLobbyPayloads.ACT_START, 0);
					onClose(); // 开局后关闭界面，直接看发牌
				});
		boolean finished = this.sync.canManage() && this.sync.phase() == PHASE_FINISHED;
		addBtn(colLX, y + 40, colW, 18,
				() -> Component.translatable("gui.richi.lobby.btn_paipu").getString(),
				PaipuListScreen::open);
		// 右列：游戏设置列表（管理员·等待/终局阶段均可；点击整行循环切换）
		boolean adminIdle = this.sync.canManage()
				&& (this.sync.phase() == PHASE_WAITING || this.sync.phase() == PHASE_FINISHED);
		if (adminIdle) {
			String[] typeKeys = { "gui.richi.lobby.type_single", "gui.richi.lobby.type_east", "gui.richi.lobby.type_full" };
			int type = Math.floorMod(this.sync.gameType(), 3);
			addBtn(colRX, queueTop + 12, colW, 18,
					() -> Component.translatable("gui.richi.lobby.btn_type",
							Component.translatable(typeKeys[Math.floorMod(this.sync.gameType(), 3)]).getString())
							.getString(),
					() -> send(MahjongLobbyPayloads.ACT_SET_TYPE,
							(Math.floorMod(this.sync.gameType(), 3) + 1) % 3));
			// 思考时间档位循环：5+20 → 10+30 → 60+0 → 3+5
			String[] thinkPresets = { "5+20", "10+30", "60+0", "3+5" };
			addBtn(colRX, queueTop + 32, colW, 18,
					() -> Component.translatable("gui.richi.lobby.btn_think",
							thinkPresets[Math.floorMod(this.sync.thinkIdx(), 4)]).getString(),
					() -> send(MahjongLobbyPayloads.ACT_SET_THINK,
							(Math.floorMod(this.sync.thinkIdx(), 4) + 1) % 4));
			String[] speedKeys = { "gui.richi.lobby.speed_fast", "gui.richi.lobby.speed_mid", "gui.richi.lobby.speed_slow" };
			addBtn(colRX, queueTop + 52, colW, 18,
					() -> Component.translatable("gui.richi.lobby.btn_speed",
							Component.translatable(speedKeys[Math.floorMod(this.sync.aiSpeedIdx(), 3)]).getString())
							.getString(),
					() -> send(MahjongLobbyPayloads.ACT_SET_SPEED,
							(Math.floorMod(this.sync.aiSpeedIdx(), 3) + 1) % 3));
			addBtn(colRX, queueTop + 72, colW, 18,
					() -> Component.translatable("gui.richi.lobby.btn_open",
							Component.translatable(this.sync.openHand() ? "gui.richi.lobby.open_on"
									: "gui.richi.lobby.open_off").getString()).getString(),
					() -> send(MahjongLobbyPayloads.ACT_SET_OPEN, this.sync.openHand() ? 0 : 1));
			if (finished)
				addBtn(colRX, queueTop + 96, colW, 18,
						() -> Component.translatable("gui.richi.lobby.btn_clear").getString(),
						() -> send(MahjongLobbyPayloads.ACT_CLEAR, 0));
		}
	}

	@Override
	public void tick() {
		refresh();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		var font = this.font;
		int left = this.width / 2 - PANEL_W / 2;
		int top = this.height / 2 - 100;
		int colW = (PANEL_W - 12) / 2;
		int colLX = left + 4, colRX = left + PANEL_W - 4 - colW;
		int queueTop = top + 4 * ROW_H + 6;

		graphics.drawCenteredString(font, this.title, this.width / 2, top - 14, 0xFFFFFF);
		if (this.sync == null) {
			graphics.drawCenteredString(font, "...", this.width / 2, top + 20, 0xAAAAAA);
			return;
		}

		// 座位区（等待阶段显示 AI 预约；对局中显示名字 + [AI]）
		// 管理悬浮提示（等待阶段）：记录悬停座位，循环结束后在鼠标旁绘制悬浮框
		String hoverHint = null;
		for (int seat = 0; seat < 4; seat++) {
			int y = top + seat * ROW_H;
			boolean hovered = this.sync.canManage() && mouseX >= left && mouseX < left + PANEL_W
					&& mouseY >= y && mouseY < y + ROW_H - 2;
			graphics.fill(left, y, left + PANEL_W, y + ROW_H - 2,
					hovered ? 0x509090C0 : 0x30A0A0A0);
			String seatLabel = Component.translatable("gui.richi.lobby.seat", seat + 1).getString();
			graphics.drawString(font, seatLabel, left + 6, y + 6, 0xFFFFA0, false);
			String name = this.sync.seatNames()[seat];
			String extra = "";
			if (name == null || name.isEmpty()) {
				name = this.sync.seatAI()[seat]
						? Component.translatable("gui.richi.lobby.ai_reserved").getString()
						: Component.translatable("gui.richi.lobby.empty").getString();
			}
			if (this.sync.seatAI()[seat]) {
				// AI 座位：名字旁显示该座位当前独立形象模式（空 = 跟随全局），行右侧显示对应流派
				extra = " [AI:" + seatAvatarLabel(this.sync.seatAvatarModes()[seat]) + "]";
			}
			graphics.drawString(font, name + extra, left + 64, y + 6, 0xFFFFFF, false);
			if (this.sync.seatAI()[seat]) {
				// 流派显示在面板外的右侧，避免与名字重叠
				String flow = flowLabel(this.sync.seatAvatarModes()[seat]);
				graphics.drawString(font, flow, left + PANEL_W + 8, y + 6, 0x80C8C8, false);
			}
			// 悬停提示（仅等待阶段：空座预约 AI / 已预约座位循环形象）
			if (hovered && this.sync.phase() == PHASE_WAITING)
				hoverHint = Component.translatable(
						this.sync.seatAI()[seat] ? "gui.richi.lobby.hint_avatar_cycle" : "gui.richi.lobby.hint_reserve")
						.getString();
		}
		if (hoverHint != null) {
			// 悬浮信息框（跟随鼠标，样式仿原版 tooltip）
			int tw = font.width(hoverHint) + 10;
			int tx = Math.min(mouseX + 14, this.width - tw - 2);
			int ty = Math.max(mouseY - 16, 4);
			graphics.fill(tx, ty, tx + tw, ty + 18, 0xF0100018);
			graphics.renderOutline(tx, ty, tw, 18, 0x505000FF);
			graphics.drawString(font, hoverHint, tx + 5, ty + 5, 0xFFFFFF, false);
		}

		// 队列区（左半边）
		graphics.drawString(font,
				Component.translatable("gui.richi.lobby.queue",
						this.sync.queueNames().size(), 4).getString(),
				colLX, queueTop, 0xAAAAAA, false);
		for (int i = 0; i < QUEUE_ROWS; i++) {
			String name = i < this.sync.queueNames().size() ? this.sync.queueNames().get(i) : null;
			if (name != null)
				graphics.drawString(font, (i + 1) + ". " + name, colLX + 8,
						queueTop + 12 + i * 12, 0xFFFFFF, false);
		}
		// 游戏设置列表头（右半边，仅管理员）
		if (this.sync.canManage() && this.sync.phase() != PHASE_PLAYING)
			graphics.drawString(font,
					Component.translatable("gui.richi.lobby.settings").getString(),
					colRX, queueTop, 0xAAAAAA, false);

		// 灰色半透明按钮（与列表行同款样式：悬停高亮）
		for (UiBtn b : this.btns) {
			boolean hov = mouseX >= b.x() && mouseX < b.x() + b.w() && mouseY >= b.y() && mouseY < b.y() + b.h();
			graphics.fill(b.x(), b.y(), b.x() + b.w(), b.y() + b.h(), hov ? 0x509090C0 : 0x30A0A0A0);
			String label = b.label().get();
			graphics.drawString(font, label, b.x() + (b.w() - font.width(label)) / 2, b.y() + 5, 0xFFFFFF, false);
		}

		// 阶段提示：对局中 = 投票进度（面板居中，投票按钮下方）；终局 = 右列（设置/清理桌子下方；非管理员在右列顶部）
		String phase = switch (this.sync.phase()) {
			case PHASE_PLAYING -> Component.translatable("gui.richi.lobby.vote",
					this.sync.voteCount(), humanSeats()).getString();
			case PHASE_FINISHED -> Component.translatable("gui.richi.lobby.finished").getString();
			default -> "";
		};
		if (!phase.isEmpty()) {
			boolean adminIdleNow = this.sync.canManage() && this.sync.phase() != PHASE_PLAYING;
			if (this.sync.phase() == PHASE_PLAYING)
				graphics.drawCenteredString(font, phase, this.width / 2, queueTop + 96, 0xFFAA55);
			else
				graphics.drawCenteredString(font, phase, colRX + colW / 2, adminIdleNow ? queueTop + 116 : queueTop + 12,
						0xFFAA55);
		}

		// 对局中：面板右侧外信息框，显示各 AI 座位的牌风流派
		if (this.sync.phase() == PHASE_PLAYING) {
			int bx = left + PANEL_W + 10;
			int bw = 104;
			var lines = new java.util.ArrayList<String>();
			for (int seat = 0; seat < 4; seat++)
				if (this.sync.seatAI()[seat] && this.sync.seatFlows()[seat] >= 0)
					lines.add(seatName(seat) + " " + this.sync.seatNames()[seat] + "："
							+ flowName(this.sync.seatFlows()[seat]));
			if (!lines.isEmpty()) {
				int bh = 16 + lines.size() * 12 + 4;
				graphics.fill(bx, top, bx + bw, top + bh, 0xC0101018);
				graphics.fill(bx, top, bx + bw, top + 1, 0xFF5588FF);
				graphics.fill(bx, top + bh - 1, bx + bw, top + bh, 0xFF5588FF);
				graphics.drawString(font,
						Component.translatable("gui.richi.lobby.flow_title").getString(), bx + 6, top + 5, 0x88CCFF, false);
				for (int i = 0; i < lines.size(); i++)
					graphics.drawString(font, lines.get(i), bx + 6, top + 18 + i * 12, 0xFFFFFF, false);
			}
		}

		// 座位 AI 类型快速选择列表（最后绘制，盖住队列/按钮；右键座位弹出，显示顺序见 pickerOrder）
		if (this.pickerSeat >= 0 && this.sync.seatAI()[this.pickerSeat] && this.sync.phase() == PHASE_WAITING) {
			int px = left + 64, py = pickerTop();
			int[] order = pickerOrder();
			graphics.fill(px - 4, py, px + PICK_W + 4, py + pickerH(), 0xFF101018); // 背景不透明
			graphics.renderOutline(px - 4, py, PICK_W + 8, pickerH(), 0x505000FF); // 描边
			String cur = this.sync.seatAvatarModes()[this.pickerSeat];
			for (int row = 0; row < order.length; row++) {
				int mode = order[row]; // -1 = 移除 AI，否则 AVATAR_MODES 下标
				int ey = py + 3 + row * PICK_ROW;
				int eyBottom = ey + PICK_ROW;
				if (mouseY >= ey && mouseY < eyBottom)
					graphics.fill(px - 2, ey, px + PICK_W + 2, eyBottom, 0x405088FF);
				boolean isRemove = mode < 0;
				String label = isRemove
						? Component.translatable("gui.richi.lobby.picker_remove").getString()
						: Component.translatable(AVATAR_KEYS[mode]).getString();
				int color = isRemove ? 0xFF8080
						: mcr.richi.game.MahjongLobby.AVATAR_MODES[mode].equals(cur) ? 0xFFCC66 : 0xFFFFFF;
				graphics.drawString(font, label, px + 2, ey + 2, color, false);
			}
		}
	}

	/** 座位风名（東南西北） */
	private String seatName(int seat) {
		return new String[] { "東", "南", "西", "北" }[seat];
	}

	/** 流派索引 → 流派名（与 RiichiBot.FLOW_NAMES 一致，客户端侧副本防类加载差异） */
	private String flowName(int flow) {
		String[] names = { "一般流", "野猪流", "门清流", "御无双", "魂天流", "闪电流", "鬼神境" };
		return flow >= 0 && flow < names.length ? names[flow] : "?";
	}

	/** 人类座位数（投票总数；AI 不参与投票） */
	private int humanSeats() {
		int n = 0;
		for (int i = 0; i < 4; i++)
			if (this.sync.seatNames()[i] != null && !this.sync.seatNames()[i].isEmpty() && !this.sync.seatAI()[i])
				n++;
		return Math.max(1, n);
	}

	/** 座位独立 AI 形象模式显示名：null/空 = 随机，未知值也回退随机 */
	private String seatAvatarLabel(String mode) {
		if (mode != null && !mode.isEmpty()) {
			for (int i = 0; i < mcr.richi.game.MahjongLobby.AVATAR_MODES.length; i++)
				if (mcr.richi.game.MahjongLobby.AVATAR_MODES[i].equals(mode))
					return Component.translatable(AVATAR_KEYS[i]).getString();
		}
		return Component.translatable(AVATAR_KEYS[0]).getString();
	}

	/** 座位 AI 形象对应的流派名（预约阶段展示；随机/人类/村民 = 随机牌风，映射同 RiichiBot.flowOfAvatar） */
	private String flowLabel(String mode) {
		String key = switch (mode == null ? "" : mode) {
			case "chen_huang" -> "flow_general";
			case "luohong" -> "flow_boar";
			case "moss_beast" -> "flow_quiet";
			case "wenyao" -> "flow_musou";
			case "komainu" -> "flow_kouten";
			case "ferocious" -> "flow_kouten";
			case "dark_fury" -> "flow_kouten";
			case "silkmoth" -> "flow_flash";
			default -> "flow_random";
		};
		return Component.translatable("gui.richi.lobby." + key).getString();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.sync != null) {
			// 灰色半透明按钮（列表样式）命中
			if (this.pickerSeat < 0 && button == 0) {
				for (UiBtn b : this.btns) {
					if (mouseX >= b.x() && mouseX < b.x() + b.w() && mouseY >= b.y() && mouseY < b.y() + b.h()) {
						b.action().run();
						return true;
					}
				}
			}
		}
		// 仅等待阶段允许操作座位行：左击空座 = 预约 AI；右击已预约 AI 座位 = 打开类型快速选择列表
		// （列表含全部形象模式 + 移除 AI）；左击已预约座位 = 循环切换（保留快捷操作）
		if (this.sync != null && this.sync.canManage() && this.sync.phase() == PHASE_WAITING) {
			int left = this.width / 2 - PANEL_W / 2;
			int top = this.height / 2 - 100;
			if (this.pickerSeat >= 0) {
				handlePickerClick(mouseX, mouseY);
				return true;
			}
			if (mouseX >= left && mouseX < left + PANEL_W && mouseY >= top && mouseY < top + 4 * ROW_H) {
				int seat = (int) ((mouseY - top) / ROW_H);
				if (button == 1 && this.sync.seatAI()[seat]) {
					this.pickerSeat = seat; // 右键：打开快速选择列表
				} else if (button == 0 && this.sync.seatAI()[seat]) {
					String cur = this.sync.seatAvatarModes()[seat];
					int idx = 0; // 空/未知 = 随机
					for (int i = 0; i < mcr.richi.game.MahjongLobby.AVATAR_MODES.length; i++)
						if (mcr.richi.game.MahjongLobby.AVATAR_MODES[i].equals(cur))
							idx = i;
					int next = (idx + 1) % mcr.richi.game.MahjongLobby.AVATAR_MODES.length;
					send(MahjongLobbyPayloads.ACT_SET_AVATAR_SEAT, (seat << 16) | next);
				} else if (button == 0
						&& (this.sync.seatNames()[seat] == null || this.sync.seatNames()[seat].isEmpty())) {
					// 仅空座可预约（队列玩家占用的座位显示名字，不可替换为 AI）
					send(MahjongLobbyPayloads.ACT_ADD_AI, seat);
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	// ==================================================================
	// 座位 AI 类型快速选择列表（右键座位弹出）
	// ==================================================================

	/** 弹出列表的座位（-1 = 关闭）；条目按 PICKER_ORDER 显示 */
	private int pickerSeat = -1;
	private static final int PICK_ROW = 13, PICK_W = 128;

	/** 弹出列表显示顺序（值 = AVATAR_MODES 下标，-1 = 移除 AI）：移除AI > 人类 > 村民 > 随机 > 其余形态 */
	private static int[] pickerOrder() {
		String[] modes = mcr.richi.game.MahjongLobby.AVATAR_MODES;
		int human = -1, villager = -1, random = -1;
		for (int i = 0; i < modes.length; i++) {
			switch (modes[i]) {
				case "human" -> human = i;
				case "villager" -> villager = i;
				case "random" -> random = i;
			}
		}
		int[] order = new int[modes.length + 1];
		int n = 0;
		order[n++] = -1; // 移除 AI
		if (human >= 0)
			order[n++] = human;
		if (villager >= 0)
			order[n++] = villager;
		if (random >= 0)
			order[n++] = random;
		for (int i = 0; i < modes.length; i++)
			if (i != human && i != villager && i != random)
				order[n++] = i;
		return order;
	}

	/** 列表几何（顶部对齐座位行下方，底部溢出时上移） */
	private int pickerTop() {
		int seatY = this.height / 2 - 100 + this.pickerSeat * ROW_H + ROW_H;
		return Math.min(seatY, this.height - pickerH() - 8);
	}

	private int pickerH() {
		return pickerOrder().length * PICK_ROW + 6;
	}

	private void handlePickerClick(double mouseX, double mouseY) {
		int seat = this.pickerSeat;
		this.pickerSeat = -1;
		int px = this.width / 2 - PANEL_W / 2 + 64, py = pickerTopFor(seat);
		int[] order = pickerOrder();
		if (mouseX >= px && mouseX < px + PICK_W && mouseY >= py) {
			int row = (int) ((mouseY - py - 3) / PICK_ROW);
			if (row >= 0 && row < order.length) {
				int mode = order[row]; // -1 = 移除 AI
				if (mode < 0)
					send(MahjongLobbyPayloads.ACT_REMOVE_AI, seat);
				else
					send(MahjongLobbyPayloads.ACT_SET_AVATAR_SEAT, (seat << 16) | mode);
			}
		}
	}

	private int pickerTopFor(int seat) {
		int saved = this.pickerSeat;
		this.pickerSeat = seat;
		int v = pickerTop();
		this.pickerSeat = saved;
		return v;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
