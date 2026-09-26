package net.mcr.astralcruse.mixin;

import net.mcr.astralcruse.events.FeralPlayerDimensions;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FeralPlayerDimensionsMixin {
	private static final float FERAL_STANDING_HEIGHT = 1.1F;
	private static final float FERAL_STANDING_EYE_HEIGHT = 1.0F;
	private static final float FERAL_CROUCH_HEIGHT = 0.9F;
	private static final float FERAL_CROUCH_EYE_HEIGHT = 0.82F;

	@Inject(method = "getDefaultDimensions", at = @At("RETURN"), cancellable = true)
	private void astralCruse$applyFeralDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		Player player = (Player) (Object) this;
		// 固定碰撞箱高度的形态（如文鳐）：所有 Pose 统一高度，蹲下/趴下不再改变
		float fixedHeight = net.mcr.murmol.feral.FeralFormManager.getForm(player).getHitboxHeight();
		if (fixedHeight > 0) {
			cir.setReturnValue(resizeKeepingAttachments(cir.getReturnValue(), fixedHeight, fixedHeight * 0.85F));
			return;
		}
		if (!FeralPlayerDimensions.isFeral(player)) {
			return;
		}
		EntityDimensions vanilla = cir.getReturnValue();
		if (pose == Pose.STANDING) {
			cir.setReturnValue(resizeKeepingAttachments(vanilla, FERAL_STANDING_HEIGHT, FERAL_STANDING_EYE_HEIGHT));
		} else if (FeralPlayerDimensions.usesFeralCrouchDimensions(pose)) {
			cir.setReturnValue(resizeKeepingAttachments(vanilla, FERAL_CROUCH_HEIGHT, FERAL_CROUCH_EYE_HEIGHT));
		}
	}

	private static EntityDimensions resizeKeepingAttachments(EntityDimensions vanilla, float height, float eyeHeight) {
		return vanilla.scale(1.0F, height / vanilla.height()).withEyeHeight(Math.min(height, eyeHeight));
	}
}
