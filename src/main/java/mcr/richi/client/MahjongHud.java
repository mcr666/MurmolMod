package mcr.richi.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import mcr.murmol.MurmolMod;
import mcr.richi.network.MahjongCountdownPayload;
import mcr.richi.network.MahjongGamePayloads;

/**
 * 麻将 HUD：
 * <ul>
 * <li>回合倒计时屏幕叠加层（热点栏上方居中文字）：20s 长考 = 黄色，5s 短考 = 白色；
 * 服务端停发（超时摸切/流局/换家）2.5s 后自动隐藏。</li>
 * <li>对局响应按钮（倒计时上方横排）：服务端推送可执行选项（荣和/自摸/碰/杠/吃/立直），
 * 点击发送 Action 包；带牌 code 的选项行右端直绘牌面贴图（TileIcons）。仅数字键操作，不与鼠标交互。</li>
 * </ul>
 * 订阅 mod 总线事件（RegisterGuiLayersEvent / InputEvent），时效判断用真实时间。
 */
@EventBusSubscriber(modid = MurmolMod.MODID, value = Dist.CLIENT)
public class MahjongHud {
	/** 最近一次收包时的真实时间（毫秒） */
	private static long lastPacketMillis;
	/** 上次见到的收包计数 */
	private static int lastSeenCounter;
	/** 停发后自动隐藏的时效（毫秒） */
	private static final long STALE_MILLIS = 2500;
	/** 长考文字颜色（黄） */
	private static final int COLOR_LONG = 0xFFFF55;
	/** 短考文字颜色（白） */
	private static final int COLOR_SHORT = 0xFFFFFF;

	/** 响应选项行（label 含键序号 + 动作 + 数据；tiles 为要展示的牌面（1~3 张），行右端直绘） */
	private record ActButton(String label, String action, String data, int[] tiles, int x, int y, int w, int h) {
	}

	private static final List<ActButton> buttons = new ArrayList<>();
	/** 叠加层面板几何（选项行区域，命中检测用） */
	private static int panelX, panelY, panelW, panelH;

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_countdown"),
				(LayeredDraw.Layer) MahjongHud::render);
	}

	private static void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
		if (MahjongCountdownPayload.receiveCounter != lastSeenCounter) {
			lastSeenCounter = MahjongCountdownPayload.receiveCounter;
			lastPacketMillis = Util.getMillis();
		}
		int shortSec = MahjongCountdownPayload.shortSec;
		int longSec = MahjongCountdownPayload.longSec;
		if ((shortSec > 0 || longSec > 0) && Util.getMillis() - lastPacketMillis <= STALE_MILLIS) {
			var font = Minecraft.getInstance().font;
			int cx = guiGraphics.guiWidth() / 2;
			int y = guiGraphics.guiHeight() - 71;
			if (shortSec > 0 && longSec > 0) {
				// "x+XX"：短考白色 + 长考黄色，两个数字同显不同色
				String s = shortSec + "+";
				String l = String.valueOf(longSec);
				int startX = cx - (font.width(s) + font.width(l)) / 2;
				guiGraphics.drawString(font, s, startX, y, COLOR_SHORT, true);
				guiGraphics.drawString(font, l, startX + font.width(s), y, COLOR_LONG, true);
			} else if (shortSec > 0) {
				// 仅短考（长考银行已空）：白色
				guiGraphics.drawCenteredString(font, String.valueOf(shortSec), cx, y, COLOR_SHORT);
			} else {
				// 短考耗尽：只显示黄色长考
				guiGraphics.drawCenteredString(font, String.valueOf(longSec), cx, y, COLOR_LONG);
			}
		}
		renderButtons(guiGraphics);
	}

	// ==================================================================
	// 响应按钮
	// ==================================================================

	private static String actionLabel(String action, String data) {
		return switch (action) {
			case "ron" -> Component.translatable("gui.richi.act.ron").getString();
			case "tsumo" -> Component.translatable("gui.richi.act.tsumo").getString();
			case "pon" -> Component.translatable("gui.richi.act.pon").getString();
			case "minkan" -> Component.translatable("gui.richi.act.minkan").getString();
			case "riichi" -> Component.translatable("gui.richi.act.riichi").getString();
			case "kyuushu" -> Component.translatable("gui.richi.act.kyuushu").getString();
			case "chi" -> Component.translatable("gui.richi.act.chi",
					data.isEmpty() ? "" : (Integer.parseInt(data) + 1)).getString();
			case "ankan" -> Component.translatable("gui.richi.act.ankan").getString();
			case "kakan" -> Component.translatable("gui.richi.act.kakan").getString();
			default -> action;
		};
	}

	/** 该选项展示的牌面（data 中的牌 code，1~3 张；chi 的 data = "idx:t1,t2,t3"） */
	private static int[] optionTiles(String action, String data) {
		try {
			return switch (action) {
				case "ron", "tsumo", "pon", "minkan", "ankan", "kakan" -> new int[] { Integer.parseInt(data.trim()) };
				case "chi" -> {
					if (!data.contains(":")) {
						yield new int[0];
					}
					String[] parts = data.substring(data.indexOf(':') + 1).split(",");
					int[] tiles = new int[parts.length];
					for (int i = 0; i < parts.length; i++)
						tiles[i] = Integer.parseInt(parts[i].trim());
					yield tiles;
				}
				default -> new int[0];
			};
		} catch (NumberFormatException e) {
			return new int[0];
		}
	}

	private static void renderButtons(GuiGraphics guiGraphics) {
		String options = MahjongGamePayloads.clientOptions;
		buttons.clear();
		if (options == null || options.isEmpty())
			return;
		var font = Minecraft.getInstance().font;
		int lineH = 18, padX = 8, padY = 5; // 行高容纳 10×13 牌面
		final int tileW = 10, tileGap = 1;
		String hint = Component.translatable("gui.richi.act.hint_keys").getString();
		record Row(String label, String action, String data, int[] tiles) {
		}
		List<Row> parts = new ArrayList<>();
		int textW = font.width(hint);
		for (String opt : options.split(";")) {
			if (opt.isEmpty())
				continue;
			String action = opt;
			String data = "";
			int colon = opt.indexOf(':');
			if (colon >= 0) {
				action = opt.substring(0, colon);
				data = opt.substring(colon + 1);
			}
			int[] tiles = optionTiles(action, data);
			String label = actionLabel(action, action.equals("chi") && data.contains(":")
					? data.substring(0, data.indexOf(':')) : data);
			parts.add(new Row(label, action, data, tiles));
			int iconW = tiles.length * (tileW + tileGap);
			textW = Math.max(textW, font.width((parts.size()) + "·" + label) + iconW + 14);
		}
		if (parts.isEmpty())
			return;
		// 面板底边在倒计时文字上方（倒计时 y = guiHeight-71）；宽度不超屏幕
		panelW = Math.min(textW + padX * 2, guiGraphics.guiWidth() - 8);
		panelH = padY * 2 + parts.size() * lineH + lineH;
		panelX = (guiGraphics.guiWidth() - panelW) / 2;
		panelY = guiGraphics.guiHeight() - 73 - panelH;
		guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x99000000);
		int bc = 0xFFB8860B; // 金色边框
		guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + 1, bc);
		guiGraphics.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, bc);
		guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelH, bc);
		guiGraphics.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, bc);
		int y = panelY + padY;
		for (int i = 0; i < parts.size(); i++) {
			Row p = parts.get(i);
			String label = (i + 1) + "·" + p.label();
			buttons.add(new ActButton(label, p.action(), p.data(), p.tiles(), panelX, y, panelW, lineH));
			guiGraphics.drawString(font, label, panelX + padX, y + 6, 0xFFFF55, true);
			// 行右端直绘牌面组合（等比 12×16），从右往左排
			int[] tiles = p.tiles();
			int tx = panelX + panelW - padX - tileW;
			for (int t = tiles.length - 1; t >= 0; t--) {
				TileIcons.draw(guiGraphics, tiles[t], tx, y + 2, tileW);
				tx -= tileW + tileGap;
			}
			y += lineH;
		}
		guiGraphics.drawString(font, hint, panelX + padX, y + 3, 0xAAAAAA, false);
	}

	/** 发送对局动作包 */
	private static void sendAction(String action, String data) {
		net.neoforged.neoforge.network.PacketDistributor.sendToServer(
				new MahjongGamePayloads.ActionMessage(MahjongGamePayloads.clientOptionsOrigin, action, data));
	}

	/** 选项面板显示时（无 Screen），吞掉数字键 1-9 的点击计数，防止 vanilla 同时切换快捷栏 */
	@SubscribeEvent
	public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
		if (Minecraft.getInstance().screen != null || buttons.isEmpty())
			return;
		drainHotbarClicks();
	}

	/** 吞掉快捷栏各键的点击计数（InputEvent.Key 与 ClientTick.Pre 双保险：键按下回调先于 tick 消费点数） */
	private static void drainHotbarClicks() {
		for (var key : Minecraft.getInstance().options.keyHotbarSlots) {
			while (key.consumeClick()) {
				// drain：阻止本帧快捷栏切换
			}
		}
	}

	/** 键盘快捷键：0（含小键盘）= 跳过，1-9 = 第 n 个选项 */
	@SubscribeEvent
	public static void onKey(InputEvent.Key event) {
		if (event.getAction() != org.lwjgl.glfw.GLFW.GLFW_PRESS
				|| Minecraft.getInstance().screen != null || buttons.isEmpty())
			return;
		int key = event.getKey();
		int idx;
		if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_0 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_9)
			idx = key - org.lwjgl.glfw.GLFW.GLFW_KEY_0;
		else if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_KP_9)
			idx = key - org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0;
		else
			return;
		drainHotbarClicks(); // 数字键已被选项占用：立即吞掉点击计数，屏蔽快捷栏切换
		if (idx == 0) {
			sendAction("skip", "");
		} else if (idx <= buttons.size()) {
			ActButton b = buttons.get(idx - 1);
			sendAction(b.action(), b.data());
		}
	}

}
