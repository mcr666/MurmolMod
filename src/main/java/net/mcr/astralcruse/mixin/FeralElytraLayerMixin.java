package net.mcr.astralcruse.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public abstract class FeralElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private FeralElytraLayerMixin(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0, shift = At.Shift.AFTER))
	private void astralCruse$applyFeralTorsoToElytra(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		if (FeralBedrockPlayerAnimator.shouldAnimate(entity) && this.getParentModel() instanceof PlayerModel<?> playerModel) {
			playerModel.body.translateAndRotate(poseStack);
			poseStack.translate(0.0F, 2.0F * 0.0625F, 2.0F * 0.0625F);
		}
	}
}
