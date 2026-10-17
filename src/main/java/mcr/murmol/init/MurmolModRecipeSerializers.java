package mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import mcr.murmol.MurmolMod;
import mcr.murmol.item.FishBubbleRepairRecipeSerializer;

public class MurmolModRecipeSerializers {
	public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MurmolMod.MODID);
	public static final DeferredHolder<RecipeSerializer<?>, FishBubbleRepairRecipeSerializer> FISH_BUBBLE_REPAIR =
			REGISTRY.register("fish_bubble_repair", () -> FishBubbleRepairRecipeSerializer.INSTANCE);
}
