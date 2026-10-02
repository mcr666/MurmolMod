package mcr.richi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import mcr.richi.network.MahjongPointDeltaPayload;

/**
 * 点数变化界面（和牌结算界面关闭后展示）：四家点数 局前 → 局后（±增减着色）。
 * 5s 自动确认（回 settle_confirm 推进服务端）；全员确认立即推进（服务端关闭包兜底）。
 */
public class MahjongPointDeltaScreen extends Screen {
	private static final int AUTO_CONFIRM_TICKS = 5 * 20;

	private MahjongPointDeltaPayload.PointDeltaMessage data;
	private int age;

	private MahjongPointDeltaScreen(MahjongPointDeltaPayload.PointDeltaMessage data) {
		super(Component.translatable("gui.richi.settlement.title"));
		this.data = data;
	}

	public static void open(MahjongPointDeltaPayload.PointDeltaMessage data) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null)
			mc.setScreen(new MahjongPointDeltaScreen(data));
	}

	/** 服务端推进下一局：关闭已打开的点数变化界面 */
	public static void closeIfOpen() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof MahjongPointDeltaScreen)
			mc.setScreen(null);
	}

	@Override
	public void tick() {
		if (++age >= AUTO_CONFIRM_TICKS)
			confirm();
	}

	/** 确认：回传服务端推进（阶段 2 全员确认即续局）并关闭界面 */
	private void confirm() {
		if (this.data != null)
			net.neoforged.neoforge.network.PacketDistributor.sendToServer(
					new mcr.richi.network.MahjongGamePayloads.ActionMessage(
							this.data.originPacked(), "settle_confirm", ""));
		onClose();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
				|| keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
				|| keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_E) {
			confirm();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		confirm();
		return true;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		if (this.data == null)
			return;
		int pad = 14;
		int rowH = 20;
		int h = pad * 2 + 14 + 12 + 4 * rowH + 14;
		int w = 210;
		int left = this.width / 2 - w / 2;
		int top = this.height / 2 - h / 2 - 8;
		g.fill(left, top, left + w, top + h, 0xE0000000);
		int border = 0xFFB8860B;
		g.fill(left, top, left + w, top + 1, border);
		g.fill(left, top + h - 1, left + w, top + h, border);
		g.fill(left, top, left + 1, top + h, border);
		g.fill(left + w - 1, top, left + w, top + h, border);

		int y = top + 6;
		g.drawCenteredString(this.font,
				Component.translatable("gui.richi.points.title").getString(), this.width / 2, y, 0xFFFF55);
		y += 16;
		String pt = Component.translatable("gui.richi.settlement.pt").getString();
		for (int seat = 0; seat < 4; seat++) {
			String name = this.data.names().get(seat);
			int before = this.data.before().get(seat);
			int after = this.data.after().get(seat);
			int delta = after - before;
			String deltaStr = (delta >= 0 ? "+" : "") + delta;
			int color = delta > 0 ? 0x7FFF7F : delta < 0 ? 0xFF7F7F : 0xFFFFFF;
			g.drawString(this.font, name, left + pad, y, 0xFFFFFF, true);
			String mid = before + " → " + after;
			g.drawString(this.font, mid, left + pad + 70, y, 0xCCCCCC, false);
			String line = deltaStr + pt;
			g.drawString(this.font, line, left + w - pad - this.font.width(line), y, color, true);
			y += rowH;
		}
		// 剩余秒数提示
		int remain = Math.max(0, (AUTO_CONFIRM_TICKS - age + 19) / 20);
		String cd = Component.translatable("gui.richi.points.confirm_in", remain).getString();
		g.drawCenteredString(this.font, cd, this.width / 2, y + 2, 0xFFAA55);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
