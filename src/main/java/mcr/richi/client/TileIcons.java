package mcr.richi.client;

import net.minecraft.resources.ResourceLocation;

/**
 * 麻雀牌面贴图直绘（客户端）：把牌面代码映射到 assets/richi/textures/item/mahjong/<名>.png，
 * GUI 上直接 blit 原始贴图（不经 item 模型渲染管线）。
 * 贴图实际尺寸 48×64（3:4 竖长），绘制时按比例缩放（宽 w → 高 w*4/3），避免压扁。
 * code：0=红5万 1-9=万 10=红5饼 11-19=饼 20=红5索 21-29=索 30-36=字。
 */
public final class TileIcons {
	private TileIcons() {
	}

	private static final String[] Z = { "1z", "2z", "3z", "4z", "5z", "6z", "7z" };
	/** 贴图原始尺寸 */
	private static final int TEX_W = 48, TEX_H = 64;

	/** 牌面代码 → 贴图路径（不含 assets/ 前缀） */
	public static ResourceLocation icon(int code) {
		String name;
		if (code < 10)
			name = code + "m";
		else if (code < 20)
			name = (code == 10 ? 0 : code - 10) + "p";
		else if (code < 30)
			name = (code == 20 ? 0 : code - 20) + "s";
		else
			name = Z[Math.min(6, Math.max(0, code - 30))];
		return ResourceLocation.fromNamespaceAndPath(mcr.richi.MahjongItems.NAMESPACE,
				"textures/item/mahjong/" + name + ".png");
	}

	/** 等比绘制：宽 w，高 w*4/3（整张贴图缩放到该尺寸） */
	public static void draw(net.minecraft.client.gui.GuiGraphics g, int code, int x, int y, int w) {
		int h = w * TEX_H / TEX_W;
		// 全图采样：uWidth/vHeight = 整张贴图像素，缩放绘制到 w×h
		g.blit(icon(code), x, y, w, h, 0.0F, 0.0F, TEX_W, TEX_H, TEX_W, TEX_H);
	}

	/** 便利重载：默认宽 12 → 高 16 */
	public static void draw(net.minecraft.client.gui.GuiGraphics g, int code, int x, int y) {
		draw(g, code, x, y, 12);
	}
}
