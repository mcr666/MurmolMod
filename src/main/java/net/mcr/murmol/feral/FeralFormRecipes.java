package net.mcr.murmol.feral;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMap;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import net.mcr.murmol.MurmolMod;
import net.mcr.murmol.init.MurmolModItems;
import net.mcr.murmol.item.AncientKnowledgeScrapItem;

/**
 * 以 {@link FeralForm} 为唯一事实来源动态生成的配方。
 * 残页合成形态灵魂物品的材料直接取自形态类的 transformMaterials，
 * 新增/修改形态材料时无需再维护数据包 JSON。
 */
public final class FeralFormRecipes {

	private FeralFormRecipes() {}

	/**
	 * 为每个非人类形态生成「古代知识残页 + 摩摩尔之魂 + 形态祭品 → 形态灵魂物品」无序配方。
	 */
	public static List<RecipeHolder<?>> scrapSoulRecipes() {
		List<RecipeHolder<?>> recipes = new ArrayList<>();
		for (FeralForm form : FeralForms.all()) {
			if (!form.isFeral())
				continue;
			NonNullList<Ingredient> ingredients = NonNullList.create();
			// 残页：按 CUSTOM_DATA 中的 FormId 匹配对应形态（非严格，允许附带其他组件）
			ingredients.add(DataComponentIngredient.of(false, AncientKnowledgeScrapItem.forForm(form.getId())));
			ingredients.add(Ingredient.of(MurmolModItems.MURMOL_SOUL.get()));
			for (ItemStack material : form.getTransformMaterials())
				ingredients.add(Ingredient.of(material.getItem()));
			ShapelessRecipe recipe = new ShapelessRecipe("", CraftingBookCategory.MISC,
					form.getSoulItem().copy(), ingredients);
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "scrap_" + form.getId() + "_soul");
			recipes.add(new RecipeHolder<>(id, recipe));
		}
		return recipes;
	}

	/** 将动态配方注入 RecipeManager 的两个索引（由 mixin 在配方加载完成后调用） */
	public static void addTo(ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType,
			ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> byName) {
		for (RecipeHolder<?> holder : scrapSoulRecipes()) {
			byType.put(holder.value().getType(), holder);
			byName.put(holder.id(), holder);
		}
	}
}
