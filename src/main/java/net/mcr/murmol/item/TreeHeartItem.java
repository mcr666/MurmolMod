package net.mcr.murmol.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class TreeHeartItem extends Item {
	public TreeHeartItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	}
}