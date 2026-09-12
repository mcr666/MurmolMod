package net.mcr.murmol.client.gui;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

import net.mcr.murmol.world.inventory.AstralBookMenu;
import net.mcr.murmol.procedures.RenderplyProcedure;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.murmol.network.AstralBookButtonMessage;
import net.mcr.murmol.init.MurmolModScreens;

import com.mojang.blaze3d.systems.RenderSystem;

public class AstralBookScreen extends AbstractContainerScreen<AstralBookMenu> implements MurmolModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_0;
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("murmol:textures/screens/astralbg.png");

	public AstralBookScreen(AstralBookMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 500;
		this.imageHeight = 220;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (RenderplyProcedure.execute(entity) instanceof LivingEntity livingEntity) {
			MurmolModScreens.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 109, this.topPos + 157, 30, 0f + (float) Math.atan((this.leftPos + 109 - mouseX) / 40.0), (float) Math.atan((this.topPos + 108 - mouseY) / 40.0),
					livingEntity);
		}
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(IMAGE_0, this.leftPos + 1, this.topPos + -1, 0, 0, 500, 220, 500, 220);
		net.minecraft.world.item.ItemStack sisPreviewItem = FeralFormManager.getForm(entity).getSoulItem();
		if (!sisPreviewItem.isEmpty()) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(this.leftPos + 234, this.topPos + 89, 0);
			guiGraphics.pose().scale(2.0f, 2.0f, 1.0f);
			guiGraphics.renderItem(sisPreviewItem, 0, 0);
			guiGraphics.renderItemDecorations(this.font, sisPreviewItem, 0, 0);
			guiGraphics.pose().popPose();
		}
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_0 = Button.builder(Component.translatable("gui.murmol.astral_book_sis_0_8.button_0"), e -> {
			int x = AstralBookScreen.this.x;
			int y = AstralBookScreen.this.y;
			if (true) {
				PacketDistributor.sendToServer(new AstralBookButtonMessage(0, x, y, z));
				AstralBookButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 235, this.topPos + 190, 30, 20).build();
		this.addRenderableWidget(button_0);
	}
}