package net.mcr.astralcruse.client.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mcr.murmol.feral.FeralFormManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class FeralBedrockPlayerAnimator {
	private static final ResourceLocation ANIMATION_FILE = ResourceLocation.fromNamespaceAndPath("astral_cruse", "player_animations/feral_anim.json");
	private static final String[] PLAYER_BONES = {"torso", "head", "right_arm", "left_arm", "right_leg", "left_leg"};
	private static final double HORIZONTAL_MOVE_THRESHOLD_SQR = 0.0004D;
	private static final float LOOP_TIME_DIVISOR = 20.0F;
	private static final float TRANSITION_TICKS = 4.0F;
	private static final float ONE_SHOT_TRANSITION_TICKS = 2.0F;
	private static final float FERAL_ENTRY_TRANSITION_TICKS = 20.0F;
	private static final float BODY_RENDER_Y_OFFSET = 1.0F;
	private static final float CROUCH_RENDER_Y_OFFSET = 2.0F;
	private static final Map<LivingEntity, AnimationTransition> TRANSITIONS = new WeakHashMap<>();
	private static final Map<LivingEntity, AnimationPlaybackClock> PLAYBACK_CLOCKS = new WeakHashMap<>();
	private static final Map<LivingEntity, FeralActivation> ACTIVATIONS = new WeakHashMap<>();
	private static final Map<LivingEntity, AnimationTickSelection> TICK_SELECTIONS = new WeakHashMap<>();
	private static Map<String, BedrockAnimation> animations;

	private FeralBedrockPlayerAnimator() {
	}

	public static void apply(PlayerModel<?> model, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!isFeral(entity)) {
			return;
		}
		Map<String, BedrockAnimation> loadedAnimations = animations();
		SelectedAnimation selectedAnimation = selectAnimation(loadedAnimations, entity, limbSwing, limbSwingAmount, ageInTicks);
		if (selectedAnimation == null) {
			return;
		}
		AnimationTransition transition = transitionFor(entity, selectedAnimation, ageInTicks);
		PlayerPose vanillaPose = PlayerPose.capture(model);
		resetPlayerParts(model);
		float time = selectedAnimation.animation.time(selectedAnimation.seconds);
		for (String boneName : PLAYER_BONES) {
			ModelPart part = partFor(model, boneName);
			float[] rotation = blendedRotation(transition, boneName, time, headPitch, netHeadYaw, ageInTicks);
			if (rotation != null) {
				if ("head".equals(boneName)) {
					// Temporarily leave head rotation disabled while testing axis issues.
				} else {
					part.xRot = degreesToRadians(rotation[0]);
					part.yRot = degreesToRadians(rotation[1]);
					part.zRot = degreesToRadians(rotation[2]);
				}
			}
			float[] position = blendedPosition(transition, boneName, time, headPitch, netHeadYaw, ageInTicks);
			if (position != null) {
				applyPosition(part, position);
			}
		}
		applyHeadLook(model, headPitch, netHeadYaw);
		float entryAlpha = feralEntryAlpha(entity, ageInTicks);
		if (entryAlpha < 1.0F) {
			vanillaPose.blendInto(model, entryAlpha);
		}
		copyOuterParts(model);
	}

	public static void applyBodyRenderTransform(LivingEntity entity, PoseStack poseStack, float ageInTicks) {
		if (!isFeral(entity)) {
			return;
		}
		Map<String, BedrockAnimation> loadedAnimations = animations();
		SelectedAnimation selectedAnimation = selectAnimation(loadedAnimations, entity, entity.walkAnimation.position(), entity.walkAnimation.speed(), ageInTicks);
		if (selectedAnimation == null) {
			return;
		}
		AnimationTransition transition = transitionFor(entity, selectedAnimation, ageInTicks);
		float time = selectedAnimation.animation.time(selectedAnimation.seconds);
		float[] scale = blendedScale(transition, "body", time, entity.getXRot(), entity.getYRot(), ageInTicks);
		float[] position = blendedPosition(transition, "body", time, entity.getXRot(), entity.getYRot(), ageInTicks);
		float[] rotation = blendedRotation(transition, "body", time, entity.getXRot(), entity.getYRot(), ageInTicks);
		if (scale == null && position == null && rotation == null) {
			return;
		}
		float entryAlpha = feralEntryAlpha(entity, ageInTicks);
		if (scale != null) {
			poseStack.scale(lerp(1.0F, scale[0], entryAlpha), lerp(1.0F, scale[1], entryAlpha), lerp(1.0F, scale[2], entryAlpha));
		}
		float bodyX = (position == null ? 0.0F : position[0]) * entryAlpha;
		float bodyY = ((position == null ? 0.0F : position[1]) + BODY_RENDER_Y_OFFSET + (isSneakingForRender(entity) ? CROUCH_RENDER_Y_OFFSET : 0.0F)) * entryAlpha;
		float bodyZ = (position == null ? 0.0F : position[2]) * entryAlpha;
		poseStack.translate(-bodyX * 0.0625F, bodyY * 0.0625F + 0.75F, bodyZ * 0.0625F);
		if (rotation != null) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[2] * entryAlpha));
			poseStack.mulPose(Axis.YP.rotationDegrees(-rotation[1] * entryAlpha));
			poseStack.mulPose(Axis.XP.rotationDegrees(-rotation[0] * entryAlpha));
		}
		poseStack.translate(0.0F, -0.75F, 0.0F);
	}

	public static void applyTorsoLayerTransform(LivingEntity entity, PoseStack poseStack, float ageInTicks) {
		if (!isFeral(entity)) {
			return;
		}
		Map<String, BedrockAnimation> loadedAnimations = animations();
		SelectedAnimation selectedAnimation = selectAnimation(loadedAnimations, entity, entity.walkAnimation.position(), entity.walkAnimation.speed(), ageInTicks);
		if (selectedAnimation == null) {
			return;
		}
		AnimationTransition transition = transitionFor(entity, selectedAnimation, ageInTicks);
		float time = selectedAnimation.animation.time(selectedAnimation.seconds);
		float[] position = blendedPosition(transition, "torso", time, entity.getXRot(), entity.getYRot(), ageInTicks);
		float[] rotation = blendedRotation(transition, "torso", time, entity.getXRot(), entity.getYRot(), ageInTicks);
		if (position == null && rotation == null) {
			return;
		}
		if (position != null) {
			poseStack.translate(position[0] * 0.0625F, position[1] * 0.0625F, position[2] * 0.0625F);
		}
		if (rotation != null) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[2]));
			poseStack.mulPose(Axis.YP.rotationDegrees(rotation[1]));
			poseStack.mulPose(Axis.XP.rotationDegrees(rotation[0]));
		}
	}

	public static void applyItemTransform(LivingEntity entity, HumanoidArm arm, PoseStack poseStack) {
		if (!isFeral(entity)) {
			return;
		}
		Map<String, BedrockAnimation> loadedAnimations = animations();
		SelectedAnimation selectedAnimation = selectAnimation(loadedAnimations, entity, entity.walkAnimation.position(), entity.walkAnimation.speed(), entity.tickCount);
		if (selectedAnimation == null) {
			return;
		}
		AnimationTransition transition = transitionFor(entity, selectedAnimation, entity.tickCount);
		String boneName = arm == HumanoidArm.LEFT ? "left_item" : "right_item";
		float time = selectedAnimation.animation.time(selectedAnimation.seconds);
		float[] scale = blendedScale(transition, boneName, time, entity.getXRot(), entity.getYRot(), entity.tickCount);
		float[] position = blendedPosition(transition, boneName, time, entity.getXRot(), entity.getYRot(), entity.tickCount);
		float[] rotation = blendedRotation(transition, boneName, time, entity.getXRot(), entity.getYRot(), entity.tickCount);
		if (scale == null && position == null && rotation == null) {
			return;
		}
		if (scale != null) {
			poseStack.scale(scale[0], scale[1], scale[2]);
		}
		if (position != null) {
			poseStack.translate(-position[0] * 0.0625F, -position[2] * 0.0625F, position[1] * 0.0625F);
		}
		if (rotation != null) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[1]));
			poseStack.mulPose(Axis.YP.rotationDegrees(-rotation[2]));
			poseStack.mulPose(Axis.XP.rotationDegrees(-rotation[0]));
		}
	}

	public static void applyMouthItemTransform(LivingEntity entity, PoseStack poseStack) {
		if (!isFeral(entity)) {
			return;
		}
		Map<String, BedrockAnimation> loadedAnimations = animations();
		BedrockAnimation animation = firstPresent(loadedAnimations, "mouth_item", "feral_mouth_item");
		if (animation == null) {
			return;
		}
		float time = animation.time(entity.tickCount / LOOP_TIME_DIVISOR);
		float[] scale = sampleBone(animation, "mouth_item", time, entity.getXRot(), entity.getYRot(), ChannelKind.SCALE);
		float[] position = sampleBone(animation, "mouth_item", time, entity.getXRot(), entity.getYRot(), ChannelKind.POSITION);
		float[] rotation = sampleBone(animation, "mouth_item", time, entity.getXRot(), entity.getYRot(), ChannelKind.ROTATION);
		if (scale != null) {
			poseStack.scale(scale[0], scale[1], scale[2]);
		}
		if (position != null) {
			poseStack.translate(-position[0] * 0.0625F, -position[2] * 0.0625F, position[1] * 0.0625F);
		}
		if (rotation != null) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[1]));
			poseStack.mulPose(Axis.YP.rotationDegrees(-rotation[2]));
			poseStack.mulPose(Axis.XP.rotationDegrees(-rotation[0]));
		}
	}

	public static boolean shouldRenderItemInMouth(LivingEntity entity, HumanoidArm arm, ItemStack itemStack) {
		return isFeral(entity) && arm == HumanoidArm.RIGHT && !itemStack.isEmpty() && !isToolLikeItem(itemStack.getItem());
	}

	private static boolean isToolLikeItem(Item item) {
		return item instanceof TieredItem
				|| item instanceof BlockItem
				|| item instanceof DiggerItem
				|| item instanceof SwordItem
				|| item instanceof BowItem
				|| item instanceof CrossbowItem
				|| item instanceof TridentItem
				|| item instanceof ShieldItem;
	}

	private static boolean isFeral(LivingEntity entity) {
		return FeralFormManager.getForm(entity).isFeral();
	}

	public static boolean shouldAnimate(LivingEntity entity) {
		boolean feral = isFeral(entity);
		if (!feral) {
			FeralActivation activation = ACTIVATIONS.computeIfAbsent(entity, ignored -> new FeralActivation());
			activation.wasFeral = false;
			activation.entryStartAge = Float.NEGATIVE_INFINITY;
		}
		return feral;
	}

	private static boolean isSneakingForRender(LivingEntity entity) {
		return entity.isCrouching() || entity.isShiftKeyDown();
	}

	private static float feralEntryAlpha(LivingEntity entity, float ageInTicks) {
		FeralActivation activation = ACTIVATIONS.computeIfAbsent(entity, ignored -> new FeralActivation());
		if (!activation.wasFeral) {
			activation.entryStartAge = ageInTicks;
		} else if (activation.entryStartAge == Float.NEGATIVE_INFINITY) {
			activation.entryStartAge = ageInTicks - FERAL_ENTRY_TRANSITION_TICKS;
		}
		activation.wasFeral = true;
		return Math.clamp((ageInTicks - activation.entryStartAge) / FERAL_ENTRY_TRANSITION_TICKS, 0.0F, 1.0F);
	}

	private static SelectedAnimation selectAnimation(Map<String, BedrockAnimation> loadedAnimations, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks) {
		AnimationState state = animationStateForTick(entity);
		BedrockAnimation animation = animationForState(loadedAnimations, state);
		if (animation == null && state == AnimationState.RUN) {
			animation = animationForState(loadedAnimations, AnimationState.WALK);
		}
		if (animation == null) {
			animation = animationForState(loadedAnimations, AnimationState.IDLE);
		}
		return animation == null ? null : new SelectedAnimation(state, animation, secondsForState(entity, state, ageInTicks));
	}

	private static AnimationState animationStateForTick(LivingEntity entity) {
		AnimationTickSelection selection = TICK_SELECTIONS.computeIfAbsent(entity, ignored -> new AnimationTickSelection());
		if (selection.tick != entity.tickCount) {
			selection.tick = entity.tickCount;
			selection.state = animationState(entity);
		}
		return selection.state;
	}

	private static AnimationTransition transitionFor(LivingEntity entity, SelectedAnimation selectedAnimation, float ageInTicks) {
		AnimationTransition transition = TRANSITIONS.computeIfAbsent(entity, ignored -> new AnimationTransition());
		if (transition.animation == null) {
			transition.state = selectedAnimation.state;
			transition.animation = selectedAnimation.animation;
			transition.lastSeconds = selectedAnimation.seconds;
			transition.transitionTicks = TRANSITION_TICKS;
			transition.transitionStartAge = ageInTicks - TRANSITION_TICKS;
			return transition;
		}
		if (transition.state != selectedAnimation.state || transition.animation != selectedAnimation.animation) {
			AnimationState previousState = transition.state;
			transition.previousAnimation = transition.animation;
			transition.previousSeconds = transition.lastSeconds;
			transition.transitionTicks = transitionTicksFor(previousState, selectedAnimation.state);
			transition.transitionStartAge = ageInTicks;
			transition.state = selectedAnimation.state;
			transition.animation = selectedAnimation.animation;
		}
		transition.lastSeconds = selectedAnimation.seconds;
		return transition;
	}

	private static float[] blendedRotation(AnimationTransition transition, String boneName, float currentTime, float headPitch, float netHeadYaw, float ageInTicks) {
		return blendedVector(transition, boneName, currentTime, headPitch, netHeadYaw, ageInTicks, ChannelKind.ROTATION);
	}

	private static float[] blendedPosition(AnimationTransition transition, String boneName, float currentTime, float headPitch, float netHeadYaw, float ageInTicks) {
		return blendedVector(transition, boneName, currentTime, headPitch, netHeadYaw, ageInTicks, ChannelKind.POSITION);
	}

	private static float[] blendedScale(AnimationTransition transition, String boneName, float currentTime, float headPitch, float netHeadYaw, float ageInTicks) {
		return blendedVector(transition, boneName, currentTime, headPitch, netHeadYaw, ageInTicks, ChannelKind.SCALE);
	}

	private static float[] blendedVector(AnimationTransition transition, String boneName, float currentTime, float headPitch, float netHeadYaw, float ageInTicks, ChannelKind kind) {
		float[] current = sampleBone(transition.animation, boneName, currentTime, headPitch, netHeadYaw, kind);
		if (transition.previousAnimation == null) {
			return current;
		}
		float alpha = transitionAlpha(transition, ageInTicks);
		if (alpha >= 1.0F) {
			transition.previousAnimation = null;
			return current;
		}
		float previousTime = transition.previousAnimation.time(transition.previousSeconds);
		float[] previous = sampleBone(transition.previousAnimation, boneName, previousTime, headPitch, netHeadYaw, kind);
		if (previous == null && current == null) {
			return null;
		}
		float[] defaultValue = kind == ChannelKind.SCALE ? new float[]{1.0F, 1.0F, 1.0F} : new float[]{0.0F, 0.0F, 0.0F};
		return blend(previous == null ? defaultValue : previous, current == null ? defaultValue : current, alpha);
	}

	private static float[] sampleBone(BedrockAnimation animation, String boneName, float time, float headPitch, float netHeadYaw, ChannelKind kind) {
		BoneAnimation bone = animation.bones.get(boneName);
		if (bone == null) {
			return null;
		}
		return switch (kind) {
			case ROTATION -> bone.rotationAt(time, headPitch, netHeadYaw);
			case POSITION -> bone.positionAt(time, headPitch, netHeadYaw);
			case SCALE -> bone.scaleAt(time, headPitch, netHeadYaw);
		};
	}

	private static float transitionAlpha(AnimationTransition transition, float ageInTicks) {
		return Math.clamp((ageInTicks - transition.transitionStartAge) / Math.max(transition.transitionTicks, 0.001F), 0.0F, 1.0F);
	}

	private static float transitionTicksFor(AnimationState from, AnimationState to) {
		return isOneShotState(from) || isOneShotState(to) ? ONE_SHOT_TRANSITION_TICKS : TRANSITION_TICKS;
	}

	private static float[] blend(float[] start, float[] end, float progress) {
		return new float[]{lerp(start[0], end[0], progress), lerp(start[1], end[1], progress), lerp(start[2], end[2], progress)};
	}

	private static AnimationState animationState(LivingEntity entity) {
		if (entity.getPose() == Pose.SLEEPING) {
			return AnimationState.SLEEP;
		}
		if (entity.swinging) {
			return AnimationState.DIG;
		}
		if (entity.getAttackAnim(1.0F) > 0.0F) {
			return AnimationState.ATTACK;
		}
		if (entity.isFallFlying()) {
			return AnimationState.ELYTRA_FLY;
		}
		if (entity instanceof Player player && player.getAbilities().flying) {
			return AnimationState.FLY;
		}
		if (entity.isSwimming()) {
			return AnimationState.SWIM;
		}
		if (!entity.onGround()) {
			return entity.getDeltaMovement().y < -0.08D ? AnimationState.FALL : AnimationState.JUMP;
		}
		if (entity.isCrouching()) {
			return isMovingHorizontally(entity) ? AnimationState.SNEAK_WALK : AnimationState.SNEAK_IDLE;
		}
		if (!isMovingHorizontally(entity)) {
			return AnimationState.IDLE;
		}
		return entity.isSprinting() ? AnimationState.RUN : AnimationState.WALK;
	}

	private static boolean isMovingHorizontally(LivingEntity entity) {
		Vec3 movement = entity.getDeltaMovement();
		return movement.x * movement.x + movement.z * movement.z > HORIZONTAL_MOVE_THRESHOLD_SQR;
	}

	private static BedrockAnimation animationForState(Map<String, BedrockAnimation> loadedAnimations, AnimationState state) {
		return switch (state) {
			case IDLE -> firstPresent(loadedAnimations, "feral_idle", "idle");
			case WALK -> firstPresent(loadedAnimations, "feral_walk", "feral.walk", "walk");
			case RUN -> firstPresent(loadedAnimations, "feral_run", "run");
			case JUMP -> firstPresent(loadedAnimations, "feral_jump", "jump");
			case FALL -> firstPresent(loadedAnimations, "feral_fall", "fall");
			case SWIM -> firstPresent(loadedAnimations, "feral_swim", "swim");
			case ELYTRA_FLY -> firstPresent(loadedAnimations, "feral_elytra_fly", "elytra_fly", "fall_flying");
			case FLY -> firstPresent(loadedAnimations, "feral_fly", "fly");
			case SLEEP -> firstPresent(loadedAnimations, "feral_sleep", "sleep");
			case ATTACK -> firstPresent(loadedAnimations, "feral_attack", "attack");
			case DIG -> firstPresent(loadedAnimations, "feral_dig", "dig");
			case SNEAK_IDLE -> firstPresent(loadedAnimations, "feral_sneak_idle", "sneak_idle");
			case SNEAK_WALK -> firstPresent(loadedAnimations, "feral_sneak_walk", "sneak_walk");
		};
	}

	private static BedrockAnimation firstPresent(Map<String, BedrockAnimation> loadedAnimations, String... names) {
		for (String name : names) {
			BedrockAnimation animation = loadedAnimations.get(name);
			if (animation != null) {
				return animation;
			}
		}
		return null;
	}

	private static float secondsForState(LivingEntity entity, AnimationState state, float ageInTicks) {
		AnimationPlaybackClock clock = PLAYBACK_CLOCKS.computeIfAbsent(entity, ignored -> new AnimationPlaybackClock());
		if (clock.state != state) {
			clock.state = state;
			clock.stateStartAge = ageInTicks;
		}
		return isOneShotState(state) ? (ageInTicks - clock.stateStartAge) / LOOP_TIME_DIVISOR : ageInTicks / LOOP_TIME_DIVISOR;
	}

	private static boolean isOneShotState(AnimationState state) {
		return state == AnimationState.ATTACK || state == AnimationState.DIG;
	}

	private static Map<String, BedrockAnimation> animations() {
		if (animations != null) {
			return animations;
		}
		Map<String, BedrockAnimation> loaded = new HashMap<>();
		try {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(ANIMATION_FILE);
			if (resource.isPresent()) {
				try (InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
					JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
					JsonObject animationRoot = root.getAsJsonObject("animations");
					for (Map.Entry<String, JsonElement> entry : animationRoot.entrySet()) {
						loaded.put(entry.getKey(), BedrockAnimation.read(entry.getKey(), entry.getValue().getAsJsonObject()));
					}
				}
			}
		} catch (Exception exception) {
			net.mcr.murmol.MurmolMod.LOGGER.error("Failed to load feral player animation {}", ANIMATION_FILE, exception);
		}
		animations = loaded;
		return animations;
	}

	private static void resetPlayerParts(PlayerModel<?> model) {
		model.leftLeg.setPos(1.9F, 12.0F, 0.0F);
		model.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		model.head.setPos(0.0F, 0.0F, 0.0F);
		model.rightArm.z = 0.0F;
		model.rightArm.x = -5.0F;
		model.leftArm.z = 0.0F;
		model.leftArm.x = 5.0F;
		model.head.xRot = 0.0F;
		model.head.yRot = 0.0F;
		model.head.zRot = 0.0F;
		model.body.xRot = 0.0F;
		model.body.yRot = 0.0F;
		model.body.zRot = 0.0F;
		model.rightArm.xRot = 0.0F;
		model.rightArm.yRot = 0.0F;
		model.rightArm.zRot = 0.0F;
		model.leftArm.xRot = 0.0F;
		model.leftArm.yRot = 0.0F;
		model.leftArm.zRot = 0.0F;
		model.rightLeg.xRot = 0.0F;
		model.rightLeg.yRot = 0.0F;
		model.rightLeg.zRot = 0.0F;
		model.leftLeg.xRot = 0.0F;
		model.leftLeg.yRot = 0.0F;
		model.leftLeg.zRot = 0.0F;
		model.rightLeg.z = 0.1F;
		model.leftLeg.z = 0.1F;
		model.rightLeg.y = 12.0F;
		model.leftLeg.y = 12.0F;
		model.head.y = 0.0F;
		model.head.zRot = 0.0F;
		model.body.y = 0.0F;
		model.body.x = 0.0F;
		model.body.z = 0.0F;
		model.head.xScale = ModelPart.DEFAULT_SCALE;
		model.head.yScale = ModelPart.DEFAULT_SCALE;
		model.head.zScale = ModelPart.DEFAULT_SCALE;
		model.body.xScale = ModelPart.DEFAULT_SCALE;
		model.body.yScale = ModelPart.DEFAULT_SCALE;
		model.body.zScale = ModelPart.DEFAULT_SCALE;
		model.rightArm.xScale = ModelPart.DEFAULT_SCALE;
		model.rightArm.yScale = ModelPart.DEFAULT_SCALE;
		model.rightArm.zScale = ModelPart.DEFAULT_SCALE;
		model.leftArm.xScale = ModelPart.DEFAULT_SCALE;
		model.leftArm.yScale = ModelPart.DEFAULT_SCALE;
		model.leftArm.zScale = ModelPart.DEFAULT_SCALE;
		model.rightLeg.xScale = ModelPart.DEFAULT_SCALE;
		model.rightLeg.yScale = ModelPart.DEFAULT_SCALE;
		model.rightLeg.zScale = ModelPart.DEFAULT_SCALE;
		model.leftLeg.xScale = ModelPart.DEFAULT_SCALE;
		model.leftLeg.yScale = ModelPart.DEFAULT_SCALE;
		model.leftLeg.zScale = ModelPart.DEFAULT_SCALE;
	}

	private static void applyPosition(ModelPart part, float[] position) {
		part.x += position[0];
		part.y -= position[1];
		part.z += position[2];
	}

	private static void applyHeadLook(PlayerModel<?> model, float headPitch, float netHeadYaw) {
		model.head.xRot += degreesToRadians(headPitch);
		model.head.yRot += degreesToRadians(netHeadYaw);
	}

	private static ModelPart partFor(PlayerModel<?> model, String bedrockBone) {
		return switch (bedrockBone) {
			case "head" -> model.head;
			case "torso" -> model.body;
			case "right_arm" -> model.rightArm;
			case "left_arm" -> model.leftArm;
			case "right_leg" -> model.rightLeg;
			case "left_leg" -> model.leftLeg;
			default -> throw new IllegalArgumentException("Unsupported player animation bone: " + bedrockBone);
		};
	}

	private static void copyOuterParts(PlayerModel<?> model) {
		model.hat.copyFrom(model.head);
		model.jacket.copyFrom(model.body);
		model.rightSleeve.copyFrom(model.rightArm);
		model.leftSleeve.copyFrom(model.leftArm);
		model.rightPants.copyFrom(model.rightLeg);
		model.leftPants.copyFrom(model.leftLeg);
	}

	private static float degreesToRadians(float degrees) {
		return degrees * ((float) Math.PI / 180.0F);
	}

	private enum AnimationState {
		IDLE,
		WALK,
		RUN,
		JUMP,
		FALL,
		SWIM,
		ELYTRA_FLY,
		FLY,
		SLEEP,
		ATTACK,
		DIG,
		SNEAK_IDLE,
		SNEAK_WALK
	}

	private enum ChannelKind {
		ROTATION,
		POSITION,
		SCALE
	}

	private static final class AnimationTransition {
		private AnimationState state;
		private BedrockAnimation animation;
		private BedrockAnimation previousAnimation;
		private float previousSeconds;
		private float transitionStartAge;
		private float transitionTicks;
		private float lastSeconds;
	}

	private static final class AnimationPlaybackClock {
		private AnimationState state;
		private float stateStartAge;
	}

	private static final class AnimationTickSelection {
		private int tick = Integer.MIN_VALUE;
		private AnimationState state = AnimationState.IDLE;
	}

	private static final class FeralActivation {
		private boolean wasFeral;
		private float entryStartAge = Float.NEGATIVE_INFINITY;
	}

	private record PlayerPose(PartPose head, PartPose body, PartPose rightArm, PartPose leftArm, PartPose rightLeg, PartPose leftLeg) {
		private static PlayerPose capture(PlayerModel<?> model) {
			return new PlayerPose(
					PartPose.capture(model.head),
					PartPose.capture(model.body),
					PartPose.capture(model.rightArm),
					PartPose.capture(model.leftArm),
					PartPose.capture(model.rightLeg),
					PartPose.capture(model.leftLeg));
		}

		private void blendInto(PlayerModel<?> model, float alpha) {
			head.blendInto(model.head, alpha);
			body.blendInto(model.body, alpha);
			rightArm.blendInto(model.rightArm, alpha);
			leftArm.blendInto(model.leftArm, alpha);
			rightLeg.blendInto(model.rightLeg, alpha);
			leftLeg.blendInto(model.leftLeg, alpha);
		}
	}

	private record PartPose(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale) {
		private static PartPose capture(ModelPart part) {
			return new PartPose(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot, part.xScale, part.yScale, part.zScale);
		}

		private void blendInto(ModelPart part, float alpha) {
			part.x = lerp(x, part.x, alpha);
			part.y = lerp(y, part.y, alpha);
			part.z = lerp(z, part.z, alpha);
			part.xRot = lerp(xRot, part.xRot, alpha);
			part.yRot = lerp(yRot, part.yRot, alpha);
			part.zRot = lerp(zRot, part.zRot, alpha);
			part.xScale = lerp(xScale, part.xScale, alpha);
			part.yScale = lerp(yScale, part.yScale, alpha);
			part.zScale = lerp(zScale, part.zScale, alpha);
		}
	}

	private record SelectedAnimation(AnimationState state, BedrockAnimation animation, float seconds) {
	}

	private record BedrockAnimation(String name, boolean loop, float length, Map<String, BoneAnimation> bones) {
		static BedrockAnimation read(String name, JsonObject json) {
			boolean loop = json.has("loop") ? json.get("loop").getAsBoolean() : shouldLoopByDefault(name);
			float length = json.has("animation_length") ? json.get("animation_length").getAsFloat() : 1.0F;
			Map<String, BoneAnimation> bones = new HashMap<>();
			JsonObject boneRoot = json.getAsJsonObject("bones");
			if (boneRoot != null) {
				for (Map.Entry<String, JsonElement> entry : boneRoot.entrySet()) {
					bones.put(entry.getKey(), BoneAnimation.read(entry.getValue().getAsJsonObject()));
				}
			}
			return new BedrockAnimation(name, loop, length, bones);
		}

		private static boolean shouldLoopByDefault(String name) {
			return name.endsWith("_idle") || name.endsWith("_walk") || name.endsWith("_run") || name.endsWith("_swim") || name.endsWith("_elytra_fly") || name.endsWith(".walk");
		}

		float time(float seconds) {
			if (length <= 0.0F) {
				return 0.0F;
			}
			return loop ? seconds % length : Math.min(seconds, length);
		}
	}

	private record BoneAnimation(Channel rotation, Channel position, Channel scale) {
		static BoneAnimation read(JsonObject json) {
			return new BoneAnimation(Channel.read(json.get("rotation")), Channel.read(json.get("position")), Channel.read(json.get("scale")));
		}

		float[] rotationAt(float time, float headPitch, float netHeadYaw) {
			return rotation == null ? null : rotation.valueAt(time, headPitch, netHeadYaw);
		}

		float[] positionAt(float time, float headPitch, float netHeadYaw) {
			return position == null ? null : position.valueAt(time, headPitch, netHeadYaw);
		}

		float[] scaleAt(float time, float headPitch, float netHeadYaw) {
			return scale == null ? null : scale.valueAt(time, headPitch, netHeadYaw);
		}
	}

	private static final class Channel {
		private final Map<Float, FrameValue> frames;

		private Channel(Map<Float, FrameValue> frames) {
			this.frames = frames;
		}

		static Channel read(JsonElement element) {
			if (element == null || element.isJsonNull()) {
				return null;
			}
			Map<Float, FrameValue> frames = new HashMap<>();
			if (element.isJsonArray()) {
				frames.put(0.0F, FrameValue.read(element.getAsJsonArray()));
			} else {
				JsonObject object = element.getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
					frames.put(Float.parseFloat(entry.getKey()), FrameValue.read(entry.getValue().getAsJsonArray()));
				}
			}
			return new Channel(frames);
		}

		float[] valueAt(float time, float headPitch, float netHeadYaw) {
			float beforeTime = Float.NEGATIVE_INFINITY;
			float afterTime = Float.POSITIVE_INFINITY;
			for (float frameTime : frames.keySet()) {
				if (frameTime <= time && frameTime > beforeTime) {
					beforeTime = frameTime;
				}
				if (frameTime >= time && frameTime < afterTime) {
					afterTime = frameTime;
				}
			}
			if (beforeTime == Float.NEGATIVE_INFINITY) {
				beforeTime = afterTime;
			}
			if (afterTime == Float.POSITIVE_INFINITY) {
				afterTime = beforeTime;
			}
			float[] before = frames.get(beforeTime).resolve(headPitch, netHeadYaw);
			float[] after = frames.get(afterTime).resolve(headPitch, netHeadYaw);
			if (beforeTime == afterTime) {
				return before;
			}
			float progress = (time - beforeTime) / (afterTime - beforeTime);
			return new float[]{lerp(before[0], after[0], progress), lerp(before[1], after[1], progress), lerp(before[2], after[2], progress)};
		}
	}

	private record FrameValue(JsonArray raw) {
		static FrameValue read(JsonArray raw) {
			return new FrameValue(raw);
		}

		float[] resolve(float headPitch, float netHeadYaw) {
			return new float[]{resolve(raw.get(0), headPitch, netHeadYaw), resolve(raw.get(1), headPitch, netHeadYaw), resolve(raw.get(2), headPitch, netHeadYaw)};
		}

		private static float resolve(JsonElement value, float headPitch, float netHeadYaw) {
			if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
				return evaluate(value.getAsString(), headPitch, netHeadYaw);
			}
			return value.getAsFloat();
		}
	}

	private static float evaluate(String expression, float headPitch, float netHeadYaw) {
		String prepared = expression.replace("query.head_x_rotation", Float.toString(headPitch)).replace("query.head_y_rotation", Float.toString(netHeadYaw)).replace("query.body_y_rotation", "0").replace(" ", "");
		float result = 0.0F;
		int sign = 1;
		StringBuilder number = new StringBuilder();
		for (int i = 0; i <= prepared.length(); i++) {
			char ch = i < prepared.length() ? prepared.charAt(i) : '+';
			if ((ch == '+' || ch == '-') && number.length() > 0 && !isExponentSign(number)) {
				result += sign * parseExpressionNumber(number.toString());
				number.setLength(0);
				sign = ch == '-' ? -1 : 1;
			} else if (ch == '-' && number.length() == 0) {
				sign = -1;
			} else if (ch != '+') {
				number.append(ch);
			}
		}
		return result;
	}

	private static boolean isExponentSign(StringBuilder number) {
		char previous = number.charAt(number.length() - 1);
		return previous == 'e' || previous == 'E';
	}

	private static float parseExpressionNumber(String value) {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException ignored) {
			return 0.0F;
		}
	}

	private static float lerp(float start, float end, float progress) {
		return start + (end - start) * progress;
	}
}
