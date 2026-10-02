/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package mcr.murmol.init;

import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import mcr.murmol.client.particle.SpiteffectParticle;
import mcr.murmol.client.particle.MurIceParticle;
import mcr.murmol.client.particle.EightparticleParticle;
import mcr.murmol.client.particle.AstralEffectParticle;
import mcr.murmol.client.particle.AstralBurstParticle;
import mcr.murmol.client.particle.FengPanEdgeParticle;
import mcr.murmol.client.particle.RiichiBurstParticle;

@EventBusSubscriber(Dist.CLIENT)
public class MurmolModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(MurmolModParticleTypes.EIGHTPARTICLE.get(), EightparticleParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.MUR_ICE.get(), MurIceParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.ASTRAL_EFFECT.get(), AstralEffectParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.SPITEFFECT.get(), SpiteffectParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.ASTRAL_BURST.get(), AstralBurstParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.FENG_PAN_EDGE.get(), FengPanEdgeParticle::provider);
		event.registerSpriteSet(MurmolModParticleTypes.RIICHI_BURST.get(), RiichiBurstParticle::provider);
	}
}