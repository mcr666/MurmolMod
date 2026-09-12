package net.mcr.murmol.feral.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.client.model.ChenhuangModel;
import net.mcr.murmol.client.model.LeafModel;
import net.mcr.murmol.client.model.LuohongModel;
import net.mcr.murmol.client.model.ModelCHtail;
import net.mcr.murmol.client.model.Modelfurtals;
import net.mcr.murmol.client.model.Modelleaftail;

/**
 * 客户端模型持有器。负责注册形态相关模型层、烘焙身体模型（包装为 PlayerModel）和尾巴模型。
 * 替代原 MurmolModHumanoidModels + MurmolModAnimatedModels 中形态相关部分。
 */
@EventBusSubscriber(Dist.CLIENT)
public class FeralFormModels {

	private static final Map<ResourceLocation, PlayerModel> BODY_MODELS = new HashMap<>();
	private static final Map<ResourceLocation, EntityModel<?>> TAIL_MODELS = new HashMap<>();

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(LuohongModel.LAYER_LOCATION, LuohongModel::createBodyLayer);
		event.registerLayerDefinition(ChenhuangModel.LAYER_LOCATION, ChenhuangModel::createBodyLayer);
		event.registerLayerDefinition(LeafModel.LAYER_LOCATION, LeafModel::createBodyLayer);
		event.registerLayerDefinition(Modelfurtals.LAYER_LOCATION, Modelfurtals::createBodyLayer);
		event.registerLayerDefinition(ModelCHtail.LAYER_LOCATION, ModelCHtail::createBodyLayer);
		event.registerLayerDefinition(Modelleaftail.LAYER_LOCATION, Modelleaftail::createBodyLayer);
	}

	@SubscribeEvent
	public static void bakeModels(EntityRenderersEvent.AddLayers event) {
		bakeBodyModel(event, "modelfurplayermodel", LuohongModel.class);
		bakeBodyModel(event, "model_c_hmodel", ChenhuangModel.class);
		bakeBodyModel(event, "modelleaf", LeafModel.class);

		bakeTailModel(event, "modelfurtals", Modelfurtals.class);
		bakeTailModel(event, "model_c_htail", ModelCHtail.class);
		bakeTailModel(event, "modelleaftail", Modelleaftail.class);
	}

	private static ModelPart getModelField(Object instance, String name) throws Exception {
		var f = instance.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return (ModelPart) f.get(instance);
	}

	private static void bakeBodyModel(EntityRenderersEvent.AddLayers event, String layerName, Class<?> modelClass) {
		try {
			var layer = new net.minecraft.client.model.geom.ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", layerName), "main");
			var root = event.getEntityModels().bakeLayer(layer);
			// 通过反射创建模型实例，提取 head/torso/left_arm 等部件包装为 PlayerModel
			var ctor = modelClass.getConstructor(ModelPart.class);
			var temp = ctor.newInstance(root);
			ModelPart head = getModelField(temp, "head");
			ModelPart torso = getModelField(temp, "torso");
			ModelPart leftArm = getModelField(temp, "left_arm");
			ModelPart rightArm = getModelField(temp, "right_arm");
			ModelPart leftLeg = getModelField(temp, "left_leg");
			ModelPart rightLeg = getModelField(temp, "right_leg");

			var wrapper = new ModelPart(Collections.emptyList(),
					Map.ofEntries(Map.entry("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("head", head), Map.entry("body", torso),
							Map.entry("left_arm", leftArm), Map.entry("right_arm", rightArm),
							Map.entry("left_leg", leftLeg), Map.entry("right_leg", rightLeg),
							Map.entry("left_sleeve", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("right_sleeve", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("left_pants", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("right_pants", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("jacket", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("cloak", new ModelPart(Collections.emptyList(), Collections.emptyMap())),
							Map.entry("ear", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
			BODY_MODELS.put(ResourceLocation.fromNamespaceAndPath("murmol", layerName), new PlayerModel(wrapper, false));
		} catch (Exception e) {
			throw new RuntimeException("Failed to bake body model: " + layerName, e);
		}
	}

	private static void bakeTailModel(EntityRenderersEvent.AddLayers event, String layerName, Class<?> modelClass) {
		try {
			var layer = new net.minecraft.client.model.geom.ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", layerName), "main");
			var root = event.getEntityModels().bakeLayer(layer);
			var ctor = modelClass.getConstructor(ModelPart.class);
			TAIL_MODELS.put(ResourceLocation.fromNamespaceAndPath("murmol", layerName), (EntityModel<?>) ctor.newInstance(root));
		} catch (Exception e) {
			throw new RuntimeException("Failed to bake tail model: " + layerName, e);
		}
	}

	public static PlayerModel getBodyModel(ResourceLocation layer) {
		return BODY_MODELS.get(layer);
	}

	public static EntityModel<?> getTailModel(ResourceLocation layer) {
		return TAIL_MODELS.get(layer);
	}
}
