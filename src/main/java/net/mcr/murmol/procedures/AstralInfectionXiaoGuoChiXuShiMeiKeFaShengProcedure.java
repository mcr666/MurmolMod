package net.mcr.murmol.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import net.mcr.murmol.init.MurmolModMobEffects;

public class AstralInfectionXiaoGuoChiXuShiMeiKeFaShengProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		entity.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("murmol:astral_infection_damage")))),
				(float) (0.3 * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(MurmolModMobEffects.ASTRAL_INFECTION) ? _livEnt.getEffect(MurmolModMobEffects.ASTRAL_INFECTION).getAmplifier() : 0)));
	}
}