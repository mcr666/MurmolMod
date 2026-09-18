/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.mcr.murmol.block.*;
import net.mcr.murmol.MurmolMod;

public class MurmolModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(MurmolMod.MODID);
	public static final DeferredBlock<Block> MAGIC_CRYSTAL_CLUSTER;
	public static final DeferredBlock<Block> ICE_FLOWER_WILD;
	public static final DeferredBlock<Block> ICE_SHARP_ORE;
	public static final DeferredBlock<Block> ASTRAL_DIRT;
	public static final DeferredBlock<Block> ASTRAL_STONE;
	public static final DeferredBlock<Block> COBBLED_ASTRAL_STONE;
	public static final DeferredBlock<Block> ASTRAL_ORE;
	public static final DeferredBlock<Block> ASTRAL_LOG;
	public static final DeferredBlock<Block> ASTRAL_PLANKS;
	public static final DeferredBlock<Block> ASTRAL_LEAF;
	public static final DeferredBlock<Block> ALFAR_SHRINE;
	public static final DeferredBlock<Block> ASTRAL_BLOCK;
	static {
		MAGIC_CRYSTAL_CLUSTER = REGISTRY.register("magic_crystal_cluster", MagicCrystalClusterBlock::new);
		ICE_FLOWER_WILD = REGISTRY.register("ice_flower_wild", IceFlowerPlantBlock::new);
		ICE_SHARP_ORE = REGISTRY.register("ice_sharp_ore", IceSharpOreBlock::new);
		ASTRAL_DIRT = REGISTRY.register("astral_dirt", AstraldirtBlock::new);
		ASTRAL_STONE = REGISTRY.register("astral_stone", AstralstoneBlock::new);
		COBBLED_ASTRAL_STONE = REGISTRY.register("cobbled_astral_stone", CobbledastralstoneBlock::new);
		ASTRAL_ORE = REGISTRY.register("astral_ore", AstralOreBlock::new);
		ASTRAL_LOG = REGISTRY.register("astral_log", AstralLogBlock::new);
		ASTRAL_PLANKS = REGISTRY.register("astral_planks", AstralWoodBlock::new);
		ASTRAL_LEAF = REGISTRY.register("astral_leaf", AstralLeafBlock::new);
		ALFAR_SHRINE = REGISTRY.register("alfar_shrine", AlfarShrineBlock::new);
		ASTRAL_BLOCK = REGISTRY.register("astral_block", AstralBlockBlock::new);
	}
	// Start of user code block custom blocks
	public static final DeferredBlock<Block> SPIRIT_TABLE = REGISTRY.register("spirit_table", () -> new SpiritTableBlock());
	// End of user code block custom blocks
}