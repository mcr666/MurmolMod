package mcr.murmol.client;

import java.util.ArrayList;
import java.util.List;

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
import mcr.murmol.network.NpcDialoguePayloads;

/**
 * Murmol NPC 对话叠加层：
 * 右键 NPC 时服务端推送选项，屏幕底部绘制对话面板（底边贴经验条上方，蓝色边框，
 * 宽度为内容两倍），选项独立成框贴屏幕右侧（橙色边框），仅数字键操作：
 * 1-9 选择第 n 个选项，0（离开）/ Esc 关闭（0 同时吞掉快捷栏切换）。
 * 玩家离开 NPC 6 格、NPC 消失或打开任意其他 GUI 时自动关闭。
 */
@EventBusSubscriber(modid = MurmolMod.MODID, value = Dist.CLIENT)
public class NpcDialogueHud {
	/** 选项动作缓存（渲染时按 payload 重建；按键用 options） */
	private static final List<String> options = new ArrayList<>();
	/** 选项文字颜色（金） */
	private static final int COLOR_OPTION = 0xFFFF55;

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "npc_dialogue"),
				(LayeredDraw.Layer) NpcDialogueHud::render);
	}

	private static void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
		options.clear();
		String raw = NpcDialoguePayloads.clientOptions;
		if (raw == null || raw.isEmpty())
			return;
		if (!"-".equals(raw)) // "-" = 无数字选项的纯回复面板，options 保持空
			for (String opt : raw.split(";"))
				if (!opt.isEmpty())
					options.add(opt);
		var font = Minecraft.getInstance().font;
		int lineH = 14, padX = 8, padY = 5;
		// ============ 选项框（橙色边框，贴屏幕右侧，与对话面板顶对齐） ============
		String leaveLabel = "0·" + Component.translatable("gui.murmol.npc.act_leave").getString();
		int optTextW = font.width(leaveLabel);
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < options.size(); i++) {
			String label = (i + 1) + "·" + Component.translatable("gui.murmol.npc.act_" + options.get(i)).getString();
			labels.add(label);
			optTextW = Math.max(optTextW, font.width(label));
		}
		int optW = optTextW + padX * 2;
		int optH = padY * 2 + (options.size() + 1) * lineH;
		int panelH = padY * 2 + lineH * 2;
		int panelY = guiGraphics.guiHeight() - 30 - panelH; // 底边贴经验条上方
		int optX = guiGraphics.guiWidth() - optW - 2, optY = panelY;
		guiGraphics.fill(optX, optY, optX + optW, optY + optH, 0x99000000);
		int oc = 0xFFB8860B; // 橙色边框（保留）
		guiGraphics.fill(optX, optY, optX + optW, optY + 1, oc);
		guiGraphics.fill(optX, optY + optH - 1, optX + optW, optY + optH, oc);
		guiGraphics.fill(optX, optY, optX + 1, optY + optH, oc);
		guiGraphics.fill(optX + optW - 1, optY, optX + optW, optY + optH, oc);
		int y = optY + padY;
		for (String label : labels) {
			guiGraphics.drawString(font, label, optX + padX, y + 3, COLOR_OPTION, true);
			y += lineH;
		}
		guiGraphics.drawString(font, leaveLabel, optX + padX, y + 3, 0xAAAAAA, true);
		// ============ 对话面板（蓝色边框，宽度 = 内容两倍，底部居中贴经验条） ============
		String title = Component.translatable(NpcDialoguePayloads.clientTitle).getString();
		String hint = Component.translatable(
				options.isEmpty() ? "gui.murmol.npc.hint_leave" : "gui.murmol.npc.hint_keys").getString();
		int contentW = Math.max(font.width(title), font.width(hint));
		int panelW = Math.min((contentW + padX * 2) * 2, guiGraphics.guiWidth() - optW - 12);
		int panelX = (guiGraphics.guiWidth() - panelW) / 2;
		guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x99000000);
		int bc = 0xFF4466CC; // 蓝色边框
		guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + 1, bc);
		guiGraphics.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, bc);
		guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelH, bc);
		guiGraphics.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, bc);
		guiGraphics.drawCenteredString(font, title, panelX + panelW / 2, panelY + padY + 3, 0xFFFFFF);
		guiGraphics.drawString(font, hint, panelX + padX, panelY + padY + lineH + 3, 0xAAAAAA, false);
	}

	/** 面板显示期间每刻：打开任意 GUI / NPC 消失 / 玩家离开 6 格自动关闭；无 Screen 时吞掉快捷栏点击 */
	@SubscribeEvent
	public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
		if (NpcDialoguePayloads.clientOptions.isEmpty())
			return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) { // 打开任意其他 GUI 后自动关闭
			NpcDialoguePayloads.clientOptions = "";
			return;
		}
		var npc = mc.level == null ? null : mc.level.getEntity(NpcDialoguePayloads.clientNpcId);
		if (npc == null || mc.player == null || mc.player.distanceToSqr(npc) > 36.0) {
			NpcDialoguePayloads.clientOptions = "";
			return;
		}
		if (!"-".equals(NpcDialoguePayloads.clientOptions)) // 纯回复面板无数字选项：不占用快捷栏
			drainHotbarClicks();
	}

	/** 吞掉快捷栏各键的点击计数，防止数字键同时切换快捷栏（同 MahjongHud） */
	private static void drainHotbarClicks() {
		for (var key : Minecraft.getInstance().options.keyHotbarSlots) {
			while (key.consumeClick()) {
				// drain：阻止本帧快捷栏切换
			}
		}
	}

	/** 键盘快捷键：1-9 = 选择第 n 个选项；0（含小键盘）/ Esc = 关闭 */
	@SubscribeEvent
	public static void onKey(InputEvent.Key event) {
		if (event.getAction() != org.lwjgl.glfw.GLFW.GLFW_PRESS
				|| Minecraft.getInstance().screen != null || NpcDialoguePayloads.clientOptions.isEmpty())
			return;
		int key = event.getKey();
		if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
				|| key == org.lwjgl.glfw.GLFW.GLFW_KEY_0 || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0) {
			drainHotbarClicks();
			close();
			return;
		}
		int idx = -1;
		if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_1 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_9)
			idx = key - org.lwjgl.glfw.GLFW.GLFW_KEY_0;
		else if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_KP_1 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_KP_9)
			idx = key - org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0;
		if (idx < 1)
			return;
		if (options.isEmpty())
			return; // 纯回复面板：数字键不选择，保持快捷栏正常使用
		drainHotbarClicks(); // 数字键已被对话占用：立即吞掉点击计数，屏蔽快捷栏切换
		net.neoforged.neoforge.network.PacketDistributor.sendToServer(
				new NpcDialoguePayloads.SelectMessage(NpcDialoguePayloads.clientNpcId, idx));
		close();
	}

	/** 关闭对话叠加层 */
	private static void close() {
		NpcDialoguePayloads.clientOptions = "";
	}
}
