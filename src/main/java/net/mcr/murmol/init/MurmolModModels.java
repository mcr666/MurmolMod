/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcr.murmol.client.model.*;

@EventBusSubscriber(Dist.CLIENT)
public class MurmolModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Modelastral_drake.LAYER_LOCATION, Modelastral_drake::createBodyLayer);
		event.registerLayerDefinition(Modelastralarmor_1.LAYER_LOCATION, Modelastralarmor_1::createBodyLayer);
		event.registerLayerDefinition(Modelunknown.LAYER_LOCATION, Modelunknown::createBodyLayer);
		event.registerLayerDefinition(Modelferal_cat.LAYER_LOCATION, Modelferal_cat::createBodyLayer);
		event.registerLayerDefinition(Modelastralarmor.LAYER_LOCATION, Modelastralarmor::createBodyLayer);
		event.registerLayerDefinition(Modelforest_colossus.LAYER_LOCATION, Modelforest_colossus::createBodyLayer);
	}
}