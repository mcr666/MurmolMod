package mcr.murmol.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import mcr.murmol.init.MurmolModItems;

public class FragrantCorsageItem extends Item {
	public FragrantCorsageItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	/** 置于副手时常驻生命恢复 I（无粒子、无图标，表现为被动属性） */
	@EventBusSubscriber
	public static class Handlers {
		@SubscribeEvent
		public static void onPlayerTick(PlayerTickEvent.Post event) {
			var player = event.getEntity();
			if (player.level().isClientSide() || !player.getOffhandItem().is(MurmolModItems.FRAGRANT_CORSAGE.get()))
				return;
			// 每 50 tick 续 100 tick，保证效果常驻且不每 tick 刷新
			if (player.tickCount % 50 == 0)
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false, false));
		}
	}
}
