package mcr.richi.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import mcr.richi.block.FengPanBlock;
import mcr.richi.game.RichiTableState;

/**
 * 风盘桌面公共布局（服务端交互代理与客户端渲染共用）：
 * 桌面几何（{@link Geom}）、手牌位置（{@link #handTarget}）、整桌摆位计划
 * （{@link #plan} → {@link Placement} 列表，含座位标签/手牌/牌河/副露/点棒/宝牌叠的位置与延迟）。
 * 服务端只取 HAND 立牌项生成 Interaction 代理；客户端按全部项生成 client-side 显示实体。
 * handCodes 由调用方传入（服务端传真实手牌、客户端传包内过滤后手牌，隐藏座位传 -1 牌背）。
 */
public final class TableLayout {
	private TableLayout() {
	}

	/** 座位名（家序 0..3） */
	public static final String[] SEAT_NAMES = { "東", "南", "西", "北" };
	/** 每家手牌张数（发牌 13 张） */
	public static final int HAND_SIZE = 13;
	/** 牌河每排张数（6 张一排，超出换行） */
	public static final int RIVER_COLS = 6;
	/** 牌河换行排距（格，≈平躺牌长 0.2083 + 缝） */
	public static final double RIVER_ROW_PITCH = 0.21;
	/** 牌河第一排行中心距桌心（格） */
	public static final double RIVER_DIST = 0.65;
	/** 中央宝牌指示牌叠数（日麻规则 5 叠） */
	public static final int DORA_STACKS = 5;
	/** 平躺牌厚度（格）＝模型厚 6px × 缩放 */
	public static final double FLAT_THICKNESS = 6 * (2.5 / 12.0) / 16;
	/** 平躺牌宽（格）＝模型宽 12px × 缩放（副露横牌紧贴计算用） */
	public static final double TILE_WIDTH = 12 * (2.5 / 12.0) / 16;
	/** 平躺牌长（格）＝模型长 16px × 缩放 */
	public static final double TILE_LONG = 16 * (2.5 / 12.0) / 16;
	/** 手牌行离桌边距离（格） */
	public static final double EDGE_INSET = 0.35;
	/** 手牌相邻牌间距（格）——牌宽 12px×s/16≈0.15625，紧贴（+0.0007 防共面闪烁） */
	public static final double TILE_GAP = 0.157;
	/** 副露行向桌心内收距离（格）——与手牌行错开 z 带，避免占用同一条线 */
	public static final double MELD_ROW_INSET = 0.20;
	/** 副露区外缘上限（格，沿玩家右侧轴）：邻家行列带从 ±(edge - TILE_WIDTH/2)=±1.572 起，留隙取 1.55，保证桌角两侧副露永不重叠 */
	public static final double MELD_EDGE_CAP = 1.55;
	/** 相邻副露组之间的最小间隙（格） */
	public static final double MELD_GAP = 0.02;
	/** 发牌间隔（tick） */
	public static final int DEAL_INTERVAL_TICKS = 1;
	/** 牌渲染缩放：模型宽度 12px（第二窄边），目标 2.5 单位 → 2.5/12 */
	public static final float TILE_SCALE = 2.5f / 12f;
	/** 座位标记离桌心距离（格，水平四方向向外） */
	public static final double SEAT_DISTANCE = 3.5;
	/** 座位标记高度（格，桌面上方） */
	public static final double SEAT_HEIGHT = 2.0;
	/** 选牌抬升高度（格）＝立牌高（16px×s/16）的一半 */
	public static final double SELECT_LIFT = TILE_SCALE / 2.0;

	//摆位类别（Placement.kind）
	public static final String KIND_SEAT = "seat";
	public static final String KIND_HAND = "hand";
	public static final String KIND_RIVER = "river";
	public static final String KIND_MELD = "meld";
	public static final String KIND_STICK = "stick";
	public static final String KIND_DORA = "dora";

	/** 桌面几何：桌心 + f/r 方向 + 各家行中心/切线/朝外/左手方向 */
	public record Geom(Vec3 center, Vec3 f, Vec3 r, Vec3[] rows, Vec3[] tangents, Vec3[] outs, Vec3[] lefts) {
	}

	/**
	 * 整桌摆位项：kind 见 KIND_* 常量；pos = 显示实体位置（已含贴桌/抬升偏移）；
	 * base = 桌面基准点（交互代理定位/倒牌级联的立牌位置）；code = 牌面（-1 = 未知牌背）；
	 * yRot = 实体朝向；axis = 平躺长轴（null = 立牌）；faceUp = 平躺牌面朝向；
	 * seat/index = 座位与牌序（非座位项为 -1）；delay = 发牌动画延迟（tick，animate=false 时全 0）。
	 */
	public record Placement(String kind, Vec3 pos, Vec3 base, int code, float yRot, Vec3 axis, boolean faceUp,
			int seat, int index, int delay) {
	}

	/** 由方块状态+桌角原点推桌面几何（家序 0=東（远侧）→1=南（右侧）→2=西（近侧）→3=北（左侧）） */
	public static Geom of(BlockGetter level, BlockPos origin) {
		BlockState anyState = level.getBlockState(origin);
		Direction forward = anyState.getValue(FengPanBlock.FACING);
		Direction right = forward.getClockWise();
		Vec3 f = new Vec3(forward.getStepX(), 0, forward.getStepZ());
		Vec3 r = new Vec3(right.getStepX(), 0, right.getStepZ());
		// 桌面基准 = 风盘方块顶面（y=origin.y）；桌心 = 2x2 区域几何中心：f/r 可为负方向（北/西）
		Vec3 center = new Vec3(
				origin.getX() + (forward.getStepX() + right.getStepX() + 1) / 2.0,
				origin.getY(),
				origin.getZ() + (forward.getStepZ() + right.getStepZ() + 1) / 2.0);
		double edge = 2.0 - EDGE_INSET; // 行中心距桌心 1.65（保持原 4x4 桌时期尺寸，不随 2x2 风盘缩小）
		Vec3[] rows = { center.subtract(f.scale(edge)), center.add(r.scale(edge)),
				center.add(f.scale(edge)), center.subtract(r.scale(edge)) };
		Vec3[] tangents = { r, f, r, f }; // 行中心沿 f/r 偏移，牌列沿切线（另一轴）展开
		Vec3[] outs = { f.scale(-1), r, f, r.scale(-1) }; // 各家朝外方向
		// 各家视角的"左"方向：面朝桌心（fc = -out）时左 = (fc.z, -fc.x) = (-out.z, out.x)
		// 手牌/牌河统一按各家玩家视角排布（西北家与东家切线方向相反，直接用切线会一半反）
		Vec3[] lefts = new Vec3[4];
		for (int h = 0; h < 4; h++)
			lefts[h] = new Vec3(-outs[h].z, 0, outs[h].x);
		return new Geom(center, f, r, rows, tangents, outs, lefts);
	}

	/**
	 * 某家第 index 张手牌的位置（渲染/选牌判定/摸牌共用）：按各家玩家视角左对齐——
	 * index 0 在玩家左手端，index 增大向玩家右侧（1m..9m 从左到右）；
	 * 刚摸的牌（index ≥ 13，无副露时）落在手牌区最右端外，隔开半个牌宽（+0.078）。
	 */
	public static Vec3 handTarget(Geom geom, int seat, int index) {
		return handTarget(geom, seat, index, index >= HAND_SIZE);
	}

	/** 同上（drawnGap = 该张为刚摸的牌：有副露时摸牌 index &lt; 13，由调用方按 drawnSeat 判定） */
	public static Vec3 handTarget(Geom geom, int seat, int index, boolean drawnGap) {
		double offset = (HAND_SIZE - 1) / 2.0 * TILE_GAP - index * TILE_GAP - (drawnGap ? 0.078 : 0);
		return geom.rows()[seat].add(geom.lefts()[seat].scale(offset));
	}

	/** 牌河第 index 张的桌面位置（6 张一排、每排均从玩家右手端向左排、行距向外侧推进；结算粒子定位用） */
	public static Vec3 riverTarget(Geom geom, int seat, int index) {
		int rowIdx = index / RIVER_COLS;
		int col = index % RIVER_COLS;
		return geom.rows()[seat]
				.subtract(geom.outs()[seat].scale(2.0 - EDGE_INSET - RIVER_DIST - rowIdx * RIVER_ROW_PITCH))
				.add(geom.lefts()[seat].scale(((RIVER_COLS - 1) / 2.0 - col) * 0.17));
	}

	/** 手牌定位标签（显示实体与交互代理共用），选中升降时按标签精确找实体 */
	public static String handTag(int seat, int index) {
		return "richi_hand_i_" + seat + "_" + index;
	}

	/** 平躺牌实体 yaw（含读向修正：长轴沿 X（东西向）的牌与沿 Z 的相反朝向才符合读牌方向） */
	public static float flatYaw(Vec3 longAxis) {
		Vec3 a = longAxis.x != 0 ? longAxis.scale(-1) : longAxis;
		return (float) Math.toDegrees(Math.atan2(-a.x, -a.z));
	}

	/** 立直点棒 yaw（无读向修正，与原 spawnRiichiStick 一致） */
	public static float stickYaw(Vec3 longAxis) {
		return (float) Math.toDegrees(Math.atan2(-longAxis.x, -longAxis.z));
	}

	/** 平躺牌显示实体位置：实体位置即牌体中心，牌底贴 target.y + lift（叠放时上张 lift=FLAT_THICKNESS） */
	public static Vec3 flatEntityPos(Vec3 target, double lift) {
		return new Vec3(target.x, target.y + 3 * TILE_SCALE / 16 + lift, target.z);
	}

	/** 立牌显示实体位置：牌底贴 tableY 上方 8px×s/16 处（模型半高） */
	public static Vec3 standingEntityPos(Vec3 tablePos) {
		return new Vec3(tablePos.x, tablePos.y + 8 * TILE_SCALE / 16, tablePos.z);
	}

	/**
	 * 生成整桌摆位计划（原 FengPanBlock.spawnTableDisplays 的全部摆位数学）。
	 * 手牌：四边各 N 张立牌（面朝各家）；牌河：平躺面朝上（长轴指向桌心，6 张一排）；
	 * 副露：平躺纵牌 + 横放声明牌（整组朝向/横牌端位对应喂牌家方位）；点棒：牌河前堆叠；宝牌：中央 5 叠×2 张。
	 * 局终展示（handsExposed=1 推倒/2 盖牌）的手牌项 axis 非空（平躺），animate 时客户端先立后平两段生成。
	 */
	public static List<Placement> plan(Geom geom, RichiTableState st, List<List<Integer>> handCodes, boolean animate) {
		List<Placement> out = new ArrayList<>();
		int step = animate ? DEAL_INTERVAL_TICKS : 0;
		Vec3 center = geom.center();
		Vec3 f = geom.f();
		Vec3 r = geom.r();
		double edge = 2.0 - EDGE_INSET; // 手牌行中心距桌心（与 of() 一致）
		double flatStep = 0.17;         // 平躺牌间距
		Vec3[] rows = geom.rows();
		Vec3[] tangents = geom.tangents();
		Vec3[] outs = geom.outs();
		float[] yRots = new float[4];
		for (int h = 0; h < 4; h++)
			yRots[h] = (float) Math.toDegrees(Math.atan2(-outs[h].x, outs[h].z));

		// 0) 座位标记：各家外侧 3.25 格（向桌心收 0.25）、上方 2 格处（文本由渲染方拼装）
		for (int h = 0; h < 4; h++) {
			Vec3 labelPos = center.add(outs[h].scale(SEAT_DISTANCE - 0.25)).add(0, SEAT_HEIGHT, 0);
			out.add(new Placement(KIND_SEAT, labelPos, labelPos, -1, 0, null, true, h, -1, h * step));
		}
		// 1) 手牌：四家同时发牌（第 k 张四家一起出：delay = (4 + k*4 + h)*step）；选中牌升高 SELECT_LIFT
		for (int h = 0; h < 4; h++) {
			List<Integer> hand = handCodes.get(h);
			int expose = st.handsExposed[h];
			boolean hasDrawn = st.drawnSeat == h && !hand.isEmpty(); // 刚摸的牌 = 手牌最右一张（与原手牌隔开一个牌位）
			for (int k = 0; k < hand.size(); k++) {
				int code = hand.get(k);
				Vec3 tilePos = handTarget(geom, h, k, hasDrawn && k == hand.size() - 1)
						.add(0, st.selectedSeat == h && st.selectedIndex == k ? SELECT_LIFT : 0, 0);
				int delay = (4 + k * 4 + h) * step;
				if (expose != RichiTableState.HAND_STAND) {
					// 推倒/盖牌：长轴指向桌心方向（与牌河同向，避免"多米诺"横躺），客户端先立后平
					Vec3 flatAxis = outs[h].scale(-1);
					out.add(new Placement(KIND_HAND, flatEntityPos(tilePos, 0), tilePos, code,
							flatYaw(flatAxis), flatAxis, expose == RichiTableState.HAND_FACE_UP, h, k, delay));
				} else {
					out.add(new Placement(KIND_HAND, standingEntityPos(tilePos), tilePos, code,
							yRots[h], null, true, h, k, delay));
				}
			}
		}
		int dealIndex = 4 + HAND_SIZE * 4; // 手牌区占用 4 标签 + 52 张的调度序号
		// 2) 牌河：每排从玩家右手端开始向左排，行距向外侧推进；立直宣言牌横摆（长轴沿切线）
		for (int h = 0; h < 4; h++) {
			Vec3 tangent = tangents[h];
			Vec3 riverDir = outs[h].scale(-1); // 长轴指向桌心
			List<Integer> river = st.riverCodes(h);
			for (int i = 0; i < river.size(); i++) {
				int rowIdx = i / RIVER_COLS;
				int col = i % RIVER_COLS;
				Vec3 target = rows[h].subtract(outs[h].scale(edge - RIVER_DIST - rowIdx * RIVER_ROW_PITCH))
						.add(geom.lefts()[h].scale(((RIVER_COLS - 1) / 2.0 - col) * flatStep));
				boolean riichiSide = i == st.riichiRiverIdx[h];
				Vec3 dir = riichiSide ? tangent : riverDir;
				out.add(new Placement(KIND_RIVER, flatEntityPos(target, 0), target, river.get(i),
						flatYaw(dir), dir, true, h, i, dealIndex++ * step));
			}
		}
		// 3) 副露（吃碰杠）：副露行向桌心内收（与手牌行错开带），组序从外向内游标式排开——
		//    每组按实际占宽（纵牌展开 + 声明牌伸出方向）推进，外缘不超过 MELD_EDGE_CAP，
		//    保证相邻两家副露在桌角处永不重叠；同一家的多组也不会因声明牌相对而压叠。
		//    整组朝向与横牌（声明牌）端位对应喂牌家座位——
		//    上家（左手边）横牌在组左端、下家（右手边）在右端、对家（对面）整组旋转 90° 朝桌心展开且横牌在朝心一端、
		//    自杠无来源方向取右端；横牌与相邻纵牌边距 = 组内牌缝（flatStep - 牌宽），紧贴无豁口。
		for (int h = 0; h < 4; h++) {
			Vec3 tangent = tangents[h];
			Vec3 facingCenter = outs[h].scale(-1);
			Vec3 playerRight = new Vec3(-facingCenter.z, 0, facingCenter.x); // 面朝桌心时的右手方向
			List<RichiTableState.MeldGroup> groups = st.meldGroupInfos(h);
			Vec3 meldRow = rows[h].subtract(outs[h].scale(MELD_ROW_INSET));
			double cursor = MELD_EDGE_CAP; // 游标：当前可用外缘（沿 playerRight 的标量）
			for (int g = 0; g < groups.size(); g++) {
				List<Integer> meld = groups.get(g).codes();
				if (meld.isEmpty())
					continue;
				int fromSeat = groups.get(g).fromSeat();
				int rel = fromSeat < 0 ? 0 : (fromSeat - h + 4) % 4;
				boolean toimen = rel == 2; // 喂牌家在对面：整组旋转 90° 朝桌心方向展开
				// 暗杠（来源 = 自己 且 4 张）：四张平行平躺，中间两张牌面朝下（盖牌）
				boolean ankan = fromSeat == h && meld.size() == 4;
				int flatCount = Math.max(0, meld.size() - 1);
				// 声明牌沿 playerRight 的伸出方向：+1 向外（桌角侧）、-1 向内、0 无（对家/暗杠，伸出垂直于轴）
				double claimDir = toimen || ankan ? 0
						: (rel == 1 || rel == 0 ? 1 : -1);
				double flush = (flatCount + 1) / 2.0 * flatStep + (TILE_LONG - TILE_WIDTH) / 2.0;
				// 组沿轴各侧占宽（中心到外缘）：纵牌展开 + 半牌宽；声明牌伸出侧为 flush + 半牌长
				double flatHalf = toimen ? TILE_LONG / 2
						: (flatCount - 1) / 2.0 * flatStep + TILE_WIDTH / 2;
				double claimHalf = flush + TILE_LONG / 2;
				double outer = claimDir > 0 ? claimHalf : flatHalf;
				double inner = claimDir < 0 ? claimHalf : flatHalf;
				double c = cursor - outer; // 组中心标量
				cursor = c - inner - MELD_GAP;
				Vec3 groupCenter = meldRow.add(playerRight.scale(c));
				Vec3 spread = toimen ? facingCenter : tangent;   // 纵牌展开方向 = 喂牌家方位
				Vec3 flatLong = toimen ? tangent : facingCenter; // 纵牌长轴（垂直于展开方向）
				if (ankan) {
					for (int j = 0; j < 4; j++) {
						Vec3 target = groupCenter.add(spread.scale((j - 1.5) * flatStep));
						boolean faceUp = j == 0 || j == 3; // 两端明牌，中间两张盖牌
						out.add(new Placement(KIND_MELD, flatEntityPos(target, 0), target, meld.get(j),
								flatYaw(flatLong), flatLong, faceUp, h, j, dealIndex++ * step));
					}
					continue;
				}
				for (int j = 0; j < flatCount; j++) {
					Vec3 target = groupCenter.add(spread.scale((j - (flatCount - 1) / 2.0) * flatStep));
					out.add(new Placement(KIND_MELD, flatEntityPos(target, 0), target, meld.get(j),
							flatYaw(flatLong), flatLong, true, h, j, dealIndex++ * step));
				}
				Vec3 claimedDir = toimen ? spread
						: (claimDir > 0 ? playerRight : playerRight.scale(-1));
				Vec3 claimedTarget = groupCenter.add(claimedDir.scale(flush));
				out.add(new Placement(KIND_MELD, flatEntityPos(claimedTarget, 0), claimedTarget,
						meld.get(meld.size() - 1), flatYaw(spread), spread, true, h, -1, dealIndex++ * step));
			}
		}
		// 3.5) 立直点棒：牌河前面（比第一排牌河更靠桌心），长轴平行于手牌行，多根依次向牌河方向堆叠
		for (int h = 0; h < 4; h++) {
			for (int k = 0; k < st.riichiSticks[h]; k++) {
				Vec3 stickPos = rows[h].subtract(outs[h].scale(edge - RIVER_DIST + 0.2 + k * 0.12));
				Vec3 toCenter = tangents[h];
				out.add(new Placement(KIND_STICK,
						new Vec3(stickPos.x, stickPos.y + 0.5 * TILE_SCALE, stickPos.z), stickPos,
						-1, stickYaw(toCenter), toCenter, true, h, k, dealIndex++ * step));
			}
		}
		// 4) 中央宝牌指示牌区：5 叠按叠序摆放，底张背面朝上；已揭开的叠数翻开上张（lift=FLAT_THICKNESS）
		Vec3 doraLong = f.scale(-1);
		List<Integer> dora = st.doraCodes();
		for (int i = 0; i < DORA_STACKS && 2 * i + 1 < dora.size(); i++) {
			Vec3 stackPos = center.add(f.scale(0.12)).add(r.scale((i - (DORA_STACKS - 1) / 2.0) * 0.157));
			out.add(new Placement(KIND_DORA, flatEntityPos(stackPos, 0), stackPos, dora.get(2 * i),
					flatYaw(doraLong), doraLong, false, -1, i, dealIndex++ * step));
			boolean revealed = i < st.revealedIndicators;
			out.add(new Placement(KIND_DORA, flatEntityPos(stackPos, FLAT_THICKNESS), stackPos, dora.get(2 * i + 1),
					flatYaw(doraLong), doraLong, revealed, -1, i, dealIndex++ * step));
		}
		return out;
	}
}
