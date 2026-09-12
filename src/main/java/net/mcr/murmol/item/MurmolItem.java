package net.mcr.murmol.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class MurmolItem extends Item {
	public MurmolItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	}
}