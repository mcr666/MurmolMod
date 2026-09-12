package net.mcr.murmol.potion;

import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.EffectCure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

import net.mcr.murmol.procedures.AstralInfectionXiaoGuoChiXuShiMeiKeFaShengProcedure;

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
		return true;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		AstralInfectionXiaoGuoChiXuShiMeiKeFaShengProcedure.execute(entity.level(), entity);
		return super.applyEffectTick(entity, amplifier);
	}
}