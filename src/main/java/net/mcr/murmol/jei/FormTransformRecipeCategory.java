package net.mcr.murmol.jei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.mcr.murmol.init.MurmolModItems;

/**
 * 幻星秘典配方分类：左侧 2x2 材料，右侧输出对应形态灵魂物品。
 * 仅在 JEI 加载时被实例化（软依赖）。
 */
public class FormTransformRecipeCategory implements IRecipeCategory<FormTransformRecipe> {

	public static final RecipeType<FormTransformRecipe> RECIPE_TYPE =
			RecipeType.create("murmol", "feral_transform", FormTransformRecipe.class);

	private final IDrawable icon;

	public FormTransformRecipeCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableItemStack(new ItemStack(MurmolModItems.THE_ASTRAL_TOME.get()));
	}

	@Override
	public RecipeType<FormTransformRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("jei.murmol.feral_transform");
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public int getWidth() {
		return 118;
	}

	@Override
	public int getHeight() {
		return 54;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FormTransformRecipe recipe, IFocusGroup focuses) {
		// 人类形态（书中配方）：水桶 + 不死图腾 + 绿宝石 + 任意船
		if (recipe.getForm().getTransformMaterials().isEmpty()) {
			IRecipeSlotBuilder water = builder.addSlot(RecipeIngredientRole.INPUT, 10, 10);
			water.addItemStack(new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
			IRecipeSlotBuilder totem = builder.addSlot(RecipeIngredientRole.INPUT, 28, 10);
			totem.addItemStack(new ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING));
			IRecipeSlotBuilder emerald = builder.addSlot(RecipeIngredientRole.INPUT, 10, 28);
			emerald.addItemStack(new ItemStack(net.minecraft.world.item.Items.EMERALD));
			IRecipeSlotBuilder boat = builder.addSlot(RecipeIngredientRole.INPUT, 28, 28);
			boat.addIngredients(net.minecraft.world.item.crafting.Ingredient.of(
					net.minecraft.tags.ItemTags.create(ResourceLocation.parse("minecraft:boats"))));
			return;
		}
		List<ItemStack> materials = recipe.getForm().getTransformMaterials();
		// 左侧 2x2 材料格（与幻星秘典 GUI 的 4 格对应）
		int[][] positions = {{10, 10}, {28, 10}, {10, 28}, {28, 28}};
		for (int i = 0; i < positions.length && i < materials.size(); i++) {
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, positions[i][0], positions[i][1]);
			slot.addItemStack(materials.get(i));
		}
		// 右侧输出：形态灵魂物品
		IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 19);
		output.addItemStack(recipe.getForm().getSoulItem());
		output.addRichTooltipCallback((view, tooltip) ->
				tooltip.add(Component.translatable("jei.murmol.feral_transform.desc")));
	}

	@Override
	public void draw(FormTransformRecipe recipe, IRecipeSlotsView recipeSlotsView, net.minecraft.client.gui.GuiGraphics guiGraphics,
			double mouseX, double mouseY) {
		// 材料 → 输出 的箭头
		guiGraphics.drawString(Minecraft.getInstance().font, "→", 58, 22, 0xFF404040, false);
	}
}
