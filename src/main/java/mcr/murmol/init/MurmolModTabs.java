/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import mcr.murmol.MurmolMod;

public class MurmolModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MurmolMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MURMOL_CX = REGISTRY.register("murmol_cx",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.murmol.murmol_cx")).icon(() -> new ItemStack(MurmolModItems.MAOCRY_BLESSING.get())).displayItems((parameters, tabData) -> {
			// ===== 系统道具 =====
			tabData.accept(MurmolModItems.THE_ASTRAL_TOME.get());
			// 灵气指南针：指向最近的幻星瘟疫群系
			tabData.accept(MurmolModItems.ASTRAL_COMPASS.get());
			// 两仪：形态开关管理
			tabData.accept(MurmolModItems.TWO_PRINCIPLES.get());
			// ===== 变形系统 =====
			// 各形态灵魂与魂之瓶
			tabData.accept(MurmolModItems.MURMOL_SOUL.get());
			tabData.accept(MurmolModItems.LUOHONG_SOUL.get());
			tabData.accept(MurmolModItems.CHEN_HUANG_SOUL.get());
			tabData.accept(MurmolModItems.MOSS_BEAST_SOUL.get());
			tabData.accept(MurmolModItems.SILKMOTH_SOUL.get());
			tabData.accept(MurmolModItems.KOMAINU_SOUL.get());
			tabData.accept(MurmolModItems.WENYAO_SOUL.get());
			tabData.accept(MurmolModItems.FEROCIOUS_SOUL.get());
			tabData.accept(MurmolModItems.DARK_FURY_SOUL.get());
			tabData.accept(MurmolModItems.SOUL_BOTTLE.get());
			// 药水（仅喷溅式，酿造见 MurmolModPotions）：
			// 石化（地狱疣+石头）、显形 1 分钟（+摩摩尔之魂）/5 分钟（+红石）、
			// 星幻感染（+幻星矿碎片）、创造冲击（+紫颂果）
			for (var potion : new net.minecraft.world.item.alchemy.Potion[] {
					MurmolModPotions.PETRIFY.get(),
					MurmolModPotions.REVEAL.get(),
					MurmolModPotions.REVEAL_LONG.get(),
					MurmolModPotions.ASTRAL_INFECTION.get(),
					MurmolModPotions.CREATIVE_SHOCK.get() }) {
				net.minecraft.world.item.ItemStack splash = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPLASH_POTION);
				splash.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
						new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(potion)));
				tabData.accept(splash);
			}
			// 古代知识残页：每种形态各一份
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("luohong"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("chen_huang"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("moss_beast"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("silkmoth"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("komainu"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("wenyao"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("ferocious"));
			tabData.accept(mcr.murmol.item.AncientKnowledgeScrapItem.forForm("dark_fury"));
			// ===== 武器与装备 =====
			// 绽花之刃 / 升级版：初始携带人类杀手附魔
			tabData.accept(mcr.murmol.item.BloomingBladeItem.createEnchantedStack(parameters.holders()));
			tabData.accept(mcr.murmol.item.BloomingBladeFullItem.createEnchantedStack(parameters.holders()));
			tabData.accept(MurmolModItems.ICE_AND_FIRE_SWORD.get());
			tabData.accept(MurmolModItems.FROST_SWORD.get());
			tabData.accept(MurmolModItems.FIRE_BLADE_CLAW.get());
			tabData.accept(MurmolModItems.FROST_PICKAXE.get());
			tabData.accept(MurmolModItems.ASTRAL_PICKAXE.get());
			tabData.accept(MurmolModItems.ASTRAL_SHOVEL.get());
			tabData.accept(MurmolModItems.ASTRAL_AXE.get());
			tabData.accept(MurmolModItems.ASTRAL_ARMOR_HELMET.get());
			tabData.accept(MurmolModItems.ASTRAL_ARMOR_CHESTPLATE.get());
			tabData.accept(MurmolModItems.ASTRAL_ARMOR_LEGGINGS.get());
			tabData.accept(MurmolModItems.ASTRAL_ARMOR_BOOTS.get());
			tabData.accept(MurmolModItems.FROST_ARMOR_HELMET.get());
			tabData.accept(MurmolModItems.FROST_ARMOR_CHESTPLATE.get());
			tabData.accept(MurmolModItems.FROST_ARMOR_LEGGINGS.get());
			tabData.accept(MurmolModItems.FROST_ARMOR_BOOTS.get());
			tabData.accept(MurmolModItems.WATER_BUBBLE.get()); // 水泡头盔（头戴时眼睛被判定浸在水中）
			// ===== 食物 =====
			tabData.accept(MurmolModItems.MANGO.get());
			tabData.accept(MurmolModItems.MANGO_GOLDEN.get());
			tabData.accept(MurmolModItems.MANGO_GOLDEN_ENCHANT.get());
			tabData.accept(MurmolModItems.MANGO_ASTRAL.get());
			// ===== 方块 =====
			// 功能方块
			tabData.accept(MurmolModItems.SPIRIT_TABLE.get());
			tabData.accept(MurmolModBlocks.BANZA.get());
			tabData.accept(MurmolModBlocks.CURSED_STONE.get());
			tabData.accept(MurmolModItems.CAGE.get());
			// 星界方块
			tabData.accept(MurmolModBlocks.ASTRAL_DIRT.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_STONE.get().asItem());
			tabData.accept(MurmolModBlocks.COBBLED_ASTRAL_STONE.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_ORE.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_LEAF.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_LOG.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_PLANKS.get().asItem());
			tabData.accept(MurmolModBlocks.STRIPPED_ASTRAL_LOG.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_PLANKS_SLAB.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_PLANKS_STAIRS.get().asItem());
			tabData.accept(MurmolModItems.ASTRAL_TORCH.get());
			tabData.accept(MurmolModBlocks.ASTRAL_LANTERN.get().asItem());
			tabData.accept(MurmolModBlocks.ASTRAL_BLOCK.get().asItem());
			tabData.accept(MurmolModItems.ASTRAL_SAPLING.get());
			tabData.accept(MurmolModBlocks.ALFAR_SHRINE.get().asItem());
			// 冰系与装饰方块
			tabData.accept(MurmolModBlocks.ICE_SHARP_ORE.get().asItem());
			tabData.accept(MurmolModBlocks.ICE_FLOWER_WILD.get().asItem());
			tabData.accept(MurmolModBlocks.MAGIC_CRYSTAL_CLUSTER.get().asItem());
			// 芒果丛
			tabData.accept(MurmolModItems.MANGO_BUSH_STAGE0.get());
			tabData.accept(MurmolModItems.MANGO_BUSH_STAGE1.get());
			tabData.accept(MurmolModItems.MANGO_BUSH_STAGE2.get());
			tabData.accept(MurmolModItems.MANGO_BUSH_STAGE3.get());
			// ===== 材料 =====
			tabData.accept(MurmolModItems.XIAOHUI.get());
			tabData.accept(MurmolModItems.ICE_FLOWER.get());
			tabData.accept(MurmolModItems.FRAGRANT_CORSAGE.get());
			tabData.accept(MurmolModItems.MAGIC_CRYSTAL.get());
			tabData.accept(MurmolModItems.ICE_BOLT_ITEM.get());
			tabData.accept(MurmolModItems.MAGIC_SHARD.get());
			tabData.accept(MurmolModItems.FROST_INGOTM.get());
			tabData.accept(MurmolModItems.ICE_SHARP.get());
			tabData.accept(MurmolModItems.ASTRAL_ORE_SHARD.get());
			tabData.accept(MurmolModItems.ASTRAL_INGOT.get());
			tabData.accept(MurmolModItems.TREE_HEART.get());
			tabData.accept(MurmolModItems.ASTRAL_CORE.get());
			tabData.accept(MurmolModItems.ASTRAL_CODEX.get());
			tabData.accept(MurmolModItems.ASTRAL_MATRIX.get());
			tabData.accept(MurmolModItems.PETAL.get());
			tabData.accept(MurmolModItems.TOTEMOF_FALLEN.get());
			// ===== 刷怪蛋 =====
			tabData.accept(MurmolModItems.ALFAR_SPAWN_EGG.get());
			tabData.accept(MurmolModItems.ASTRAL_DRAKE_SPAWN_EGG.get());
			tabData.accept(MurmolModItems.MURMOL_NPC_SPAWN_EGG.get());
			// ===== 音乐唱片 =====
			tabData.accept(MurmolModItems.SNOWY_HILLS.get());
			tabData.accept(MurmolModItems.GRACEFUL.get());
			// ===== 幻星麻雀 =====
			tabData.accept(mcr.richi.MahjongItems.FENG_PAN.get()); // 风盘（麻雀牌桌）
			tabData.accept(mcr.richi.MahjongTileItem.create(-1)); // 未知牌（背面）
			tabData.accept(mcr.richi.MahjongItems.ITEMS.get(0).get()); // 点棒 1000（立直宣言用）
		}).build());
}