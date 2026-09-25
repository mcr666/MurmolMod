package net.mcr.astralcruse.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import net.mcr.murmol.feral.FeralFormManager;

/**
 * 狛犬石像状态隐匿：2 格以外的怪物无法将石像状态的玩家选为目标。
 */
@Mixin(TargetingConditions.class)
public class TargetingConditionsMixin {

	@Inject(method = "test(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",
			at = @At("HEAD"), cancellable = true)
	private void murmol$statueHide(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if (attacker instanceof Mob
				&& target instanceof Player
				&& FeralFormManager.isInStatue(target)
				&& attacker.distanceToSqr(target) > 4.0D) {
			cir.setReturnValue(false);
		}
	}
}
