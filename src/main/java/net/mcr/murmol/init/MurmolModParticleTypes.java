/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

import net.mcr.murmol.MurmolMod;

public class MurmolModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, MurmolMod.MODID);
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EIGHTPARTICLE = REGISTRY.register("eightparticle", () -> new SimpleParticleType(true));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MUR_ICE = REGISTRY.register("mur_ice", () -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ASTRAL_EFFECT = REGISTRY.register("astral_effect", () -> new SimpleParticleType(true));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPITEFFECT = REGISTRY.register("spiteffect", () -> new SimpleParticleType(true));
}