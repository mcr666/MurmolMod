/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcr.murmol.MurmolMod;

public class MurmolModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MurmolMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MURMOL_CX = REGISTRY.register("murmol_cx",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.murmol.murmol_cx")).icon(() -> new ItemStack(MurmolModItems.MAOCRY_BLESSING.get())).displayItems((parameters, tabData) -> {
				tabData.accept(MurmolModItems.MAOCRY_BLESSING.get());
				tabData.accept(MurmolModItems.THE_ASTRAL_TOME.get());
				// 变形系列：灵魂物品与残页放在最前
				tabData.accept(MurmolModItems.MURMOL_SOUL.get());
				tabData.accept(MurmolModItems.LUOHONG_SOUL.get());
				tabData.accept(MurmolModItems.CHEN_HUANG_SOUL.get());
				tabData.accept(MurmolModItems.MOSS_BEAST_SOUL.get());
				tabData.accept(MurmolModItems.SILKMOTH_SOUL.get());
				tabData.accept(MurmolModItems.KOMAINU_SOUL.get());
				tabData.accept(MurmolModItems.SOUL_BOTTLE.get());
				// 古代知识残页：每种形态各一份
				tabData.accept(net.mcr.murmol.item.AncientKnowledgeScrapItem.forForm("luohong"));
				tabData.accept(net.mcr.murmol.item.AncientKnowledgeScrapItem.forForm("chen_huang"));
				tabData.accept(net.mcr.murmol.item.AncientKnowledgeScrapItem.forForm("moss_beast"));
				tabData.accept(net.mcr.murmol.item.AncientKnowledgeScrapItem.forForm("silkmoth"));
				tabData.accept(net.mcr.murmol.item.AncientKnowledgeScrapItem.forForm("komainu"));

				tabData.accept(MurmolModItems.ASTRAL_ARMOR_HELMET.get());
				tabData.accept(MurmolModItems.ASTRAL_ARMOR_CHESTPLATE.get());
				tabData.accept(MurmolModItems.ASTRAL_ARMOR_LEGGINGS.get());
				tabData.accept(MurmolModItems.ASTRAL_ARMOR_BOOTS.get());
				tabData.accept(MurmolModItems.ICE_FISH.get());
				tabData.accept(MurmolModItems.ICE_AND_FIRE_SWORD.get());
				tabData.accept(MurmolModItems.FROST_SWORD.get());
				tabData.accept(MurmolModItems.FROST_PICKAXE.get());
				tabData.accept(MurmolModItems.ICE_WAND.get());
				// 绽花之刃 / 升级版绽花之刃：初始携带人类杀手附魔
				tabData.accept(net.mcr.murmol.item.BloomingBladeItem.createEnchantedStack(parameters.holders()));
				tabData.accept(net.mcr.murmol.item.BloomingBladeFullItem.createEnchantedStack(parameters.holders()));
				tabData.accept(MurmolModItems.FIRE_BLADE_CLAW.get());
				tabData.accept(MurmolModItems.FROST_ARMOR_HELMET.get());
				tabData.accept(MurmolModItems.FROST_ARMOR_CHESTPLATE.get());
				tabData.accept(MurmolModItems.FROST_ARMOR_LEGGINGS.get());
				tabData.accept(MurmolModItems.FROST_ARMOR_BOOTS.get());
				tabData.accept(MurmolModBlocks.ASTRAL_DIRT.get().asItem());
				tabData.accept(MurmolModBlocks.ASTRAL_STONE.get().asItem());
				tabData.accept(MurmolModBlocks.COBBLED_ASTRAL_STONE.get().asItem());
				tabData.accept(MurmolModBlocks.ASTRAL_ORE.get().asItem());
				tabData.accept(MurmolModBlocks.ICE_SHARP_ORE.get().asItem());
				tabData.accept(MurmolModBlocks.ASTRAL_LEAF.get().asItem());
				tabData.accept(MurmolModBlocks.ASTRAL_LOG.get().asItem());
				tabData.accept(MurmolModBlocks.ASTRAL_PLANKS.get().asItem());
				tabData.accept(MurmolModBlocks.ASTRAL_BLOCK.get().asItem());
				tabData.accept(MurmolModBlocks.ALFAR_SHRINE.get().asItem());
				tabData.accept(MurmolModItems.SPIRIT_TABLE.get());
				tabData.accept(MurmolModBlocks.BANZA.get());
				tabData.accept(MurmolModBlocks.MAGIC_CRYSTAL_CLUSTER.get().asItem());
				tabData.accept(MurmolModItems.XIAOHUI.get());
				tabData.accept(MurmolModItems.ICE_FLOWER.get());
				tabData.accept(MurmolModItems.FRAGRANT_CORSAGE.get());
				tabData.accept(MurmolModItems.ICE_BOLT_ITEM.get());
				tabData.accept(MurmolModItems.MAGIC_CRYSTAL.get());
				tabData.accept(MurmolModBlocks.ICE_FLOWER_WILD.get().asItem());
				tabData.accept(MurmolModItems.FROST_INGOTM.get());
				tabData.accept(MurmolModItems.ICE_SHARP.get());
				tabData.accept(MurmolModItems.MAGIC_SHARD.get());
				tabData.accept(MurmolModItems.ASTRAL_MATRIX.get());
				tabData.accept(MurmolModItems.ASTRAL_CORE.get());
				tabData.accept(MurmolModItems.TREE_HEART.get());
				tabData.accept(MurmolModItems.ASTRAL_ORE_SHARD.get());
				tabData.accept(MurmolModItems.ASTRAL_INGOT.get());
				tabData.accept(MurmolModItems.PETAL.get());
				// 绽花之刃：初始携带人类杀手附魔
				tabData.accept(MurmolModItems.TOTEMOF_FALLEN.get());
				tabData.accept(MurmolModItems.MANGO.get());
				tabData.accept(MurmolModItems.MANGO_BUSH_STAGE0.get());
				tabData.accept(MurmolModItems.MANGO_BUSH_STAGE1.get());
				tabData.accept(MurmolModItems.MANGO_BUSH_STAGE2.get());
				tabData.accept(MurmolModItems.MANGO_BUSH_STAGE3.get());
				tabData.accept(MurmolModItems.MANGO_GOLDEN.get());
				tabData.accept(MurmolModItems.MANGO_GOLDEN_ENCHANT.get());
				tabData.accept(MurmolModItems.MANGO_ASTRAL.get());
				tabData.accept(MurmolModItems.ALFAR_SPAWN_EGG.get());
				tabData.accept(MurmolModItems.PEPPER_CH_SPAWN_EGG.get());
				tabData.accept(MurmolModItems.ASTRAL_DRAKE_SPAWN_EGG.get());
			}).build());
}