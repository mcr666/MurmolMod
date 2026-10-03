package mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.registries.Registries;

import mcr.murmol.potion.PetrifyMobEffect;
import mcr.murmol.MurmolMod;

/**
 * 药水注册与酿造：
 * - 石化药水 = 粗制药水（地狱疣）+ 石头
 * - 显形药水（1 分钟）= 粗制药水（地狱疣）+ 摩摩尔之魂；显形药水（延长，5 分钟）= 显形药水 + 红石
 */
@EventBusSubscriber
public class MurmolModPotions {

	public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(Registries.POTION, MurmolMod.MODID);

	/** 石化：30 秒（可按需调整时长） */
	public static final DeferredHolder<Potion, Potion> PETRIFY = REGISTRY.register("petrify",
			() -> new Potion("petrify", new net.minecraft.world.effect.MobEffectInstance(
					mcr.murmol.init.MurmolModMobEffects.PETRIFY, 600)));

	/** 显形：1 分钟 */
	public static final DeferredHolder<Potion, Potion> REVEAL = REGISTRY.register("reveal",
			() -> new Potion("reveal", new net.minecraft.world.effect.MobEffectInstance(
					MurmolModMobEffects.REVEAL, 1200)));

	/** 显形（延长）：5 分钟 */
	public static final DeferredHolder<Potion, Potion> REVEAL_LONG = REGISTRY.register("reveal_long",
			() -> new Potion("reveal_long", new net.minecraft.world.effect.MobEffectInstance(
					MurmolModMobEffects.REVEAL, 6000)));

	@SubscribeEvent
	public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
		var builder = event.getBuilder();
		// 石化：粗制 + 石头
		builder.addMix(Potions.AWKWARD, Items.STONE, PETRIFY);
		// 显形：粗制 + 摩摩尔之魂；延长：显形 + 红石
		builder.addMix(Potions.AWKWARD, mcr.murmol.init.MurmolModItems.MURMOL_SOUL.get(), REVEAL);
		builder.addMix(REVEAL, Items.REDSTONE, REVEAL_LONG);
	}
}
