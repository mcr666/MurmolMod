package mcr.richi.game;

import net.minecraft.world.phys.Vec3;

/**
 * 麻雀牌局的「参牌者」抽象：一个可占用牌桌座位、作为 AI 出牌的真实实体
 * （如 murmol_npc，或未来其它 mod 加入的女仆等）。
 * <p>
 * 实现方负责自身的钉位（等候停驻 / 对局面向桌心）与解除。
 * MurmolNPC 在自身 tick() 中完成钉位；外部包装实现（如女仆适配器）可经 {@link #tickPin()}
 * 由调度器每 tick 驱动归位。
 */
public interface MahjongParticipant {
	/** 座位 / 队列显示名（如自定义名，否则 "Murmol" / "女仆"） */
	String participantName();

	/** 等候钉位：原地停驻（加入队列后、未开局前） */
	void pinWaiting(Vec3 pos, float yaw);

	/** 对局钉位：钉在座位站位并面向桌心（不注视玩家） */
	void pinForGame(Vec3 pos, float yaw);

	/** 解除位置固定（离开牌局 / 牌局结束） */
	void unpin();

	boolean isPinned();

	/** 已加入的牌桌 origin 长坐标（-1 = 未加入） */
	long queuedTable();

	void setQueuedTable(long origin);

	/**
	 * 每 tick 归位 + 锁朝向。实体自身已在 tick() 中钉位的实现（如 MurmolNPC）可留空；
	 * 外部包装实现（如女仆适配器）由此方法每 tick 驱动归位。
	 */
	default void tickPin() {
	}


	/** 对局结算回调：pt 为本场天凤 pt 增量（可为负）。默认不处理；实现方（如女仆）可借此加好感度等 */
	default void onGameSettled(double pt) {
	}
}
