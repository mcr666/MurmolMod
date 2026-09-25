# MurmolMod Workspace Record

Generated: 2026-07-20

## Project Identity

- Workspace path: `D:\McreatorWorkspaces\MurmolMod`
- Project type: MCreator generated Minecraft mod workspace
- Mod name: `Murmol`
- Mod ID: `murmol`
- Package root: `net.mcr.murmol`
- Version in Gradle: `1.0`
- Version in `neoforge.mods.toml`: `1.0.0`
- Author: `Maocry`
- Credits: `Trae.ai`
- Display URL: `https://bilibili.com`
- License: `Not specified`

## Target Runtime

- Minecraft: `1.21.1`
- NeoForge: `21.1.232`
- Loader: `javafml`
- Loader version range: `[4,)`
- Java toolchain: Java 21
- Resource pack format: `48`

## Build Configuration

- Main build file: `build.gradle`
- Extra MCreator Gradle file: `mcreator.gradle`
- Gradle wrapper: `gradlew`, `gradlew.bat`, `gradle/wrapper/`
- Archive base name: `modid`
- Built jar observed: `build/libs/modid-1.0.jar`

Gradle properties:

- `org.gradle.jvmargs=-Xmx1G`
- `org.gradle.daemon=true`
- `org.gradle.parallel=true`
- `org.gradle.caching=true`
- `org.gradle.configuration-cache=true`

Configured repositories:

- `mavenLocal()`
- `gradlePluginPortal()`
- `https://maven.neoforged.net/releases`

## Main Directory Map

- `.mcreator/` - MCreator workspace metadata.
- `elements/` - MCreator element definition files. Current count: 89 `.json` files.
- `src/main/java/net/mcr/murmol/` - Java source code.
- `src/main/resources/` - Minecraft assets, data packs, metadata, mixin config, and logo.
- `models/` - Additional model workspace assets.
- `gradle/` - Gradle wrapper files.
- `build/` - Generated build output and caches. Usually reproducible.
- `run/` - Local Minecraft run directory, configs, mods, logs, and saves. Usually machine-local.

## Source File Inventory

Files under `src/`:

- `.json`: 157
- `.java`: 100
- `.png`: 49
- `.ogg`: 3
- `.cfg`: 1
- `.toml`: 1
- `.mcmeta`: 1
- `.nbt`: 1

Java package areas:

- `procedures`: 24 files
- `item`: 15 files
- `init`: 15 files
- `mixin`: 10 files
- `block`: 10 files
- `entity`: 4 files
- `client/model`: 4 files
- `network`: 3 files
- `client/particle`: 3 files
- `client/model/animations`: 3 files
- root package files: 3 files
- `potion`: 2 files
- `client/renderer`: 2 files
- `world/inventory`: 1 file
- `client/gui`: 1 file

## Key Java Entry Points

- `src/main/java/net/mcr/murmol/MurmolMod.java`
- `src/main/java/net/mcr/murmol/MurmolModPlayerAnimationAPI.java`
- `src/main/java/net/mcr/murmol/network/MurmolModVariables.java`

Registration classes in `init/`:

- `MurmolModAnimatedModels.java`
- `MurmolModBiomes.java`
- `MurmolModBlocks.java`
- `MurmolModEntities.java`
- `MurmolModEntityRenderers.java`
- `MurmolModHumanoidModels.java`
- `MurmolModItems.java`
- `MurmolModMenus.java`
- `MurmolModMobEffects.java`
- `MurmolModModels.java`
- `MurmolModParticles.java`
- `MurmolModParticleTypes.java`
- `MurmolModScreens.java`
- `MurmolModSounds.java`
- `MurmolModTabs.java`

## Resource Areas

Assets under `src/main/resources/assets/murmol/`:

- `blockstates/`
- `lang/`
- `models/`
- `particles/`
- `sounds/`
- `textures/`

Data under `src/main/resources/data/murmol/`:

- `advancement/`
- `bedrock_animations/`
- `damage_type/`
- `loot_table/`
- `neoforge/`
- `recipe/`
- `structure/`
- `worldgen/`

Observed data file groups:

- Recipes: 16 recipe JSON files plus 5 recipe advancement JSON files.
- Loot tables: 10 block loot tables, 1 entity loot table, 1 chest loot table.
- World generation: biome, configured features, placed features, structure, structure set, and template pool files.
- NeoForge biome modifiers: 7 files.
- Structure asset: `src/main/resources/data/murmol/structure/astral_ruin.nbt`
- Bedrock animation data: `src/main/resources/data/murmol/bedrock_animations/playermodel.animation.json`

## Main Mod Content Observed

Blocks and world content:

- Astral stone, dirt, ore, log, leaf, planks, and cobbled astral stone.
- Ice sharp ore.
- Ice flower plant.
- Magic crystal cluster.
- Astral infection biome and related world generation.
- Astral ruins structure.

Items and equipment:

- Maocry's Blessing.
- Murmol Book.
- Magic Crystal.
- Ice Flower.
- Ice Bolt Item.
- Ice Wand.
- Ice and Fire Sword.
- Frost ingot, sword, pickaxe, and armor pieces.
- Astral ore shard and astral ingot.

Entities and effects:

- Feral Cat.
- Pepper CH.
- Ice Bolt projectile.
- XiaoHui projectile.
- Frozen effect.
- Astral Infection effect and custom damage type.

Systems:

- Custom procedures under `procedures/`.
- Player animation API and mixins.
- Custom GUI/menu for Machine of Murmol.
- Custom particles and sounds.

## Localization Notes

- `en_us.json` is readable and contains current English names.
- `zh_cn.json` appears to contain mojibake/encoding-corrupted Chinese text when read from the shell. Before release, verify this file in an editor with the intended encoding and restore UTF-8 text if needed.

## Local/Generated Content Notes

- `build/` contains compiled classes, generated resources, NeoForge artifacts, reports, and caches.
- `run/` contains local test game configs, installed runtime mods, command history, and several Minecraft saves.
- These folders are useful for local testing but are usually not the canonical source of mod content.

## Tooling Notes

- `git` command was not available in the current shell, so repository status could not be checked from CLI.
- `rg --files` was available and used for workspace file discovery.

## Maintenance Checklist

- Keep authoritative source changes in `src/main/java`, `src/main/resources`, `elements`, and `murmol.mcreator`.
- Treat `build/` as generated output unless a specific artifact is intentionally preserved.
- Treat `run/` as local testing state; review carefully before sharing or packaging.
- Keep `build.gradle`, `gradle.properties`, and `settings.gradle` aligned with the intended Minecraft/NeoForge version.
- Re-check `zh_cn.json` encoding before publishing.
