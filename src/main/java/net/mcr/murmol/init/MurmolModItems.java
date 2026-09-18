/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcr.murmol.item.*;
import net.mcr.murmol.MurmolMod;

public class MurmolModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MurmolMod.MODID);
	public static final DeferredItem<Item> MAOCRY_BLESSING;
	public static final DeferredItem<Item> XIAOHUI;
	public static final DeferredItem<Item> ICE_FLOWER;
	public static final DeferredItem<Item> ICE_BOLT_ITEM;
	public static final DeferredItem<Item> ICE_WAND;
	public static final DeferredItem<Item> MAGIC_CRYSTAL;
	public static final DeferredItem<Item> MAGIC_CRYSTAL_CLUSTER;
	public static final DeferredItem<Item> ICE_FLOWER_WILD;
	public static final DeferredItem<Item> ICE_AND_FIRE_SWORD;
	public static final DeferredItem<Item> FROST_INGOTM;
	public static final DeferredItem<Item> ICE_SHARP;
	public static final DeferredItem<Item> ICE_SHARP_ORE;
	public static final DeferredItem<Item> FROST_ARMOR_HELMET;
	public static final DeferredItem<Item> FROST_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> FROST_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> FROST_ARMOR_BOOTS;
	public static final DeferredItem<Item> FROST_SWORD;
	public static final DeferredItem<Item> FROST_PICKAXE;
	public static final DeferredItem<Item> SPIRIT_TABLE;
	public static final DeferredItem<Item> ASTRAL_DIRT;
	public static final DeferredItem<Item> ASTRAL_STONE;
	public static final DeferredItem<Item> COBBLED_ASTRAL_STONE;
	public static final DeferredItem<Item> ASTRAL_ORE;
	public static final DeferredItem<Item> ASTRAL_ORE_SHARD;
	public static final DeferredItem<Item> ASTRAL_INGOT;
	public static final DeferredItem<Item> PEPPER_CH_SPAWN_EGG;
	public static final DeferredItem<Item> ASTRAL_LOG;
	public static final DeferredItem<Item> ASTRAL_PLANKS;
	public static final DeferredItem<Item> ASTRAL_LEAF;
	public static final DeferredItem<Item> MURMOL_TALE;
	public static final DeferredItem<Item> MURMOL_SOUL;
	public static final DeferredItem<Item> THE_ASTRAL_TOME;
	public static final DeferredItem<Item> ASTRAL_ARMOR_HELMET;
	public static final DeferredItem<Item> ASTRAL_ARMOR_CHESTPLATE;
	public static final DeferredItem<Item> ASTRAL_ARMOR_LEGGINGS;
	public static final DeferredItem<Item> ASTRAL_ARMOR_BOOTS;
	public static final DeferredItem<Item> BLOOMING_BLADE;
	public static final DeferredItem<Item> PETAL;
	public static final DeferredItem<Item> BLOOMING_BLADE_FULL;
	public static final DeferredItem<Item> FIRE_BLADE_CLAW;
	public static final DeferredItem<Item> CH_SOUL;
	public static final DeferredItem<Item> PETALFALL_VERNAL_SOUL;
	public static final DeferredItem<Item> MAGIC_SHARD;
	public static final DeferredItem<Item> ICE_FISH;
	public static final DeferredItem<Item> TOTEMOF_FALLEN;
	public static final DeferredItem<Item> MANGO;
	public static final DeferredItem<Item> MANGO_ASTRAL;
	public static final DeferredItem<Item> MANGO_GOLDEN;
	public static final DeferredItem<Item> ALFAR_SPAWN_EGG;
	public static final DeferredItem<Item> ALFAR_SHRINE;
	public static final DeferredItem<Item> ALFAR_SPIT;
	public static final DeferredItem<Item> ASTRAL_DRAKE_SPAWN_EGG;
	public static final DeferredItem<Item> ASTRAL_BLOCK;
	public static final DeferredItem<Item> ASTRAL_MATRIX;
	public static final DeferredItem<Item> ASTRAL_CORE;
	public static final DeferredItem<Item> TREE_HEART;
	public static final DeferredItem<Item> FRAGRANT_CORSAGE;
	public static final DeferredItem<Item> MURMOL;
	public static final DeferredItem<Item> MOSS_SOUL;
	public static final DeferredItem<Item> MANGO_GOLDEN_ENCHANT;
	static {
		MAOCRY_BLESSING = REGISTRY.register("maocry_blessing", MaocryBlessingItem::new);
		XIAOHUI = REGISTRY.register("xiaohui", XiaohuiItem::new);
		ICE_FLOWER = REGISTRY.register("ice_flower", IceFlowerItem::new);
		ICE_BOLT_ITEM = REGISTRY.register("ice_bolt_item", IceBoltItemItem::new);
		ICE_WAND = REGISTRY.register("ice_wand", IceWandItem::new);
		MAGIC_CRYSTAL = REGISTRY.register("magic_crystal", MagicCrystalItem::new);
		MAGIC_CRYSTAL_CLUSTER = block(MurmolModBlocks.MAGIC_CRYSTAL_CLUSTER, new Item.Properties().rarity(Rarity.UNCOMMON));
		ICE_FLOWER_WILD = block(MurmolModBlocks.ICE_FLOWER_WILD);
		ICE_AND_FIRE_SWORD = REGISTRY.register("ice_and_fire_sword", IceAndFireSwordItem::new);
		FROST_INGOTM = REGISTRY.register("frost_ingotm", FrostIngotmItem::new);
		ICE_SHARP = REGISTRY.register("ice_sharp", IceSharpItem::new);
		ICE_SHARP_ORE = block(MurmolModBlocks.ICE_SHARP_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));
		FROST_ARMOR_HELMET = REGISTRY.register("frost_armor_helmet", FrostArmorItem.Helmet::new);
		FROST_ARMOR_CHESTPLATE = REGISTRY.register("frost_armor_chestplate", FrostArmorItem.Chestplate::new);
		FROST_ARMOR_LEGGINGS = REGISTRY.register("frost_armor_leggings", FrostArmorItem.Leggings::new);
		FROST_ARMOR_BOOTS = REGISTRY.register("frost_armor_boots", FrostArmorItem.Boots::new);
		FROST_SWORD = REGISTRY.register("frost_sword", FrostSwordItem::new);
		FROST_PICKAXE = REGISTRY.register("frost_pickaxe", FrostPickaxeItem::new);
		SPIRIT_TABLE = block(MurmolModBlocks.SPIRIT_TABLE);
		ASTRAL_DIRT = block(MurmolModBlocks.ASTRAL_DIRT);
		ASTRAL_STONE = block(MurmolModBlocks.ASTRAL_STONE);
		COBBLED_ASTRAL_STONE = block(MurmolModBlocks.COBBLED_ASTRAL_STONE);
		ASTRAL_ORE = block(MurmolModBlocks.ASTRAL_ORE, new Item.Properties().rarity(Rarity.RARE).fireResistant());
		ASTRAL_ORE_SHARD = REGISTRY.register("astral_ore_shard", AstralOreShardItem::new);
		ASTRAL_INGOT = REGISTRY.register("astral_ingot", AstralIngotItem::new);
		PEPPER_CH_SPAWN_EGG = REGISTRY.register("pepper_ch_spawn_egg", () -> new DeferredSpawnEggItem(MurmolModEntities.PEPPER_CH, -13312, -52, new Item.Properties()));
		ASTRAL_LOG = block(MurmolModBlocks.ASTRAL_LOG);
		ASTRAL_PLANKS = block(MurmolModBlocks.ASTRAL_PLANKS);
		ASTRAL_LEAF = block(MurmolModBlocks.ASTRAL_LEAF);
		MURMOL_TALE = REGISTRY.register("murmol_tale", MurmolTaleItem::new);
		MURMOL_SOUL = REGISTRY.register("murmol_soul", MurmolSoulItem::new);
		THE_ASTRAL_TOME = REGISTRY.register("the_astral_tome", TheAstralTomeItem::new);
		ASTRAL_ARMOR_HELMET = REGISTRY.register("astral_armor_helmet", AstralArmorItem.Helmet::new);
		ASTRAL_ARMOR_CHESTPLATE = REGISTRY.register("astral_armor_chestplate", AstralArmorItem.Chestplate::new);
		ASTRAL_ARMOR_LEGGINGS = REGISTRY.register("astral_armor_leggings", AstralArmorItem.Leggings::new);
		ASTRAL_ARMOR_BOOTS = REGISTRY.register("astral_armor_boots", AstralArmorItem.Boots::new);
		BLOOMING_BLADE = REGISTRY.register("blooming_blade", BloomingBladeItem::new);
		PETAL = REGISTRY.register("petal", PetalItem::new);
		BLOOMING_BLADE_FULL = REGISTRY.register("blooming_blade_full", BloomingBladeFullItem::new);
		FIRE_BLADE_CLAW = REGISTRY.register("fire_blade_claw", FireBladeClawItem::new);
		CH_SOUL = REGISTRY.register("ch_soul", ChSoulItem::new);
		PETALFALL_VERNAL_SOUL = REGISTRY.register("petalfall_vernal_soul", PetalfallVernalSoulItem::new);
		MAGIC_SHARD = REGISTRY.register("magic_shard", MagicShardItem::new);
		ICE_FISH = REGISTRY.register("ice_fish", IceFishItem::new);
		TOTEMOF_FALLEN = REGISTRY.register("totemof_fallen", TotemofFallenItem::new);
		MANGO = REGISTRY.register("mango", MangoItem::new);
		MANGO_ASTRAL = REGISTRY.register("mango_astral", MangoAstralItem::new);
		MANGO_GOLDEN = REGISTRY.register("mango_golden", MangoGoldenItem::new);
		ALFAR_SPAWN_EGG = REGISTRY.register("alfar_spawn_egg", () -> new DeferredSpawnEggItem(MurmolModEntities.ALFAR, -6737152, -13421824, new Item.Properties()));
		ALFAR_SHRINE = block(MurmolModBlocks.ALFAR_SHRINE, new Item.Properties().rarity(Rarity.EPIC));
		ALFAR_SPIT = REGISTRY.register("alfar_spit", AlfarSpitItem::new);
		ASTRAL_DRAKE_SPAWN_EGG = REGISTRY.register("astral_drake_spawn_egg", () -> new DeferredSpawnEggItem(MurmolModEntities.ASTRAL_DRAKE, -13408513, -16724788, new Item.Properties()));
		ASTRAL_BLOCK = block(MurmolModBlocks.ASTRAL_BLOCK, new Item.Properties().rarity(Rarity.RARE).fireResistant());
		ASTRAL_MATRIX = REGISTRY.register("astral_matrix", AstralMatrixItem::new);
		ASTRAL_CORE = REGISTRY.register("astral_core", AstralCoreItem::new);
		TREE_HEART = REGISTRY.register("tree_heart", TreeHeartItem::new);
		FRAGRANT_CORSAGE = REGISTRY.register("fragrant_corsage", FragrantCorsageItem::new);
		MURMOL = REGISTRY.register("murmol", MurmolItem::new);
		MOSS_SOUL = REGISTRY.register("moss_soul", LeafSoulItem::new);
		MANGO_GOLDEN_ENCHANT = REGISTRY.register("mango_golden_enchant", MangoGoldenEnchantItem::new);
	}

	// Start of user code block custom items
	// End of user code block custom items

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}