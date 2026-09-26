package net.mcr.astralcruse.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import net.mcr.murmol.potion.PetrifyMobEffect;

/**
 * 石化贴图：被石化的生物保留原模型，仅将渲染贴图替换为狛犬石像石头贴图。
 * 注入 LivingEntityRenderer.getRenderType——所有生物模型贴图都经此。
 */
@Mixin(LivingEntityRenderer.class)
public class PetrifyTextureMixin {

	private static final ResourceLocation PETRIFY_TEXTURE = ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/komainu_stone.png");

	@SuppressWarnings("unchecked")
	@Inject(method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;",
			at = @At("HEAD"), cancellable = true)
	private void murmol$petrifyTexture(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing,
			CallbackInfoReturnable<RenderType> cir) {
		if (PetrifyMobEffect.isPetrified(entity)) {
			cir.setReturnValue(RenderType.entityCutoutNoCull(PETRIFY_TEXTURE));
		}
	}
}
