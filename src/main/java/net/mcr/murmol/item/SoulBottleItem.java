package net.mcr.murmol.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

/**
 * 魂之瓶。手持右键唤灵台（台面上摆有完整祭品）可收取对应形态的灵魂物品。
 * 交互逻辑集中在 SpiritTableBlock。
 */
public class SoulBottleItem extends Item {
	public SoulBottleItem() {
		super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
	}
}
