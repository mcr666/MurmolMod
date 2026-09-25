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
import net.minecraft.client.model.geom.ModelPart;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 玩家变形渲染调度。
 * 采用原版 MCreator 的 renderHumanoid/renderEntity 渲染逻辑：
 * 自行调用 setupRotations、prepareMobModel、setupAnim（触发 AstralCruse Bedrock 动画 Mixin），
 * 并应用正确的 scale/translate 偏移。
 */
@EventBusSubscriber(Dist.CLIENT)
public class FeralFormRenderer {

	/** 整体偏移（模型坐标 Y+ 朝下），当前值由玩家实测调定为 13 */
	private static final float BODY_Y_OFFSET = 13.0F;

	/** 整体向前偏移（模型坐标 -Z 为实体面朝方向），5 个模型单位 = 5/16 格 */
	private static final float BODY_Z_OFFSET = -5.0F;

	/** 蹲下/潜行时的额外上移（沿用原 applyBodyRenderTransform 的 CROUCH_RENDER_Y_OFFSET = 2 个模型单位 = 0.125 格） */
	private static final float CROUCH_Y_OFFSET = -2.0F;

	/** 尾巴 idle↔walk 交叉混合时长（tick），与身体动画 TRANSITION_TICKS 一致 */
	private static final float TAIL_BLEND_TICKS = 4.0F;

	/** 尾巴混合状态（每实体，键为弱引用避免泄漏） */
	private static final java.util.WeakHashMap<Player, TailBlend> TAIL_BLENDS = new java.util.WeakHashMap<>();
	/** 石像状态冻结的朝向（按玩家 UUID 记录进入石像那一刻的 身体yaw / 头部相对yaw / 头部俯仰） */
	private static final java.util.Map<java.util.UUID, float[]> STATUE_POSES = new java.util.HashMap<>();


	@SubscribeEvent
	public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
		net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
		if (player == null)
			return;
		FeralFormManager.hoverFlightClientTick(player, FeralFormManager.getForm(player));
	}

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

		ResourceLocation texture = resolveTexture(form, event.getEntity());
		if (texture != null) {
			renderHumanoid(event, form, bodyModel,
					event.getMultiBufferSource().getBuffer(RenderType.armorCutoutNoCull(texture)));
		}
	}

	/** 石像状态贴图：石头 / 磐座上为苔石（专用狛犬石像贴图，与模型 UV 布局一致） */
	private static final ResourceLocation STONE_TEXTURE = ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/komainu_stone.png");
	private static final ResourceLocation MOSSY_STONE_TEXTURE = ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/komainu_mossy_stone.png");

	/** 狛犬石像状态改用石头/苔石贴图，其余形态返回形态贴图 */
	private static ResourceLocation resolveTexture(FeralForm form, net.minecraft.world.entity.LivingEntity entity) {
		if (form.hasStatueState() && net.mcr.murmol.feral.FeralFormManager.isInStatue(entity))
			return net.mcr.murmol.feral.FeralFormManager.isOnBanza(entity) ? MOSSY_STONE_TEXTURE : STONE_TEXTURE;
		return form.getTexture();
	}

	@SubscribeEvent
	public static void onRenderArm(RenderArmEvent event) {
		Player player = event.getPlayer();
		FeralForm form = FeralFormManager.getForm(player);
		if (!form.isFeral())
			return;
		// 按形态属性决定是否显示；手动开关可强制显示形态定义中隐藏的手臂
		if (!form.showFirstPersonArm() && !net.mcr.murmol.MurmolModConfig.SHOW_HIDDEN_FERAL_ARM.get()) {
			// 不显示形态手臂时，同时隐藏原版玩家第一人称手臂
			event.setCanceled(true);
			return;
		}

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
		// 隐藏手臂上的物品挂点骨骼（left_item/right_item），避免第一人称出现多余面片
		for (String name : new String[] {"left_item", "right_item"}) {
			ModelPart itemBone = FeralBedrockPlayerAnimator.findBone(part, name);
			if (itemBone != null) {
				itemBone.visible = false;
			}
		}
		part.zRot = 0.1f;
		part.xRot = -0.005f;
		part.y = 2;
		// 第一人称专属手臂动画：右臂 Y 旋转 +45°，左臂 Y 旋转 -45°
		float armYaw = (float) Math.toRadians(45.0);
		if (lefthanded) {
			part.yRot -= armYaw;
		} else {
			part.yRot += armYaw;
		}
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
		shiftModelY(bodyModel, -BODY_Y_OFFSET - form.getBodyYOffset());
		shiftModelZ(bodyModel, -BODY_Z_OFFSET);
		shiftExtraPartsY(form, -BODY_Y_OFFSET - form.getBodyYOffset());
		shiftExtraPartsZ(form, -BODY_Z_OFFSET);
	}

	private static Map<String, ModelPart> extraBodyParts(FeralForm form) {
		return FeralFormModels.getExtraBodyParts(form.getBodyLayer());
	}

	private static void shiftExtraPartsY(FeralForm form, float dy) {
		for (ModelPart part : extraBodyParts(form).values()) {
			part.y += dy;
		}
	}

	private static void shiftExtraPartsZ(FeralForm form, float dz) {
		for (ModelPart part : extraBodyParts(form).values()) {
			part.z += dz;
		}
	}

	private static void shiftModelY(PlayerModel model, float dy) {
		model.head.y += dy;
		model.body.y += dy;
		model.leftArm.y += dy;
		model.rightArm.y += dy;
		model.leftLeg.y += dy;
		model.rightLeg.y += dy;
	}

	private static void shiftModelZ(PlayerModel model, float dz) {
		model.head.z += dz;
		model.body.z += dz;
		model.leftArm.z += dz;
		model.rightArm.z += dz;
		model.leftLeg.z += dz;
		model.rightLeg.z += dz;
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
		hideItemBones(model);
		AbstractClientPlayer entity = (AbstractClientPlayer) event.getEntity();
		float partialTick = event.getPartialTick();
		model.attackTime = entity.getAttackAnim(partialTick);
		float limbSwing = entity.walkAnimation.position(partialTick);
		float limbSwingAmount = entity.walkAnimation.speed(partialTick);
		// 石像状态冻结关键帧推进（尾部/附加骨骼动画与本模型 setupAnim 后的采样共用此时钟）
		float ageInTicks = FeralBedrockPlayerAnimator.effectiveAge(entity, entity.tickCount + partialTick);
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
		// 石像状态：身体与头部朝向全部冻结在进入石像那一刻，不跟随视角转动
		float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		float netHeadYaw;
		if (net.mcr.murmol.feral.FeralFormManager.isInStatue(entity)) {
			float currentBodyYaw = interpolatedBodyYaw;
			float currentNetHeadYaw = interpolatedHeadYaw - currentBodyYaw;
			float currentPitch = headPitch;
			float[] frozen = STATUE_POSES.computeIfAbsent(entity.getUUID(),
					k -> new float[] {currentBodyYaw, currentNetHeadYaw, currentPitch});
			interpolatedBodyYaw = frozen[0];
			netHeadYaw = frozen[1];
			headPitch = frozen[2];
		} else {
			STATUE_POSES.remove(entity.getUUID());
			netHeadYaw = interpolatedHeadYaw - interpolatedBodyYaw;
		}
		poseStack.pushPose();
		model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		applyTailAnimation(form, entity, model, limbSwing, limbSwingAmount, ageInTicks);
		applyBipedAnimation(form, entity, limbSwing, limbSwingAmount, ageInTicks);
		// 整体向下偏移（含形态专属偏移），直接烘进骨骼坐标，手持物品等跟随渲染自动对齐
		shiftModelY(model, BODY_Y_OFFSET + form.getBodyYOffset());
		// 整体向前偏移（烘进骨骼坐标，渲染结束后还原）
		shiftModelZ(model, BODY_Z_OFFSET);
		// biped_* 附加骨骼同步偏移，保持与身体对齐
		shiftExtraPartsY(form, BODY_Y_OFFSET + form.getBodyYOffset());
		shiftExtraPartsZ(form, BODY_Z_OFFSET);
		// 蹲下/潜行时额外上移（原由 FeralPlayerRendererMixin -> applyBodyRenderTransform 提供，该 Mixin 已移除）
		if (entity.isCrouching() || entity.isShiftKeyDown()) {
			shiftModelY(model, CROUCH_Y_OFFSET);
			shiftExtraPartsY(form, CROUCH_Y_OFFSET);
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
		// SCALE 属性（如春花形态 0.75x）影响碰撞箱与原版渲染，这里同步应用到自定义模型
		float bodyScale = entity.getScale();
		poseStack.scale(-0.938f * bodyScale, -0.938f * bodyScale, 0.938f * bodyScale);
		poseStack.translate(0.0D, -1.50D, 0.0D);
		Vector3f offset = new Vector3f(0.015f);
		offsetScale(model, offset);
		for (ModelPart part : extraBodyParts(form).values()) {
			part.offsetScale(offset);
		}
		model.renderToBuffer(poseStack, vertexConsumer, event.getPackedLight(),
				LivingEntityRenderer.getOverlayCoords(entity, 0));
		// biped_* 附加骨骼额外渲染（不在包装模型内，与身体同变换、同纹理）
		for (ModelPart part : extraBodyParts(form).values()) {
			part.render(poseStack, vertexConsumer, event.getPackedLight(),
					LivingEntityRenderer.getOverlayCoords(entity, 0), -1);
		}
		offset.negate();
		offsetScale(model, offset);
		for (ModelPart part : extraBodyParts(form).values()) {
			part.offsetScale(offset);
		}
		poseStack.popPose();
	}

	/**
	 * 隐藏 left_item / right_item 骨骼组：它们仅作为手持物品的变换挂点
	 * （见 FeralBedrockPlayerAnimator.applyItemTransform），自身是 0 宽度盒子，
	 * 参与渲染会出现多余面片，故整体设为不可见（变换定位不受 visible 影响）。
	 */
	private static void hideItemBones(PlayerModel model) {
		for (ModelPart arm : new ModelPart[] {model.leftArm, model.rightArm}) {
			for (String name : new String[] {"left_item", "right_item"}) {
				ModelPart part = FeralBedrockPlayerAnimator.findBone(arm, name);
				if (part != null) {
					part.visible = false;
				}
			}
		}
	}

	/**
	 * 尾巴已整合进躯干模型。尾部动画取自形态专属动画文件的 tail_idle / tail_walk，
	 * 在 Bedrock 身体动画之后，对涉及的尾巴骨骼（按动画定义查找，如 TailPrimary/bep1）应用采样旋转。
	 * 先重置尾巴部件到默认姿态，再交叉混合：待机与行走按 4 tick 渐进权重过渡（idle×(1-w) + walk×w），
	 * 避免起步/停步时姿态硬切。
	 */
	private static void applyTailAnimation(FeralForm form, Player entity, PlayerModel model, float limbSwing,
			float limbSwingAmount, float ageInTicks) {
		FeralBedrockPlayerAnimator.BedrockAnimation idle = FeralBedrockPlayerAnimator.animationOf(form, "tail_idle");
		FeralBedrockPlayerAnimator.BedrockAnimation walk = FeralBedrockPlayerAnimator.animationOf(form, "tail_walk");
		if (idle == null && walk == null)
			return;

		// 按动画定义的骨骼名在躯干子树中查找尾巴部件
		Set<String> boneNames = new HashSet<>();
		if (idle != null) {
			boneNames.addAll(FeralBedrockPlayerAnimator.boneNames(idle));
		}
		if (walk != null) {
			boneNames.addAll(FeralBedrockPlayerAnimator.boneNames(walk));
		}
		Map<String, ModelPart> parts = new HashMap<>();
		for (String boneName : boneNames) {
			ModelPart part = FeralBedrockPlayerAnimator.findBone(model.body, boneName);
			if (part != null) {
				parts.put(boneName, part);
			}
		}
		if (parts.isEmpty())
			return;

		// 重置尾巴部件及其子部件到默认姿态，避免上一帧动画残留
		parts.values().forEach(part -> part.getAllParts().forEach(ModelPart::resetPose));

		// 行走/待机目标状态切换时记录起点，4 tick 渐进过渡（与身体动画 TRANSITION_TICKS 一致）
		boolean targetWalking = limbSwingAmount > 0.01F;
		TailBlend blend = TAIL_BLENDS.computeIfAbsent(entity, ignored -> new TailBlend());
		if (blend.walking != targetWalking) {
			blend.walking = targetWalking;
			blend.blendStartAge = ageInTicks;
		}
		// 行走相位仅在行走时推进；淡出期间冻结，避免停止后尾巴继续迈步
		float walkPhase = limbSwing * 0.03331F;
		if (targetWalking) {
			blend.walkPhase = walkPhase;
		}
		float progress = Mth.clamp((ageInTicks - blend.blendStartAge) / TAIL_BLEND_TICKS, 0.0F, 1.0F);
		float walkWeight = blend.walking ? progress : 1.0F - progress;

		float idleTime = FeralBedrockPlayerAnimator.animationTime(idle, ageInTicks / 20.0F);
		// 原版 animateWalk 相位：limbSwing * 0.6662 * 50 毫秒，换算为秒 = *0.03331
		float walkTime = FeralBedrockPlayerAnimator.animationTime(walk, blend.walkPhase);
		for (String boneName : boneNames) {
			ModelPart part = parts.get(boneName);
			if (part == null) {
				continue;
			}
			float x = 0.0F;
			float y = 0.0F;
			float z = 0.0F;
			if (idle != null && walkWeight < 1.0F) {
				float[] rotation = FeralBedrockPlayerAnimator.sampleExtraRotation(idle, boneName, idleTime);
				if (rotation != null) {
					x += rotation[0] * (1.0F - walkWeight);
					y += rotation[1] * (1.0F - walkWeight);
					z += rotation[2] * (1.0F - walkWeight);
				}
			}
			if (walk != null && walkWeight > 0.0F) {
				float[] rotation = FeralBedrockPlayerAnimator.sampleExtraRotation(walk, boneName, walkTime);
				if (rotation != null) {
					x += rotation[0] * limbSwingAmount * walkWeight;
					y += rotation[1] * limbSwingAmount * walkWeight;
					z += rotation[2] * limbSwingAmount * walkWeight;
				}
			}
			part.xRot += (float) Math.toRadians(x);
			part.yRot += (float) Math.toRadians(y);
			part.zRot += (float) Math.toRadians(z);
		}
	}

	/** 尾巴交叉混合状态（每实体） */
	private static final class TailBlend {
		boolean walking;
		float blendStartAge = Float.NEGATIVE_INFINITY;
		/** 最近一次行走时的行走相位（淡出期间冻结用） */
		float walkPhase;
	}

	/**
	 * biped_* 附加骨骼动画：每帧还原到烘焙姿态后，按 moth_walk 的对应通道 × limbSwingAmount 叠加摆动。
	 * 与尾巴共用同一实体行走状态（含冻结相位），停步后不会多摆。
	 */
	private static void applyBipedAnimation(FeralForm form, Player entity, float limbSwing,
			float limbSwingAmount, float ageInTicks) {
		Map<String, ModelPart> extras = extraBodyParts(form);
		if (extras.isEmpty())
			return;
		FeralBedrockPlayerAnimator.BedrockAnimation legWalk = FeralBedrockPlayerAnimator.animationOf(form, "moth_walk");
		if (legWalk == null)
			return;

		boolean targetWalking = limbSwingAmount > 0.01F;
		TailBlend blend = TAIL_BLENDS.computeIfAbsent(entity, ignored -> new TailBlend());
		if (blend.walking != targetWalking) {
			blend.walking = targetWalking;
			blend.blendStartAge = ageInTicks;
		}
		if (targetWalking) {
			blend.walkPhase = limbSwing * 0.03331F;
		}
		float walkTime = FeralBedrockPlayerAnimator.animationTime(legWalk, blend.walkPhase);
		for (Map.Entry<String, ModelPart> entry : extras.entrySet()) {
			ModelPart part = entry.getValue();
			part.getAllParts().forEach(ModelPart::resetPose);
			if (!targetWalking) {
				continue;
			}
			float[] rotation = FeralBedrockPlayerAnimator.sampleExtraRotation(legWalk, entry.getKey(), walkTime);
			if (rotation != null) {
				part.xRot += (float) Math.toRadians(rotation[0] * limbSwingAmount);
				part.yRot += (float) Math.toRadians(rotation[1] * limbSwingAmount);
				part.zRot += (float) Math.toRadians(rotation[2] * limbSwingAmount);
			}
		}
	}
}
