package mcr.astralcruse.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class FeralPlayerRendererMixin {
	/**
	 * body 根骨骼变换的适用形态：全部野性形态，但排除整体替换模型形态（文鳐——
	 * WenyaoPlayerRenderer 重写 render 并自行调用原版 setupRotations，注入会双重施加）。
	 * 人形自定义模型形态与保留原版模型形态（test）均由 applyBodyRenderTransform 按形态分支偏移。
	 */
	private static boolean bodyTransformApplies(AbstractClientPlayer player) {
		var form = mcr.murmol.feral.FeralFormManager.getForm(player);
		return form != null && form.isFeral() && form.getWholeModelLayer() == null;
	}

	@Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("HEAD"), cancellable = true)
	private void astralCruse$replaceVanillaPlayerRotations(AbstractClientPlayer player, PoseStack poseStack, float bob, float bodyYaw, float partialTick, float scale, CallbackInfo ci) {
		if (FeralBedrockPlayerAnimator.shouldAnimate(player, player.tickCount + partialTick) && bodyTransformApplies(player)) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
			FeralBedrockPlayerAnimator.applyBodyRenderTransform(player, poseStack, player.tickCount + partialTick);
			ci.cancel();
		}
	}

	@Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("RETURN"))
	private void astralCruse$applyFeralBodyTransform(AbstractClientPlayer player, PoseStack poseStack, float bob, float bodyYaw, float partialTick, float scale, CallbackInfo ci) {
		if (bodyTransformApplies(player))
			FeralBedrockPlayerAnimator.applyBodyRenderTransform(player, poseStack, player.tickCount + partialTick);
	}
}
