package net.mcr.murmol.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;

public class MangoAstralItem extends Item {
	public MangoAstralItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.5f).build()));
	}
}