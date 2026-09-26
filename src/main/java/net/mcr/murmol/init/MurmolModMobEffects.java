/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;

import net.mcr.murmol.procedures.CreativeShockZhuangTaiXiaoGuoJieShuShiProcedure;
import net.mcr.murmol.potion.FrozenedMobEffect;
import net.mcr.murmol.potion.CreativeShockMobEffect;
import net.mcr.murmol.potion.AstralInfectionMobEffect;
import net.mcr.murmol.potion.PetrifyMobEffect;
import net.mcr.murmol.MurmolMod;

@EventBusSubscriber
public class MurmolModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, MurmolMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> FROZENED = REGISTRY.register("frozened", FrozenedMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> ASTRAL_INFECTION = REGISTRY.register("astral_infection", AstralInfectionMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> CREATIVE_SHOCK = REGISTRY.register("creative_shock", CreativeShockMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> PETRIFY = REGISTRY.register("petrify", PetrifyMobEffect::new);

	@SubscribeEvent
	public static void onEffectRemoved(MobEffectEvent.Remove event) {
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null) {
			expireEffects(event.getEntity(), effectInstance);
		}
	}

	@SubscribeEvent
	public static void onEffectExpired(MobEffectEvent.Expired event) {
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null) {
			expireEffects(event.getEntity(), effectInstance);
		}
	}

	private static void expireEffects(Entity entity, MobEffectInstance effectInstance) {
		if (effectInstance.getEffect().is(CREATIVE_SHOCK)) {
			CreativeShockZhuangTaiXiaoGuoJieShuShiProcedure.execute(entity);
		}
	}
}