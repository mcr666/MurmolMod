/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;

import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;

import com.mojang.datafixers.util.Pair;

@EventBusSubscriber
public class MurmolModBiomes {
	public static final ResourceLocation OVERWORLD_BIOMESOURCE_PRESET_ID = ResourceLocation.withDefaultNamespace("overworld");
	public static final ResourceLocation NETHER_BIOMESOURCE_PRESET_ID = ResourceLocation.withDefaultNamespace("nether");

	/** 星幻感染群系的静态 Key，供各处群系判定复用（避免每次调用重复解析 ResourceLocation） */
	public static final ResourceKey<Biome> ASTRAL_INFECTION_BIOME = ResourceKey.create(Registries.BIOME,
			ResourceLocation.fromNamespaceAndPath("murmol", "astral_infection_biome"));

	private static boolean BOOTSTRAP_VALIDATION_PASSED = false;

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		BOOTSTRAP_VALIDATION_PASSED = true;
	}

	/**
	 * 出生点兜底搜索：PlayerRespawnLogicMixin 已让原版 ±5 区块螺旋搜索跳过星幻之地，
	 * 但若候选点周边整片都是星幻之地，则原版会退回噪声中心（仍在群系内）。
	 * 此处在服务器启动时检测最终出生点，若落在星幻之地，则向外逐圈搜索（最多 32 圈），
	 * 找到第一个可落脚的正常群系区块并设为出生点。
	 */
	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event) {
		MinecraftServer server = event.getServer();
		ServerLevel level = server.overworld();
		if (level == null)
			return;
		BlockPos spawnPos = level.getSharedSpawnPos();
		if (!level.getBiome(spawnPos).is(ASTRAL_INFECTION_BIOME))
			return;
		ChunkPos center = new ChunkPos(spawnPos);
		for (int r = 1; r <= 32; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r)
						continue;
					BlockPos found = PlayerRespawnLogic.getSpawnPosInChunk(level, new ChunkPos(center.x + dx, center.z + dz));
					if (found != null) {
						((ServerLevelData) server.getWorldData()).setSpawn(found, level.getSharedSpawnAngle());
						return;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		Registry<LevelStem> levelStemTypeRegistry = event.getServer().registryAccess().registryOrThrow(Registries.LEVEL_STEM);
		for (LevelStem levelStem : levelStemTypeRegistry.stream().toList()) {
			Holder<DimensionType> dimensionType = levelStem.type();
			if (dimensionType.is(BuiltinDimensionTypes.NETHER) || dimensionType.is(BuiltinDimensionTypes.OVERWORLD)) {
				if (levelStem.generator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
					((MurmolModNoiseGeneratorSettings) (Object) noiseGenerator.generatorSettings().value()).setmurmolDimensionTypeReference(dimensionType);
				}
			}
		}
	}

	public static SurfaceRules.RuleSource adaptSurfaceRule(SurfaceRules.RuleSource currentRuleSource, Holder<DimensionType> dimensionType) {
		if (dimensionType.is(BuiltinDimensionTypes.OVERWORLD))
			return injectOverworldSurfaceRules(currentRuleSource);
		return currentRuleSource;
	}

	public static <T> Climate.ParameterList<T> adaptPresetParameterList(ResourceLocation idArg, Climate.ParameterList<T> originalList, Function<ResourceKey<Biome>, T> lookup) {
		if (!BOOTSTRAP_VALIDATION_PASSED)
			return originalList;
		if (idArg.equals(OVERWORLD_BIOMESOURCE_PRESET_ID))
			return MurmolModBiomes.modifyOverworldParameterPoints(originalList, lookup);
		return originalList;
	}

	private static SurfaceRules.RuleSource injectOverworldSurfaceRules(SurfaceRules.RuleSource currentRuleSource) {
		List<SurfaceRules.RuleSource> customSurfaceRules = new ArrayList<>();
		customSurfaceRules.add(preliminarySurfaceRule(ASTRAL_INFECTION_BIOME, MurmolModBlocks.ASTRAL_DIRT.get().defaultBlockState(),
				MurmolModBlocks.ASTRAL_DIRT.get().defaultBlockState(), MurmolModBlocks.ASTRAL_STONE.get().defaultBlockState()));
		if (currentRuleSource instanceof SurfaceRules.SequenceRuleSource sequenceRuleSource) {
			customSurfaceRules.addAll(sequenceRuleSource.sequence());
			return SurfaceRules.sequence(customSurfaceRules.toArray(SurfaceRules.RuleSource[]::new));
		} else {
			customSurfaceRules.add(currentRuleSource);
			return SurfaceRules.sequence(customSurfaceRules.toArray(SurfaceRules.RuleSource[]::new));
		}
	}

	public static <T> Climate.ParameterList<T> modifyOverworldParameterPoints(Climate.ParameterList<T> originalList, Function<ResourceKey<Biome>, T> lookup) {
		List<Pair<Climate.ParameterPoint, T>> parameters = new ArrayList<>(originalList.values());
		// 大范围内陆气候格 + offset=0（最高优先级）：
		// offset 实际是第 7 个气候维度（取值 0.0~1.0，采样目标恒为 0，越大距离越远），
		// 0 表示与原版常规群系同优先级，在大气候格内公平竞争即可形成大片连续区域。
		// 注意：不能用负值——负值会作为距离参与计算（平方），导致参数点永远落选。
		// 各气候维度区间宽度已减半（中心不变），使群系生成范围约为原来的一半。
		Climate.ParameterPoint surfacePoint = new Climate.ParameterPoint(
				Climate.Parameter.span(-0.35f, 0.15f),   // 温度：偏冷到温和（原 -0.60~0.40，减半）
				Climate.Parameter.span(-0.20f, 0.20f),   // 湿度：中等区间（原 -0.40~0.40，减半）
				Climate.Parameter.span(0.00f, 0.37f),    // 大陆性：海岸到内陆（原 -0.19~0.55，减半）
				Climate.Parameter.span(-0.45f, 0.22f),   // 侵蚀度：排除尖峰山地（原 -0.78~0.55，减半）
				Climate.Parameter.span(0.0f, 0.0f),      // 深度：仅地表
				Climate.Parameter.span(-1.0f, 1.0f),     // 奇异性：全范围
				0);
		parameters.add(new Pair<>(surfacePoint, lookup.apply(ASTRAL_INFECTION_BIOME)));
		return new Climate.ParameterList<>(parameters);
	}

	private static SurfaceRules.RuleSource preliminarySurfaceRule(ResourceKey<Biome> biomeKey, BlockState groundBlock, BlockState undergroundBlock, BlockState underwaterBlock) {
		return SurfaceRules.ifTrue(SurfaceRules.isBiome(biomeKey),
				SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),
						SurfaceRules.sequence(
								SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
										SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), SurfaceRules.state(groundBlock)), SurfaceRules.state(underwaterBlock))),
								SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR), SurfaceRules.state(undergroundBlock)))));
	}

	public interface MurmolModNoiseGeneratorSettings {
		void setmurmolDimensionTypeReference(Holder<DimensionType> dimensionType);
	}
}