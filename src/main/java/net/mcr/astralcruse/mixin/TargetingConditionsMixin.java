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
import net.mcr.murmol.potion.PetrifyMobEffect;

/**
 * 狛犬石像/石化药水隐匿：2 格以外的怪物无法将目标选中。
 */
@Mixin(TargetingConditions.class)
public class TargetingConditionsMixin {

	@Inject(method = "test(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",
			at = @At("HEAD"), cancellable = true)
	private void murmol$statueHide(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if (attacker instanceof Mob
				&& attacker.distanceToSqr(target) > 4.0D
				// 石化对所有生物生效；狛犬石像仅玩家形态有
				&& (PetrifyMobEffect.isPetrified(target)
						|| (target instanceof Player && FeralFormManager.isInStatue(target)))) {
			cir.setReturnValue(false);
		}
	}
}
