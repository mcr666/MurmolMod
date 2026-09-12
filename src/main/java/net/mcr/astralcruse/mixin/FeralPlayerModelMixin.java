package net.mcr.astralcruse.mixin;

import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class FeralPlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
	private FeralPlayerModelMixin(ModelPart root) {
		super(root);
	}

	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void astralCruse$clearPreviousFeralPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		PlayerModel<?> model = (PlayerModel<?>) (Object) this;
		if (FeralBedrockPlayerAnimator.shouldAnimate(entity)) {
			this.attackTime = 0.0F;
			this.leftArmPose = ArmPose.EMPTY;
			this.rightArmPose = ArmPose.EMPTY;
		}
		model.head.resetPose();
		model.body.resetPose();
		model.rightArm.resetPose();
		model.leftArm.resetPose();
		model.rightLeg.resetPose();
		model.leftLeg.resetPose();
		model.hat.resetPose();
		model.jacket.resetPose();
		model.rightSleeve.resetPose();
		model.leftSleeve.resetPose();
		model.rightPants.resetPose();
		model.leftPants.resetPose();
	}

	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
	private void astralCruse$applyFeralAnimation(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		if (FeralBedrockPlayerAnimator.shouldAnimate(entity)) {
			this.attackTime = 0.0F;
			this.leftArmPose = ArmPose.EMPTY;
			this.rightArmPose = ArmPose.EMPTY;
		}
		FeralBedrockPlayerAnimator.apply((PlayerModel<?>) (Object) this, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}
}
