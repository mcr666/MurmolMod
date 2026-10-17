package mcr.murmol.item;

import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

/**
 * 鱼泡工作台修复配方的序列化器（无额外 JSON 参数，仅类型 + category）。
 */
public class FishBubbleRepairRecipeSerializer extends SimpleCraftingRecipeSerializer<FishBubbleRepairRecipe> {

	public static final FishBubbleRepairRecipeSerializer INSTANCE = new FishBubbleRepairRecipeSerializer();

	public FishBubbleRepairRecipeSerializer() {
		super(FishBubbleRepairRecipe::new);
	}
}
