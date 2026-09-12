package net.mcr.murmol.feral.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.mcr.murmol.client.model.Modelastraltail;
import net.mcr.murmol.init.MurmolModAttributes;
import net.mcr.murmol.init.MurmolModItems;

/**
 * 星幻护甲尾巴渲染（独立于形态系统）。
 * - RenderLivingEvent.Pre：生物胸部护甲带 mod:enableastraltail 标签时渲染星幻尾巴
 * - RenderPlayerEvent.Pre：玩家穿戴星幻胸甲时渲染星幻尾巴
 */
@EventBusSubscriber(Dist.CLIENT)
public class AstralTailArmorRenderer {

	private static final ResourceLocation ASTRAL_TAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/astralarmor.png");

	private static EntityModel<?> astralTailModel;

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Modelastraltail.LAYER_LOCATION, Modelastraltail::createBodyLayer);
	}

	@SubscribeEvent
	public static void bakeModels(EntityRenderersEvent.AddLayers event) {
		astralTailModel = new Modelastraltail(event.getEntityModels().bakeLayer(Modelastraltail.LAYER_LOCATION));
	}

	@SubscribeEvent
	public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
		LivingEntity entity = event.getEntity();
		if (!shouldRenderTail(entity))
			return;
		renderTail(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), entity, event.getPartialTick());
	}

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		Player player = event.getEntity();
		// 仅在非野性形态下渲染护甲尾巴（野性形态用自己的尾巴）
		if (net.mcr.murmol.feral.FeralFormManager.getForm(player).isFeral())
			return;
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!chest.is(MurmolModItems.ASTRAL_ARMOR_CHESTPLATE.get()))
			return;
		renderTail(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), player, event.getPartialTick());
	}

	private static boolean shouldRenderTail(LivingEntity entity) {
		// ASTRALTAILIO 属性为 1 时渲染
		if (entity.getAttribute(MurmolModAttributes.ASTRALTAILIO) != null
				&& entity.getAttribute(MurmolModAttributes.ASTRALTAILIO).getValue() > 0.5) {
			return true;
		}
		// 或胸部护甲带 enableastraltail 标签
		ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
		return chest.is(ItemTags.create(ResourceLocation.parse("mod:enableastraltail")));
	}

	private static void renderTail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
			LivingEntity entity, float partialTick) {
		if (astralTailModel == null)
			return;
		float ageInTicks = entity.tickCount + partialTick;
		@SuppressWarnings("unchecked")
		EntityModel<net.minecraft.world.entity.Entity> rawTail = (EntityModel<net.minecraft.world.entity.Entity>) astralTailModel;
		rawTail.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
		VertexConsumer buffer = bufferSource.getBuffer(astralTailModel.renderType(ASTRAL_TAIL_TEXTURE));
		astralTailModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, -1);
	}
}
