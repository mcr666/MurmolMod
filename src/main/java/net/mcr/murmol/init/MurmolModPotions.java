package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.registries.Registries;

import net.mcr.murmol.potion.PetrifyMobEffect;
import net.mcr.murmol.MurmolMod;

/**
 * 药水注册与酿造：石化药水 = 粗制药水（地狱疣）+ 石头。
 */
@EventBusSubscriber
public class MurmolModPotions {

	public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(Registries.POTION, MurmolMod.MODID);

	/** 石化：30 秒（可按需调整时长） */
	public static final DeferredHolder<Potion, Potion> PETRIFY = REGISTRY.register("petrify",
			() -> new Potion("petrify", new net.minecraft.world.effect.MobEffectInstance(
					net.mcr.murmol.init.MurmolModMobEffects.PETRIFY, 600)));

	/** 酿造：粗制药水（地狱疣基底）+ 石头 → 石化药水 */
	@SubscribeEvent
	public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
		event.getBuilder().addMix(Potions.AWKWARD, Items.STONE, PETRIFY);
	}
}
