package mcr.murmol.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralForms;
import mcr.murmol.network.SoulbindPayloads;

/**
 * 塑魂之约确认界面：展示将绑定的形态，提供「确认绑定」/「取消」两个按钮。
 * 确认后向服务端发送 {@link SoulbindPayloads.ConfirmMessage}，由服务端完成绑定与消耗物品。
 */
public class SoulbindConfirmScreen extends Screen {
	private static final int BTN_W = 90;
	private static final int BTN_H = 20;

	private final String formId;

	public SoulbindConfirmScreen(String formId) {
		super(Component.translatable("screen.murmol.soulbind.title"));
		this.formId = formId;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int cy = this.height / 2;
		this.addRenderableWidget(Button.builder(Component.translatable("screen.murmol.soulbind.confirm"), b -> {
			SoulbindPayloads.sendConfirm(formId);
			this.onClose();
		}).bounds(cx - BTN_W - 5, cy + 30, BTN_W, BTN_H).build());
		this.addRenderableWidget(Button.builder(Component.translatable("screen.murmol.soulbind.cancel"), b -> this.onClose())
				.bounds(cx + 5, cy + 30, BTN_W, BTN_H).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.fill(this.width / 2 - 130, this.height / 2 - 60, this.width / 2 + 130, this.height / 2 + 66, 0xC0101010);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 48, 0xFFFFFF);
		FeralForm form = FeralForms.byId(formId);
		graphics.drawCenteredString(this.font,
				Component.translatable("screen.murmol.soulbind.form_text",
						form != null ? form.getDisplayName() : Component.literal(formId)),
				this.width / 2, this.height / 2 - 26, 0xFFFF55);
		graphics.drawCenteredString(this.font, Component.translatable("screen.murmol.soulbind.warn"),
				this.width / 2, this.height / 2 - 6, 0xCCCCCC);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
