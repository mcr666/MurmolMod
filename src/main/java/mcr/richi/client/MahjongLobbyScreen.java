package mcr.richi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import mcr.richi.network.MahjongLobbyPayloads;

/**
 * 麻将大厅管理界面（潜行右击风盘打开）：可视化查看座位/等待队列，底部按钮加入或退出队列、
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
			"gui.richi.lobby.av_ferocious", "gui.richi.lobby.av_villager" };

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

	@Override
	protected void init() {
		refresh();
		if (this.sync == null)
			return;
		int left = this.width / 2 - PANEL_W / 2;
		int y = this.height / 2 + 46;
		if (this.sync.phase() == PHASE_PLAYING) {
			// 对局中：投票结束对局界面（旧操作全部无效）
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_vote_end"),
					b -> send(MahjongLobbyPayloads.ACT_VOTE_END, 0))
					.bounds(left, y, 130, 20).build());
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_close"), b -> onClose())
					.bounds(left + 134, y, 60, 20).build());
			return;
		}
		// 第二行（管理员·等待阶段）："游戏设置"——对局类型 + 思考时限（收进主面板，避免左侧超出屏幕）
		if (this.sync.canManage() && this.sync.phase() == PHASE_WAITING) {
			String[] typeKeys = { "gui.richi.lobby.type_single", "gui.richi.lobby.type_east", "gui.richi.lobby.type_full" };
			int type = Math.floorMod(this.sync.gameType(), 3);
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_type",
							Component.translatable(typeKeys[type]).getString()),
					b -> send(MahjongLobbyPayloads.ACT_SET_TYPE, (type + 1) % 3))
					.bounds(left, y + 26, 110, 20).build());
			// 思考时间档位循环：5+20 → 10+30 → 60+0 → 3+5
			int think = Math.floorMod(this.sync.thinkIdx(), 4);
			String[] thinkPresets = { "5+20", "10+30", "60+0", "3+5" };
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_think", thinkPresets[think]),
					b -> send(MahjongLobbyPayloads.ACT_SET_THINK, (think + 1) % 4))
					.bounds(left + 114, y + 26, 116, 20).build());
		}
		// 最后一行：查看牌谱（通栏）；终局阶段右侧挤 1/3 宽度放"清理桌子"
		boolean finished = this.sync.canManage() && this.sync.phase() == PHASE_FINISHED;
		int paipuY = this.sync.canManage() && this.sync.phase() == PHASE_WAITING ? y + 78
				: finished ? y + 26 : y + 26;
		addRenderableWidget(Button.builder(
				Component.translatable("gui.richi.lobby.btn_paipu"),
				b -> PaipuListScreen.open())
				.bounds(left, paipuY, finished ? 150 : 226, 20).build());
		if (finished)
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_clear"),
					b -> send(MahjongLobbyPayloads.ACT_CLEAR, 0))
					.bounds(left + 154, paipuY, 72, 20).build());
		// AI 打牌速度 + 明牌开关并排（管理员·等待阶段）
		if (this.sync.canManage() && this.sync.phase() == PHASE_WAITING) {
			int speed = Math.floorMod(this.sync.aiSpeedIdx(), 3);
			String[] speedKeys = { "gui.richi.lobby.speed_fast", "gui.richi.lobby.speed_mid", "gui.richi.lobby.speed_slow" };
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_speed",
							Component.translatable(speedKeys[speed]).getString()),
					b -> send(MahjongLobbyPayloads.ACT_SET_SPEED, (speed + 1) % 3))
					.bounds(left, y + 52, 110, 20).build());
			boolean open = this.sync.openHand();
			addRenderableWidget(Button.builder(
					Component.translatable("gui.richi.lobby.btn_open",
							Component.translatable(open ? "gui.richi.lobby.open_on" : "gui.richi.lobby.open_off")
									.getString()),
					b -> send(MahjongLobbyPayloads.ACT_SET_OPEN, open ? 0 : 1))
					.bounds(left + 114, y + 52, 112, 20).build());
		}
		// 第一行：加入/退出队列、开始对局、关闭
		addRenderableWidget(Button.builder(
				Component.translatable(inQueue() ? "gui.richi.lobby.btn_leave" : "gui.richi.lobby.btn_join"),
				b -> send(MahjongLobbyPayloads.ACT_JOIN_QUEUE, 0))
				.bounds(left, y, 90, 20).build());
		addRenderableWidget(Button.builder(
				Component.translatable("gui.richi.lobby.btn_start"),
				b -> send(MahjongLobbyPayloads.ACT_START, 0))
				.bounds(left + 94, y, 80, 20).build());
		addRenderableWidget(Button.builder(
				Component.translatable("gui.richi.lobby.btn_close"), b -> onClose())
				.bounds(left + 178, y, 52, 20).build());
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

		// 座位 AI 类型快速选择列表（右键座位弹出，最下方为移除 AI）
		if (this.pickerSeat >= 0 && this.sync.seatAI()[this.pickerSeat] && this.sync.phase() == PHASE_WAITING) {
			int px = left + 64, py = pickerTop();
			int modes = mcr.richi.game.MahjongLobby.AVATAR_MODES.length;
			graphics.fill(px - 4, py, px + PICK_W + 4, py + pickerH(), 0xE8101018);
			graphics.renderOutline(px - 4, py, PICK_W + 8, pickerH(), 0x505000FF);
			String cur = this.sync.seatAvatarModes()[this.pickerSeat];
			for (int i = 0; i <= modes; i++) {
				boolean isRemove = i == modes;
				int ey = py + 3 + i * PICK_ROW;
				int eyBottom = ey + PICK_ROW;
				if (mouseY >= ey && mouseY < eyBottom)
					graphics.fill(px - 2, ey, px + PICK_W + 2, eyBottom, 0x405088FF);
				String label = isRemove
						? Component.translatable("gui.richi.lobby.picker_remove").getString()
						: Component.translatable(AVATAR_KEYS[i]).getString();
				int color = isRemove ? 0xFF8080 : 0xFFFFFF;
				if (!isRemove && mcr.richi.game.MahjongLobby.AVATAR_MODES[i].equals(cur))
					color = 0xFFCC66; // 当前模式高亮
				graphics.drawString(font, label, px + 2, ey + 2, color, false);
			}
		}

		// 队列区
		int queueTop = top + 4 * ROW_H + 6;
		graphics.drawString(font,
				Component.translatable("gui.richi.lobby.queue",
						this.sync.queueNames().size(), 4).getString(),
				left + 4, queueTop, 0xAAAAAA, false);
		for (int i = 0; i < QUEUE_ROWS; i++) {
			String name = i < this.sync.queueNames().size() ? this.sync.queueNames().get(i) : null;
			if (name != null)
				graphics.drawString(font, (i + 1) + ". " + name, left + 12,
						queueTop + 12 + i * 14, 0xFFFFFF, false);
		}

		// 阶段提示（队列列表下方）；对局中显示投票进度
		String phase = switch (this.sync.phase()) {
			case PHASE_PLAYING -> Component.translatable("gui.richi.lobby.vote",
					this.sync.voteCount(), humanSeats()).getString();
			case PHASE_FINISHED -> Component.translatable("gui.richi.lobby.finished").getString();
			default -> "";
		};
		if (!phase.isEmpty())
			graphics.drawCenteredString(font, phase, this.width / 2, queueTop + 12 + QUEUE_ROWS * 14 + 2, 0xFFAA55);

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
			case "silkmoth" -> "flow_flash";
			default -> "flow_random";
		};
		return Component.translatable("gui.richi.lobby." + key).getString();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

	/** 弹出列表的座位（-1 = 关闭）；条目 0..n-1 = AVATAR_MODES，n = 移除 AI */
	private int pickerSeat = -1;
	private static final int PICK_ROW = 13, PICK_W = 128;

	/** 列表几何（顶部对齐座位行下方，底部溢出时上移） */
	private int pickerTop() {
		int seatY = this.height / 2 - 100 + this.pickerSeat * ROW_H + ROW_H;
		return Math.min(seatY, this.height - pickerH() - 8);
	}

	private int pickerH() {
		return (mcr.richi.game.MahjongLobby.AVATAR_MODES.length + 1) * PICK_ROW + 6;
	}

	private void handlePickerClick(double mouseX, double mouseY) {
		int seat = this.pickerSeat;
		this.pickerSeat = -1;
		int px = this.width / 2 - PANEL_W / 2 + 64, py = pickerTopFor(seat);
		int modes = mcr.richi.game.MahjongLobby.AVATAR_MODES.length;
		if (mouseX >= px && mouseX < px + PICK_W && mouseY >= py) {
			int i = (int) ((mouseY - py - 3) / PICK_ROW);
			if (i >= 0 && i < modes)
				send(MahjongLobbyPayloads.ACT_SET_AVATAR_SEAT, (seat << 16) | i);
			else if (i == modes)
				send(MahjongLobbyPayloads.ACT_REMOVE_AI, seat);
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
