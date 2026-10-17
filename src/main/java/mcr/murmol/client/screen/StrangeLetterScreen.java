package mcr.murmol.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 陌生的来信阅读界面。简洁的信纸样式，居中显示信件内容。
 */
public class StrangeLetterScreen extends Screen {

	private static final int W = 330; // 1.5x 原宽 220
	private static final int H = 200;

	public StrangeLetterScreen() {
		super(Component.translatable("item.murmol.strange_letter"));
	}

	public static void open() {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new StrangeLetterScreen());
	}

	// 信件内容（带缩进格式，不含标题）
	private static final String[] LINES = {
		"",
		"  嗨，生还者，别来无恙？",
		"",
		"  星幻瘟疫已经席卷许多年了，你的存活不得不使人点赞。可能是标准的安全规范，",
		"  让我也成了当初与星辰矿见过面的人中唯一的在世者，这大概是一种缘分。",
		"",
		"  总之，我很想见见你，但因为没有 GPS 和北斗系统，只能等你来找我了。在此之",
		"  前，好好活着，可以吗？",
		"",
		"  最好先做一台星象仪——用红石粉、铁锭加玻璃就行，它会在旅途中帮到你的。",
		"",
		"  我在唤星学会的雀庄里等着你。祝你好运！",
		"",
		"—— Murmol"
	};

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
		this.renderBackground(g, mouseX, mouseY, pt);
		int ox = (this.width - W) / 2;
		int oy = (this.height - H) / 2;

		// 信纸背景（米黄色）
		g.fill(ox, oy, ox + W, oy + H, 0xFFF5E6C8);
		g.fill(ox + 1, oy + 1, ox + W - 1, oy + H - 1, 0xFFF0DDB0);
		// 边框
		g.fill(ox, oy, ox + W, oy + 2, 0xFF8B6914);
		g.fill(ox, oy + H - 2, ox + W, oy + H, 0xFF8B6914);
		g.fill(ox, oy, ox + 2, oy + H, 0xFF8B6914);
		g.fill(ox + W - 2, oy, ox + W, oy + H, 0xFF8B6914);

		// 文字
		int cy = oy + 14;
		for (String line : LINES) {
			if (line.startsWith("——")) {
				// 署名右对齐（距右边 28px）
				int tw = this.font.width(line);
				g.drawString(this.font, line, ox + W - tw - 28, cy, 0xFF5C3A1E, false);
			} else {
				g.drawString(this.font, line, ox + 16, cy, 0xFF5C3A1E, false);
			}
			cy += 12;
		}

		// 底部提示
		g.drawCenteredString(this.font, "（按 Esc 关闭）", this.width / 2, oy + H - 14, 0xFF998866);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
