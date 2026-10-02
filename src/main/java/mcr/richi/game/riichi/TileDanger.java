package mcr.richi.game.riichi;

import java.util.ArrayList;
import java.util.List;

/**
 * 科学防守危险度（纯逻辑，无 MC 依赖）。
 * <p>算法移植自 mahjong-helper（MIT License，© 2018 Σndless，Go → Java）：</p>
 * <ul>
 * <li>util/risk_data.go：铳率表（巡目 × 牌类型）、宝牌铳率修正</li>
 * <li>util/risk_base.go：筋/半筋/无筋、壁（断牌）、NC/DNC、现物铳率计算</li>
 * <li>util/risk_wall.go：No Chance / Double No Chance 安牌判定</li>
 * </ul>
 * <p>34 数组表示与 {@link TileEfficiency} 一致。铳率单位为百分数（0-30）。</p>
 */
public final class TileDanger {
	private TileDanger() {
	}

	// ==================================================================
	// tileType（与 RiskRate 列一一对应）
	// ==================================================================
	private static final int T_NOSUJI5 = 0, T_NOSUJI46 = 1, T_NOSUJI37 = 2, T_NOSUJI28 = 3, T_NOSUJI19 = 4;
	private static final int T_HALFSUJI5 = 5, T_HALFSUJI46A = 6, T_HALFSUJI46B = 7;
	private static final int T_SUJI37 = 8, T_SUJI28 = 9, T_SUJI19 = 10;
	private static final int T_DOUBLESUJI5 = 11, T_DOUBLESUJI46 = 12;
	private static final int T_YAKUHAI_L3 = 13, T_YAKUHAI_L2 = 14, T_YAKUHAI_L1 = 15;
	private static final int T_OTAKAZE_L3 = 16, T_OTAKAZE_L2 = 17, T_OTAKAZE_L1 = 18;

	/** [巡目][类型]（巡目 = 对手舍牌数，1 起；超出取最后一行） */
	private static final double[][] RISK_RATE = {
			{},
			{5.7, 5.7, 5.8, 4.7, 3.4, 2.5, 2.5, 3.1, 5.6, 3.8, 1.8, 0.8, 2.6, 2.1, 1.2, 0.5, 2.4, 1.4, 1.2},
			{6.6, 6.9, 6.3, 5.2, 4.0, 3.5, 3.5, 4.1, 5.3, 3.5, 1.9, 0.8, 2.6, 2.3, 1.2, 0.5, 2.7, 1.3, 0.4},
			{7.7, 8.0, 6.7, 5.8, 4.6, 4.3, 4.1, 4.9, 5.2, 3.6, 1.8, 1.6, 2.0, 2.4, 1.2, 0.3, 2.6, 1.2, 0.3},
			{8.5, 8.9, 7.1, 6.2, 5.1, 4.8, 4.7, 5.6, 5.2, 3.8, 1.7, 1.6, 2.0, 2.6, 1.1, 0.2, 2.6, 1.2, 0.2},
			{9.4, 9.7, 7.5, 6.7, 5.5, 5.3, 5.1, 6.0, 5.3, 3.7, 1.7, 1.7, 2.0, 2.9, 1.2, 0.2, 2.8, 1.2, 0.2},
			{10.2, 10.5, 7.9, 7.1, 5.9, 5.8, 5.6, 6.4, 5.2, 3.7, 1.7, 1.8, 2.0, 3.2, 1.3, 0.2, 2.9, 1.3, 0.2},
			{11.0, 11.3, 8.4, 7.5, 6.3, 6.3, 6.1, 6.8, 5.3, 3.7, 1.7, 2.0, 2.1, 3.6, 1.4, 0.2, 3.2, 1.4, 0.2},
			{11.9, 12.2, 8.9, 8.0, 6.8, 6.9, 6.6, 7.4, 5.3, 3.8, 1.7, 2.1, 2.2, 4.0, 1.6, 0.2, 3.5, 1.6, 0.2},
			{12.8, 13.1, 9.5, 8.6, 7.4, 7.4, 7.2, 7.9, 5.5, 3.9, 1.8, 2.2, 2.3, 4.6, 1.9, 0.3, 4.0, 1.8, 0.2},
			{13.8, 14.1, 10.1, 9.2, 8.0, 8.0, 7.8, 8.5, 5.6, 4.0, 1.9, 2.4, 2.4, 5.3, 2.2, 0.3, 4.6, 2.1, 0.3},
			{14.9, 15.1, 10.8, 9.9, 8.7, 8.7, 8.5, 9.2, 5.7, 4.2, 2.0, 2.5, 2.6, 6.0, 2.6, 0.4, 5.1, 2.5, 0.3},
			{16.0, 16.3, 11.6, 10.6, 9.4, 9.4, 9.2, 9.9, 6.0, 4.4, 2.2, 2.7, 2.7, 6.8, 3.1, 0.4, 5.1, 2.5, 0.3},
			{17.2, 17.5, 12.4, 11.4, 10.2, 10.2, 10.0, 10.6, 6.2, 4.6, 2.4, 3.0, 3.0, 7.8, 3.7, 0.5, 6.6, 3.7, 0.5},
			{18.5, 18.8, 13.3, 12.3, 11.1, 11.0, 10.9, 11.4, 6.6, 4.9, 2.7, 3.2, 3.1, 8.8, 4.4, 0.7, 7.4, 4.4, 0.6},
			{19.9, 20.1, 14.3, 13.3, 12.0, 11.9, 11.8, 12.3, 7.0, 5.3, 3.0, 3.4, 3.4, 9.9, 5.2, 0.8, 8.4, 5.3, 0.8},
			{21.3, 21.7, 15.4, 14.3, 13.1, 12.9, 12.8, 13.3, 7.4, 5.7, 3.3, 3.7, 3.6, 11.2, 6.2, 1.0, 9.4, 6.5, 0.9},
			{22.9, 23.2, 16.6, 15.4, 14.2, 14.0, 13.8, 14.4, 8.0, 6.1, 3.6, 3.9, 3.9, 12.4, 7.3, 1.3, 10.5, 7.7, 1.2},
			{24.7, 24.9, 17.9, 16.7, 15.4, 15.2, 15.0, 15.6, 8.5, 6.6, 4.0, 4.3, 4.2, 13.9, 8.5, 1.7, 11.8, 9.4, 1.6},
			{27.5, 27.8, 20.4, 19.1, 17.8, 17.5, 17.5, 17.5, 9.8, 7.4, 5.0, 5.1, 5.1, 18.1, 12.1, 2.8, 14.7, 12.6, 2.1},
	};

	/** 宝牌铳率修正倍数（综合放铳率与失点，与 tileType 下标一一对应） */
	private static final double[] FIXED_DORA_MULTI = {
			14.9 / 12.8 * 78 / 58, 15.0 / 13.1 * 78 / 58, 12.1 / 9.5 * 75 / 56, 10.3 / 8.6 * 75 / 54,
			8.9 / 7.4 * 77 / 53, 9.7 / 7.4 * 81 / 60, 8.9 / 7.2 * 81 / 60, 10.4 / 7.9 * 81 / 60,
			8.0 / 5.5 * 75 / 56, 5.5 / 3.9 * 81 / 56, 3.5 / 1.8 * 92 / 58, 4.1 / 2.2 * 88 / 62,
			4.1 / 2.3 * 88 / 62, 5.2 / 4.6 * 96 / 67, 2.9 / 1.9 * 96 / 67, 1.1 / 0.3 * 96 / 67,
			5.1 / 4.0 * 92 / 56, 3.0 / 1.8 * 92 / 56, 0.8 / 0.2 * 92 / 56,
	};

	/**
	 * [需要判断危险度的牌号 0-8][对应现物状态] → tileType。
	 * 123789: 0=无现物 1=有现物；456: 0=无17现物 1=无1有7 2=有1无7 3=有17。
	 */
	private static final int[][] TILE_TYPE_TABLE = {
			{T_NOSUJI19, T_SUJI19}, {T_NOSUJI28, T_SUJI28}, {T_NOSUJI37, T_SUJI37},
			{T_NOSUJI46, T_HALFSUJI46B, T_HALFSUJI46A, T_DOUBLESUJI46},
			{T_NOSUJI5, T_HALFSUJI5, T_HALFSUJI5, T_DOUBLESUJI5},
			{T_NOSUJI46, T_HALFSUJI46A, T_HALFSUJI46B, T_DOUBLESUJI46},
			{T_NOSUJI37, T_SUJI37}, {T_NOSUJI28, T_SUJI28}, {T_NOSUJI19, T_SUJI19},
	};

	/** [是否役牌 0-1][剩余数-1] → tileType */
	private static final int[][] HONOR_TILE_TYPE = {
			{T_OTAKAZE_L1, T_OTAKAZE_L2, T_OTAKAZE_L3, T_OTAKAZE_L3},
			{T_YAKUHAI_L1, T_YAKUHAI_L2, T_YAKUHAI_L3, T_YAKUHAI_L3},
	};

	// ==================================================================
	// 壁安牌（No Chance / Double No Chance）
	// ==================================================================

	/** 壁安牌：Tile34 索引 + 安全类型（数值越小越安全） */
	public record WallSafeTile(int tile34, int safeType) {
	}

	private static final int SAFE_DNC = 0; // 只输单骑/对碰
	private static final int SAFE_NC = 1; // 不输两面

	/** No Chance 安牌（根据壁：不输两面） */
	private static List<WallSafeTile> calcNCSafeTiles(int[] leftTiles34) {
		List<WallSafeTile> out = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int idx = 9 * i + j;
				if (nc(leftTiles34, idx + 1) || nc(leftTiles34, idx + 2))
					out.add(new WallSafeTile(idx, SAFE_NC));
			}
			for (int j = 3; j < 6; j++) {
				int idx = 9 * i + j;
				if ((nc(leftTiles34, idx - 2) || nc(leftTiles34, idx - 1))
						&& (nc(leftTiles34, idx + 1) || nc(leftTiles34, idx + 2)))
					out.add(new WallSafeTile(idx, SAFE_NC));
			}
			for (int j = 6; j < 9; j++) {
				int idx = 9 * i + j;
				if (nc(leftTiles34, idx - 2) || nc(leftTiles34, idx - 1))
					out.add(new WallSafeTile(idx, SAFE_NC));
			}
		}
		return out;
	}

	/** Double No Chance 安牌（壁 + 现物：只输单骑、对碰） */
	private static List<WallSafeTile> calcDNCSafeTilesWithDiscards(int[] leftTiles34, boolean[] safeTiles34) {
		List<WallSafeTile> out = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			// 2/3断的1
			if (nc(leftTiles34, 9 * i + 1) || nc(leftTiles34, 9 * i + 2))
				out.add(new WallSafeTile(9 * i, SAFE_DNC));
			// 3/14断的2
			if (nc(leftTiles34, 9 * i + 2) || (nc(leftTiles34, 9 * i) && nc(leftTiles34, 9 * i + 3)))
				out.add(new WallSafeTile(9 * i + 1, SAFE_DNC));
			// 14/24/25断的3（4567 同理）
			for (int j = 2; j <= 6; j++) {
				int idx = 9 * i + j;
				if ((nc(leftTiles34, idx - 2) && nc(leftTiles34, idx + 1))
						|| (nc(leftTiles34, idx - 1) && nc(leftTiles34, idx + 1))
						|| (nc(leftTiles34, idx - 1) && nc(leftTiles34, idx + 2)))
					out.add(new WallSafeTile(idx, SAFE_DNC));
			}
			// 7/69断的8
			if (nc(leftTiles34, 9 * i + 6) || (nc(leftTiles34, 9 * i + 5) && nc(leftTiles34, 9 * i + 8)))
				out.add(new WallSafeTile(9 * i + 7, SAFE_DNC));
			// 7/8断的9
			if (nc(leftTiles34, 9 * i + 6) || nc(leftTiles34, 9 * i + 7))
				out.add(new WallSafeTile(9 * i + 8, SAFE_DNC));
		}
		// 相邻一侧为壁、另一侧为现物筋 → DNC
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j < 3; j++) {
				int idx = 9 * i + j;
				if (nc(leftTiles34, idx - 1) && safeTiles34[idx + 3])
					out.add(new WallSafeTile(idx, SAFE_DNC));
			}
			for (int j = 3; j < 6; j++) {
				int idx = 9 * i + j;
				if ((nc(leftTiles34, idx - 1) && safeTiles34[idx + 3])
						|| (nc(leftTiles34, idx + 1) && safeTiles34[idx - 3]))
					out.add(new WallSafeTile(idx, SAFE_DNC));
			}
			for (int j = 6; j < 8; j++) {
				int idx = 9 * i + j;
				if (nc(leftTiles34, idx + 1) && safeTiles34[idx - 3])
					out.add(new WallSafeTile(idx, SAFE_DNC));
			}
		}
		return out;
	}

	private static boolean nc(int[] leftTiles34, int idx) {
		return leftTiles34[idx] == 0;
	}

	// ==================================================================
	// 危险度计算
	// ==================================================================

	/**
	 * 计算对某对手的基础铳率表。
	 *
	 * @param turns         该对手的舍牌数（巡目，1 起）
	 * @param safeTiles34   现物及立直后通过的牌（该对手牌河）
	 * @param leftTiles34   各牌在山中剩余枚数
	 * @param doraTiles34   当前宝牌 34 索引列表（可重复 = 多重宝牌）
	 * @param roundWindTile 场风 34 索引（27-30）
	 * @param playerWindTile 该对手自风 34 索引
	 * @return 34 数组，铳率百分数
	 */
	public static double[] calculateRisk(int turns, boolean[] safeTiles34, int[] leftTiles34,
			List<Integer> doraTiles34, int roundWindTile, int playerWindTile) {
		double[] risk = new double[34];
		int t = Math.max(1, Math.min(turns, RISK_RATE.length - 1));
		double[] rate = RISK_RATE[t];

		// 低危险牌（现物 + 断牌推定筋），用于生成筋
		int[] lowRisk27 = calcLowRiskTiles27(safeTiles34, leftTiles34);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int idx = 9 * i + j;
				int type = TILE_TYPE_TABLE[j][lowRisk27[idx + 3]];
				risk[idx] = rate[type] * doraMulti(doraTiles34, idx, type);
				if (j == 0 && safeTiles34[idx + 3] && leftTiles34[idx] == 0)
					risk[idx] = 0; // (1) 两面/对碰单骑都不可能 → 安牌
			}
			for (int j = 3; j < 6; j++) {
				int idx = 9 * i + j;
				int mix = lowRisk27[idx - 3] << 1 | lowRisk27[idx + 3];
				int type = TILE_TYPE_TABLE[j][mix];
				risk[idx] = rate[type] * doraMulti(doraTiles34, idx, type);
			}
			for (int j = 6; j < 9; j++) {
				int idx = 9 * i + j;
				int type = TILE_TYPE_TABLE[j][lowRisk27[idx - 3]];
				risk[idx] = rate[type] * doraMulti(doraTiles34, idx, type);
				if (j == 8 && safeTiles34[idx - 3] && leftTiles34[idx] == 0)
					risk[idx] = 0; // (9) 同上
			}
			// 5断，37 视作安牌筋
			if (leftTiles34[9 * i + 4] == 0) {
				risk[9 * i + 2] = rate[T_SUJI37] * doraMulti(doraTiles34, 9 * i + 2, T_SUJI37);
				risk[9 * i + 6] = rate[T_SUJI37] * doraMulti(doraTiles34, 9 * i + 6, T_SUJI37);
			}
		}
		for (int i = 27; i < 34; i++) {
			if (leftTiles34[i] > 0) {
				boolean isYakuHai = i == roundWindTile || i == playerWindTile || i >= 31;
				int type = HONOR_TILE_TYPE[isYakuHai ? 1 : 0][Math.min(leftTiles34[i], 4) - 1];
				risk[i] = rate[type] * doraMulti(doraTiles34, i, type);
			} else {
				risk[i] = 0; // 剩余 0 视作安牌（忽略国士）
			}
		}

		// No Chance 的危险度
		for (WallSafeTile t2 : calcNCSafeTiles(leftTiles34)) {
			int idx = t2.tile34();
			int n = idx % 9 + 1;
			double r;
			switch (n) {
				case 1, 9 -> r = rate[T_SUJI19];
				case 2, 8 -> r = rate[T_SUJI19] * 1.1;
				case 3, 7 -> r = rate[T_SUJI28];
				case 4, 6 -> r = rate[T_DOUBLESUJI46];
				default -> r = rate[T_DOUBLESUJI5];
			}
			risk[idx] = r * doraMulti(doraTiles34, idx, n <= 2 || n >= 8 ? T_SUJI19 : n <= 3 || n >= 7 ? T_SUJI28 : T_DOUBLESUJI46);
		}

		// Double No Chance 的危险度
		for (WallSafeTile t2 : calcDNCSafeTilesWithDiscards(leftTiles34, safeTiles34)) {
			int idx = t2.tile34();
			if (leftTiles34[idx] > 0) {
				double r = rate[T_SUJI19] * doraMulti(doraTiles34, idx, T_SUJI19);
				if (idx % 9 > 0 && idx % 9 < 8)
					r *= 1.1; // 非 19 仍有断幺危险
				risk[idx] = r;
			} else {
				risk[idx] = 0;
			}
		}

		// 现物铳率为 0
		for (int i = 0; i < 34; i++)
			if (safeTiles34[i])
				risk[i] = 0;

		return risk;
	}

	/** 宝牌铳率修正（多重宝牌连乘） */
	private static double doraMulti(List<Integer> doraTiles34, int tile, int type) {
		double multi = 1.0;
		for (int dora : doraTiles34)
			if (tile == dora)
				multi *= FIXED_DORA_MULTI[type];
		return multi;
	}

	/**
	 * 低危险牌表：现物 + 断牌推定筋（如 2 断则 1 视作打过 1 的筋）。
	 * 移植自 risk_base.go calcLowRiskTiles27。
	 */
	private static int[] calcLowRiskTiles27(boolean[] safeTiles34, int[] leftTiles34) {
		int[] low = new int[27];
		for (int i = 0; i < 27; i++)
			if (safeTiles34[i])
				low[i] = 1;
		for (int i = 0; i < 3; i++) {
			if (leftTiles34[9 * i + 1] == 0) // 2断 → 1
				low[9 * i] = 1;
			if (leftTiles34[9 * i + 2] == 0) { // 3断 → 12
				low[9 * i] = 1;
				low[9 * i + 1] = 1;
			}
			if (leftTiles34[9 * i + 3] == 0) { // 4断 → 23
				low[9 * i + 1] = 1;
				low[9 * i + 2] = 1;
			}
			if (leftTiles34[9 * i + 5] == 0) { // 6断 → 78
				low[9 * i + 6] = 1;
				low[9 * i + 7] = 1;
			}
			if (leftTiles34[9 * i + 6] == 0) { // 7断 → 89
				low[9 * i + 7] = 1;
				low[9 * i + 8] = 1;
			}
			if (leftTiles34[9 * i + 7] == 0) // 8断 → 9
				low[9 * i + 8] = 1;
		}
		return low;
	}
}
