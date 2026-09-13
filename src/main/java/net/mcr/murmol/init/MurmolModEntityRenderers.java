/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import net.mcr.murmol.client.renderer.PepperCHRenderer;
import net.mcr.murmol.client.renderer.AstralDrakeRenderer;
import net.mcr.murmol.client.renderer.AlfarRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class MurmolModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(MurmolModEntities.XIAO_HUI_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(MurmolModEntities.ICE_BOLT.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(MurmolModEntities.PEPPER_CH.get(), PepperCHRenderer::new);
		event.registerEntityRenderer(MurmolModEntities.PETAL_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(MurmolModEntities.ALFAR.get(), AlfarRenderer::new);
		event.registerEntityRenderer(MurmolModEntities.ALFA_SPIT_PROJ.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(MurmolModEntities.ASTRAL_DRAKE.get(), AstralDrakeRenderer::new);
	}
}