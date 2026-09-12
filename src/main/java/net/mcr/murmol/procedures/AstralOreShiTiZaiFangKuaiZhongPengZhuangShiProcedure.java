package net.mcr.murmol.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcr.murmol.init.MurmolModMobEffects;

public class AstralOreShiTiZaiFangKuaiZhongPengZhuangShiProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MurmolModMobEffects.ASTRAL_INFECTION, 100, 1));
	}
}