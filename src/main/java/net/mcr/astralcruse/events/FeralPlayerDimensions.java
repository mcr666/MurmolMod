package net.mcr.astralcruse.events;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

import net.mcr.murmol.feral.FeralFormManager;

/**
 * 野性玩家尺寸管理。
 * 形态标识由 MurmolMod 的 FeralFormManager（基于附件 feralFormId）提供，
 * 不再使用 feral_type 属性。
 */
@EventBusSubscriber(modid = "murmol")
public class FeralPlayerDimensions {
	private static final float HEIGHT_EPSILON = 0.01F;
	private static final Map<Player, Boolean> LAST_FERAL_STATES = new WeakHashMap<>();

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Pre event) {
		refreshIfNeeded(event.getEntity(), false);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		refreshIfNeeded(event.getEntity(), false);
	}

	/**
	 * 客户端附件同步到达后立即刷新尺寸。
	 * 由 MurmolModVariables.PlayerVariablesSyncMessage.handleData 调用。
	 */
	public static void refreshAfterClientAttributeSync(Player player) {
		refreshIfNeeded(player, true);
	}

	private static void refreshIfNeeded(Player player, boolean force) {
		boolean feral = isFeral(player);
		Boolean previous = LAST_FERAL_STATES.put(player, feral);
		float expectedHeight = player.getDefaultDimensions(player.getPose()).height();
		boolean wrongHeight = Math.abs(player.getBbHeight() - expectedHeight) > HEIGHT_EPSILON;
		if (force || previous == null || previous != feral || wrongHeight) {
			player.refreshDimensions();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerClone(PlayerEvent.Clone event) {
		LAST_FERAL_STATES.remove(event.getOriginal());
		LAST_FERAL_STATES.remove(event.getEntity());
		event.getEntity().refreshDimensions();
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		refreshAndSync(event.getEntity());
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		Player player = event.getEntity();
		LAST_FERAL_STATES.remove(player);
		player.refreshDimensions();
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		refreshAndSync(event.getEntity());
	}

	private static void refreshAndSync(Player player) {
		LAST_FERAL_STATES.remove(player);
		player.refreshDimensions();
	}

	public static boolean usesFeralCrouchDimensions(Pose pose) {
		return pose == Pose.CROUCHING
				|| pose == Pose.SWIMMING
				|| pose == Pose.FALL_FLYING
				|| pose == Pose.SPIN_ATTACK;
	}

	public static boolean isFeral(Player player) {
		return FeralFormManager.getForm(player).isFeral();
	}
}
