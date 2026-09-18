package net.mcr.murmol.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.ItemStackHandler;

import net.mcr.murmol.block.entity.SpiritTableBlockEntity;

public class SpiritTableRenderer implements BlockEntityRenderer<SpiritTableBlockEntity> {
	public SpiritTableRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SpiritTableBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
		Level level = blockEntity.getLevel();
		if (level == null)
			return;
		ItemStackHandler inventory = blockEntity.getInventory();
		int lightColor = LevelRenderer.getLightColor(level, blockEntity.getBlockPos());
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			poseStack.pushPose();
			// 2x2 网格平放于顶面（顶面 y=0.75），中心对称偏移 ±0.25
			double offsetX = (i % 2 == 0) ? -0.25 : 0.25;
			double offsetZ = (i < 2) ? -0.25 : 0.25;
			poseStack.translate(0.5 + offsetX, 0.83, 0.5 + offsetZ);
			poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			poseStack.scale(0.4f, 0.4f, 0.4f);
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, lightColor, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 0);
			poseStack.popPose();
		}
	}
}
