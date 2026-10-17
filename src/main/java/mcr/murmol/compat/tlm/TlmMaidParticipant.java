package mcr.murmol.compat.tlm;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import mcr.richi.game.MahjongLobby;
import mcr.richi.game.MahjongParticipant;

/**
 * 女仆参牌者包装：把 TLM 女仆（{@link EntityMaid}）适配为麻雀牌局的参牌者
 * （入座 / 钉位 / 释放全部走泛化后的 {@link MahjongLobby} 与 {@link MahjongParticipant} 契约）。
 * <p>
 * 钉位状态存于静态弱引用表（无法给 TLM 实体加字段）；每 tick 由
 * {@link #tickLevel(ServerLevel)} 驱动：传送到钉位、锁朝向、停导航、清零速度，
 * 从而在钉位期间压制女仆 MaidBrain 的自发移动。
 * <p>
 * 女仆死亡 / 卸载 / 跨维度迁移时自动清理其牌桌座位并解除钉位。
 */
public final class TlmMaidParticipant implements MahjongParticipant {
	/** 未命名女仆的显示名 */
	public static final String DEFAULT_NAME = "女仆";

	/** 钉位状态（按女仆实体弱引用存储；实体回收即随弱键清除） */
	private static final Map<EntityMaid, PinState> PIN_STATES = new WeakHashMap<>();

	private record PinState(Vec3 pos, float yaw, ResourceKey<Level> dim, boolean active, long queuedTable,
			boolean game, boolean viaTask) {
		PinState withPin(Vec3 p, float y, ResourceKey<Level> d, boolean g) {
			return new PinState(p, y, d, true, queuedTable, g, viaTask);
		}

		PinState withActive(boolean a) {
			return new PinState(pos, yaw, dim, a, queuedTable, game, viaTask);
		}

		PinState withQueued(long q) {
			return new PinState(pos, yaw, dim, active, q, game, viaTask);
		}

		PinState withViaTask(boolean v) {
			return new PinState(pos, yaw, dim, active, queuedTable, game, v);
		}
	}

	private final EntityMaid maid;

	private TlmMaidParticipant(EntityMaid maid) {
		this.maid = maid;
	}

	public static TlmMaidParticipant of(EntityMaid maid) {
		return new TlmMaidParticipant(maid);
	}

	private static PinState state(EntityMaid maid) {
		PinState s = PIN_STATES.get(maid);
		if (s == null) {
			s = new PinState(Vec3.ZERO, 0, maid.level().dimension(), false, -1, false, false);
			PIN_STATES.put(maid, s);
		}
		return s;
	}

	@Override
	public String participantName() {
		// 1) 玩家命名的名字（CustomName）优先
		Component cn = maid.getCustomName();
		if (cn != null && !cn.getString().isBlank())
			return cn.getString();
		// 2) TLM 显示名：getDisplayName = CustomName 或 getTypeName()（内部已解析 YSM 模型名→模型id→类型名）
		Component dn = maid.getDisplayName();
		String s = dn == null ? "" : dn.getString();
		if (!s.isBlank() && !DEFAULT_NAME.equals(s))
			return s;
		// 3) YSM 模型名 / 模型 id 兜底
		Component ysm = maid.getYsmModelName();
		if (ysm != null && !ysm.getString().isBlank())
			return ysm.getString();
		String mid = maid.getYsmModelId();
		if (mid != null && !mid.isBlank())
			return mid;
		return DEFAULT_NAME;
	}

	@Override
	public void pinWaiting(Vec3 pos, float yaw) {
		PIN_STATES.put(maid, state(maid).withPin(pos, yaw, maid.level().dimension(), false));
		maid.getNavigation().stop();
	}

	@Override
	public void pinForGame(Vec3 pos, float yaw) {
		// 对局钉位与等候一致：钉定 + 面向桌心（yaw 由调用方按座位计算给出）；并标记对局以启用桌心俯角
		PIN_STATES.put(maid, state(maid).withPin(pos, yaw, maid.level().dimension(), true));
		maid.getNavigation().stop();
	}

	@Override
	public void unpin() {
		PIN_STATES.put(maid, state(maid).withActive(false));
	}

	@Override
	public boolean isPinned() {
		return state(maid).active();
	}

	@Override
	public long queuedTable() {
		return state(maid).queuedTable();
	}

	@Override
	public void setQueuedTable(long origin) {
		PinState s = state(maid);
		boolean entering = s.queuedTable() == -1 && origin != -1;
		PinState ns = s.withQueued(origin);
		if (entering)
			ns = ns.withViaTask(isMahjongTask(maid)); // 入队时记录：是否经"雀士"工作状态入队
		PIN_STATES.put(maid, ns);
	}

	/** 当前任务是否为"雀士" */
	private static boolean isMahjongTask(EntityMaid maid) {
		var task = maid.getTask();
		return task != null && TaskMahjongPlayer.UID.equals(task.getUid());
	}

	/** 每 tick 归位 + 锁朝向（钉位期间压制 MaidBrain 自发移动） */
	@Override
	public void tickPin() {
		PinState s = state(maid);
		if (!s.active() || maid.isRemoved() || !maid.isAlive())
			return;
		if (!maid.level().dimension().equals(s.dim()))
			return; // 跨维度：由 tickLevel 统一清理
		maid.moveTo(s.pos(), s.yaw(), maid.getXRot());
		maid.yBodyRot = s.yaw();
		maid.yHeadRot = s.yaw();
		if (s.game())
			maid.setXRot(lookPitchAtTableCenter()); // 对局中始终俯视牌桌中心点
		maid.getNavigation().stop();
		maid.setDeltaMovement(Vec3.ZERO);
		maid.setJumping(false);
	}

	/** 对局钉位期间看向牌桌中心点的俯角（度；中心低于视线为正 = 向下看；找不到牌桌则保持原俯角） */
	private float lookPitchAtTableCenter() {
		long q = state(maid).queuedTable();
		if (q == -1)
			return maid.getXRot();
		ServerLevel sl = (ServerLevel) maid.level();
		net.minecraft.core.BlockPos origin = net.minecraft.core.BlockPos.of(q);
		if (!(sl.getBlockState(origin).getBlock() instanceof mcr.richi.block.FengPanBlock))
			return maid.getXRot();
		Vec3 c = mcr.richi.block.FengPanBlock.tableCenter(sl, origin);
		double dy = maid.getY() + maid.getEyeHeight() - c.y;
		double dist = Math.max(1.0E-4, Math.hypot(c.x - maid.getX(), c.z - maid.getZ()));
		return (float) Math.toDegrees(Math.atan2(dy, dist));
	}

	/** 每个服务端维度每 tick 调用：驱动本维度参牌女仆钉位，并清理已死/卸载/跨维度者 */
	public static void tickLevel(ServerLevel level) {		ArrayList<EntityMaid> stale = new ArrayList<>();
		for (Map.Entry<EntityMaid, PinState> e : PIN_STATES.entrySet()) {
			EntityMaid m = e.getKey();
			PinState s = e.getValue();
			if (m.level() != level)
				continue; // 每个维度只处理本维度的女仆
			if (m.isRemoved() || !m.isAlive() || !m.level().dimension().equals(s.dim())) {
				stale.add(m);
				continue;
			}
			// 经"雀士"任务入队者：工作状态切走后自动退出牌桌（退队列/清座位/解除钉位）
			if (s.viaTask() && (s.active() || s.queuedTable() != -1) && !isMahjongTask(m)) {
				stale.add(m);
				continue;
			}
			if (!s.active())
				continue;
			new TlmMaidParticipant(m).tickPin();
		}
		if (!stale.isEmpty()) {
			var server = level.getServer();
			for (EntityMaid m : stale) {
				PIN_STATES.remove(m);
				// 牌桌可能在任意维度：全维度清理（leaveNpc 幂等）
				for (ServerLevel l : server.getAllLevels())
					MahjongLobby.leaveNpc(l, m);
			}
		}
	}

	/** 确定性清理：实体离开世界（魂符收回/死亡/卸载/跨维度）时立即解除钉位状态 */
	public static void clear(EntityMaid maid) {
		PIN_STATES.remove(maid);
	}

	/** 好感度与天凤 pt 增量的换算比例（pt × 该系数 = 好感度；赢正加、输负减，可调） */
	private static final double FAVOR_SCALE = 1.0;

	@Override
	public void onGameSettled(double pt) {
		int delta = (int) Math.round(pt * FAVOR_SCALE);
		if (delta == 0)
			return;
		// 走 TLM FavorabilityManager.add：上限钳制 + 爱心粒子 + 等级升级触发
		maid.getFavorabilityManager().add(delta);
	}
}
