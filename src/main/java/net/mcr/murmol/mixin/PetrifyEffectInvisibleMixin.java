package net.mcr.murmol.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import net.mcr.murmol.init.MurmolModMobEffects;

/**
 * 石化效果规则：
 * - 狛犬形态玩家免疫石化（任何施加路径均无效）
 * - 其余目标石化始终不显示粒子（覆盖诅咒石头、饮用/喷溅药水、指令等），
 *   仅隐藏环境粒子，HUD 图标保留（showIcon 不变）
 */
@Mixin(LivingEntity.class)
public abstract class PetrifyEffectInvisibleMixin {

	@Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
			at = @At("HEAD"), cancellable = true)
	private void murmol$petrifyRules(MobEffectInstance instance, Entity source, CallbackInfoReturnable<Boolean> cir) {
		if (instance.getEffect() != MurmolModMobEffects.PETRIFY.get())
			return;
		// 狛犬形态免疫石化：直接施加失败（不进效果链，不触发 onEffectStarted）
		if ((Object) this instanceof net.minecraft.world.entity.player.Player player
				&& net.mcr.murmol.feral.FeralFormManager.getForm(player) == net.mcr.murmol.feral.FeralForms.KOMAINU)
			cir.setReturnValue(false);
		else if (instance.isVisible())
			// 以不可见副本重新施加（新副本不可见，不会再进入本分支，无递归风险）
			cir.setReturnValue(((LivingEntity) (Object) this).addEffect(new MobEffectInstance(instance.getEffect(),
					instance.getDuration(), instance.getAmplifier(), instance.isAmbient(), false, instance.showIcon()), source));
	}
}
