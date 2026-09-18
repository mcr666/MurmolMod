package net.mcr.murmol.init;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.mcr.murmol.client.renderer.SpiritTableRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class MurmolModBlockEntityRenderers {
	@SubscribeEvent
	public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(MurmolModBlockEntities.SPIRIT_TABLE.get(), SpiritTableRenderer::new);
	}
}
