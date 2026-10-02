package mcr.richi.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * 风盘牌局管理器（服务端唯一逻辑拥有者）：按桌角原点记录每张牌局的 RichiTableState。
 * 牌局状态变化时调用 {@link #markDirty}，服务端 tick 会自动重渲染对应桌面（客户端只被动渲染）。
 * 当前为演示发牌：从 136 张完整牌山随机发四家手牌/牌河/副露/宝牌指示。
 */
public final class RichiTableManager {
	private RichiTableManager() {
	}

	/** 桌角原点 → 牌局状态（仅服务端访问） */
private static final Map<BlockPos, RichiTableState> TABLES = new HashMap<>();
/** 桌角原点 → 所在维度（重渲染/指令查询用） */
private static final Map<BlockPos, ServerLevel> LEVELS = new HashMap<>();
/** 桌角原点 → 进行中的对局状态机（PHASE_PLAYING 时存在） */
private static final Map<BlockPos, mcr.richi.game.riichi.RiichiGame> GAMES = new HashMap<>();
/** 待重渲染的牌局（状态已变化，tick 中重建显示实体） */
private static final Set<BlockPos> DIRTY = new HashSet<>();

public static RichiTableState get(BlockPos origin) {
	return TABLES.get(origin);
}

public static mcr.richi.game.riichi.RiichiGame getGame(BlockPos origin) {
	return GAMES.get(origin);
}

public static void setGame(BlockPos origin, mcr.richi.game.riichi.RiichiGame game) {
	if (game == null)
		GAMES.remove(origin);
	else
		GAMES.put(origin.immutable(), game);
}

/** 取牌局状态，不存在则新建空大厅（等待排队；不再自动演示发牌）。新建时应用该桌持久化设置 */
public static RichiTableState getOrCreate(ServerLevel level, BlockPos origin) {
	BlockPos key = origin.immutable();
	LEVELS.putIfAbsent(key, level);
	boolean[] created = { false };
	RichiTableState st = TABLES.computeIfAbsent(key, pos -> {
		created[0] = true;
		return new RichiTableState();
	});
	if (created[0])
		MahjongTableSavedData.get(level).applyTo(key, st); // 恢复该桌上一次的配置（对局类型/思考时限/明牌/AI 预约/形象）
	return st;
}

/** 上局结束后重置：新建状态（保留队列与持久化设置）、清桌、清对局 */
public static RichiTableState reset(ServerLevel level, BlockPos origin) {
	BlockPos key = origin.immutable();
	RichiTableState old = TABLES.get(key);
	RichiTableState fresh = new RichiTableState();
	if (old != null)
		fresh.queue.addAll(old.queue);
	TABLES.put(key, fresh);
	GAMES.remove(key);
	LEVELS.put(key, level);
	MahjongTableSavedData.get(level).applyTo(key, fresh); // 清桌不清设置：保留上一次配置
	mcr.richi.block.FengPanBlock.clearDisplays(level, origin);
	return fresh;
}

public static void remove(BlockPos origin) {
	TABLES.remove(origin);
	LEVELS.remove(origin);
	GAMES.remove(origin);
	DIRTY.remove(origin);
}

	/** 标记牌局状态已变化（服务端 tick 中自动重渲染） */
	public static void markDirty(BlockPos origin) {
		if (TABLES.containsKey(origin))
			DIRTY.add(origin.immutable());
	}

	/** 某维度内所有牌局的原点（refresh 指令用） */
	public static List<BlockPos> originsIn(ServerLevel level) {
		List<BlockPos> result = new ArrayList<>();
		LEVELS.forEach((pos, lvl) -> {
			if (lvl == level && TABLES.containsKey(pos))
				result.add(pos);
		});
		return result;
	}

	/** 维度内离 pos 最近的牌局原点（quit 指令用），超过 maxDist 返回 null */
	public static BlockPos nearest(ServerLevel level, Vec3 pos, double maxDist) {
		BlockPos best = null;
		double bestDist = maxDist * maxDist;
		for (Map.Entry<BlockPos, ServerLevel> e : LEVELS.entrySet()) {
			if (e.getValue() != level || !TABLES.containsKey(e.getKey()))
				continue;
			double d = e.getKey().getCenter().distanceToSqr(pos);
			if (d <= bestDist) {
				bestDist = d;
				best = e.getKey();
			}
		}
		return best;
	}

	/** 服务端 tick：重渲染状态变化的牌局（由 MahjongTicker 每 tick 调用） */
	public static void processDirty() {
		if (DIRTY.isEmpty())
			return;
		for (BlockPos origin : new HashSet<>(DIRTY)) {
			DIRTY.remove(origin);
			ServerLevel level = LEVELS.get(origin);
			if (level == null)
				continue;
			mcr.richi.block.FengPanBlock.rerenderTable(level, origin);
		}
	}

	/** 服务端 tick：推进本维度所有对局状态机（AI 调度 + 响应窗口超时） */
	public static void tickGames(ServerLevel level) {
		if (GAMES.isEmpty())
			return;
		long now = level.getGameTime();
		for (Map.Entry<BlockPos, ServerLevel> e : LEVELS.entrySet()) {
			if (e.getValue() != level)
				continue;
			var game = GAMES.get(e.getKey());
			if (game != null)
				game.tick(now);
		}
	}
}
