package mcr.murmol.potion;

import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.EffectCure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import mcr.murmol.init.MurmolModMobEffects;

import java.util.Set;

public class AstralInfectionMobEffect extends MobEffect {
	public AstralInfectionMobEffect() {
		super(MobEffectCategory.HARMFUL, -6749953);
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
		cures.add(EffectCures.MILK);
		cures.add(EffectCures.PROTECTED_BY_TOTEM);
		cures.add(EffectCures.HONEY);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		// 每 20 tick（1 秒）结算一次伤害
		return duration % 20 == 0;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		// 每刻结算星幻感染伤害：基础 1 × (感染等级+1)
		entity.hurt(new DamageSource(entity.level().holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("murmol:astral_infection_damage")))),
				(float) (1.0 * (entity.hasEffect(MurmolModMobEffects.ASTRAL_INFECTION) ? entity.getEffect(MurmolModMobEffects.ASTRAL_INFECTION).getAmplifier() + 1 : 1)));
		return super.applyEffectTick(entity, amplifier);
	}
}