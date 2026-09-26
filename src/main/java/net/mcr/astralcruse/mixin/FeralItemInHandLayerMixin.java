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
		boolean leftHanded = arm == HumanoidArm.LEFT;
		PlayerModel<?> feralModel = form.getBodyModel();
		ModelPart bindPart;
		if (feralModel != null) {
			bindPart = leftHanded ? feralModel.leftArm : feralModel.rightArm;
		} else if (form.getWholeModelPart() != null) {
			// 整体替换模型形态（如文鳐）：物品绑定到模型的 right_item/left_item 挂点骨骼，跟随鱼形动画
			bindPart = FeralBedrockPlayerAnimator.findBone(form.getWholeModelPart(), leftHanded ? "left_item" : "right_item");
			if (bindPart == null) {
				return;
			}
			poseStack.pushPose();
			// 挂点骨骼在尾巴子树（不在头部下），手动叠加头部视角旋转使物品跟随视线
			float partialTick = net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
			float headYaw = net.minecraft.util.Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
			float headPitch = net.minecraft.util.Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
			float oldYRot = bindPart.yRot, oldXRot = bindPart.xRot;
			bindPart.yRot += headYaw * net.minecraft.util.Mth.DEG_TO_RAD;
			bindPart.xRot += headPitch * net.minecraft.util.Mth.DEG_TO_RAD;
			bindPart.translateAndRotate(poseStack);
			// 物品显示朝向与原版手臂持握一致
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			this.itemInHandRenderer.renderItem(entity, itemStack, displayContext, leftHanded, poseStack, bufferSource, packedLight);
			// 还原骨骼旋转，避免累积到下一帧
			bindPart.yRot = oldYRot;
			bindPart.xRot = oldXRot;
			poseStack.popPose();
			ci.cancel();
			return;
		} else {
			return;
		}
		poseStack.pushPose();
		bindPart.translateAndRotate(poseStack);
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
