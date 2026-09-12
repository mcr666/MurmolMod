package net.mcr.astralcruse.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class AstralTitleButtonMixin extends AbstractWidget {
	private static final WidgetSprites ASTRAL_CRUSE$TITLE_BUTTONS = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath("astral_cruse", "widget/title_button"),
			ResourceLocation.fromNamespaceAndPath("astral_cruse", "widget/title_button_disabled"),
			ResourceLocation.fromNamespaceAndPath("astral_cruse", "widget/title_button_highlighted")
	);

	protected AstralTitleButtonMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Shadow
	public abstract void renderString(GuiGraphics guiGraphics, Font font, int color);

	@Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
	private void astralCruse$renderCustomTitleButton(
			GuiGraphics guiGraphics,
			int mouseX,
			int mouseY,
			float partialTick,
			CallbackInfo ci
	) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!(minecraft.screen instanceof TitleScreen)) {
			return;
		}

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		guiGraphics.blitSprite(
				ASTRAL_CRUSE$TITLE_BUTTONS.get(this.active, this.isHoveredOrFocused()),
				this.getX(),
				this.getY(),
				this.getWidth(),
				this.getHeight()
		);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int color = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
		this.renderString(guiGraphics, minecraft.font, color);
		ci.cancel();
	}
}
