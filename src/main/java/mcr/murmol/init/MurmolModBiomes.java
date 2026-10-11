/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package mcr.murmol.init;

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

	public static <T> Climate.ParameterList<T> adaptPresetParameterList(ResourceLocation idArg, Climate.ParameterList<T> originalList, Function<ResourceKey<Biome>, T> lookup, int infectionRange) {
		if (!BOOTSTRAP_VALIDATION_PASSED)
			return originalList;
		if (idArg.equals(OVERWORLD_BIOMESOURCE_PRESET_ID))
			return MurmolModBiomes.modifyOverworldParameterPoints(originalList, lookup, infectionRange);
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

	/**
	 * 注入幻星瘟疫群系气候点。四个可竞争气候维度（温度/湿度/大陆性/侵蚀度）的
	 * 区间半宽按规则等级 range（1~10）缩放：
	 * range=10 时半宽系数 1.0（满气候范围），range 越小半宽越窄（线性降到 0.1）。
	 * 现行默认视觉（手动减半）对应 range=2：半宽系数 0.5，与历史参数一致。
	 * 深度固定地表、奇异性全范围、offset=0（与原版常规群系同优先级公平竞争）。
	 */
	public static <T> Climate.ParameterList<T> modifyOverworldParameterPoints(Climate.ParameterList<T> originalList, Function<ResourceKey<Biome>, T> lookup, int infectionRange) {
		List<Pair<Climate.ParameterPoint, T>> parameters = new ArrayList<>(originalList.values());
		float half = MurmolModGameRules.astralInfectionHalfWidth(infectionRange);
		Climate.ParameterPoint surfacePoint = new Climate.ParameterPoint(
				spanHalf(-0.10f, 0.50f, half),   // 温度：中心 -0.10，满半宽 0.50（-0.60~0.40）
				spanHalf(0.00f, 0.40f, half),    // 湿度：中心 0，满半宽 0.40
				spanHalf(0.18f, 0.37f, half),    // 大陆性：中心 0.18，满半宽 0.37（-0.19~0.55）
				spanHalf(-0.115f, 0.665f, half), // 侵蚀度：中心 -0.115，满半宽 0.665（-0.78~0.55）
				Climate.Parameter.span(0.0f, 0.0f),  // 深度：仅地表
				Climate.Parameter.span(-1.0f, 1.0f),  // 奇异性：全范围
				0);
		parameters.add(new Pair<>(surfacePoint, lookup.apply(ASTRAL_INFECTION_BIOME)));
		return new Climate.ParameterList<>(parameters);
	}

	/** 以 center 为中点、fullHalfWidth*scale 为半宽构造气候参数区间，夹到 [-1,1] */
	private static Climate.Parameter spanHalf(float center, float fullHalfWidth, float scale) {
		float hw = fullHalfWidth * scale;
		return Climate.Parameter.span(center - hw, center + hw);
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