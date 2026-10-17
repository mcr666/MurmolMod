package mcr.murmol.compat.tlm;

import java.util.Map;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import mcr.richi.game.MahjongLobby;
import mcr.richi.game.RichiTableManager;

/**
 * "雀士"工作行为（{@link Behavior}{@code <EntityMaid>}），由
 * {@link TaskMahjongPlayer#createBrainTasks(EntityMaid)} 注入 Work 活动，工作状态下每 tick 执行。
 * <ul>
 * <li>已入座（钉位 active）：本任务不再移动（钉位由 {@link TlmMaidParticipant#tickLevel}
 *     每 tick 驱动，见 {@link TlmCompat}）。</li>
 * <li>已在等候队列（queuedTable != -1）：已钉在队列位，站等开局。</li>
 * <li>否则：探测范围内风盘（{@link RichiTableManager#nearest}），沿寻路走过去
 *     （{@link BehaviorUtils#setWalkAndLookTargetMemories}，由大脑 Core 的
 *     {@code MoveToTargetSink} 消费）；到桌旁后经 {@link MahjongLobby#npcJoinTable} 请求入队并钉位。</li>
 * </ul>
 */
public class MaidPlayMahjongTask extends Behavior<EntityMaid> {
	/** 风盘探测半径（格） */
	private static final double SEARCH = 24.0;
	/** 到达判定的水平距离平方（格） */
	private static final double JOIN_DIST_SQ = 2.5 * 2.5;
	private final float speed;

	public MaidPlayMahjongTask(float speed) {
		super(Map.of()); // 无内存前提；寻路经 WALK_TARGET 内存驱动
		this.speed = speed;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
		// 仅当女仆当前任务为"雀士"时驱动走向牌桌（CORE 对全部女仆注入，这里按任务门控）
		var task = maid.getTask();
		return task != null && TaskMahjongPlayer.UID.equals(task.getUid());
	}

	@Override
	protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
		var task = maid.getTask();
		if (task == null || !TaskMahjongPlayer.UID.equals(task.getUid()))
			return false; // 任务已切走
		// 已钉位（已入座）后本任务不再移动
		return !TlmMaidParticipant.of(maid).isPinned();
	}

	@Override
	protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
		TlmMaidParticipant p = TlmMaidParticipant.of(maid);
		if (p.isPinned())
			return;
		// 已在等候队列（pinWaiting 已钉在队列位）：站等开局
		if (p.queuedTable() != -1)
			return;
		BlockPos nearest = RichiTableManager.nearest(level, maid.position(), SEARCH);
		if (nearest == null) {
			maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			return;
		}
		if (nearest.distToCenterSqr(maid.position()) < JOIN_DIST_SQ) {
			// 到达牌桌：请求入队；牌局进行中/满员则站近等待，下个 tick 重试
			if (!MahjongLobby.npcJoinTable(level, maid, nearest))
				maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			return;
		}
		BehaviorUtils.setWalkAndLookTargetMemories(maid, nearest, speed, 1);
	}
}
