package mcr.astralcruse.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
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
		// FPM 第一人称（本地玩家）：手持物品始终按嘴叼规则渲染，参考 shape-shifter 的 MouthItemFeature
		if (entity instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer
				&& mcr.murmol.feral.client.FeralFormRenderer.isFirstPersonLocalPlayer(clientPlayer)) {
			if (FeralBedrockPlayerAnimator.shouldRenderItemInMouth(entity, arm, itemStack)) {
				renderItemInMouthFirstPerson(entity, itemStack, arm, poseStack, bufferSource, packedLight);
				ci.cancel();
			}
			// 非嘴叼物品（工具等）继续走 bindItemToFeralBone（手臂骨骼），此处不取消
			return;
		}
		if (!FeralBedrockPlayerAnimator.shouldRenderItemInMouth(entity, arm, itemStack)) {
			return;
		}
		renderItemInMouth(entity, itemStack, displayContext, arm, poseStack, bufferSource, packedLight);
		ci.cancel();
	}

	/** 第三人称嘴叼渲染（原有逻辑：绑定兽形头部骨骼 + 嘴部骨骼动画）。 */
	private void renderItemInMouth(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		poseStack.pushPose();
		// 直接绑定到兽形模型的头部骨骼，跟随兽形动画
		PlayerModel<?> feralModel = FeralFormManager.getForm(entity).getBodyModel();
		ModelPart headPart = feralModel != null ? feralModel.head : ((PlayerModel<?>) this.getParentModel()).head;
		headPart.translateAndRotate(poseStack);
		// 统一嘴部配置开启时，副手物品挪到嘴侧，避免与主手物品重叠
		if (arm == HumanoidArm.LEFT
				&& mcr.murmol.MurmolModConfig.FERAL_ITEMS_ALL_IN_MOUTH.get()) {
			poseStack.translate(0.22F, 0.0F, 0.0F);
		}
		FeralBedrockPlayerAnimator.applyMouthItemTransform(entity, poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		this.itemInHandRenderer.renderItem(entity, itemStack, displayContext, false, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	/**
	 * 第一人称嘴叼渲染：参考 shape-shifter-curse 的 MouthItemFeature.renderItemInMouth——
	 * 定位到头部 pivot、随头部偏航/俯仰旋转、前移 0.35 格、放大 1.25、X90/Z180 摆正，
	 * 以 GROUND 模式渲染（物品平躺在嘴里）。
	 */
	private void renderItemInMouthFirstPerson(LivingEntity entity, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		poseStack.pushPose();
		PlayerModel<?> feralModel = FeralFormManager.getForm(entity).getBodyModel();
		ModelPart headPart = feralModel != null ? feralModel.head : ((PlayerModel<?>) this.getParentModel()).head;
		poseStack.translate(headPart.x / 16.0F, headPart.y / 16.0F, headPart.z / 16.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(headPart.yRot * Mth.RAD_TO_DEG));
		poseStack.mulPose(Axis.XP.rotationDegrees(headPart.xRot * Mth.RAD_TO_DEG));
		// 统一嘴部配置开启时，副手物品挪到嘴侧，避免与主手物品重叠
		if (arm == HumanoidArm.LEFT
				&& mcr.murmol.MurmolModConfig.FERAL_ITEMS_ALL_IN_MOUTH.get()) {
			poseStack.translate(0.22F, 0.0F, 0.0F);
		}
		poseStack.translate(0.06F, 0.085F, -0.35D);
		poseStack.scale(1.25F, 1.25F, 1.25F);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		poseStack.translate(1.0F / 16.0F, -2.0F / 16.0F, 1.0F / 16.0F);
		this.itemInHandRenderer.renderItem(entity, itemStack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight);
		poseStack.popPose();
	}

	/**
	 * 兽形态下取消原版手持渲染，把手持物品直接绑定到兽形模型的对应手臂骨骼。
	 * 手臂骨骼的姿态由 FeralFormRenderer.setupAnim（含 Bedrock 动画）驱动，物品自然跟随。
	 */
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void astralCruse$bindItemToFeralBone(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		// 石像状态：直接隐藏手持物品
		if (mcr.murmol.feral.FeralFormManager.isInStatue(entity)) {
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
			// 整模形态（如文鳐）：第三人称不渲染手持物品（第一人称仍保留，见 FeralFormRenderer.onRenderArm）
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
