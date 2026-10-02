package mcr.richi.game.riichi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * 对局 AI（纯逻辑）：虚拟 UUID 固定占位，名字 AI·東/南/西/北。
 * <p>流派（由 AI 形象/名字决定，见 {@link #flowOfAvatar}）：</p>
 * <ul>
 * <li>0 一般流：20% 概率打权重第二高的切牌；可副露、可防守</li>
 * <li>1 野猪流：可副露、不防守</li>
 * <li>2 门清流：不副露、可防守</li>
 * <li>3 御无双：不副露、不防守</li>
 * <li>4 魂天流：可副露、会防守</li>
 * <li>5 闪电流：只摸切</li>
 * <li>6 鬼神境：知道他家手牌（选牌按全知可见牌计算）；可副露、可防守</li>
 * </ul>
 * <p>算法移植自 mahjong-helper（MIT License）：</p>
 * <ul>
 * <li>选牌：TileEfficiency 搜索树综合评分（MixedWaitsScore）</li>
 * <li>副露：CalculateMeld 鸣牌评估——鸣牌后最优切牌综合分 vs 不鸣牌基线分</li>
 * <li>防守：TileDanger 科学危险度（筋/壁/NC/宝牌修正/巡目铳率表）</li>
 * </ul>
 */
public final class RiichiBot {
	private RiichiBot() {
	}

	private static final String[] NAMES = { "AI·東", "AI·南", "AI·西", "AI·北" };

	/** 流派名（日志/调试用） */
	public static final String[] FLOW_NAMES = { "一般流", "野猪流", "门清流", "御无双", "魂天流", "闪电流", "鬼神境" };

	/** AI 虚拟 UUID（UUID.fromString 可解析的固定占位，按座位区分） */
	public static String aiUuid(int seat) {
		return String.format("00000000-0000-0000-0000-%012d", seat);
	}

	public static String aiName(int seat) {
		return NAMES[seat];
	}

	/**
	 * 形象/名字 → 流派：乘黄=0 落红=1 苔叶兽=2 文鳐=3 狛犬=4 月蛾(silkmoth)=5；
	 * 人类/村民 → 随机 0-5（名字 Maocry55 的人类 = 6 鬼神境）。
	 */
	public static int flowOfAvatar(String form, String name, DoubleSupplier random) {
		if ("human".equals(form) && "Maocry55".equals(name))
			return 6;
		int f = switch (form == null ? "" : form) {
			case "chen_huang" -> 0;
			case "luohong" -> 1;
			case "moss_beast" -> 2;
			case "wenyao" -> 3;
			case "komainu" -> 4;
			case "ferocious" -> 4; // 凶兽 = 魂天流
			case "silkmoth" -> 5;
			default -> -1; // human/villager → 随机 0-4（不含闪电流/鬼神境）
		};
		if (f >= 0)
			return f;
		return (int) (random.getAsDouble() * 5);
	}

	/** 某家手牌的向听数（13/11/10 张等，按 count 折算副露） */
	public static int shantenOf(List<Integer> handCodes) {
		int[] t = new int[34];
		for (int c : handCodes)
			t[TileEfficiency.codeToIndex(c)]++;
		return TileEfficiency.shantenNormal(t, handCodes.size());
	}

	/**
	 * 选打牌（牌效率 + 防守铳率罚分 + 次优支持）。
	 *
	 * @param declareRiichi 立直宣言打牌：必须打后听牌
	 * @param danger34      铳率百分数表（需防守时非空，null = 不防守）
	 * @param dangerWeight  防守权重（每 1% 铳率的评分罚分）
	 * @param rank          0=最高权重切牌；1=权重第二高（一般流 20% 概率）
	 */
	public static int chooseDiscard(RiichiPlayer p, int[] visible34, boolean[] dora34,
			DoubleSupplier random, boolean declareRiichi, double[] danger34, double dangerWeight, int rank) {
		if (p.riichi)
			return p.hand.size() - 1; // 立直后摸切
		return TileEfficiency.chooseDiscard(p.hand, visible34, dora34, random, declareRiichi, danger34,
				dangerWeight, rank);
	}

	/**
	 * 鸣牌决策（CalculateMeld 移植的取舍层）：枚举碰/吃方案，与不鸣牌的基线分比较。
	 *
	 * @param visible34 全场可见牌（不含这张被鸣的牌也行，evaluateMelds 会修正剩余枚数）
	 * @param calledCode 被鸣的牌 code
	 * @param allowChi  是否允许吃
	 * @param turns     当前巡目（本局全场切牌总数，局面感知取舍用）
	 * @param ownDora   手内宝牌数（含赤5：宝牌多时开门提速的打点损失更小）
	 * @param roundWindIdx 场风 34 索引（役路判定；27=東..30=北）
	 * @param selfWindIdx  自风 34 索引（役路判定）
	 * @return 应执行的方案；不鸣返回 null
	 */
	public static TileEfficiency.MeldOption bestMeld(RiichiPlayer p, int[] visible34, int calledCode,
			boolean allowChi, int turns, int ownDora, int roundWindIdx, int selfWindIdx) {
		List<TileEfficiency.MeldOption> opts = TileEfficiency.evaluateMelds(p.hand, visible34, calledCode,
				allowChi);
		if (opts.isEmpty())
			return null;

		// 基线：不鸣牌的 13 张手牌分析
		int[] tiles = new int[34];
		for (int c : p.hand)
			tiles[TileEfficiency.codeToIndex(c)]++;
		int[] left = new int[34];
		for (int i = 0; i < 34; i++)
			left[i] = Math.max(0, 4 - visible34[i]);
		TileEfficiency.Analysis13 base = TileEfficiency.analyze13(tiles, left);

		TileEfficiency.MeldOption best = null;
		for (TileEfficiency.MeldOption o : opts)
			if (best == null || o.score > best.score)
				best = o;
		if (best == null)
			return null;

		// 役路门槛（修"无役乱吃碰"）：开门 = 放弃立直，必须仍有一条保证役才允许副露——
		// 断幺路（开门后全 2-8 数牌）、役牌（三元 + 场风 + 自风，参照 mahjong-helper PlayerInfo 建模）
		// 对/刻或直接碰；已开门（此前过门槛 = 已有役）不再限制
		if (p.melds.isEmpty()) {
			int ci = TileEfficiency.codeToIndex(calledCode);
			boolean tanyao = ci < 27 && ci % 9 >= 1 && ci % 9 <= 7;
			if (tanyao)
				for (int c : p.hand) {
					int i = TileEfficiency.codeToIndex(c);
					if (i >= 27 || i % 9 == 0 || i % 9 == 8) {
						tanyao = false;
						break;
					}
				}
			boolean yakuhai = tiles[roundWindIdx] >= 2 || tiles[selfWindIdx] >= 2
					|| tiles[31] >= 2 || tiles[32] >= 2 || tiles[33] >= 2;
			if (best.pon && (ci == roundWindIdx || ci == selfWindIdx
					|| ci == 31 || ci == 32 || ci == 33))
				yakuhai = true;
			if (!tanyao && !yakuhai)
				return null; // 无役路：不鸣（保立直/保速度防守价值）
		}

		if (best.pon && TileEfficiency.codeToIndex(calledCode) >= 27) {
			// 字牌（役牌）碰：评分不倒退即可（役牌碰几乎总是正收益）
			return best.score >= base.mixedScore - 1e-9 ? best : null;
		}
		// 数牌碰/吃：局面感知取舍。开门的隐性代价 = 弃立直（役+打点+供托+威慑）+ 打点低 + 泄露手牌信息。
		// 场况修正：手内宝牌多（含赤5）时打点损失更小，门槛相应降低。
		if (base.shanten <= 0)
			return null; // 已听牌：默听保立直/保打点，除非役牌碰（上方已处理）
		int shantenGain = base.shanten - best.shanten;
		double scoreGain = best.score - base.mixedScore;
		double threshold = 3.0 - Math.min(2, ownDora) * 0.5; // 3.0 / 2.5 / 2.0
		if (scoreGain >= threshold)
			return best; // 明显提速（吃碰带来大量进张面/有效牌）
		if (shantenGain >= 2)
			return best; // 大幅向听前进
		if (shantenGain == 1 && best.shanten <= 1 && turns >= (ownDora >= 2 ? 6 : 8))
			return best; // 中后盘：1 步换一向听/听牌提速（宝牌多则更积极）
		return null;
	}

	/**
	 * 科学防守危险度（TileDanger 移植的聚合层）：对每个立直家计算铳率表，取各家最大值。
	 *
	 * @param riichiSeats  立直家座位列表
	 * @param rivers       各立直家的牌河（现物）
	 * @param visible34    全场可见牌
	 * @param doraCodes    当前宝牌 code 列表（可重复 = 多重宝牌）
	 * @param roundWindIdx 场风 34 索引（27=東..30=北）
	 * @param dealerSeat   庄家座位（推各家自风）
	 */
	public static double[] defenseDanger(List<Integer> riichiSeats, List<List<Integer>> rivers,
			int[] visible34, List<Integer> doraCodes, int roundWindIdx, int dealerSeat) {
		double[] risk = new double[34];
		if (riichiSeats == null || riichiSeats.isEmpty())
			return risk;
		int[] left = new int[34];
		for (int i = 0; i < 34; i++)
			left[i] = Math.max(0, 4 - visible34[i]);
		List<Integer> doraIdx = new ArrayList<>();
		for (int c : doraCodes)
			doraIdx.add(TileEfficiency.codeToIndex(c));
		for (int k = 0; k < riichiSeats.size(); k++) {
			int s = riichiSeats.get(k);
			List<Integer> river = rivers.get(k);
			boolean[] safe = new boolean[34];
			for (int c : river)
				safe[TileEfficiency.codeToIndex(c)] = true;
			int selfWind = 27 + Math.floorMod(s - dealerSeat + 4, 4);
			int turns = Math.max(1, river.size());
			double[] r = TileDanger.calculateRisk(turns, safe, left, doraIdx, roundWindIdx, selfWind);
			for (int i = 0; i < 34; i++)
				risk[i] = Math.max(risk[i], r[i]);
		}
		return risk;
	}
}
