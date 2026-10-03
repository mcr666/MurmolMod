/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import mcr.murmol.block.*;
import mcr.murmol.MurmolMod;

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
	// 两仪：太极图形态开关管理方块，右键打开 GUI
	public static final DeferredBlock<Block> TWO_PRINCIPLES = REGISTRY.register("two_principles",
			() -> new mcr.murmol.block.TwoPrinciplesBlock(Block.Properties.of()
					.mapColor(net.minecraft.world.level.material.MapColor.STONE)
					.strength(1.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.STONE)
					.noOcclusion()
					.isSuffocating((state, level, pos) -> false)
					.isViewBlocking((state, level, pos) -> false)));

	// 星辉火把/灯笼：发光粒子用灵魂火火焰（蓝青色），配合蓝紫染色贴图呈现蓝紫光
	public static final DeferredBlock<Block> ASTRAL_TORCH = REGISTRY.register("astral_torch",
			() -> new net.minecraft.world.level.block.TorchBlock(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
					Block.Properties.of().noCollission().instabreak().randomTicks()
							.sound(net.minecraft.world.level.block.SoundType.WOOD).lightLevel(state -> 14)
							.pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
	public static final DeferredBlock<Block> ASTRAL_WALL_TORCH = REGISTRY.register("astral_wall_torch",
			() -> new net.minecraft.world.level.block.WallTorchBlock(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
					Block.Properties.of().noCollission().instabreak().randomTicks()
							.sound(net.minecraft.world.level.block.SoundType.WOOD).lightLevel(state -> 14)
							.pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
	public static final DeferredBlock<Block> ASTRAL_LANTERN = REGISTRY.register("astral_lantern",
			() -> new net.minecraft.world.level.block.LanternBlock(Block.Properties.of()
					.mapColor(net.minecraft.world.level.material.MapColor.METAL).forceSolidOn().strength(3.5F)
					.sound(net.minecraft.world.level.block.SoundType.LANTERN).lightLevel(state -> 15).noCollission()));
	public static final DeferredBlock<Block> ASTRAL_PLANKS_SLAB = REGISTRY.register("astral_planks_slab",
			() -> new net.minecraft.world.level.block.SlabBlock(Block.Properties.of()
					.mapColor(net.minecraft.world.level.material.MapColor.WOOD).sound(net.minecraft.world.level.block.SoundType.WOOD)
					.strength(3f, 10f).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block> ASTRAL_PLANKS_STAIRS = REGISTRY.register("astral_planks_stairs",
			() -> new net.minecraft.world.level.block.StairBlock(ASTRAL_PLANKS.get().defaultBlockState(),
					Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOD)
							.sound(net.minecraft.world.level.block.SoundType.WOOD).strength(3f, 10f)
							.requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block> STRIPPED_ASTRAL_LOG = REGISTRY.register("stripped_astral_log",
			() -> new net.minecraft.world.level.block.RotatedPillarBlock(Block.Properties.of()
					.sound(net.minecraft.world.level.block.SoundType.WOOD).strength(3f, 10f)
					.requiresCorrectToolForDrops()));

	// 照妖镜方块：类似物品展示框，可贴六面放置（物品潜行右键放置）
	public static final DeferredBlock<Block> TRUTH_MIRROR = REGISTRY.register("truth_mirror",
			() -> new mcr.murmol.block.TruthMirrorBlock(Block.Properties.of()
					.mapColor(net.minecraft.world.level.material.MapColor.METAL)
					.strength(0.3F).sound(net.minecraft.world.level.block.SoundType.AMETHYST)
					.noOcclusion().forceSolidOn()
					.isSuffocating((state, level, pos) -> false)
					.isViewBlocking((state, level, pos) -> false)));

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