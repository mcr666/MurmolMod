package net.mcr.murmol.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class AstralMatrixItem extends Item {
	public AstralMatrixItem() {
		super(new Item.Properties());
	}

	@Override
	public boolean isPiglinCurrency(ItemStack stack) {
		return true;
	}
}