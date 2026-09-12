package net.mcr.astralcruse.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class FeralPlayerRendererMixin {
	@Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("HEAD"), cancellable = true)
	private void astralCruse$replaceVanillaPlayerRotations(AbstractClientPlayer player, PoseStack poseStack, float bob, float bodyYaw, float partialTick, float scale, CallbackInfo ci) {
		if (FeralBedrockPlayerAnimator.shouldAnimate(player)) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
			FeralBedrockPlayerAnimator.applyBodyRenderTransform(player, poseStack, player.tickCount + partialTick);
			ci.cancel();
		}
	}

	@Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("RETURN"))
	private void astralCruse$applyFeralBodyTransform(AbstractClientPlayer player, PoseStack poseStack, float bob, float bodyYaw, float partialTick, float scale, CallbackInfo ci) {
		FeralBedrockPlayerAnimator.applyBodyRenderTransform(player, poseStack, player.tickCount + partialTick);
	}
}
