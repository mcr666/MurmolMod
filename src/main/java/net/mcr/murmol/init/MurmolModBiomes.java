/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
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
		// 收窄气候参数范围：减少与原版群系的重叠，使星幻感染群系出现在温和偏冷、中等侵蚀度的区域
		Climate.ParameterPoint surfacePoint = new Climate.ParameterPoint(
				Climate.Parameter.span(-0.45f, -0.15f),
				Climate.Parameter.span(-0.15f, 0.25f),
				Climate.Parameter.span(-0.15f, 0.15f),
				Climate.Parameter.span(0.10f, 0.40f),
				Climate.Parameter.span(-0.05f, 0.20f),
				Climate.Parameter.span(0.0f, 0.0f), 0);
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