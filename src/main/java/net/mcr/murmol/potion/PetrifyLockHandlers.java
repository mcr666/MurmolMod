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

	/** 石化结束（到时自然过期）恢复生物 AI 并清除同步标记 */
	@SubscribeEvent
	public static void onEffectExpired(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired event) {
		if (event.getEffectInstance() != null
				&& event.getEffectInstance().getEffect() == net.mcr.murmol.init.MurmolModMobEffects.PETRIFY.get()
				&& event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living)
			clearPetrify(living);
	}

	/** 石化被移除（牛奶治愈等）恢复生物 AI 并清除同步标记 */
	@SubscribeEvent
	public static void onEffectRemoved(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event) {
		if (event.getEffect() != null && event.getEffect().value() == net.mcr.murmol.init.MurmolModMobEffects.PETRIFY.get()
				&& event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living)
			clearPetrify(living);
	}

	private static void clearPetrify(net.minecraft.world.entity.LivingEntity living) {
		if (living instanceof net.minecraft.world.entity.Mob mob)
			mob.setNoAi(false);
		if (!living.level().isClientSide())
			living.setData(net.mcr.murmol.network.MurmolModVariables.PETRIFIED_STATE, false);
	}

	/** 石化、被囚笼禁锢，或处于变身冲击（creative_shock，变身动画期间禁止交互） */
	private static boolean isLocked(net.minecraft.world.entity.LivingEntity entity) {
		return PetrifyMobEffect.isPetrified(entity)
				|| entity.getData(net.mcr.murmol.network.MurmolModVariables.CAGED_STATE)
				|| entity.hasEffect(net.mcr.murmol.init.MurmolModMobEffects.CREATIVE_SHOCK);
	}

	/** 被囚笼禁锢的实体服务端逐 tick 定身（石化已有 applyEffectTick 处理） */
	@SubscribeEvent
	public static void onLivingTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living)
				|| living.level().isClientSide())
			return;
		// 自愈：石化效果已消失但标记残留（过期/治愈事件未触发的兜底），清除标记并恢复 AI
		if (living.getData(net.mcr.murmol.network.MurmolModVariables.PETRIFIED_STATE)
				&& !living.hasEffect(net.mcr.murmol.init.MurmolModMobEffects.PETRIFY))
			clearPetrify(living);
		if (living.getData(net.mcr.murmol.network.MurmolModVariables.CAGED_STATE)) {
			// 自愈：标记还在但脚下已不是囚笼（方块被破坏等遗漏路径），立即释放并还原缩放
			if (!(living.level().getBlockState(living.blockPosition())
					.getBlock() instanceof net.mcr.murmol.block.CageBlock)) {
				net.mcr.murmol.block.CageBlock.releaseEntity(living);
				return;
			}
			living.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
			if (living instanceof net.minecraft.server.level.ServerPlayer player)
				player.hurtMarked = true;
		}
	}

	@SubscribeEvent
	public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
		if (isLocked(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (isLocked(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (isLocked(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (isLocked(event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		if (isLocked(event.getEntity()))
			event.setCanceled(true);
	}

	/**
	 * 被囚笼禁锢的玩家只能挖掘脚下的囚笼方块（挖掘时间等效黑曜石），不能破坏任何其他方块。
	 * 石化玩家则完全不能挖掘（取消全部）。
	 */
	@SubscribeEvent
	public static void onBreakSpeed(net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
		net.minecraft.world.entity.player.Player player = event.getEntity();
		if (player.getData(net.mcr.murmol.network.MurmolModVariables.CAGED_STATE)) {
			java.util.Optional<net.minecraft.core.BlockPos> pos = event.getPosition();
			boolean diggingCage = pos.isPresent()
					&& player.level().getBlockState(pos.get()).getBlock() instanceof net.mcr.murmol.block.CageBlock;
			if (!diggingCage) {
				event.setCanceled(true);
			} else {
				// 囚笼硬度 8：被困时挖掘时长等效黑曜石（硬度 50）→ 速度按 8/50 缩放
				event.setNewSpeed(event.getNewSpeed() * (8.0F / 50.0F));
			}
		}
	}
}
