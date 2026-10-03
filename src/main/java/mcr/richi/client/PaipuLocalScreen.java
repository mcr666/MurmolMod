package mcr.richi.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.network.PacketDistributor;

import mcr.richi.network.PaipuPayload;

/**
 * 本地牌谱库界面：列出游戏根目录/richi/*.log（按修改时间倒序）。
 * 点击条目 → 本地回放（Esc 返回本列表）；条目右侧"上传"按钮 → 覆写服务端同名桌的牌谱。
 * Esc 返回服务端牌谱列表。
 */
public class PaipuLocalScreen extends Screen {
	private List<PaipuLocal.Entry> files = List.of();
	private int scroll;

	public PaipuLocalScreen() {
		super(Component.translatable("gui.richi.paipu.local_title"));
	}

	public static void open() {
		Minecraft.getInstance().setScreen(new PaipuLocalScreen());
	}

	@Override
	protected void init() {
		files = PaipuLocal.list();
		scroll = 0;
	}

	private static final int ROW_H = 22;
	private static final int PANEL_W = 240;
	/** 条目右侧"上传"按钮宽 */
	private static final int UPLOAD_W = 44;

	private int rowsVisible() {
		return Math.max(1, (this.height - 70) / ROW_H);
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		super.render(gfx, mouseX, mouseY, partialTick);
		var font = this.font;
		int left = this.width / 2 - PANEL_W / 2;
		int top = 30;
		gfx.drawCenteredString(font, this.title, this.width / 2, top - 16, 0xFFFFFF);

		if (files.isEmpty()) {
			gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.local_none").getString(),
					this.width / 2, top + 20, 0xFFCC66);
		} else {
			int max = Math.min(files.size(), scroll + rowsVisible());
			for (int i = scroll; i < max; i++) {
				PaipuLocal.Entry e = files.get(i);
				int y = top + (i - scroll) * ROW_H;
				boolean hovRow = mouseX >= left && mouseX < left + PANEL_W - UPLOAD_W
						&& mouseY >= y && mouseY < y + ROW_H - 2;
				gfx.fill(left, y, left + PANEL_W, y + ROW_H - 2, hovRow ? 0x509090C0 : 0x30A0A0A0);
				String label = e.name().substring(0, e.name().length() - 4).replace("_", ", ");
				gfx.drawString(font, label, left + 6, y + 4, 0xFFFFA0, false);
				String size = e.bytes() < 1024 ? e.bytes() + "B" : (e.bytes() / 1024) + "KB";
				gfx.drawString(font, size, left + PANEL_W - UPLOAD_W - 8 - font.width(size), y + 4, 0xAAAAAA, false);
				// 上传按钮
				boolean hovUp = mouseX >= left + PANEL_W - UPLOAD_W && mouseX < left + PANEL_W
						&& mouseY >= y && mouseY < y + ROW_H - 2;
				gfx.fill(left + PANEL_W - UPLOAD_W, y, left + PANEL_W, y + ROW_H - 2,
						hovUp ? 0x5060A0C0 : 0x30406080);
				gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.upload").getString(),
						left + PANEL_W - UPLOAD_W / 2, y + 4, 0x99FF99);
			}
		}
		gfx.drawCenteredString(font, Component.translatable("gui.richi.paipu.local_hint").getString(),
				this.width / 2, this.height - 18, 0x909090);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && !files.isEmpty()) {
			int left = this.width / 2 - PANEL_W / 2;
			int top = 30;
			int max = Math.min(files.size(), scroll + rowsVisible());
			for (int i = scroll; i < max; i++) {
				PaipuLocal.Entry e = files.get(i);
				int y = top + (i - scroll) * ROW_H;
				if (mouseY < y || mouseY >= y + ROW_H - 2)
					continue;
				if (mouseX >= left && mouseX < left + PANEL_W - UPLOAD_W) {
					// 本地回放（Esc 返回本列表）
					String content = PaipuLocal.read(e.name());
					long packed = PaipuLocal.packedOfName(e.name());
					if (content != null && packed != 0)
						PaipuReplay.openLocal(packed, content);
					return true;
				}
				if (mouseX >= left + PANEL_W - UPLOAD_W && mouseX < left + PANEL_W) {
					// 上传到服务端（覆写同名桌牌谱）
					String content = PaipuLocal.read(e.name());
					long packed = PaipuLocal.packedOfName(e.name());
					if (content != null && packed != 0)
						PacketDistributor.sendToServer(new PaipuPayload.UploadData(packed, content));
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
		scroll = Math.max(0, Math.min(scroll - (int) yDelta, Math.max(0, files.size() - rowsVisible())));
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
			PaipuListScreen.open();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		// Esc 走 keyPressed 已返回服务端列表；这里兜底（如 mod 菜单强制关闭）
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
