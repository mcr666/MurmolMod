package mcr.murmol.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import mcr.murmol.planetarium.PlanetariumEntry;
import mcr.murmol.planetarium.PlanetariumWiki;

/**
 * 星象仪 wiki 界面。
 */
public class PlanetariumScreen extends Screen {

	private static final int W = 360;
	private static final int H = 260;
	private static final int SIDEBAR_W = 120;
	private static final int ROW_H = 11;
	private static final int CAT_H = 12;
	private static final int ICON_TEXT_GAP = 26; // 图标到文字的间距（加宽）

	private final List<CategoryNode> nodes = new ArrayList<>();
	private static String lastEntryId = null;
	private String currentEntryId = null;
	private boolean biographyTab = false;

	private int ox, oy;
	private int sidebarScroll = 0;
	private int contentScroll = 0;
	private int sidebarContentHeight = 0;
	private int contentMaxScroll = 0;

	// 鼠标悬停位置（渲染反馈用）
	private int hoverRowY = -1;
	private int hoverCatY = -1;

	public PlanetariumScreen() {
		super(Component.translatable("gui.murmol.planetarium.title"));
	}

	public static void open() {
		Minecraft.getInstance().setScreen(new PlanetariumScreen());
	}

	public static class CategoryNode {
		public PlanetariumWiki.Category cat;
		public boolean expanded = true;
	}

	@Override
	protected void init() {
		PlanetariumWiki.loadIfNeeded();
		nodes.clear();
		for (var c : PlanetariumWiki.categories()) {
			CategoryNode n = new CategoryNode();
			n.cat = c;
			nodes.add(n);
		}
		if (currentEntryId == null) {
			if (lastEntryId != null) {
				currentEntryId = lastEntryId;
			} else {
				for (var n : nodes)
					for (var e : n.cat.entries)
						if (e.id.equals("welcome")) { currentEntryId = e.id; break; }
				if (currentEntryId == null && !nodes.isEmpty() && !nodes.get(0).cat.entries.isEmpty())
					currentEntryId = nodes.get(0).cat.entries.get(0).id;
			}
		}
		ox = (this.width - W) / 2;
		oy = (this.height - H) / 2;
	}

	private int sidebarTop() { return oy + 20; }
	private int sidebarBottom() { return oy + H - 6; }
	private int contentTop() { return oy + 20; }
	private int titleBottom() { return oy + 40; } // 标题固定区域高度
	private int contentBottom() { return oy + H - (isCurrentForm() ? 28 : 10); }

	private boolean isCurrentForm() {
		if (currentEntryId == null) return false;
		PlanetariumEntry e = PlanetariumWiki.byId(currentEntryId);
		return e != null && e.isForm();
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
		this.renderBackground(g, mouseX, mouseY, pt);
		var font = this.font;

		g.fill(ox, oy, ox + W, oy + H, 0xF02A2A3E);
		g.fill(ox + 1, oy + 1, ox + W - 1, oy + H - 1, 0xF01E1E30);
		g.fill(ox, oy, ox + SIDEBAR_W, oy + H, 0xF034344A);
		g.fill(ox + SIDEBAR_W, oy, ox + SIDEBAR_W + 1, oy + H, 0xFF555577);

		g.drawString(font, Component.translatable("gui.murmol.planetarium.title"),
				ox + 6, oy + 6, 0xFFFFAA00, false);
		// 右上角：mod 版本号
		String ver = "";
		try {
			var mod = net.neoforged.fml.ModList.get().getModFileById("murmol");
			if (mod != null) ver = "v" + mod.versionString();
		} catch (Exception ignored) {}
		if (!ver.isEmpty()) {
			int vw = font.width(ver);
			g.drawString(font, ver, ox + W - vw - 6, oy + 6, 0x888888, false);
		}

		// ---- 左侧目录 ----
		int sTop = sidebarTop();
		int sBot = sidebarBottom();
		hoverRowY = -1; hoverCatY = -1;
		g.enableScissor(ox, sTop, ox + SIDEBAR_W, sBot);
		int sy = sTop - sidebarScroll;
		sidebarContentHeight = 0;
		for (var node : nodes) {
			String arrow = node.expanded ? "v" : ">";
			boolean catHover = mouseX >= ox && mouseX <= ox + SIDEBAR_W && mouseY >= sy && mouseY < sy + CAT_H;
			if (catHover) hoverCatY = sy;
			g.drawString(font, arrow, ox + 3, sy, 0xFFFFDD55, false);
			g.drawString(font, node.cat.title, ox + 14, sy, catHover ? 0xFFFFFF : 0xFFFFDD55, false);
			sy += CAT_H;
			sidebarContentHeight += CAT_H;
			if (node.expanded) {
				for (var entry : node.cat.entries) {
					if (!isEntryVisible(entry)) continue;
					boolean selected = entry.id.equals(currentEntryId);
					boolean rowHover = mouseX >= ox && mouseX <= ox + SIDEBAR_W && mouseY >= sy && mouseY < sy + ROW_H;
					if (rowHover) hoverRowY = sy;
					// 选中/悬停背景
					if (selected) {
						g.fill(ox + 2, sy - 1, ox + SIDEBAR_W - 2, sy + ROW_H, 0x66FFAA00);
					} else if (rowHover) {
						g.fill(ox + 2, sy - 1, ox + SIDEBAR_W - 2, sy + ROW_H, 0x33FFFFFF);
					}
					// 图标
					ItemStack iconStack = resolveIcon(entry.icon);
					if (!iconStack.isEmpty()) g.renderItem(iconStack, ox + 4, sy - 1);
					// 文字
					int textColor = selected ? 0xFFFFFF : (rowHover ? 0xEEEEEE : 0xCCCCCC);
					String title = entry.title;
					int maxTextW = SIDEBAR_W - ICON_TEXT_GAP - 4;
					if (font.width(title) > maxTextW)
						title = font.plainSubstrByWidth(title, maxTextW - 6) + "…";
					g.drawString(font, title, ox + ICON_TEXT_GAP, sy, textColor, false);
					sy += ROW_H;
					sidebarContentHeight += ROW_H;
				}
				sidebarContentHeight += 0;
				sy += 0;
			}
		}
		g.disableScissor();

		// 侧边滚动条
		int sViewH = sBot - sTop;
		if (sidebarContentHeight > sViewH) {
			int barH = Math.max(14, sViewH * sViewH / sidebarContentHeight);
			int barY = sTop + (sViewH - barH) * sidebarScroll / Math.max(1, sidebarContentHeight - sViewH);
			g.fill(ox + SIDEBAR_W - 4, barY, ox + SIDEBAR_W - 1, barY + barH, 0xBBCCCCCC);
		}

		if (nodes.isEmpty()) {
			String err = PlanetariumWiki.error();
			g.drawString(font, "加载失败: " + (err != null ? err : "无内容"), ox + 6, oy + 30, 0xFFFF6666, false);
		}

		renderContent(g, mouseX, mouseY, pt);
	}

	private void renderContent(GuiGraphics g, int mouseX, int mouseY, float pt) {
		if (currentEntryId == null) return;
		PlanetariumEntry entry = PlanetariumWiki.byId(currentEntryId);
		if (entry == null) return;

		int cx = ox + SIDEBAR_W + 6;
		int cw = W - SIDEBAR_W - 12;
		var font = this.font;

		// ---- 固定标题区（不随滚动） ----
		int titleY = contentTop();
		ItemStack iconStack = resolveIcon(entry.icon);
		if (!iconStack.isEmpty()) g.renderItem(iconStack, cx, titleY - 2);
		g.drawString(font, entry.title, cx + 20, titleY + 2, 0xFFFFEE88, false);
		// 标题下分隔线
		g.fill(cx, titleY + 14, cx + cw, titleY + 15, 0x55555577);

		int bodyY = titleBottom();
		int cBot = contentBottom();

		// ---- 滚动内容区（裁剪在标题下方、底部按钮上方） ----
		g.enableScissor(cx - 2, bodyY, ox + W - 4, cBot);

		if (entry.isForm()) {
			renderFormContent(g, entry, cx, bodyY, cw);
		} else if (entry.isItem()) {
			renderItemContent(g, entry, cx, bodyY, cw);
		} else {
			List<MarkdownLine> lines = parseMarkdown(entry.text, cw - 4);
			contentMaxScroll = Math.max(0, lines.size() * 11 - (cBot - bodyY));
			int by = bodyY - contentScroll;
			for (var line : lines) {
				if (by + 10 < bodyY - 2) { by += line.height; continue; }
				if (by > cBot + 2) break;
				int lx = cx + (line.isIndent ? 12 : 0);
				if (line.isRightAlign) {
					int tw = font.width(line.text);
					lx = cx + cw - tw;
				}
				line.render(g, lx, by, font);
				by += line.height;
			}
		}

		g.disableScissor();

		// 右侧滚动条
		if (contentMaxScroll > 0) {
			int viewH = cBot - bodyY;
			int barH = Math.max(12, viewH * viewH / (contentMaxScroll + viewH));
			int barY = bodyY + (viewH - barH) * contentScroll / Math.max(1, contentMaxScroll);
			g.fill(ox + W - 4, barY, ox + W - 2, barY + barH, 0x88AAAAAA);
		}

		// 形态页 tab
		if (entry.isForm()) {
			int tabY = oy + H - 20;
			int tabX = cx;
			boolean bio = biographyTab;
			g.fill(tabX, tabY, tabX + 50, tabY + 12, bio ? 0xFF222233 : 0xFF4466AA);
			g.drawString(font, "简介", tabX + 6, tabY + 2, bio ? 0x888888 : 0xFFFFFF, false);
			g.fill(tabX + 54, tabY, tabX + 104, tabY + 12, bio ? 0xFF4466AA : 0xFF222233);
			g.drawString(font, "传记", tabX + 60, tabY + 2, bio ? 0xFFFFFF : 0x888888, false);
		}
	}

	private void renderFormContent(GuiGraphics g, PlanetariumEntry entry, int cx, int cy, int cw) {
		var font = this.font;

		// 右上：2x2 祭物（数据来自 entry.offering）
		int gridX = cx + cw - 44;
		int gridY = cy;
		g.drawString(font, "祭物", gridX, gridY - 10, 0x88DDFF, false);
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {
				int px = gridX + c * 18;
				int py = gridY + r * 18;
				g.fill(px, py, px + 16, py + 16, 0xFF111122);
				ItemStack stack = resolveIcon(r < entry.offering.length && c < entry.offering[r].length
						? entry.offering[r][c] : "");
				if (!stack.isEmpty()) g.renderItem(stack, px, py);
			}
		}

		// 左列：属性面板（带不透明背景，滚动文字不会穿透）
		int ax = cx, ay = cy;
		int attrH = 12 + entry.attributes.size() * 10 + 4;
		// 属性背景面板
		g.fill(ax - 2, ay - 2, ax + cw - 60, ay + attrH, 0xCC222238);
		g.drawString(font, "属性 / 能力", ax, ay, 0x88DDFF, false);
		int attrLineY = ay + 12;
		for (String attr : entry.attributes) {
			g.drawString(font, "• " + attr, ax + 4, attrLineY, 0xDDDDDD, false);
			attrLineY += 10;
		}

		// 正文（在属性面板下方独立裁剪滚动）
		int textTop = ay + attrH + 6;
		int textBottom = contentBottom();
		String body = biographyTab ? entry.biography : entry.text;
		List<MarkdownLine> lines = parseMarkdown(body, cw - 4);
		contentMaxScroll = Math.max(0, lines.size() * 11 - (textBottom - textTop));
		int by = textTop - contentScroll;
		// 正文区域单独裁剪（在属性面板下方）
		g.enableScissor(cx - 2, textTop, ox + W - 4, textBottom);
		for (var line : lines) {
			if (by + 10 < textTop - 2) { by += line.height; continue; }
			if (by > textBottom + 2) break;
			int lx = cx + (line.isIndent ? 12 : 0);
			line.render(g, lx, by, font);
			by += line.height;
		}
		g.disableScissor();
	}

	private int bodyY_safe() { return titleBottom() - 2; }

	private void renderItemContent(GuiGraphics g, PlanetariumEntry entry, int cx, int cy, int cw) {
		var font = this.font;

		if (!entry.craftingPattern.isEmpty()) {
			String[] rows = entry.craftingPattern.split("\n");
			int gridX = cx + cw - 60;
			int gridY = cy;
			g.drawString(font, "合成", gridX, gridY - 10, 0x88DDFF, false);
			Map<Character, String> keyMap = parseCraftingKey(entry.craftingKey);
			for (int r = 0; r < rows.length && r < 3; r++) {
				String row = rows[r];
				for (int c = 0; c < row.length() && c < 3; c++) {
					char ch = row.charAt(c);
					int px = gridX + c * 18;
					int py = gridY + r * 18;
					g.fill(px, py, px + 16, py + 16, 0xFF111122);
					if (ch != ' ') {
						String itemId = keyMap.get(ch);
						ItemStack stack = resolveIcon(itemId != null ? itemId : "");
						if (!stack.isEmpty()) g.renderItem(stack, px, py);
					}
				}
			}
		}

		List<MarkdownLine> lines = parseMarkdown(entry.text, cw - 4);
		int textY = cy;
		int textBottom = contentBottom();
		contentMaxScroll = Math.max(0, lines.size() * 11 - (textBottom - textY));
		int by = textY - contentScroll;
		for (var line : lines) {
			if (by + 10 < bodyY_safe()) { by += line.height; continue; }
			if (by > textBottom + 2) break;
			int lx = cx + (line.isIndent ? 12 : 0);
			line.render(g, lx, by, font);
			by += line.height;
		}
	}

	// ---- Markdown 行 ----
	private static class MarkdownLine {
		final Component text;
		final int height;
		final boolean isIndent;
		final boolean isRightAlign;
		final int color;

		MarkdownLine(Component text, int height, boolean isIndent, boolean isRightAlign, int color) {
			this.text = text; this.height = height;
			this.isIndent = isIndent; this.isRightAlign = isRightAlign;
			this.color = color;
		}

		void render(GuiGraphics g, int x, int y, net.minecraft.client.gui.Font font) {
			g.drawString(font, text, x, y, color, false);
		}
	}

	private List<MarkdownLine> parseMarkdown(String md, int maxWidth) {
		List<MarkdownLine> result = new ArrayList<>();
		if (md == null || md.isEmpty()) return result;

		String text = md.replace("$(br)", "\n").replace("$(li)", "\n• ");
		text = text.replaceAll("\\$\\([^)]*\\)", "");

		String[] rawLines = text.split("\n");

		for (String raw : rawLines) {
			String line = raw.trim();
			if (line.isEmpty()) {
				result.add(new MarkdownLine(Component.literal(""), 5, false, false, 0xFFFFFF));
				continue;
			}

			// ### 开头 = 右对齐署名
			boolean isSignature = line.startsWith("### ");
			if (isSignature) line = line.substring(4).trim();

			boolean isList = line.startsWith("• ");
			boolean isHeading = line.startsWith("# ") && !isSignature;
			String content = isList ? line.substring(2) : (isHeading ? line.substring(2) : line);

			// 解析行内 **加粗** → 换成金色
			MutableComponent comp = parseInlineColor(content);

			int color;
			if (isSignature) color = 0xBBAA88; // 署名：灰金色
			else if (isHeading) color = 0xFF88DDFF;
			else if (isList) color = 0xCCCCCC;
			else color = 0xDDDDDD;

			// 段首缩进：非列表、非署名、非标题的段落首行
			boolean indent = !isList && !isHeading && !isSignature;

			List<net.minecraft.util.FormattedCharSequence> wrapped = font.split(comp, maxWidth);
			for (int i = 0; i < wrapped.size(); i++) {
				result.add(new MarkdownLine(
						fcsToComponent(wrapped.get(i)), 11,
						indent && i == 0, // 仅段落第一行缩进
						isSignature,
						color));
			}
		}
		return result;
	}

	/** **text** → 金色（替代加粗） */
	private MutableComponent parseInlineColor(String text) {
		MutableComponent result = Component.empty();
		int i = 0;
		while (i < text.length()) {
			int start = text.indexOf("**", i);
			if (start < 0) { result.append(Component.literal(text.substring(i))); break; }
			if (start > i) result.append(Component.literal(text.substring(i, start)));
			int end = text.indexOf("**", start + 2);
			if (end < 0) { result.append(Component.literal(text.substring(start))); break; }
			String inner = text.substring(start + 2, end);
			result.append(Component.literal(inner).setStyle(Style.EMPTY.withColor(0xFFFFCC66)));
			i = end + 2;
		}
		return result;
	}

	private boolean isEntryVisible(PlanetariumEntry entry) {
		if (entry.advancement == null || entry.advancement.isEmpty()) return true;
		var mc = Minecraft.getInstance();
		if (mc.player == null) return true;
		try {
			var adv = mc.player.connection.getAdvancements()
					.get(ResourceLocation.parse(entry.advancement));
			if (adv == null) return false;
			var prog = ((net.minecraft.client.multiplayer.ClientAdvancements) mc.player.connection
					.getAdvancements()).progress.get(adv);
			return prog != null && prog.isDone();
		} catch (Exception e) { return true; }
	}

	private ItemStack resolveIcon(String id) {
		if (id == null || id.isEmpty()) return ItemStack.EMPTY;
		try {
			// 支持 "murmol:form_soul#<formId>"：返回带形态数据组件的灵魂，图标随形态变化
			if (id.startsWith("murmol:form_soul#"))
				return mcr.murmol.item.FormSoulItem.forForm(id.substring("murmol:form_soul#".length()));
			return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));
		} catch (Exception e) { return ItemStack.EMPTY; }
	}

	private static Component fcsToComponent(net.minecraft.util.FormattedCharSequence fcs) {
		StringBuilder sb = new StringBuilder();
		fcs.accept((index, style, cp) -> { sb.appendCodePoint(cp); return true; });
		return Component.literal(sb.toString());
	}

	private Map<Character, String> parseCraftingKey(String json) {
		Map<Character, String> map = new java.util.HashMap<>();
		try {
			var obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
			for (var e : obj.entrySet()) {
				String k = e.getKey();
				if (!k.isEmpty()) map.put(k.charAt(0), e.getValue().getAsString());
			}
		} catch (Exception ignored) {}
		return map;
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double dx, double dy) {
		int sViewH = sidebarBottom() - sidebarTop();
		if (mx >= ox && mx <= ox + SIDEBAR_W && sidebarContentHeight > sViewH) {
			sidebarScroll = Math.max(0, Math.min(sidebarContentHeight - sViewH, sidebarScroll - (int) dy * 12));
			return true;
		}
		if (mx > ox + SIDEBAR_W && contentMaxScroll > 0) {
			contentScroll = Math.max(0, Math.min(contentMaxScroll, contentScroll - (int) dy * 12));
			return true;
		}
		return super.mouseScrolled(mx, my, dx, dy);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		int cy = sidebarTop() - sidebarScroll;
		for (var node : nodes) {
			if (mx >= ox && mx <= ox + SIDEBAR_W && my >= cy && my < cy + CAT_H) {
				node.expanded = !node.expanded;
				return true;
			}
			cy += CAT_H;
			if (node.expanded) {
				for (var entry : node.cat.entries) {
					if (!isEntryVisible(entry)) continue;
					if (mx >= ox && mx <= ox + SIDEBAR_W && my >= cy && my < cy + ROW_H) {
						currentEntryId = entry.id;
						lastEntryId = currentEntryId;
						biographyTab = false;
						contentScroll = 0;
						return true;
					}
					cy += ROW_H;
				}
				cy += 2;
			}
		}
		if (currentEntryId != null) {
			PlanetariumEntry e = PlanetariumWiki.byId(currentEntryId);
			if (e != null && e.isForm()) {
				int cx = ox + SIDEBAR_W + 6;
				int tabY = oy + H - 20;
				if (my >= tabY && my < tabY + 12) {
					if (mx >= cx && mx < cx + 50) { biographyTab = false; contentScroll = 0; return true; }
					if (mx >= cx + 54 && mx < cx + 104) { biographyTab = true; contentScroll = 0; return true; }
				}
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean isPauseScreen() { return false; }
}
