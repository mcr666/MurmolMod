package net.mcr.murmol.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralForms;
import net.mcr.murmol.init.MurmolModItems;

/**
 * JEI 软依赖插件：仅当 JEI 存在时由 JEI 通过 @JeiPlugin 扫描加载，
 * JEI 未安装时此类不会被加载，无任何影响。
 * 注册幻星秘典形态配方：每个形态的 4 格材料组合 → 形态灵魂物品。
 */
@JeiPlugin
public class MurmolJeiPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath("murmol", "jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new FormTransformRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		List<FormTransformRecipe> recipes = new ArrayList<>();
		for (FeralForm form : FeralForms.all()) {
			// 人类形态（变回人类：食用堕落者图腾）也生成一条展示配方
			if (form != FeralForms.HUMAN && form.getTransformMaterials().isEmpty())
				continue;
			recipes.add(new FormTransformRecipe(form));
		}
		registration.addRecipes(FormTransformRecipeCategory.RECIPE_TYPE, recipes);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(MurmolModItems.THE_ASTRAL_TOME.get()),
				FormTransformRecipeCategory.RECIPE_TYPE);
	}
}
