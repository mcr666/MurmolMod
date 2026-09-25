package net.mcr.astralcruse.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class FeralItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
	@Shadow @Final private ItemInHandRenderer itemInHandRenderer;

	private FeralItemInHandLayerMixin(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void astralCruse$renderRightItemInMouth(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		if (!FeralBedrockPlayerAnimator.shouldRenderItemInMouth(entity, arm, itemStack)) {
			return;
		}
		poseStack.pushPose();
		// 直接绑定到兽形模型的头部骨骼，跟随兽形动画
		PlayerModel<?> feralModel = FeralFormManager.getForm(entity).getBodyModel();
		ModelPart headPart = feralModel != null ? feralModel.head : ((PlayerModel<?>) this.getParentModel()).head;
		headPart.translateAndRotate(poseStack);
		FeralBedrockPlayerAnimator.applyMouthItemTransform(entity, poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		this.itemInHandRenderer.renderItem(entity, itemStack, displayContext, false, poseStack, bufferSource, packedLight);
		poseStack.popPose();
		ci.cancel();
	}

	/**
	 * 兽形态下取消原版手持渲染，把手持物品直接绑定到兽形模型的对应手臂骨骼。
	 * 手臂骨骼的姿态由 FeralFormRenderer.setupAnim（含 Bedrock 动画）驱动，物品自然跟随。
	 */
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void astralCruse$bindItemToFeralBone(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		// 石像状态：直接隐藏手持物品
		if (net.mcr.murmol.feral.FeralFormManager.isInStatue(entity)) {
			ci.cancel();
			return;
		}
		// 嘴部物品由上面的注入处理
		if (FeralBedrockPlayerAnimator.shouldRenderItemInMouth(entity, arm, itemStack)) {
			return;
		}
		if (itemStack.isEmpty()) {
			return;
		}
		FeralForm form = FeralFormManager.getForm(entity);
		if (!form.isFeral()) {
			return;
		}
		PlayerModel<?> feralModel = form.getBodyModel();
		if (feralModel == null) {
			return;
		}
		boolean leftHanded = arm == HumanoidArm.LEFT;
		ModelPart armPart = leftHanded ? feralModel.leftArm : feralModel.rightArm;
		poseStack.pushPose();
		armPart.translateAndRotate(poseStack);
		// 以下为原版 renderArmWithItem 的物品摆放逻辑
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate((leftHanded ? -1 : 1) / 16.0F, 0.125F, -0.625F);
		this.itemInHandRenderer.renderItem(entity, itemStack, displayContext, leftHanded, poseStack, bufferSource, packedLight);
		poseStack.popPose();
		ci.cancel();
	}

	@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
	private void astralCruse$applyFeralItemAnimation(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		// 兽形态已由 astralCruse$bindItemToFeralBone 接管，不再叠加动画偏移
	}
}
