package mcr.richi;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;

/**
 * 麻将内容服务端简易调度器：按延迟 tick 执行任务（发牌动画等）。
 * 后续正式对局逻辑可扩展为统一的行为调度入口。
 */
@net.neoforged.fml.common.EventBusSubscriber
public class MahjongTicker {
	/** 待执行任务（origin = 所属桌角原点，null = 无主任务；重渲染前按桌取消，防重复生成实体） */
	private record Scheduled(long runAtTick, BlockPos origin, Runnable task) {}

	private static final List<Scheduled> TASKS = new ArrayList<>();
	private static long tick;

	/** 延迟 delayTicks 后在服务端主线程执行 */
	public static void schedule(int delayTicks, Runnable task) {
		schedule(delayTicks, null, task);
	}

	/** 延迟 delayTicks 后在服务端主线程执行，任务归属指定桌（可被该桌重渲染取消） */
	public static void schedule(int delayTicks, BlockPos origin, Runnable task) {
		TASKS.add(new Scheduled(tick + Math.max(0, delayTicks), origin, task));
	}

	/** 取消某桌所有待执行任务（重渲染/发牌前调用，防止发牌动画残留任务重复生成实体） */
	public static void cancel(BlockPos origin) {
		TASKS.removeIf(s -> origin.equals(s.origin()));
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		tick++;
		mcr.richi.game.RichiTableManager.processDirty(); // 状态变化的牌局实时重渲染
		mcr.richi.block.FengPanBlock.processTurnTimers(event.getServer()); // 回合倒计时/超时摸切
		// 对局未开始的风盘：4x4 边缘少量标记粒子（每 0.5s 一轮，force 不受客户端粒子数量设置影响）
		if (tick % 10 == 0)
			for (net.minecraft.server.level.ServerLevel lvl : event.getServer().getAllLevels())
				for (BlockPos o : mcr.richi.game.RichiTableManager.originsIn(lvl))
					mcr.richi.block.FengPanBlock.spawnIdleParticles(lvl, o);
		// 对局状态机推进：AI 延迟决策 + 响应窗口超时
		for (net.minecraft.server.level.ServerLevel lvl : event.getServer().getAllLevels())
			mcr.richi.game.RichiTableManager.tickGames(lvl);
		if (TASKS.isEmpty())
			return;
		Iterator<Scheduled> it = TASKS.iterator();
		List<Scheduled> leftovers = new ArrayList<>();
		while (it.hasNext()) {
			Scheduled s = it.next();
			it.remove();
			if (s.runAtTick() <= tick)
				s.task().run();
			else
				leftovers.add(s);
		}
		TASKS.addAll(leftovers);
	}

	/** 清空所有待执行任务（刷新指令用） */
	public static void clear() {
		TASKS.clear();
	}

	/** 仅本桌牌局进行中（PHASE_PLAYING）时风盘无法破坏；等待/终局阶段可正常挖掘 */
	@SubscribeEvent
	public static void onBreakBlock(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
		if (!(event.getState().getBlock() instanceof mcr.richi.block.FengPanBlock))
			return;
		var origin = mcr.richi.block.FengPanBlock.getOrigin(event.getState(), event.getPos());
		var st = mcr.richi.game.RichiTableManager.get(origin);
		if (st != null && st.phase == mcr.richi.game.RichiTableState.PHASE_PLAYING) {
			event.setCanceled(true);
			if (event.getPlayer() != null)
				event.getPlayer().displayClientMessage(
						net.minecraft.network.chat.Component.translatable("message.richi.fengpan_locked"), true);
		}
	}

	/** 右键手牌交互实体（richi_hand 标签）：选牌/打出（准心直接指到牌实体即可，无需点到风盘方块） */
	@SubscribeEvent
	public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
		if (event.getLevel().isClientSide()
				|| !(event.getTarget() instanceof net.minecraft.world.entity.Interaction interaction)
				|| !interaction.getTags().contains("richi_hand"))
			return;
		event.setCanceled(true); // 阻止原版交互（如手持方块误放置）
		if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel))
			return;
		var origin = mcr.richi.game.RichiTableManager.nearest(serverLevel, interaction.position(), 8);
		if (origin != null)
			mcr.richi.block.FengPanBlock.handleHandInteract(serverLevel, origin, interaction, event.getEntity());
	}

	/** 左键手牌交互实体（richi_hand 标签）：当前回合家 = 摸切；回合外 = 过牌 */
	@SubscribeEvent
	public static void onAttackEntity(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
		if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
				|| player.level().isClientSide())
			return;
		if (!event.getTarget().getTags().contains("richi_hand"))
			return;
		var origin = mcr.richi.game.RichiTableManager.nearest(player.serverLevel(), event.getTarget().position(), 8);
		if (origin == null)
			return;
		var st = mcr.richi.game.RichiTableManager.get(origin);
		if (st == null)
			return;
		event.setCanceled(true);
		for (int seat = 0; seat < 4; seat++) {
			if (st.playerOf(player.serverLevel(), seat) == player) {
				mcr.richi.block.FengPanBlock.handleHandAttack(player, origin, st, seat);
				return;
			}
		}
	}

	/**
	 * 牌局实体（richi_display 标签）不持久化：牌局状态在内存，服务器重启后
	 * 存档里的陈旧实体会与状态脱节，故仅取消「从磁盘加载」的实体（新生的不受影响）。
	 */
	@SubscribeEvent
	public static void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
		if (event.loadedFromDisk() && !event.getLevel().isClientSide()
				&& (event.getEntity().getType() == net.minecraft.world.entity.EntityType.ITEM_DISPLAY
						|| event.getEntity().getType() == net.minecraft.world.entity.EntityType.TEXT_DISPLAY
						|| event.getEntity().getType() == net.minecraft.world.entity.EntityType.INTERACTION)
				&& event.getEntity().getTags().contains("richi_display")) {
			event.setCanceled(true);
		}
	}

	/** 玩家退出游戏：自动退出牌局（对局中 AI 接管） */
	@SubscribeEvent
	public static void onPlayerLoggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
		if (!event.getEntity().level().isClientSide())
			mcr.richi.game.MahjongLobby.handlePlayerLeave(event.getEntity().getServer(),
					event.getEntity().getStringUUID());
	}

	/** 玩家切换维度：自动退出牌局 */
	@SubscribeEvent
	public static void onPlayerChangedDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
		if (!event.getEntity().level().isClientSide())
			mcr.richi.game.MahjongLobby.handlePlayerLeave(event.getEntity().getServer(),
					event.getEntity().getStringUUID());
	}
}
