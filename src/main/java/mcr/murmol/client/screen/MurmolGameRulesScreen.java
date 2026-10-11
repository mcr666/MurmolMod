package mcr.murmol.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import mcr.murmol.network.MurmolGameRulePayloads;
import mcr.murmol.network.MurmolGameRulePayloads.Kind;
import mcr.murmol.network.MurmolGameRulePayloads.RuleDef;

/**
 * 两仪「游戏规则」设置界面：列出本 mod 注册的全部游戏规则。
 * 布尔规则：点击行切换开/关；整数规则：行内滑块，点击定位或滚轮 ±1。
 * 所有玩家可查看；2 级权限（与原版 /gamerule 一致）可编辑，改动经
 * {@link MurmolGameRulePayloads.SetMessage} 提交服务端，服务端广播后刷新本地显示。
 */
public class MurmolGameRulesScreen extends Screen {
	private static final int ROW_HEIGHT = 30;
	private static final int ROW_WIDTH = 280;
	private static final int TRACK_WIDTH = 110;

	private final Screen parent;
	private boolean canEdit = false;

	public MurmolGameRulesScreen(Screen parent) {
		super(Component.translatable("gui.murmol.game_rules.title"));
		this.parent = parent;
	}

	/** 打开界面并向服务端请求当前规则值 */
	public static void open(Screen parent) {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new MurmolGameRulesScreen(parent));
		PacketDistributor.sendToServer(new MurmolGameRulePayloads.RequestMessage());
	}

	@Override
	protected void init() {
		this.canEdit = this.minecraft != null && this.minecraft.player != null
				&& this.minecraft.player.hasPermissions(2);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		var font = this.font;

		graphics.drawCenteredString(font, this.title, this.width / 2, 16, 0xFFFFFF);

		int rows = MurmolGameRulePayloads.RULES.size();
		int left = this.width / 2 - ROW_WIDTH / 2;
		int top = 34;
		int listHeight = rows * ROW_HEIGHT;

		graphics.fill(left - 4, top - 4, left + ROW_WIDTH + 4, top + listHeight + 4, 0x88000000);

		for (int i = 0; i < rows; i++) {
			RuleDef def = MurmolGameRulePayloads.RULES.get(i);
			int y = top + i * ROW_HEIGHT;
			Object value = MurmolGameRulePayloads.CLIENT_RULES.get(def.id());
			boolean hovered = this.canEdit && value != null
					&& mouseX >= left && mouseX < left + ROW_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2;

			graphics.fill(left, y, left + ROW_WIDTH, y + ROW_HEIGHT - 2, hovered ? 0x509090C0 : 0x30A0A0A0);

			// 规则名与说明
			graphics.drawString(font, Component.translatable("gamerule." + def.id()), left + 6, y + 3,
					0xFFFFFF, false);
			graphics.drawString(font, Component.translatable("gamerule." + def.id() + ".description"),
					left + 6, y + 14, 0x999999, false);

			int right = left + ROW_WIDTH - 6;
			if (def.kind() == Kind.INT) {
				int intValue = value instanceof Number n ? n.intValue() : def.min();
				int trackX = right - TRACK_WIDTH;
				int trackY = y + 7;
				// 轨道
				graphics.fill(trackX, trackY + 4, trackX + TRACK_WIDTH, trackY + 8, 0xFF202020);
				// 已选填充
				float ratio = (float) (intValue - def.min()) / Math.max(def.max() - def.min(), 1);
				int fillWidth = Math.round((TRACK_WIDTH - 2) * ratio);
				graphics.fill(trackX + 1, trackY + 5, trackX + 1 + fillWidth, trackY + 7, 0xFF7070C0);
				// 手柄
				int knobX = trackX + Math.round((TRACK_WIDTH - 8) * ratio);
				graphics.fill(knobX, trackY + 1, knobX + 8, trackY + 11, value == null ? 0xFF808080 : 0xFFC0C0FF);
				// 数值
				String num = value == null ? "…" : Integer.toString(intValue);
				graphics.drawString(font, num, trackX - 2 - font.width(num), trackY + 3, 0xFFFFFF, false);
			} else {
				String stateText;
				int color;
				if (value == null) {
					stateText = "…";
					color = 0xAAAAAA;
				} else if ((Boolean) value) {
					stateText = Component.translatable("gui.murmol.two_principles.state_enabled").getString();
					color = 0x55FF55;
				} else {
					stateText = Component.translatable("gui.murmol.two_principles.state_disabled").getString();
					color = 0xFF5555;
				}
				graphics.drawString(font, stateText, right - font.width(stateText), y + 8, color, false);
			}
		}

		// 「返回」按钮（与列表行同款灰色半透明样式）
		int bw = 60, bh = 20;
		int bx = this.width / 2 - bw / 2;
		int by = top + listHeight + 10;
		boolean hov = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
		graphics.fill(bx, by, bx + bw, by + bh, hov ? 0x509090C0 : 0x30A0A0A0);
		String back = Component.translatable("gui.murmol.game_rules.btn_back").getString();
		graphics.drawString(font, back, bx + (bw - font.width(back)) / 2, by + 6, 0xFFFFFF, false);

		Component hint = this.canEdit
				? Component.translatable("gui.murmol.game_rules.hint_edit")
				: Component.translatable("gui.murmol.game_rules.readonly");
		graphics.drawCenteredString(font, hint, this.width / 2, by + bh + 10, 0xAAAAAA);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0)
			return super.mouseClicked(mouseX, mouseY, button);

		int rows = MurmolGameRulePayloads.RULES.size();
		int left = this.width / 2 - ROW_WIDTH / 2;
		int top = 34;

		if (this.canEdit && mouseX >= left && mouseX < left + ROW_WIDTH && mouseY >= top) {
			int index = (int) ((mouseY - top) / ROW_HEIGHT);
			if (index >= 0 && index < rows) {
				RuleDef def = MurmolGameRulePayloads.RULES.get(index);
				Object value = MurmolGameRulePayloads.CLIENT_RULES.get(def.id());
				if (value != null) {
					if (def.kind() == Kind.INT) {
						// 点击滑块轨道按比例定位（点击处即使在行内非轨道区也按水平比例，手感更宽容）
						double ratio = Math.max(0, Math.min(1, (mouseX - left) / (double) ROW_WIDTH));
						int next = (int) Math.round(def.min() + ratio * (def.max() - def.min()));
						PacketDistributor.sendToServer(new MurmolGameRulePayloads.SetMessage(def.id(), next));
					} else {
						PacketDistributor.sendToServer(new MurmolGameRulePayloads.SetMessage(def.id(),
								(Boolean) value ? 0 : 1));
					}
					return true;
				}
			}
		}

		// 返回按钮
		int bw = 60, bh = 20;
		int bx = this.width / 2 - bw / 2;
		int by = top + rows * ROW_HEIGHT + 10;
		if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh) {
			Minecraft.getInstance().setScreen(this.parent);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.canEdit) {
			int rows = MurmolGameRulePayloads.RULES.size();
			int left = this.width / 2 - ROW_WIDTH / 2;
			int top = 34;
			if (mouseX >= left && mouseX < left + ROW_WIDTH && mouseY >= top && mouseY < top + rows * ROW_HEIGHT) {
				int index = (int) ((mouseY - top) / ROW_HEIGHT);
				if (index >= 0 && index < rows) {
					RuleDef def = MurmolGameRulePayloads.RULES.get(index);
					if (def.kind() == Kind.INT) {
						Object value = MurmolGameRulePayloads.CLIENT_RULES.get(def.id());
						if (value instanceof Number n) {
							int next = Math.max(def.min(), Math.min(def.max(), n.intValue() + (scrollY > 0 ? 1 : -1)));
							PacketDistributor.sendToServer(new MurmolGameRulePayloads.SetMessage(def.id(), next));
							return true;
						}
					}
				}
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
