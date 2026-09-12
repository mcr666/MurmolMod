/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcr.murmol.client.particle.SpiteffectParticle;
import net.mcr.murmol.client.particle.MurIceParticle;
import net.mcr.murmol.client.particle.EightparticleParticle;
import net.mcr.murmol.client.particle.AstralEffectParticle;

@EventBusSubscriber(Dist.CLIENT)
public class MurmolModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(MurmolModParticleTypes.EIGHTPARTICLE.get(), EightparticleParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.MUR_ICE.get(), MurIceParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.ASTRAL_EFFECT.get(), AstralEffectParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.SPITEFFECT.get(), SpiteffectParticle::provider);
	}
}