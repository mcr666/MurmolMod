package mcr.murmol.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 工作台修复配方：耐久不满的鱼泡 + 一个水瓶（minecraft:potion），
 * 消耗一个水瓶并恢复 REPAIR_AMOUNT 点耐久。仅当鱼泡耐久不满时可合成。
 */
public class FishBubbleRepairRecipe extends CustomRecipe {

	public FishBubbleRepairRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		ItemStack bubble = ItemStack.EMPTY;
		int bottles = 0;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty())
				continue;
			if (stack.getItem() instanceof WaterBubbleHelmetItem) {
				if (!bubble.isEmpty())
					return false; // 只允许一个鱼泡
				bubble = stack;
			} else if (stack.is(Items.POTION)) {
				bottles++;
			} else {
				return false; // 出现无关物品
			}
		}
		// 恰好一个鱼泡 + 一个水瓶，且鱼泡耐久不满
		return !bubble.isEmpty() && bottles == 1 && bubble.getDamageValue() > 0;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		ItemStack bubble = ItemStack.EMPTY;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof WaterBubbleHelmetItem) {
				bubble = stack;
				break;
			}
		}
		if (bubble.isEmpty())
			return ItemStack.EMPTY;
		ItemStack out = bubble.copy();
		out.setDamageValue(Math.max(0, out.getDamageValue() - WaterBubbleHelmetItem.REPAIR_AMOUNT));
		return out;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FishBubbleRepairRecipeSerializer.INSTANCE;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return new ItemStack(mcr.murmol.init.MurmolModItems.FISH_BUBBLE.get());
	}
}
