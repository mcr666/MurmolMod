package mcr.richi.game.riichi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 牌效率评估（纯逻辑，无 MC 依赖）。
 * <p>算法移植自 mahjong-helper（MIT License，© 2018 Σndless，Go → Java）：</p>
 * <ul>
 * <li>向听数：ara.moo.jp 标准拆解递归（一般型 + 七对子，不含国士）——util/shanten_base.go</li>
 * <li>搜索树 + 综合评分（进张/前进后进张/改良/MixedWaitsScore）——util/shanten_search.go / shanten_improve.go 完整版</li>
 * <li>鸣牌评估——util/shanten_improve.go CalculateMeld</li>
 * </ul>
 * <p>34 数组表示：0-8 万 / 9-17 饼 / 18-26 索 / 27-33 字。</p>
 */
public final class TileEfficiency {
	private TileEfficiency() {
	}

	/** 和牌（向听 -1） */
	public static final int AGARI = -1;

	// ==================================================================
	// code（0m..9m/0p..9p/0s..9s/1z..7z）↔ 34 索引
	// ==================================================================

	public static int codeToIndex(int code) {
		if (code == 0)
			return 4; // 红5万
		if (code < 10)
			return code - 1; // 1-9m → 0-8
		if (code < 20)
			return code == 10 ? 13 : 8 + (code - 10); // 10=红5饼，11-19p → 9-17
		if (code < 30)
			return code == 20 ? 22 : 17 + (code - 20); // 20=红5索，21-29s → 18-26
		return 27 + (code - 30); // 30-36z → 27-33
	}

	// ==================================================================
	// 向听数
	// ==================================================================

	/** 七对子向听 = 6 - 对子数 + max(0, 7 - 种类数) */
	public static int shantenChiitoi(int[] t) {
		int shanten = 6;
		int numKind = 0;
		for (int c : t) {
			if (c == 0)
				continue;
			if (c >= 2)
				shanten--;
			numKind++;
		}
		shanten += Math.max(0, 7 - numKind);
		return shanten;
	}

	/** 向听数（一般型 + 七对，3k+1 / 3k+2 张手牌均适用；不含国士） */
	public static int shanten(int[] t) {
		int count = 0;
		for (int c : t)
			count += c;
		int min = shantenNormal(t, count);
		if (count >= 13)
			min = Math.min(min, shantenChiitoi(t));
		return min;
	}

	/** 一般型向听（副露通过 countOfTiles 折算：numberMelds = (14 - count) / 3） */
	public static int shantenNormal(int[] tiles, int countOfTiles) {
		if (SHANTEN_CACHE != null) {
			ShantenKey key = ShantenKey.of(tiles);
			Integer cached = SHANTEN_CACHE.get(key);
			if (cached != null)
				return cached;
			int r = shantenNormalUncached(tiles, countOfTiles);
			if (SHANTEN_CACHE.size() < 400_000)
				SHANTEN_CACHE.put(key, r);
			return r;
		}
		return shantenNormalUncached(tiles, countOfTiles);
	}

	private static int shantenNormalUncached(int[] tiles, int countOfTiles) {
		St st = new St(tiles, countOfTiles);
		st.scanCharacters(countOfTiles);
		for (int i = 0; i < 27; i++)
			if (tiles[i] == 4)
				st.ankan |= 1 << i;
		st.run(0);
		return st.min;
	}

	/** 和牌判定（14 张：1 雀头 + 4 面子） */
	public static boolean isAgari(int[] t) {
		for (int i = 0; i < 34; i++) {
			if (t[i] < 2)
				continue;
			t[i] -= 2;
			boolean ok = canMelds(t);
			t[i] += 2;
			if (ok)
				return true;
		}
		return false;
	}

	private static boolean canMelds(int[] t) {
		int i = 0;
		while (i < 34 && t[i] == 0)
			i++;
		if (i >= 34)
			return true;
		if (t[i] >= 3) {
			t[i] -= 3;
			boolean ok = canMelds(t);
			t[i] += 3;
			if (ok)
				return true;
		}
		if (i < 27 && i % 9 <= 6 && t[i + 1] > 0 && t[i + 2] > 0) {
			t[i]--;
			t[i + 1]--;
			t[i + 2]--;
			boolean ok = canMelds(t);
			t[i]++;
			t[i + 1]++;
			t[i + 2]++;
			if (ok)
				return true;
		}
		return false;
	}

	/** 拆解递归状态（翻译自 shanten struct） */
	private static final class St {
		final int[] tiles;
		int melds;
		int tatsu;
		int pairs;
		int jidahai; // 13 张后至少还需打出的字牌数（向听下限修正）
		int ankan; // 暗杠位压缩（27bit 数牌 | 1bit 字牌）
		int isolated; // 孤张位压缩（同上）
		int min = 8;

		St(int[] tiles, int countOfTiles) {
			this.tiles = tiles;
			this.melds = (14 - countOfTiles) / 3;
		}

		void scanCharacters(int countOfTiles) {
			int ankanT = 0, isoT = 0;
			for (int i = 27; i < 34; i++) {
				int c = tiles[i];
				switch (c) {
					case 1 -> isoT |= 1 << i;
					case 2 -> pairs++;
					case 3 -> melds++;
					case 4 -> {
						melds++;
						jidahai++;
						ankanT |= 1 << i;
						isoT |= 1 << i;
					}
				}
			}
			if (jidahai > 0 && countOfTiles % 3 == 2)
				jidahai--;
			if (isoT > 0) {
				isolated |= 1 << 27;
				if ((ankanT | isoT) == ankan)
					ankan |= 1 << 27; // 此孤张不能视作单骑做雀头的材料
			}
		}

		int calcNormal() {
			int sh = 8 - 2 * melds - tatsu - pairs;
			int kouho = melds + tatsu;
			if (pairs > 0)
				kouho += pairs - 1;
			else if (ankan > 0 && isolated > 0 && (ankan | isolated) == ankan)
				sh++; // 无雀头且除暗杠外无孤张（如 5555m 应算一向听）
			if (kouho > 4)
				sh += kouho - 4;
			if (sh != AGARI && sh < jidahai)
				return jidahai;
			return sh;
		}

		void run(int depth) {
			if (min == AGARI)
				return;
			while (depth < 27 && tiles[depth] == 0)
				depth++;
			if (depth >= 27) {
				min = Math.min(min, calcNormal());
				return;
			}
			int i = depth % 9;

			switch (tiles[depth]) {
				case 1 -> {
					if (i < 6 && tiles[depth + 1] == 1 && tiles[depth + 2] > 0 && tiles[depth + 3] < 4) {
						// 延べ単：顺子优先
						melds++;
						tiles[depth]--;
						tiles[depth + 1]--;
						tiles[depth + 2]--;
						run(depth + 2);
						tiles[depth]++;
						tiles[depth + 1]++;
						tiles[depth + 2]++;
						melds--;
					} else {
						// 浮牌
						isolated |= 1 << depth;
						run(depth + 1);
						isolated &= ~(1 << depth);
						if (i < 7 && tiles[depth + 2] > 0) {
							if (tiles[depth + 1] != 0) {
								melds++;
								tiles[depth]--;
								tiles[depth + 1]--;
								tiles[depth + 2]--;
								run(depth + 1);
								tiles[depth]++;
								tiles[depth + 1]++;
								tiles[depth + 2]++;
								melds--;
							}
							// 坎张搭子
							tatsu++;
							tiles[depth]--;
							tiles[depth + 2]--;
							run(depth + 1);
							tiles[depth]++;
							tiles[depth + 2]++;
							tatsu--;
						}
						if (i < 8 && tiles[depth + 1] > 0) {
							// 两面/边张搭子
							tatsu++;
							tiles[depth]--;
							tiles[depth + 1]--;
							run(depth + 1);
							tiles[depth]++;
							tiles[depth + 1]++;
							tatsu--;
						}
					}
				}
				case 2 -> {
					// 雀头
					tiles[depth] -= 2;
					pairs++;
					run(depth + 1);
					pairs--;
					tiles[depth] += 2;
					if (i < 7 && tiles[depth + 1] > 0 && tiles[depth + 2] > 0) {
						melds++;
						tiles[depth]--;
						tiles[depth + 1]--;
						tiles[depth + 2]--;
						run(depth);
						tiles[depth]++;
						tiles[depth + 1]++;
						tiles[depth + 2]++;
						melds--;
					}
				}
				case 3 -> {
					// 暗刻
					tiles[depth] -= 3;
					melds++;
					run(depth + 1);
					melds--;
					tiles[depth] += 3;

					tiles[depth] -= 2;
					pairs++;
					if (i < 7 && tiles[depth + 1] > 0 && tiles[depth + 2] > 0) {
						// 雀头 + 顺子
						melds++;
						tiles[depth]--;
						tiles[depth + 1]--;
						tiles[depth + 2]--;
						run(depth + 1);
						tiles[depth]++;
						tiles[depth + 1]++;
						tiles[depth + 2]++;
						melds--;
					} else {
						if (i < 7 && tiles[depth + 2] > 0) {
							// 雀头 + 坎张搭子
							tatsu++;
							tiles[depth]--;
							tiles[depth + 2]--;
							run(depth + 1);
							tiles[depth]++;
							tiles[depth + 2]++;
							tatsu--;
						}
						if (i < 8 && tiles[depth + 1] > 0) {
							// 雀头 + 两面/边张搭子
							tatsu++;
							tiles[depth]--;
							tiles[depth + 1]--;
							run(depth + 1);
							tiles[depth]++;
							tiles[depth + 1]++;
							tatsu--;
						}
					}
					pairs--;
					tiles[depth] += 2;

					if (i < 7 && tiles[depth + 1] >= 2 && tiles[depth + 2] >= 2) {
						// 一杯口
						melds += 2;
						tiles[depth] -= 2;
						tiles[depth + 1] -= 2;
						tiles[depth + 2] -= 2;
						run(depth);
						tiles[depth] += 2;
						tiles[depth + 1] += 2;
						tiles[depth + 2] += 2;
						melds -= 2;
					}
				}
				case 4 -> {
					tiles[depth] -= 3;
					melds++;
					if (i < 7 && tiles[depth + 2] > 0) {
						if (tiles[depth + 1] > 0) {
							// 暗刻 + 顺子
							tiles[depth]--;
							tiles[depth + 1]--;
							tiles[depth + 2]--;
							run(depth + 1);
							tiles[depth]++;
							tiles[depth + 1]++;
							tiles[depth + 2]++;
						}
						// 暗刻 + 坎张搭子
						tatsu++;
						tiles[depth]--;
						tiles[depth + 2]--;
						run(depth + 1);
						tiles[depth]++;
						tiles[depth + 2]++;
						tatsu--;
					}
					if (i < 8 && tiles[depth + 1] > 0) {
						// 暗刻 + 两面/边张搭子
						tatsu++;
						tiles[depth]--;
						tiles[depth + 1]--;
						run(depth + 1);
						tiles[depth]++;
						tiles[depth + 1]++;
						tatsu--;
					}
					// 暗刻 + 孤张
					isolated |= 1 << depth;
					run(depth + 1);
					isolated &= ~(1 << depth);
					melds--;
					tiles[depth] += 3;

					tiles[depth] -= 2;
					pairs++;
					if (i < 7 && tiles[depth + 2] > 0) {
						if (tiles[depth + 1] > 0) {
							// 雀头 + 顺子
							tiles[depth]--;
							tiles[depth + 1]--;
							tiles[depth + 2]--;
							run(depth);
							tiles[depth]++;
							tiles[depth + 1]++;
							tiles[depth + 2]++;
						}
						// 雀头 + 坎张搭子
						tatsu++;
						tiles[depth]--;
						tiles[depth + 2]--;
						run(depth + 1);
						tiles[depth]++;
						tiles[depth + 2]++;
						tatsu--;
					}
					if (i < 8 && tiles[depth + 1] > 0) {
						// 雀头 + 两面/边张搭子
						tatsu++;
						tiles[depth]--;
						tiles[depth + 1]--;
						run(depth + 1);
						tiles[depth]++;
						tiles[depth + 1]++;
						tatsu--;
					}
					pairs--;
					tiles[depth] += 2;
				}
			}
		}
	}

	// ==================================================================
	// 向听数记忆化缓存（仅批量计算期间启用，控制搜索树开销）
	// ==================================================================

	/** 34 数组压缩键（每格 3bit，0-4） */
	private record ShantenKey(long lo, long hi) {
		static ShantenKey of(int[] tiles) {
			long lo = 0, hi = 0;
			for (int i = 0; i < 17; i++)
				lo |= ((long) tiles[i] & 7) << (3 * i);
			for (int i = 17; i < 34; i++)
				hi |= ((long) tiles[i] & 7) << (3 * (i - 17));
			return new ShantenKey(lo, hi);
		}
	}

	private static HashMap<ShantenKey, Integer> SHANTEN_CACHE;

	/** 在缓存启用状态下批量执行（完成后清缓存） */
	private static <T> T withCache(java.util.function.Supplier<T> body) {
		SHANTEN_CACHE = new HashMap<>();
		try {
			return body.get();
		} finally {
			SHANTEN_CACHE = null;
		}
	}

	// ==================================================================
	// 搜索树（翻译自 shanten_search.go）
	// ==================================================================

	/** 3k+1 张手牌节点：进张（值 = 剩余枚数）+ 向听前进的摸牌 → 14 节点（null = 不再深入） */
	private static final class Node13 {
		final int shanten;
		final int[] waits = new int[34];
		final Map<Integer, Node14> children = new HashMap<>();
		int waitsCount;

		Node13(int shanten) {
			this.shanten = shanten;
		}
	}

	/** 14 张（摸牌后）节点：向听不变的舍牌 → 13 节点 */
	private static final class Node14 {
		final int shanten;
		final Map<Integer, Node13> children = new HashMap<>();

		Node14(int shanten) {
			this.shanten = shanten;
		}
	}

	/** 向听搜索截止深度（shanten_improve.go _stopShanten） */
	private static int stopShanten(int shanten) {
		return shanten >= 3 ? shanten - 1 : shanten - 2;
	}

	private static Node13 search13(int currentShanten, int[] tiles, int[] left, int stopAt) {
		Node13 node = new Node13(currentShanten);
		boolean isTenpai = currentShanten == 0;
		for (int i = 0; i < 34; i++) {
			if (tiles[i] == 4)
				continue;
			tiles[i]++;
			if (isTenpai) {
				// 优化：听牌时改用快速的 IsAgari
				if (isAgari(tiles))
					node.waits[i] = left[i];
			} else if (shanten(tiles) < currentShanten) {
				// 向听前进了，这张牌为进张，进张数即剩余枚数
				node.waits[i] = left[i];
				if (left[i] > 0 && currentShanten - 1 >= stopAt) {
					left[i]--;
					node.children.put(i, search14(currentShanten - 1, tiles, left, stopAt));
					left[i]++;
				} else {
					node.children.put(i, null);
				}
			}
			tiles[i]--;
		}
		for (int w : node.waits)
			node.waitsCount += w;
		return node;
	}

	private static Node14 search14(int targetShanten, int[] tiles, int[] left, int stopAt) {
		Node14 node = new Node14(targetShanten);
		for (int i = 0; i < 34; i++) {
			if (tiles[i] == 0)
				continue;
			tiles[i]--;
			if (shanten(tiles) == targetShanten)
				node.children.put(i, search13(targetShanten, tiles, left, stopAt));
			tiles[i]++;
		}
		return node;
	}

	// ==================================================================
	// 13 张手牌分析（翻译自 shanten_improve.go node13.analysis，去掉役种/打点/和率部分）
	// ==================================================================

	/** 3k+1 张手牌的分析结果 */
	public static final class Analysis13 {
		public int shanten;
		/** 进张枚数（考虑剩余枚数） */
		public int waitsCount;
		/** 向听前进后的（最大）进张枚数加权均值 */
		public double avgNext;
		/** 综合了进张与向听前进后进张的评分（≈向听前进两次概率 × 100；二向听时 /4） */
		public double mixedScore;
		/** 摸到非进张牌时的进张数加权均值（含改良） */
		public double avgImprove;
	}

	/**
	 * 3k+1 张牌：计算向听数、进张、改良（考虑剩余枚数）。
	 * 移植自 CalculateShantenWithImproves13。
	 *
	 * @param tiles 34 数组（3k+1 张），会被临时修改但恢复
	 * @param left  剩余枚数 34 数组，同上
	 */
	public static Analysis13 analyze13(int[] tiles, int[] left) {
		int count = 0;
		for (int c : tiles)
			count += c;
		int shanten = shanten(tiles);
		Node13 root = search13(shanten, tiles, left, stopShanten(shanten));
		return analyze13(root, tiles, left, true, count);
	}

	private static Analysis13 analyze13(Node13 n, int[] tiles, int[] left, boolean considerImprove, int tileCount) {
		int shanten13 = n.shanten;
		int waitsCount = n.waitsCount;

		Map<Integer, Integer> nextMap = new HashMap<>();
		int improveWayCount = 0;
		int[] maxImprove = new int[34];
		for (int i = 0; i < 34; i++)
			maxImprove[i] = waitsCount;
		boolean hasImprove = false;

		for (int i = 0; i < 34; i++) {
			if (left[i] == 0)
				continue;
			left[i]--;
			tiles[i]++;

			Node14 child = n.children.get(i);
			if (child != null) {
				// 摸到的是进张：计算最大向听前进后的进张
				int maxNext = 0;
				for (Node13 c : child.children.values())
					maxNext = Math.max(maxNext, c.waitsCount);
				nextMap.put(i, maxNext);
			} else if (considerImprove) {
				// 摸到的不是进张，但可能有改良（摸 i 切 j 后进张变多）
				for (int j = 0; j < 34; j++) {
					if (tiles[j] == 0 || j == i)
						continue;
					tiles[j]--;
					if (shanten(tiles) == shanten13) {
						int wc = shantenAndWaits13Count(tiles, left);
						if (wc > waitsCount) {
							improveWayCount++;
							if (wc > maxImprove[i])
								maxImprove[i] = wc;
							hasImprove = true;
						}
					}
					tiles[j]++;
				}
			}

			tiles[i]--;
			left[i]++;
		}

		Analysis13 r = new Analysis13();
		r.shanten = shanten13;
		r.waitsCount = waitsCount;

		if (shanten13 == 0) {
			// 听牌：Go 版此处分按局收支/和率排序（需役种/打点表，未移植），
			// 改为按进张枚数单调排序（+100 保证高于一切一向听评分）
			r.avgNext = waitsCount;
			r.mixedScore = 100 + waitsCount;
		} else {
			r.avgNext = weightedAvgNext(nextMap, left);
			r.mixedScore = speedScore(waitsCount, r.avgNext, tileCount > 0 ? leftSum(left) : 1);
			if (shanten13 == 2)
				r.mixedScore /= 4; // 二向听特殊处理（与 Go 版一致）
		}
		if (hasImprove) {
			int sum = 0, weight = 0;
			for (int i = 0; i < 34; i++) {
				int w = left[i];
				sum += w * maxImprove[i];
				weight += w;
			}
			r.avgImprove = weight > 0 ? (double) sum / weight : waitsCount;
		} else {
			r.avgImprove = waitsCount;
		}
		return r;
	}

	private static double weightedAvgNext(Map<Integer, Integer> nextMap, int[] left) {
		int sum = 0, weight = 0;
		for (Map.Entry<Integer, Integer> e : nextMap.entrySet()) {
			int w = left[e.getKey()];
			sum += w * e.getValue();
			weight += w;
		}
		return weight > 0 ? (double) sum / weight : 0;
	}

	/** 3k+1 张牌：向听 + 进张枚数（一层搜索，CalculateShantenAndWaits13 的枚数版） */
	private static int shantenAndWaits13Count(int[] tiles, int[] left) {
		int shanten = shanten(tiles);
		Node13 node = search13(shanten, tiles, left, shanten);
		return node.waitsCount;
	}

	private static int leftSum(int[] left) {
		int sum = 0;
		for (int c : left)
			sum += c;
		return Math.max(sum, 1);
	}

	/**
	 * speedScore（mahjong-helper）：近似"向听前进两次"的概率强度，剩余 10 巡内。
	 */
	private static double speedScore(double waitsCount, double avgNext, int leftCount) {
		if (waitsCount <= 0 || avgNext <= 0 || leftCount <= 0)
			return 0;
		double lc = leftCount;
		double p2 = waitsCount / lc;
		double p1 = Math.min(1.0, avgNext / lc);
		double p2i = 1 - p2, p1i = 1 - p1;
		if (p2i <= 0 || p1i <= 0)
			return 100;
		final double leftTurns = 10.0;
		double sumP2 = p2i * (1 - Math.pow(p2i, leftTurns)) / p2;
		double sumP1 = p1i * (1 - Math.pow(p1i, leftTurns)) / p1;
		return p2 * p1 * (sumP2 - sumP1) / (p2i - p1i) * 100;
	}

	// ==================================================================
	// 牌效率选牌（翻译自 CalculateShantenWithImproves14 的选牌用法）
	// ==================================================================

	/**
	 * 牌效率选牌：对每张可打牌用搜索树分析（进张 + 前进后进张 + 改良的综合分），
	 * 取最高分（平分随机 tie-break，宝牌留牌倾向）。
	 *
	 * @param handCodes 手牌 code（3k+2 张，含刚摸的）
	 * @param visible34 全场可见牌 34 数组（含自己手牌；剩余 = 4 - 可见）
	 * @param dora34    当前宝牌（指示牌下一张）34 布尔
	 * @param random    tie-break 随机源
	 * @param requireTenpai 立直宣言打牌约束：必须打后听牌（sh13 == 0）
	 * @return 打出的手牌下标
	 */
	public static int chooseDiscard(List<Integer> handCodes, int[] visible34, boolean[] dora34,
			java.util.function.DoubleSupplier random, boolean requireTenpai) {
		return chooseDiscard(handCodes, visible34, dora34, random, requireTenpai, null, 0.0, 0);
	}

	/**
	 * 牌效率选牌（可带防守 + 次优选择）。
	 *
	 * @param danger34 每种牌的铳率百分数（0-30；null = 不考虑防守）
	 * @param dangerWeight 防守权重（每 1% 铳率的评分罚分；0 = 无视危险）
	 * @param rank 0=最高权重切牌；1=权重第二高的切牌（一般流 20% 概率用）
	 */
	public static int chooseDiscard(List<Integer> handCodes, int[] visible34, boolean[] dora34,
			java.util.function.DoubleSupplier random, boolean requireTenpai,
			double[] danger34, double dangerWeight, int rank) {
		int n = handCodes.size();
		int[] tiles = new int[34];
		for (int c : handCodes)
			tiles[codeToIndex(c)]++;
		int[] left = new int[34];
		for (int i = 0; i < 34; i++)
			left[i] = Math.max(0, 4 - visible34[i]);

		int cur = shanten(tiles); // 14 张向听
		if (cur == AGARI)
			return n - 1; // 已和牌（理论上不会走到）：摸切保底
		return withCache(() -> chooseDiscardInner(handCodes, tiles, left, dora34, random, requireTenpai,
				danger34, dangerWeight, rank, cur));
	}

	private static int chooseDiscardInner(List<Integer> handCodes, int[] tiles, int[] left, boolean[] dora34,
			java.util.function.DoubleSupplier random, boolean requireTenpai,
			double[] danger34, double dangerWeight, int rank, int cur) {
		int n = handCodes.size();
		int stopAt = stopShanten(cur);
		// 正确舍牌树（打后向听不变）+ 倒退舍牌树（打后向听 +1）
		Node14 correctRoot = search14(cur, tiles, left, stopAt);
		Node14 incRoot = search14(cur + 1, tiles, left, stopAt + 1);

		int bestIdx = n - 1, secondIdx = n - 1;
		double bestScore = Double.NEGATIVE_INFINITY, secondScore = Double.NEGATIVE_INFINITY;
		boolean[] seen = new boolean[34];
		for (int hi = 0; hi < n; hi++) {
			int d = codeToIndex(handCodes.get(hi));
			if (seen[d])
				continue; // 同种牌只评估一次
			seen[d] = true;
			tiles[d]--;
			double score;
			Node13 node = correctRoot.children.get(d);
			if (node != null) {
				Analysis13 a = analyze13(node, tiles, left, false, 13);
				score = a.mixedScore;
				// 立直宣言：打后必须听牌
				if (requireTenpai && a.shanten != 0)
					score = -1000 + (dora34 != null && dora34[d] ? 1 : 0);
				// 宝牌留牌倾向（打宝牌小罚分）
				else if (dora34 != null && dora34[d])
					score -= 1.5;
			} else {
				// 向听倒退（或立直约束失败）：重罚
				boolean inInc = incRoot.children.containsKey(d);
				score = inInc ? -500 : -1000;
				if (dora34 != null && dora34[d])
					score += 1;
			}
			// 防守：铳率罚分
			if (danger34 != null && dangerWeight > 0 && d < danger34.length)
				score -= dangerWeight * danger34[d];
			tiles[d]++;
			score += random.getAsDouble() * 0.01; // 微扰 tie-break
			if (score > bestScore) {
				secondScore = bestScore;
				secondIdx = bestIdx;
				bestScore = score;
				bestIdx = hi;
			} else if (score > secondScore) {
				secondScore = score;
				secondIdx = hi;
			}
		}
		return rank >= 1 ? secondIdx : bestIdx;
	}

	// ==================================================================
	// 鸣牌评估（翻译自 shanten_improve.go calculateMeldShanten / CalculateMeld）
	// ==================================================================

	/** 一种鸣牌方案（碰/吃）的评估结果 */
	public static final class MeldOption {
		/** 自家手牌中组成该副露的两张 code（吃 = 组合，碰 = 重复两张） */
		public int[] selfCodes;
		/** 鸣牌后（最优切牌下）的综合评分 */
		public double score;
		/** 鸣牌后最优切牌的向听数 */
		public int shanten;
		/** 是否为碰 */
		public boolean pon;
	}

	/**
	 * 鸣牌评估：枚举碰/吃方案，各自计算鸣牌后手牌（3k+2 张）的最优切牌综合分。
	 * 调用方自行与不鸣牌的基线分比较决定是否鸣。
	 *
	 * @param handCodes  自家 13 张手牌 code
	 * @param visible34  全场可见牌（不含这张被鸣的牌）
	 * @param calledCode 被鸣的牌 code
	 * @param allowChi   是否允许吃
	 */
	public static List<MeldOption> evaluateMelds(List<Integer> handCodes, int[] visible34,
			int calledCode, boolean allowChi) {
		return withCache(() -> evaluateMeldsInner(handCodes, visible34, calledCode, allowChi));
	}

	private static List<MeldOption> evaluateMeldsInner(List<Integer> handCodes, int[] visible34,
			int calledCode, boolean allowChi) {
		List<MeldOption> out = new ArrayList<>();
		int[] tiles = new int[34];
		for (int c : handCodes)
			tiles[codeToIndex(c)]++;
		int called = codeToIndex(calledCode);
		int[] left = new int[34];
		for (int i = 0; i < 34; i++)
			left[i] = Math.max(0, 4 - visible34[i]);
		left[called] = Math.max(0, left[called] - 1); // 被鸣的牌不在山中

		// 碰
		if (tiles[called] >= 2) {
			MeldOption o = evalMeldOption(handCodes, calledCode, new int[] { calledCode, calledCode }, true,
					tiles, left, called);
			if (o != null)
				out.add(o);
		}
		// 吃（三种组合）
		if (allowChi && called < 27) {
			int t9 = called % 9;
			List<int[]> pairs = new ArrayList<>();
			if (t9 >= 2)
				pairs.add(new int[] { called - 2, called - 1 });
			if (t9 >= 1 && t9 <= 7)
				pairs.add(new int[] { called - 1, called + 1 });
			if (t9 <= 6)
				pairs.add(new int[] { called + 1, called + 2 });
			for (int[] p : pairs) {
				if (tiles[p[0]] > 0 && tiles[p[1]] > 0) {
					int idxA = indexToCode(p[0]), idxB = indexToCode(p[1]);
					MeldOption o = evalMeldOption(handCodes, calledCode, new int[] { idxA, idxB }, false,
							tiles, left, called);
					if (o != null)
						out.add(o);
				}
			}
		}
		return out;
	}

	private static int indexToCode(int idx) {
		return idx < 9 ? idx + 1 : idx < 18 ? idx - 8 + 10 : idx < 27 ? idx - 17 + 20 : idx - 27 + 30;
	}

	private static MeldOption evalMeldOption(List<Integer> handCodes, int calledCode, int[] selfCodes,
			boolean pon, int[] tiles, int[] left, int called) {
		tiles[codeToIndex(selfCodes[0])]--;
		tiles[codeToIndex(selfCodes[1])]--;
		// 鸣牌后手牌 = 3k+2 张（剩余手牌 11 张，副露 1 组由 count 折算）
		int shanten = shanten(tiles);
		int stopAt = stopShanten(shanten);
		Node14 root = search14(shanten, tiles, left, stopAt);
		double bestScore = Double.NEGATIVE_INFINITY;
		int bestShanten = shanten;
		for (Map.Entry<Integer, Node13> e : root.children.entrySet()) {
			Analysis13 a = analyze13(e.getValue(), tiles, left, false, 13);
			if (a.mixedScore > bestScore) {
				bestScore = a.mixedScore;
				bestShanten = a.shanten;
			}
		}
		tiles[codeToIndex(selfCodes[0])]++;
		tiles[codeToIndex(selfCodes[1])]++;
		if (bestScore == Double.NEGATIVE_INFINITY)
			return null;
		MeldOption o = new MeldOption();
		o.selfCodes = selfCodes;
		o.score = bestScore;
		o.shanten = bestShanten;
		o.pon = pon;
		return o;
	}
}
