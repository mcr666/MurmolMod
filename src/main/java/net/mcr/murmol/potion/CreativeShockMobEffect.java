package net.mcr.murmol.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

import net.mcr.murmol.procedures.CreativeShockXiaoGuoChiXuShiMeiKeFaShengProcedure;

public class CreativeShockMobEffect extends MobEffect {
	public CreativeShockMobEffect() {
		super(MobEffectCategory.HARMFUL, -1);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		CreativeShockXiaoGuoChiXuShiMeiKeFaShengProcedure.execute(entity);
		return super.applyEffectTick(entity, amplifier);
	}
}