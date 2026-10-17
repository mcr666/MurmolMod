package mcr.murmol.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import mcr.murmol.init.MurmolModItems;

/**
 * 玩家事件：每名玩家首次进入世界时获得一封「陌生的来信」（带持久标记，死亡重生也不会重复发放）。
 */
@EventBusSubscriber
public class MurmolPlayerEvents {
	private static final String FLAG_LETTER_GIVEN = "murmol_letter_given";

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		if (persisted.getBoolean(FLAG_LETTER_GIVEN))
			return;
		// 背包/副手已有信件则视为已获得，不再重复发放
		if (player.getInventory().contains(new ItemStack(MurmolModItems.STRANGE_LETTER.get())))
			return;
		// 背包放不下时不标记，下次登录再发
		if (player.getInventory().add(new ItemStack(MurmolModItems.STRANGE_LETTER.get()))) {
			persisted.putBoolean(FLAG_LETTER_GIVEN, true);
			player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
		}
	}
}
