package mcr.richi.game.ai.akochan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import net.minecraft.server.level.ServerLevel;

import mcr.richi.game.RichiTableState;

/**
 * akochan 会话注册表（服务端唯一拥有者）：按桌 origin + 座位维护进程会话。
 * <ul>
 * <li>开局（MahjongLobby.startGame）对 akochan 流派 AI 座位 {@link #ensureSessions}；</li>
 * <li>清桌（MahjongTicker.cancel）{@link #releaseAll}；</li>
 * <li>牌局事件经 {@link #broadcast} 全量下发（akochan 服务端视角完整记录）；</li>
 * <li>决策 {@link #decide}：引擎后台线程等待响应，回调切回服务端主线程执行。</li>
 * </ul>
 */
public final class AkochanEngine {
	private AkochanEngine() {
	}

	/** key = origin.asLong() → 座位 0..3 → 会话 */
	private static final Map<Long, Map<Integer, AkochanSession>> TABLES = new HashMap<>();
	/** 决策等待线程池（阻塞等待响应，回调切回服务端线程） */
	private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "akochan-decide");
		t.setDaemon(true);
		return t;
	});

	/** 开局为 akochan 流派 AI 座位创建会话（新会话补发 start_game） */
	public static synchronized void ensureSessions(ServerLevel level, net.minecraft.core.BlockPos origin,
			RichiTableState st) {
		AkochanConfig cfg = AkochanConfig.get();
		String invalid = cfg.validate();
		if (invalid != null) {
			com.mojang.logging.LogUtils.getLogger().info("[Akochan] 未启用/不可用（{}），akochan 流派回落本地 AI", invalid);
			return;
		}
		long key = origin.asLong();
		int kyokuFirst = switch (st.gameType) {
			case 2 -> 8;
			default -> 4;
		};
		String[] names = new String[4];
		for (int i = 0; i < 4; i++)
			names[i] = st.players[i] == null ? "AI" + i : st.players[i];
		for (int seat = 0; seat < 4; seat++) {
			if (!st.aiSeat[seat] || Math.floorMod(st.aiFlows[seat], 8) != 7)
				continue;
			Map<Integer, AkochanSession> seats = TABLES.computeIfAbsent(key, k -> new HashMap<>());
			if (seats.containsKey(seat) && seats.get(seat).healthy())
				continue;
			AkochanSession s = AkochanSession.start(seat);
			if (s != null) {
				seats.put(seat, s);
				s.send(MjaiProtocolConverter.startGame(names, kyokuFirst));
			}
		}
	}

	/** 清桌释放该桌全部会话 */
	public static synchronized void releaseAll(net.minecraft.core.BlockPos origin) {
		Map<Integer, AkochanSession> seats = TABLES.remove(origin.asLong());
		if (seats != null)
			for (AkochanSession s : seats.values())
				s.close();
	}

	/** 该桌是否有活跃 akochan 会话 */
	public static synchronized boolean hasAny(net.minecraft.core.BlockPos origin) {
		Map<Integer, AkochanSession> seats = TABLES.get(origin.asLong());
		return seats != null && !seats.isEmpty();
	}

	/** 该桌 akochan 会话座位列表 */
	public static synchronized List<Integer> seatsOf(net.minecraft.core.BlockPos origin) {
		Map<Integer, AkochanSession> seats = TABLES.get(origin.asLong());
		return seats == null ? List.of() : new ArrayList<>(seats.keySet());
	}

	/** 全桌广播事件（所有会话各收一份） */
	public static synchronized void broadcast(net.minecraft.core.BlockPos origin, String json) {
		Map<Integer, AkochanSession> seats = TABLES.get(origin.asLong());
		if (seats == null)
			return;
		for (AkochanSession s : seats.values())
			s.send(json);
	}

	/**
	 * 异步决策：等待该座位响应后回调（已切回服务端主线程）。
	 * actions == null = 超时/会话不存在（调用方回落本地 AI）。无会话时同步回调 null。
	 */
	public static void decide(net.minecraft.core.BlockPos origin, int seat,
			Consumer<List<MjaiProtocolConverter.Action>> serverThreadCallback, ServerLevel level) {
		AkochanSession s;
		synchronized (AkochanEngine.class) {
			Map<Integer, AkochanSession> seats = TABLES.get(origin.asLong());
			s = seats == null ? null : seats.get(seat);
		}
		if (s == null) {
			level.getServer().execute(() -> serverThreadCallback.accept(null));
			return;
		}
		long timeout = AkochanConfig.get().decisionTimeoutMs;
		POOL.submit(() -> {
			List<MjaiProtocolConverter.Action> result = s.request(timeout);
			level.getServer().execute(() -> serverThreadCallback.accept(result));
		});
	}

	/** 丢弃该座位一行残留响应（fire-and-forget 触发后不再决策的场景）；回调切主线程后执行 */
	public static void drain(net.minecraft.core.BlockPos origin, int seat, ServerLevel level) {
		level.getServer().execute(() -> {
			AkochanSession s;
			synchronized (AkochanEngine.class) {
				Map<Integer, AkochanSession> seats = TABLES.get(origin.asLong());
				s = seats == null ? null : seats.get(seat);
			}
			if (s != null)
				s.drainOne();
		});
	}

	/** 状态描述（status 命令） */
	public static synchronized String status() {
		StringBuilder sb = new StringBuilder();
		String invalid = AkochanConfig.get().validate();
		sb.append("配置: ").append(invalid == null ? "§a可用" : "§c不可用（" + invalid + "）");
		int total = 0;
		for (var e : TABLES.entrySet()) {
			for (var s : e.getValue().entrySet()) {
				total++;
				sb.append("\n  桌 ").append(java.lang.Long.toHexString(e.getKey()))
						.append(" 座位 ").append(s.getKey())
						.append(s.getValue().healthy() ? " §a运行中" : " §c异常");
			}
		}
		sb.append("\n活跃会话: ").append(total);
		return sb.toString();
	}
}
