package net.mcr.murmol.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import net.mcr.murmol.feral.FeralFormRecipes;

/**
 * 配方加载完成时注入以 FeralForm 为事实来源动态生成的配方（残页合成形态灵魂）。
 * 注入的是原版 ShapelessRecipe，可被正常序列化同步给客户端，JEI 也会自动识别。
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

	@Shadow
	private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

	@Shadow
	private Map<ResourceLocation, RecipeHolder<?>> byName;

	@Inject(method = "apply", at = @At("TAIL"))
	private void murmol$addDynamicRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resources,
			CallbackInfo ci) {
		com.google.common.collect.LinkedHashMultimap<RecipeType<?>, RecipeHolder<?>> newByType =
				com.google.common.collect.LinkedHashMultimap.create(byType);
		Map<ResourceLocation, RecipeHolder<?>> newByName = new java.util.HashMap<>(byName);
		for (RecipeHolder<?> holder : FeralFormRecipes.scrapSoulRecipes()) {
			newByType.put(holder.value().getType(), holder);
			newByName.put(holder.id(), holder);
		}
		this.byType = newByType;
		this.byName = ImmutableMap.copyOf(newByName);
	}
}
