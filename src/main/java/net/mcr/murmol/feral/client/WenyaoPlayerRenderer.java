package net.mcr.murmol.feral.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;

/**
 * 文鳐整体替换渲染器（参考 Changed mod 的渲染器偷换方案）。
 * EntityRenderDispatcher.getRenderer 被 Mixin 劫持后，文鳐形态的玩家由本渲染器绘制：
 * 完全不调用原版人形渲染（super.render），只画鱼模型 + 绑定挂点手持物品。
 * 继承 PlayerRenderer 是为了满足原版对玩家渲染器的类型要求。
 */
public class WenyaoPlayerRenderer extends PlayerRenderer {

	/** 渲染 Y 校正（模型单位，1/16 格）：正=下移、负=上移，实测微调用 */
	private static final float RENDER_Y_OFFSET = 0.0F;

	/** 蹲下/潜行时的额外上移（模型单位，与其他形态的 CROUCH_Y_OFFSET = 2 一致） */
	private static final float CROUCH_LIFT = -2.0F;

	private final boolean slim;

	public WenyaoPlayerRenderer(EntityRendererProvider.Context context, boolean slim) {
		super(context, slim);
		this.slim = slim;
	}

	public boolean isSlim() {
		return slim;
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
		return net.mcr.murmol.feral.client.FeralFormRenderer.resolveTexture(
				FeralFormManager.getForm(entity), entity);
	}

	@Override
	public void render(AbstractClientPlayer entity, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		if (entity.isSpectator())
			return;
		FeralForm form = FeralFormManager.getForm(entity);
		ModelPart root = form.getWholeModelPart();
		if (root == null) {
			super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
			return;
		}

		float limbSwingAmount = entity.walkAnimation.speed(partialTick);
		float ageInTicks = FeralBedrockPlayerAnimator.effectiveAge(entity, entity.tickCount + partialTick);
		float interpolatedBodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
		if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity vehicle) {
			interpolatedBodyYaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
		}

		FeralFormRenderer.applyWenyaoAnimation(form, entity, root, limbSwingAmount, ageInTicks, partialTick);

		if (entity.hasPose(Pose.SLEEPING)) {
			net.minecraft.core.Direction direction = entity.getBedOrientation();
			if (direction != null) {
				float eyeHeightOffset = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((float) (-direction.getStepX()) * eyeHeightOffset, 0.0F,
						(float) (-direction.getStepZ()) * eyeHeightOffset);
			}
		}
		this.setupRotations(entity, poseStack, ageInTicks, interpolatedBodyYaw, partialTick, 0);
		// 取消游泳/爬行时的模型前倾旋转（与其他形态一致）
		FeralFormRenderer.cancelSwimRotation(entity, poseStack, partialTick);
		float bodyScale = entity.getScale();
		poseStack.scale(-0.9375F * bodyScale, -0.9375F * bodyScale, 0.9375F * bodyScale);
		poseStack.translate(0.0D, -1.501D, 0.0D);
		poseStack.translate(0.0D, RENDER_Y_OFFSET / 16.0D, 0.0D);
		// 蹲下/潜行/爬下时额外上移（与其他形态的 shiftModelY(CROUCH_Y_OFFSET) 等效：
		// 模型空间 Y+ 朝下，负值即向上抬升）
		if (entity.isCrouching() || entity.isShiftKeyDown() || entity.hasPose(Pose.SWIMMING)) {
			poseStack.translate(0.0D, CROUCH_LIFT / 16.0D, 0.0D);
		}

		ResourceLocation texture = getTextureLocation(entity);
		poseStack.pushPose();
		root.render(poseStack, buffers.getBuffer(RenderType.armorCutoutNoCull(texture)),
				packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0), -1);

		// 手持物品绑定到 right_item/left_item 挂点骨骼（跟随鱼形动画与视角）
		renderHandItem(entity, poseStack, buffers, packedLight, root, HumanoidArm.RIGHT);
		renderHandItem(entity, poseStack, buffers, packedLight, root, HumanoidArm.LEFT);
		poseStack.popPose();

		// 渲染名牌：与原版一致（poseStack 已还原到实体原点，renderNameTag 自行按 nameTagAttachment 定位）
		if (this.shouldShowName(entity)) {
			this.renderNameTag(entity, entity.getDisplayName(), poseStack, buffers, packedLight, partialTick);
		}
	}

	private void renderHandItem(AbstractClientPlayer entity, PoseStack poseStack, MultiBufferSource buffers,
			int packedLight, ModelPart root, HumanoidArm arm) {
		ItemStack stack = arm == HumanoidArm.LEFT ? entity.getOffhandItem() : entity.getMainHandItem();
		if (stack.isEmpty())
			return;
		ModelPart bone = FeralBedrockPlayerAnimator.findBone(root, arm == HumanoidArm.LEFT ? "left_item" : "right_item");
		if (bone == null)
			return;
		float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
		float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
		float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		float oldYRot = bone.yRot, oldXRot = bone.xRot;
		bone.yRot += headYaw * Mth.DEG_TO_RAD;
		bone.xRot += headPitch * Mth.DEG_TO_RAD;
		poseStack.pushPose();
		bone.translateAndRotate(poseStack);
		// 物品显示朝向与原版手臂持握一致
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack,
				ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, arm == HumanoidArm.LEFT,
				poseStack, buffers, entity.level(), packedLight,
				LivingEntityRenderer.getOverlayCoords(entity, 0), entity.getId() + (arm == HumanoidArm.LEFT ? 1 : 0));
		poseStack.popPose();
		bone.yRot = oldYRot;
		bone.xRot = oldXRot;
	}
}
