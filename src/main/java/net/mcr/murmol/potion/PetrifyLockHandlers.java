package net.mcr.murmol.potion;

import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 石化状态的交互锁定（双端）：
 * - 无法使用物品（食物/弓/盾等 LivingEntityUseItemEvent）
 * - 无法右键交互（方块/实体）
 * - 无法攻击（AttackEntityEvent）
 * 玩家移动锁定见 PetrifyMovementLock（客户端专属）。
 */
@EventBusSubscriber
public class PetrifyLockHandlers {

	/** 石化结束（到时自然过期）恢复生物 AI */
	@SubscribeEvent
	public static void onEffectExpired(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired event) {
		if (event.getEffectInstance() != null
				&& event.getEffectInstance().getEffect() == net.mcr.murmol.init.MurmolModMobEffects.PETRIFY.get()
				&& event.getEntity() instanceof net.minecraft.world.entity.Mob mob)
			mob.setNoAi(false);
	}

	/** 石化被移除（牛奶治愈等）恢复生物 AI */
	@SubscribeEvent
	public static void onEffectRemoved(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event) {
		if (event.getEffect() != null && event.getEffect().value() == net.mcr.murmol.init.MurmolModMobEffects.PETRIFY.get()
				&& event.getEntity() instanceof net.minecraft.world.entity.Mob mob)
			mob.setNoAi(false);
	}

	@SubscribeEvent
	public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
		if (PetrifyMobEffect.isPetrified(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (PetrifyMobEffect.isPetrified(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (PetrifyMobEffect.isPetrified(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (PetrifyMobEffect.isPetrified(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		if (PetrifyMobEffect.isPetrified(event.getEntity()))
			event.setCanceled(true);
	}
}
