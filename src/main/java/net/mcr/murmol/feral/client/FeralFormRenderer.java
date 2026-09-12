package net.mcr.murmol.feral.client;

import org.joml.Vector3f;

import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;

/**
 * 玩家变形渲染调度。
 * 采用原版 MCreator 的 renderHumanoid/renderEntity 渲染逻辑：
 * 自行调用 setupRotations、prepareMobModel、setupAnim（触发 AstralCruse Bedrock 动画 Mixin），
 * 并应用正确的 scale/translate 偏移。
 */
@EventBusSubscriber(Dist.CLIENT)
public class FeralFormRenderer {

	/** 整体偏移（模型坐标 Y+ 朝下），当前值由玩家实测调定为 13 */
	private static final float BODY_Y_OFFSET = 14.0F;

	/** 蹲下/潜行时的额外上移（沿用原 applyBodyRenderTransform 的 CROUCH_RENDER_Y_OFFSET = 2 个模型单位 = 0.125 格） */
	private static final float CROUCH_Y_OFFSET = -2.0F;

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		Player player = event.getEntity();
		FeralForm form = FeralFormManager.getForm(player);
		if (!form.isFeral())
			return;

		PlayerModel bodyModel = form.getBodyModel();
		if (bodyModel == null)
			return;

		PlayerModel originalModel = event.getRenderer().getModel();
		originalModel.head.visible = false;
		originalModel.hat.visible = false;
		originalModel.body.visible = false;
		originalModel.jacket.visible = false;
		originalModel.leftArm.visible = false;
		originalModel.leftLeg.visible = false;
		originalModel.leftPants.visible = false;
		originalModel.leftSleeve.visible = false;
		originalModel.rightArm.visible = false;
		originalModel.rightLeg.visible = false;
		originalModel.rightPants.visible = false;
		originalModel.rightSleeve.visible = false;

		ResourceLocation texture = form.getTexture();
		if (texture != null) {
			renderHumanoid(event, form, bodyModel,
					event.getMultiBufferSource().getBuffer(RenderType.armorCutoutNoCull(texture)));
		}
	}

	@SubscribeEvent
	public static void onRenderArm(RenderArmEvent event) {
		Player player = event.getPlayer();
		FeralForm form = FeralFormManager.getForm(player);
		if (!form.isFeral())
			return;

		PlayerModel bodyModel = form.getBodyModel();
		if (bodyModel == null)
			return;

		ResourceLocation texture = form.getTexture();
		if (texture == null)
			return;

		event.setCanceled(true);

		PlayerModel playerOriginal = (PlayerModel) ((net.minecraft.client.renderer.entity.player.PlayerRenderer) (Object) net.minecraft.client.Minecraft.getInstance()
				.getEntityRenderDispatcher().getRenderer(player)).getModel();
		boolean lefthanded = event.getArm() == HumanoidArm.LEFT;
		ModelPart part = lefthanded ? bodyModel.leftArm : bodyModel.rightArm;
		boolean partVisible = part.visible;
		part.resetPose();
		if (lefthanded)
			playerOriginal.leftArm.resetPose();
		else
			playerOriginal.rightArm.resetPose();
		part.copyFrom(lefthanded ? playerOriginal.leftArm : playerOriginal.rightArm);
		part.visible = true;
		part.zRot = 0.1f;
		part.xRot = -0.005f;
		part.y = 2;
		part.render(event.getPoseStack(),
				event.getMultiBufferSource().getBuffer(RenderType.armorCutoutNoCull(texture)),
				event.getPackedLight(), net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
		part.visible = partVisible;
	}

	/** 渲染结束后还原骨骼偏移，避免下一帧累积（手臂的 y 不会被 setupAnim 重置） */
	@SubscribeEvent
	public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
		Player player = event.getEntity();
		FeralForm form = FeralFormManager.getForm(player);
		if (!form.isFeral())
			return;
		PlayerModel bodyModel = form.getBodyModel();
		if (bodyModel == null)
			return;
		shiftModelY(bodyModel, -BODY_Y_OFFSET);
	}

	private static void shiftModelY(PlayerModel model, float dy) {
		model.head.y += dy;
		model.body.y += dy;
		model.leftArm.y += dy;
		model.rightArm.y += dy;
		model.leftLeg.y += dy;
		model.rightLeg.y += dy;
	}

	private static void offsetScale(PlayerModel model, Vector3f offset) {
		model.head.offsetScale(offset);
		model.head.y += offset.x() > 0 ? 0.05 : -0.05;
		model.body.offsetScale(offset);
		model.leftArm.offsetScale(offset);
		model.rightArm.offsetScale(offset);
		model.leftLeg.offsetScale(offset);
		model.rightLeg.offsetScale(offset);
		model.hat.offsetScale(offset);
		model.hat.y += offset.x() > 0 ? 0.05 : -0.05;
		model.jacket.offsetScale(offset);
		model.leftSleeve.offsetScale(offset);
		model.rightSleeve.offsetScale(offset);
		model.leftPants.offsetScale(offset);
		model.rightPants.offsetScale(offset);
	}

	/**
	 * 原版 MCreator renderHumanoid 逻辑。
	 * setupAnim 会被 AstralCruse 的 FeralPlayerModelMixin 注入，应用 Bedrock 动画。
	 * 尾巴已整合进躯干模型，此处额外应用尾巴关键帧动画。
	 */
	private static void renderHumanoid(RenderPlayerEvent event, FeralForm form, PlayerModel model, VertexConsumer vertexConsumer) {
		PoseStack poseStack = event.getPoseStack();
		((HumanoidModel) event.getRenderer().getModel()).copyPropertiesTo(model);
		AbstractClientPlayer entity = (AbstractClientPlayer) event.getEntity();
		float partialTick = event.getPartialTick();
		model.attackTime = entity.getAttackAnim(partialTick);
		float limbSwing = entity.walkAnimation.position(partialTick);
		float limbSwingAmount = entity.walkAnimation.speed(partialTick);
		float ageInTicks = entity.tickCount + partialTick;
		float interpolatedBodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
		float interpolatedHeadYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
		if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity vehicle) {
			interpolatedBodyYaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
			float yawDiff = Mth.wrapDegrees(interpolatedHeadYaw - interpolatedBodyYaw);
			if (yawDiff < -85.0F)
				yawDiff = -85.0F;
			if (yawDiff >= 85.0F)
				yawDiff = 85.0F;
			interpolatedBodyYaw = interpolatedHeadYaw - yawDiff;
			if (yawDiff * yawDiff > 2500.0F) {
				interpolatedBodyYaw += yawDiff * 0.2F;
			}
			interpolatedBodyYaw = Mth.wrapDegrees(interpolatedBodyYaw);
		}
		float netHeadYaw = interpolatedHeadYaw - interpolatedBodyYaw;
		float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		poseStack.pushPose();
		model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		applyTailAnimation(form, model, limbSwing, limbSwingAmount, ageInTicks);
		// 整体向下偏移，直接烘进骨骼坐标，手持物品等跟随渲染自动对齐
		shiftModelY(model, BODY_Y_OFFSET);
		// 蹲下/潜行时额外上移（原由 FeralPlayerRendererMixin -> applyBodyRenderTransform 提供，该 Mixin 已移除）
		if (entity.isCrouching() || entity.isShiftKeyDown()) {
			shiftModelY(model, CROUCH_Y_OFFSET);
		}
		if (entity.hasPose(Pose.SLEEPING)) {
			Direction direction = entity.getBedOrientation();
			if (direction != null) {
				float eyeHeightOffset = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((float) (-direction.getStepX()) * eyeHeightOffset, 0.0F,
						(float) (-direction.getStepZ()) * eyeHeightOffset);
			}
		}
		event.getRenderer().setupRotations(entity, poseStack, ageInTicks, interpolatedBodyYaw, partialTick, 0);
		poseStack.scale(-0.938f, -0.938f, 0.938f);
		poseStack.translate(0.0D, -1.50D, 0.0D);
		Vector3f offset = new Vector3f(0.015f);
		offsetScale(model, offset);
		model.renderToBuffer(poseStack, vertexConsumer, event.getPackedLight(),
				LivingEntityRenderer.getOverlayCoords(entity, 0));
		offset.negate();
		offsetScale(model, offset);
		poseStack.popPose();
	}

	private static final Vector3f TAIL_ANIM_VECTOR_CACHE = new Vector3f();

	/**
	 * 尾巴已整合进躯干模型。在 Bedrock 身体动画之后，对躯干下的尾巴部件应用关键帧动画。
	 * 先重置尾巴部件到默认姿态，再叠加待机动画（始终）与行走动画（按 limbSwingAmount 缩放）。
	 */
	private static void applyTailAnimation(FeralForm form, PlayerModel model, float limbSwing,
			float limbSwingAmount, float ageInTicks) {
		AnimationDefinition idle = form.getTailIdleAnimation();
		AnimationDefinition walk = form.getTailWalkAnimation();
		if (idle == null && walk == null)
			return;

		// 重置尾巴及其所有子部件到默认姿态，避免上一帧动画残留
		ModelPart tail;
		try {
			tail = model.body.getChild("Tail");
		} catch (java.util.NoSuchElementException ignored) {
			return;
		}
		tail.getAllParts().forEach(ModelPart::resetPose);

		// 尾巴挂在躯干（model.body）下，以躯干为根搜索尾巴部件
		HierarchicalModel<net.minecraft.world.entity.Entity> tailRoot = new HierarchicalModel<>() {
			@Override
			public ModelPart root() {
				return model.body;
			}

			@Override
			public void setupAnim(net.minecraft.world.entity.Entity entity, float pLimbSwing, float pLimbSwingAmount,
					float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
				long time = (long) (pAgeInTicks * 50.0F);
				if (idle != null) {
					KeyframeAnimations.animate(this, idle, time, 1.0F, TAIL_ANIM_VECTOR_CACHE);
				}
				if (walk != null) {
					this.animateWalk(walk, pLimbSwing, pLimbSwingAmount, 1.2F, 1.0F);
				}
			}
		};
		tailRoot.setupAnim(null, limbSwing, limbSwingAmount, ageInTicks, 0.0F, 0.0F);
	}
}
