package net.mcr.murmol.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class MurmolSoulItem extends Item {
	public MurmolSoulItem() {
		super(new Item.Properties().stacksTo(16).rarity(Rarity.RARE));
	}
}