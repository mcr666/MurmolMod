package mcr.richi.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import mcr.richi.network.MahjongSettlementPayload;

/**
 * 结算界面（S→C 推送后自动打开）：
 * <ul>
 * <li>和牌结算：和牌者 + 荣和/自摸 + 役种翻符，手牌与副露直接 blit 牌面贴图（TileIcons），
 * 和牌张抬高并高亮。</li>
 * <li>终局结算：四家最终点数排名（高→低），冠军标注。</li>
 * </ul>
 * 役种播报结束后进入 5s 确认窗口，全员确定或超时由服务端推进并推送关闭包；
 * 之后展示点数变化界面（MahjongPointDeltaScreen）。
 */
public class MahjongSettlementScreen extends Screen {
	/** 确认窗口（刻）：役种播报结束后 5s 内全员确定，否则服务端强制推进 */
	private static final int CONFIRM_WINDOW_TICKS = 5 * 20;
	/** 牌面绘制步长 = 贴图宽（缩放后 14px 宽 × 18px 高），牌与牌紧贴 */
	private static final int TILE_STEP = 14;

	private MahjongSettlementPayload.Settlement data;
	private int age;
	/** 已播报条数：每新出现一条役种播放一次扔雪球音效 */
	private int announced;

	private MahjongSettlementScreen() {
		super(Component.translatable("gui.richi.settlement.title"));
	}

	public static void open() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null)
			mc.setScreen(new MahjongSettlementScreen());
	}

	/** 服务端推进下一局：关闭已打开的结算界面 */
	public static void closeIfOpen() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof MahjongSettlementScreen)
			mc.setScreen(null);
	}

	@Override
	protected void init() {
		this.data = MahjongSettlementPayload.last;
		// 单一"确定"按钮：回传确认（服务端全员确认后推进）并关闭界面
		addRenderableWidget(Button.builder(Component.translatable("gui.richi.settlement.confirm"), b -> {
					if (this.data != null && this.data.mode() == MahjongSettlementPayload.MODE_HAND_WIN)
						net.neoforged.neoforge.network.PacketDistributor.sendToServer(
								new mcr.richi.network.MahjongGamePayloads.ActionMessage(
										this.data.originPacked(), "settle_confirm", ""));
					onClose();
				}).bounds(this.width / 2 - 40, Math.min(this.height - 26, this.height / 2 + 78), 80, 20).build());
	}

	@Override
	public void tick() {
		// 自动关闭兜底：播报结束 + 确认窗口 + 2s 余量
		if (++age >= announceEndTick() + CONFIRM_WINDOW_TICKS + 40)
			onClose();
		// 报番音效：每新出现一条役种播放一次扔雪球音（可见条数由 age 推导，与渲染一致）
		if (this.data != null && !this.data.yakuList().isBlank()) {
			int n = this.data.yakuList().split(";").length;
			int shown = Math.min(n, n <= 4 ? 1 : (n + 3) / 4 * 4);
			int visible = 0;
			for (int i = 0; i < shown; i++)
				if (age >= 15 + 7 * i)
					visible++;
			if (visible > this.announced) {
				this.announced = visible;
				Minecraft.getInstance().getSoundManager().play(
						net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
								net.minecraft.sounds.SoundEvents.SNOWBALL_THROW, 1.0F));
			}
		}
	}

	/** 役种逐条播报结束时刻（15tick 起、间隔 7tick、尾条停留 1s） */
	private int announceEndTick() {
		int n = this.data == null || this.data.yakuList().isBlank() ? 0
				: this.data.yakuList().split(";").length;
		return n == 0 ? 20 : 15 + 7 * (n - 1) + 20;
	}

	private static List<Integer> parseCsv(String csv) {
		List<Integer> codes = new ArrayList<>();
		for (String p : csv.split(","))
			if (!p.isBlank())
				try {
					codes.add(Integer.parseInt(p.trim()));
				} catch (NumberFormatException ignored) {
				}
		return codes;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		if (this.data == null) {
			g.drawCenteredString(this.font, "...", this.width / 2, this.height / 2, 0xAAAAAA);
			return;
		}
		// 确认倒计时（右上角）：役种播报结束后才开始 5s 倒计时；全员确定或倒计时结束服务端强制推进
		int announceEnd = announceEndTick();
		boolean win = this.data.mode() == MahjongSettlementPayload.MODE_HAND_WIN;
		List<Integer> doraTiles = win ? parseCsv(this.data.doraCsv()) : List.of();
		List<Integer> uraTiles = win ? parseCsv(this.data.uraCsv()) : List.of();
		List<String> yakus = win && !this.data.yakuList().isBlank()
				? List.of(this.data.yakuList().split(";"))
				: List.of();
		// 役种网格：固定每列 4 个（排满一列再排下一列，≤4 条目单列）
		int cols = yakus.size() <= 4 ? 1 : (yakus.size() + 3) / 4;
		int yakuRows = yakus.isEmpty() ? 0 : Math.min(4, yakus.size());
		int COL_W = 78;
		int pad = 12;
		// 动态高度：标题+宝牌指示牌（表/里同一排）+手牌+副露+役种网格+番符点数；终局 = 标题+4 行排名
		int handRows = win ? 1 + meldGroups().size() : 0;
		int indRows = win && (!doraTiles.isEmpty() || !uraTiles.isEmpty()) ? 1 : 0;
		int indW = (doraTiles.size() + uraTiles.size()) * TILE_STEP + 110;
		int h = pad + 12 + (win ? 12 + (indRows * 32) + handRows * 22
				+ (yakuRows == 0 ? 0 : yakuRows * 13 + 4) + 14 : 4 * 18 + 8) + pad;
		int w = Math.min(this.width - 8, win
				? Math.max(220, Math.max(parseCsv(this.data.handCsv()).size() * TILE_STEP,
						yakus.size() > 1 ? cols * COL_W : 0) + pad * 2)
				: 180);
		if (win)
			w = Math.min(this.width - 8, Math.max(w, indW));
		int left = this.width / 2 - w / 2;
		int top = this.height / 2 - h / 2 - 8;
		g.fill(left, top, left + w, top + h, 0xE0000000);
		int border = 0xFFB8860B;
		g.fill(left, top, left + w, top + 1, border);
		g.fill(left, top + h - 1, left + w, top + h, border);
		g.fill(left, top, left + 1, top + h, border);
		g.fill(left + w - 1, top, left + w, top + h, border);

		int y = top + 6;
		// 标题 = 荣和/自摸/流局满贯 + 和牌者
		String title = Component.translatable(
				win ? "gui.richi.settlement." + this.data.winType() : "gui.richi.settlement.final").getString();
		String head = win ? title + "　" + this.data.winnerName() : title;
		g.drawCenteredString(this.font, head, this.width / 2, y, 0xFFFF55);
		// 倒计时在役种播报结束后才开始显示
		if (age >= announceEnd) {
			int remain = Math.max(0, (announceEnd + CONFIRM_WINDOW_TICKS - age + 19) / 20);
			String cd = Component.translatable("gui.richi.settlement.countdown", remain).getString();
			g.drawString(this.font, cd, left + w - pad - this.font.width(cd), top + 6, 0xFFAA55, false);
		}
		y += 14;

		if (win) {
			// 宝牌指示牌行（表宝牌，含里宝：立直和牌时展示）
			// 表宝牌指示牌与里宝牌同一排；各自小字标签画在牌面上方一排（0.75 倍字号）
			if (!doraTiles.isEmpty() || !uraTiles.isEmpty()) {
				int ty = y + 9;
				int tx = left + pad;
				if (!doraTiles.isEmpty()) {
					drawSmall(g, Component.translatable("gui.richi.settlement.dora_ind").getString(),
							tx, y, 0x7FDDFF);
					for (int code : doraTiles)
						TileIcons.draw(g, code, tx += TILE_STEP, ty, TILE_STEP);
					tx += TILE_STEP + 8;
				}
				if (!uraTiles.isEmpty()) {
					drawSmall(g, Component.translatable("gui.richi.settlement.ura").getString(),
							tx, y, 0xAA88FF);
					for (int code : uraTiles)
						TileIcons.draw(g, code, tx += TILE_STEP, ty, TILE_STEP);
				}
				y += 32;
			}
			// 手牌（和牌张抬高 3px + 金框；等比 16×21）
			List<Integer> hand = parseCsv(this.data.handCsv());
			int tx = left + pad;
			for (int i = 0; i < hand.size(); i++) {
				int code = hand.get(i);
				boolean isWinTile = code == this.data.winTile() && i == hand.size() - 1;
				int ty = y - (isWinTile ? 3 : 0);
				if (isWinTile)
					g.fill(tx - 1, ty - 1, tx + TILE_STEP + 1, ty + TILE_STEP * 4 / 3 + 1, 0xFFB8860B);
				TileIcons.draw(g, code, tx, ty, TILE_STEP);
				tx += TILE_STEP;
			}
			y += 20;
			// 副露组
			for (List<Integer> group : meldGroups()) {
				tx = left + pad;
				for (int code : group)
					TileIcons.draw(g, code, tx += TILE_STEP, y, TILE_STEP);
				y += 22;
			}
			// 役种逐条播报：延迟 15 tick，间隔 7 tick 依次出现（≤4 一行一个，否则 3 列网格）
			if (!yakus.isEmpty()) {
				y += 2;
				int shown = Math.min(yakus.size(), cols * 4);
				for (int i = 0; i < shown; i++) {
					if (age < 15 + 7 * i)
						break;
					// 列优先且每列固定 4 个：col = i/4, row = i%4（播报顺序与排布一致）
					int col = i / 4;
					int row = i % 4;
					g.drawString(this.font, yakus.get(i), left + pad + col * COL_W, y + row * 13, 0x7FFF7F, true);
				}
				if (shown < yakus.size() && age >= 15 + 7 * (shown - 1))
					g.drawString(this.font, "+" + (yakus.size() - shown), left + pad, y + yakuRows * 13, 0xAAAAAA, true);
				y += yakuRows * 13 + 4;
			}
			// 底部：番数·符数·点数（役满显示"役满"；流局满贯只显示点数）
			String score;
			if ("nagashi".equals(this.data.winType()))
				score = Component.translatable("gui.richi.settlement.pts_only", this.data.gain()).getString();
			else if (this.data.han() <= 0 && this.data.fu() <= 0)
				score = Component.translatable("gui.richi.settlement.yakuman").getString();
			else
				score = Component.translatable("gui.richi.settlement.score",
						this.data.han(), this.data.fu(), this.data.gain()).getString();
			g.drawCenteredString(this.font, score, this.width / 2, y, 0xFFFF55);
		} else {
			// 终局排名：按点数高→低
			Integer[] order = { 0, 1, 2, 3 };
			java.util.Arrays.sort(order, (a, b) -> Integer.compare(this.data.points()[b], this.data.points()[a]));
			for (int rank = 0; rank < 4; rank++) {
				int seat = order[rank];
				String line = (rank + 1) + ". " + this.data.names()[seat]
						+ "　" + this.data.points()[seat]
						+ Component.translatable("gui.richi.settlement.pt").getString();
				int color = rank == 0 ? 0xFFFF55 : 0xFFFFFF;
				g.drawString(this.font, line, left + pad, y, color, true);
				y += 18;
			}
		}
	}

	/** 小字标签（0.75 倍字号）：宝牌行上方的提示文字 */
	private void drawSmall(GuiGraphics g, String text, int x, int y, int color) {
		var pose = g.pose();
		pose.pushPose();
		pose.translate(x, y, 0);
		pose.scale(0.75f, 0.75f, 1.0f);
		g.drawString(this.font, text, 0, 0, color, true);
		pose.popPose();
	}

	private List<List<Integer>> meldGroups() {
		List<List<Integer>> groups = new ArrayList<>();
		for (String grp : this.data.meldsGroups().split(";")) {
			if (grp.isBlank())
				continue;
			groups.add(parseCsv(grp));
		}
		return groups;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
