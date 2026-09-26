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
	public static final DeferredBlock<Block> MANGO_BUSH = REGISTRY.register("mango_bush", () -> new MangoBushBlock(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).randomTicks().noCollission().sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
	public static final DeferredBlock<Block> BANZA = REGISTRY.register("banza", () -> new BanzaBlock(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).strength(2.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.DEEPSLATE)));
	public static final DeferredBlock<Block> CURSED_STONE = REGISTRY.register("cursed_stone", () -> new CursedStoneBlock(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).strength(0.8F).sound(net.minecraft.world.level.block.SoundType.STONE)));
	public static final DeferredBlock<Block> CAGE = REGISTRY.register("cage", () -> new CageBlock(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).requiresCorrectToolForDrops().strength(8.0F, 9.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
	// 幻星树苗：种下后长成幻星巨树（astral_infection_biome_tree）；贴图暂用原版金合欢树苗
	public static final DeferredBlock<Block> ASTRAL_SAPLING = REGISTRY.register("astral_sapling",
			MurmolModBlocks::astralSapling);

	private static net.minecraft.world.level.block.SaplingBlock astralSapling() {
		java.util.Optional<net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>>> tree =
				java.util.Optional.of(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
						net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "astral_infection_biome_tree")));
		return new net.minecraft.world.level.block.SaplingBlock(
				new net.minecraft.world.level.block.grower.TreeGrower("astral_sapling", java.util.Optional.empty(), tree, java.util.Optional.empty()),
				Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().randomTicks()
						.instabreak().sound(net.minecraft.world.level.block.SoundType.GRASS)
						.pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
	}
	// End of user code block custom blocks
}