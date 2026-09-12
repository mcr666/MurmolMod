package net.mcr.murmol.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class AstralOreShardItem extends Item {
	public AstralOreShardItem() {
		super(new Item.Properties().fireResistant().rarity(Rarity.RARE));
	}
}