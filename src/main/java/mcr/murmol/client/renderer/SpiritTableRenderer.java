package mcr.murmol.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.ItemStackHandler;

import mcr.murmol.block.entity.SpiritTableBlockEntity;

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
			// 同原版营火：四件物品分占四个象限（两个水平分量同为 0.21875=3.5 像素，
			// 比营火原版的 0.3125=5 像素整体内收 1.5 像素），绕中心对称。
			// 台面位于 y=0.75，加 0.0117 间隙避免 Z-fighting（营火同款 0.44921875 思路）。
			poseStack.translate(0.5, 0.7617, 0.5);
			Direction direction = Direction.from2DDataValue(i);
			poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			poseStack.translate(-0.21875, -0.21875, 0.0);
			poseStack.scale(0.375F, 0.375F, 0.375F);
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, lightColor, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, i);
			poseStack.popPose();
		}
	}
}
