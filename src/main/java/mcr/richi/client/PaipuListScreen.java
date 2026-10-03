package mcr.richi.client;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.network.PacketDistributor;

import mcr.richi.network.PaipuPayload;

/**
 * 牌谱列表界面：打开时向服务端请求 data/mahjong/*.log 清单（按修改时间倒序），
 * 点击条目 → 请求该桌牌谱全文 → 打开 {@link PaipuReplay} 叠加层回放；Esc 返回时重新请求列表。
 * 条目格式 "x_y_z|局序|开始毫秒"（服务端 list() 按对局生成，一桌多局各成条目）。
 */
public class PaipuListScreen extends Screen {
	/** 最近一次服务端清单（apply 写入，屏幕 tick 读取） */
	private static volatile String[] latest = null;

	/** 滚动偏移（条目行数） */
	private int scroll;

	public PaipuListScreen() {
		super(Component.translatable("gui.richi.paipu.list_title"));
	}

	public static void open() {
		Minecraft.getInstance().setScreen(new PaipuListScreen());
	}

	/** 服务端清单到达 */
	public static void apply(String[] entries) {
		latest = entries;
		if (Minecraft.getInstance().screen instanceof PaipuListScreen s)
			s.scroll = 0;
	}

	@Override
	protected void init() {
		latest = null;
		PacketDistributor.sendToServer(new PaipuPayload.ListRequest());
	}

	private static final int ROW_H = 22;
	private static final int PANEL_W = 240;
	/** 条目右侧"下载"按钮宽 */
	private static final int DL_W = 36;

	private int rowsVisible() {
		return Math.max(1, (this.height - 60) / ROW_H);
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		super.render(gfx, mouseX, mouseY, partialTick);
		var font = this.font;
		int left = this.width / 2 - PANEL_W / 2;
		int top = 30;
		gfx.drawCenteredString(font, this.title, this.width / 2, top - 16, 0xFFFFFF);

		if (latest == null) {
			gfx.drawCenteredString(font, "...", this.width / 2, top + 20, 0xAAAAAA);
			return;
		}
		if (latest.length == 0) {
			gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.none").getString(),
					this.width / 2, top + 20, 0xFFCC66);
			return;
		}
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		int max = Math.min(latest.length, scroll + rowsVisible());
		for (int i = scroll; i < max; i++) {
			String[] p = latest[i].split("\\|");
			int y = top + (i - scroll) * ROW_H;
			boolean hovRow = mouseX >= left && mouseX < left + PANEL_W - DL_W
					&& mouseY >= y && mouseY < y + ROW_H - 2;
			gfx.fill(left, y, left + PANEL_W, y + ROW_H - 2, hovRow ? 0x509090C0 : 0x30A0A0A0);
			// 对局序号 + 桌坐标
			String label = "对局 " + (p.length > 1 ? (safeInt(p[1]) + 1) : 1) + " · 风盘 ("
					+ (p.length > 0 ? p[0].replace("_", ", ") : "?") + ")";
			gfx.drawString(font, label, left + 6, y + 4, 0xFFFFA0, false);
			// 下载按钮（整桌 .log 存到本地 richi 目录）
			boolean hovDl = mouseX >= left + PANEL_W - DL_W && mouseX < left + PANEL_W
					&& mouseY >= y && mouseY < y + ROW_H - 2;
			gfx.fill(left + PANEL_W - DL_W, y, left + PANEL_W, y + ROW_H - 2,
					hovDl ? 0x5060A0C0 : 0x30406080);
			gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.download").getString(),
					left + PANEL_W - DL_W / 2, y + 4, 0x99CCFF);
			// 开始时间（下载按钮左侧右对齐）
			String info = "";
			try {
				info = fmt.format(new Date(Long.parseLong(p[2])));
			} catch (Exception ignored) {
			}
			gfx.drawString(font, info, left + PANEL_W - DL_W - 8 - font.width(info), y + 4, 0xAAAAAA, false);
		}
		// 底部提示
		gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.list_hint").getString(),
				this.width / 2, this.height - 30, 0x909090);
		// "本地牌谱"按钮
		int btnW = 120, btnH = 20;
		int bx = this.width / 2 - btnW / 2, by = this.height - 24;
		boolean hovBtn = mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH;
		gfx.fill(bx, by, bx + btnW, by + btnH, hovBtn ? 0x5060A0C0 : 0x30406080);
		gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.btn_local").getString(),
				this.width / 2, by + 6, 0xFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			// "本地牌谱"按钮
			int btnW = 120, btnH = 20;
			int bx = this.width / 2 - btnW / 2, by = this.height - 24;
			if (mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH) {
				PaipuLocalScreen.open();
				return true;
			}
		}
		if (button == 0 && latest != null && latest.length > 0) {
			int left = this.width / 2 - PANEL_W / 2;
			int top = 30;
			int max = Math.min(latest.length, scroll + rowsVisible());
			for (int i = scroll; i < max; i++) {
				int y = top + (i - scroll) * ROW_H;
				if (mouseX >= left && mouseX < left + PANEL_W && mouseY >= y && mouseY < y + ROW_H - 2) {
					try {
						String[] p = latest[i].split("\\|");
						String[] xyz = p[0].split("_");
						long packed = net.minecraft.core.BlockPos
								.asLong(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]),
										Integer.parseInt(xyz[2]));
						// 右侧下载区：整桌 .log 下载到本地 richi 目录（不进入回放）
						if (mouseX >= left + PANEL_W - DL_W) {
							PacketDistributor.sendToServer(new PaipuPayload.DownloadRequest(packed));
							return true;
						}
						int gameIdx = p.length > 1 ? safeInt(p[1]) : 0;
						PaipuReplay.pendingFromList = true;
						PacketDistributor.sendToServer(new PaipuPayload.Request(packed, gameIdx));
						return true;
					} catch (Exception ignored) {
					}
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
		scroll = Math.max(0, Math.min(scroll - (int) yDelta, Math.max(0, latestLength() - rowsVisible())));
		return true;
	}

	private int latestLength() {
		return latest != null ? latest.length : 0;
	}

	private static int safeInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public void tick() {
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
