package net.mcr.murmol.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class FragrantCorsageItem extends Item {
	public FragrantCorsageItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	}
}