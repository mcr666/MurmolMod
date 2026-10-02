package mcr.richi.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import mcr.richi.block.FengPanBlock;
import mcr.richi.network.MahjongTablePayload;

/**
 * 牌局渲染同步（防抓包看牌核心）：把 RichiTableState 按观看者过滤后整桌广播
 * （RiichiGame.writeBack / FengPanBlock 重渲染与发牌动画时调用）。
 * 过滤规则：visible(座位 s) = 观战者(viewerSeat&lt;0) || openHand || s==viewer || handsExposed[s]!=HAND_STAND；
 * 不可见座位只发牌数（hidden），客户端渲染牌背。牌河/副露/立直棒永远真实；
 * 宝牌指示区仅已揭开的上张下发真实牌面（底张=里宝与未翻开上张发未知占位 "?"，防抓包/实体检视泄露）；
 * 局终盖牌（FACE_DOWN）发真实牌面（客户端面朝下渲染，无泄露）。
 */
public final class RichiTableSync {
	/** 同步范围：桌心 16 格内同维玩家 */
	private static final double RANGE = 16;

	private RichiTableSync() {
	}

	public static void broadcast(ServerLevel level, BlockPos origin) {
		broadcast(level, origin, 0);
	}

	/**
	 * 桌已拆除：广播一包空等待快照（无形象），客户端据此清掉牌面实体与 AI 假玩家/盔甲架。
	 * 风盘被挖/连锁拆除后调用，避免上一局角色残留在原地。
	 */
	public static void broadcastGone(ServerLevel level, BlockPos origin) {
		Vec3 center = new Vec3(origin.getX() + 1.0, origin.getY() + 0.5, origin.getZ() + 1.0);
		String[] empty4 = { "", "", "", "" };
		int[] zeros = new int[4];
		for (ServerPlayer p : level.players()) {
			if (p.level() != level)
				continue;
			if (p.distanceToSqr(center.x, center.y, center.z) > RANGE * RANGE)
				continue;
			net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, new MahjongTablePayload.ViewMessage(
					origin.asLong(), -1, false, 0, empty4, zeros, zeros, empty4, empty4,
					zeros, zeros, zeros, zeros, empty4, empty4, empty4, empty4,
					"", 0, 0, -1, 0, 0, 0, -1, -1, -1, 0, ""));
		}
	}

	/** animate &gt; 0 时客户端按摆位 delay 重放发牌动画 */
	public static void broadcast(ServerLevel level, BlockPos origin, int animate) {
		RichiTableState st = RichiTableManager.get(origin);
		if (st == null || !(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		Vec3 center = FengPanBlock.getTableCenter(level.getBlockState(origin), origin);
		for (ServerPlayer p : level.players()) {
			if (p.level() != level)
				continue;
			if (p.distanceToSqr(center.x, center.y, center.z) > RANGE * RANGE)
				continue;
			sendTo(level, origin, st, p, animate);
		}
	}

	/** 对单个观看者构造过滤快照并发送 */
	private static void sendTo(ServerLevel level, BlockPos origin, RichiTableState st, ServerPlayer p, int animate) {
		int viewerSeat = -1;
		for (int s = 0; s < 4; s++) {
			String u = st.players[s];
			if (u != null && u.equals(p.getStringUUID()))
				viewerSeat = s;
		}
		String[] hands = new String[4];
		int[] hidden = new int[4];
		int[] tedashi = new int[4];
		String[] names = new String[4];
		String[] avatarForms = new String[4];
		String[] avatarNames = new String[4];
		String[] avatarSkins = new String[4];
		for (int s = 0; s < 4; s++) {
			boolean visible = viewerSeat < 0 || st.openHand || s == viewerSeat
					|| st.handsExposed[s] != RichiTableState.HAND_STAND;
			if (visible) {
				hands[s] = st.hands[s] == null ? "" : st.hands[s];
				hidden[s] = 0;
			} else {
				hands[s] = "";
				hidden[s] = st.handCodes(s).size();
			}
			tedashi[s] = st.lastTedashi[s];
			names[s] = seatName(level, st, s);
			// AI 形象只要座位是 AI 就下发（等待阶段按预约标记，对局中按 aiSeat）：
			// 客户端据此创建/移除各 AI 座位的 RemotePlayer 假玩家
			boolean aiSeat = st.aiSeat[s]
					|| (st.phase == RichiTableState.PHASE_WAITING && st.reservedAI[s]);
			if (aiSeat) {
				avatarForms[s] = st.aiAvatarForms[s] == null ? "" : st.aiAvatarForms[s];
				avatarNames[s] = st.aiAvatarNames[s] == null ? "" : st.aiAvatarNames[s];
				avatarSkins[s] = st.aiAvatarSkins[s] == null ? "" : st.aiAvatarSkins[s];
			}
		}
		// 宝牌指示区按公开程度过滤：底张（里宝）恒为未知占位；上张仅已揭开叠数下发真实牌面
		List<Integer> doraAll = st.doraCodes();
		List<Integer> doraFiltered = new ArrayList<>(doraAll.size());
		for (int i = 0; i < doraAll.size(); i++)
			doraFiltered.add(i % 2 == 1 && i / 2 < st.revealedIndicators ? doraAll.get(i) : -1);
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, new MahjongTablePayload.ViewMessage(
				origin.asLong(), viewerSeat, st.openHand, animate,
				hands, hidden, tedashi, Arrays.copyOf(st.rivers, 4), Arrays.copyOf(st.melds, 4),
				Arrays.copyOf(st.riichiRiverIdx, 4), Arrays.copyOf(st.riichiSticks, 4),
				Arrays.copyOf(st.handsExposed, 4), Arrays.copyOf(st.points, 4),
				names, avatarForms, avatarNames, avatarSkins, MahjongTileNotation.format(doraFiltered), st.revealedIndicators,
				st.wallCodes().size(), st.turnSeat, st.round, st.roundWind, st.honba,
				st.selectedSeat, st.selectedIndex, st.drawnSeat, st.phase, st.banner == null ? "" : st.banner));
	}

	/** 座位显示名：人类 = 游戏档案名，AI = 流派 AI 名，空位 = 空串 */
	private static String seatName(ServerLevel level, RichiTableState st, int seat) {
		ServerPlayer p = st.playerOf(level, seat);
		if (p != null)
			return p.getGameProfile().getName();
		if (st.aiSeat[seat])
			return mcr.richi.game.riichi.RiichiBot.aiName(seat);
		return "";
	}
}
