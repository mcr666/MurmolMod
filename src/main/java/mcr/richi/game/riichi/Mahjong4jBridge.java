package mcr.richi.game.riichi;

import java.util.ArrayList;
import java.util.List;

import org.mahjong4j.GeneralSituation;
import org.mahjong4j.PersonalSituation;
import org.mahjong4j.Player;
import org.mahjong4j.Score;
import org.mahjong4j.hands.Hands;
import org.mahjong4j.hands.Kantsu;
import org.mahjong4j.hands.Kotsu;
import org.mahjong4j.hands.Mentsu;
import org.mahjong4j.hands.Shuntsu;
import org.mahjong4j.tile.Tile;
import org.mahjong4j.yaku.normals.NormalYaku;
import org.mahjong4j.yaku.yakuman.Yakuman;

/**
 * 牌面代码 ↔ Mahjong4j 桥接（纯逻辑）。
 * 内部代码：0m..9m=0..9、0p..9p=10..19、0s..9s=20..29（0=红五，判定按普通 5）、1z..7z=30..36。
 * Mahjong4j Tile 枚举序：M1..M9=0..8、P1..P9=9..17、S1..S9=18..26、TON/NAN/SHA/PEI/HAK/HAT/CHN=27..33。
 * 判定模式照抄源项目：Hands(int[34], lastTile, mentsuList) → Player.calculate() → 役/翻/符/点。
 */
public final class Mahjong4jBridge {
	private Mahjong4jBridge() {
	}

	/** 内部代码 → Mahjong4j Tile */
	public static Tile toTile(int code) {
		if (code < 30) {
			int suit = code / 10;
			int digit = code % 10;
			if (digit == 0)
				digit = 5; // 红五按普通 5 判定
			return Tile.values()[suit * 9 + (digit - 1)];
		}
		return Tile.values()[27 + (code - 30)];
	}

	/** 34 长度计数数组（下标 = Tile 序） */
	private static int[] toComp(List<Integer> codes) {
		int[] comp = new int[34];
		for (int c : codes)
			comp[toTile(c).ordinal()]++;
		return comp;
	}

	/** 副露 → 已知面子列表（暗杠按闭处理，不破坏门清判定） */
	private static List<Mentsu> toMentsuList(List<Fuuro> melds) {
		List<Mentsu> list = new ArrayList<>();
		for (Fuuro f : melds) {
			boolean open = f.type != Fuuro.Type.ANKAN;
			switch (f.type) {
				case CHII -> {
					// 三张直接给 4 参构造（内部自行校验顺子），避免中张识别与受检异常
					Tile a = toTile(f.tiles[0]);
					Tile b = toTile(f.tiles[1]);
					Tile c = toTile(f.tiles[2]);
					list.add(new Shuntsu(open, a, b, c));
				}
				case PON -> list.add(new Kotsu(open, toTile(f.calledTile)));
				default -> list.add(new Kantsu(open, toTile(f.calledTile)));
			}
		}
		return list;
	}

	/** 和牌形判定（不含役）：handCodes 必须含和牌张，共 14 张（副露后为 14-3×副露数）。
	 *  注意：Mahjong4j 的 Hands 要求 comp 数组包含和牌张在内的全部牌（13+last 形式恒判不能和） */
	public static boolean isWinnableShape(List<Integer> handCodes, List<Fuuro> melds) {
		try {
			int last = handCodes.get(handCodes.size() - 1);
			int[] comp = toComp(handCodes);
			Hands hands = new Hands(comp, toTile(last), toMentsuList(melds));
			return hands.getCanWin();
		} catch (Exception e) {
			return false;
		}
	}

	/** 听牌判定：对 34 种牌逐一试补，任一能成和牌形即听牌（立直宣言用） */
	public static boolean isTenpai(List<Integer> hand13, List<Fuuro> melds) {
		for (int code = 0; code < 37; code++) {
			if (code == 0 || code == 10 || code == 20)
				continue; // 红五与普通五同码判定，跳过重复
			List<Integer> test = new ArrayList<>(hand13);
			test.add(code);
			if (isWinnableShape(test, melds))
				return true;
		}
		return false;
	}

	/** 结算点数（ron / 庄家自摸各付 / 闲家自摸庄付 / 闲家自摸闲付）——替代 mahjong4j Score 枚举（其役满表缺 n≥2 值） */
	public record ScoreVal(int ron, int parentTsumo, int parent, int child) {
		public static final ScoreVal ZERO = new ScoreVal(0, 0, 0, 0);

		public int getRon() {
			return ron;
		}

		public int getParentTsumo() {
			return parentTsumo;
		}

		public int getParent() {
			return parent;
		}

		public int getChild() {
			return child;
		}

		public static ScoreVal of(org.mahjong4j.Score s) {
			return new ScoreVal(s.getRon(), s.getParentTsumo(), s.getParent(), s.getChild());
		}

		/**
		 * 役满结算：单役满 庄家荣和 48000 / 闲家荣和 32000；庄家自摸各家 16000；
		 * 闲家自摸庄家付 16000、闲家付 8000。n 个役满（倍数役满）按 n 倍计。
		 */
		public static ScoreVal yakuman(boolean isParent, int count) {
			int k = Math.max(1, count);
			return isParent ? new ScoreVal(48000 * k, 16000 * k, 16000 * k, 16000 * k)
					: new ScoreVal(32000 * k, 16000 * k, 16000 * k, 8000 * k);
		}
	}

	/** 和牌结算结果（点数以庄家/闲家语义给出；doraHits/uraHits/redHits = 命中宝牌的牌面码，供播报聚合） */
	public record WinResult(boolean win, boolean yakuman, int han, int fu,
			List<String> yakuNames, ScoreVal score, List<Integer> doraHits, List<Integer> uraHits,
			List<Integer> redHits) {
		public WinResult(boolean win, boolean yakuman, int han, int fu, List<String> yakuNames, ScoreVal score) {
			this(win, yakuman, han, fu, yakuNames, score, List.of(), List.of(), List.of());
		}
	}

	/**
	 * 役种 + 翻符 + 点数结算。
	 *
	 * @param handWithWinTile 手牌（最后一张为和牌张；自摸时即摸牌，荣和时为荣和牌）
	 * @param winnerSeat      和牌者座位（0=庄家）
	 * @param tsumo           是否自摸
	 * @param doraIndicators  已揭开宝牌指示牌
	 */
	/** 场况附加标记（双立直/抢杠/岭上/海底/河底/里宝牌） */
	public record Flags(boolean doubleRiichi, boolean chankan, boolean rinshan,
			boolean haitei, boolean houtei, List<Integer> uraDoras) {
		public static final Flags NONE = new Flags(false, false, false, false, false, List.of());
	}

	/**
	 * 役种 + 翻符 + 点数结算（多局版：自风随庄家轮转，场风随局数）。
	 *
	 * @param dealerSeat 当前局庄家座位（自风=座位相对庄家的偏移）
	 * @param roundWind  局风（0=東 1=南）
	 * @param doraIndicators 已揭开宝牌（注意：应传"实际宝牌"，即指示牌下一张）
	 */
	public static WinResult score(List<Integer> handWithWinTile, List<Fuuro> melds, int winnerSeat,
			int dealerSeat, int roundWind, boolean tsumo, boolean riichi, boolean ippatsu,
			Flags flags, List<Integer> doraIndicators) {
		try {
			int last = handWithWinTile.get(handWithWinTile.size() - 1);
			int[] comp = toComp(handWithWinTile); // 含和牌张（Mahjong4j 要求全量）
			List<Mentsu> mentsu = toMentsuList(melds);
			Hands hands = new Hands(comp, toTile(last), mentsu);
			if (!hands.getCanWin())
				return new WinResult(false, false, 0, 0, List.of(), ScoreVal.ZERO);

			// 场况：场风随局数（東/南），自风 = 座位相对庄家偏移
			Tile bakaze = roundWind == 0 ? Tile.TON : Tile.NAN;
			Tile jikaze = Tile.values()[27 + (winnerSeat - dealerSeat + 4) % 4];
			GeneralSituation general = new GeneralSituation(false, false, bakaze,
					toTiles(doraIndicators), List.of());
			boolean isParent = winnerSeat == dealerSeat;
			// 参数序照源项目：isTsumo, isIppatsu, isRiichi, isDoubleRiichi, isChankan, isRinshanKaihoh, jikaze
			PersonalSituation personal = new PersonalSituation(tsumo,
					ippatsu && riichi, riichi, flags.doubleRiichi(), flags.chankan(), flags.rinshan(), jikaze);
			Player player = new Player(hands, general, personal);
			player.calculate();

			List<Yakuman> yakumanList = player.getYakumanList();
			List<String> names = new ArrayList<>();
			boolean open = melds.stream().anyMatch(f -> f.type != Fuuro.Type.ANKAN);
			if (!yakumanList.isEmpty()) {
				int count = yakumanList.size();
				for (Yakuman y : yakumanList)
					names.add(y.name());
				// 雀魂倍役满补判（mahjong4j 无对应 resolver）：大四喜/四暗刻单骑/纯正九莲宝灯/国士十三面 各 +1 倍
				Tile winTile = toTile(last);
				if (yakumanList.contains(Yakuman.DAISUSHI))
					count++; // 雀魂：大四喜 = 倍役满
				if (yakumanList.contains(Yakuman.SUANKO) && hasAnkoExcludingWin(hands, winTile, 4)) {
					names.remove(Yakuman.SUANKO.name()); // 单骑覆盖四暗刻，不重复显示/计倍
					names.add("SUANKO_TANKI"); // 和牌张为雀头 → 单骑听，倍役满
					count++;
				}
				if (yakumanList.contains(Yakuman.KOKUSHIMUSO) && isKokushi13(comp, winTile)) {
					names.remove(Yakuman.KOKUSHIMUSO.name());
					names.add("KOKUSHIMUSO_13");
					count++;
				}
				if (yakumanList.contains(Yakuman.CHURENPOHTO) && isChurenPure9(comp, winTile)) {
					names.remove(Yakuman.CHURENPOHTO.name());
					names.add("CHURENPOHTO_PURE");
					count++;
				}
				// 大四喜覆盖小四喜（理论互斥，防御去重）
				if (yakumanList.contains(Yakuman.DAISUSHI) && yakumanList.contains(Yakuman.SHOSUSHI)) {
					names.remove(Yakuman.SHOSUSHI.name());
					count--;
				}
				// 役满点数自行计算：mahjong4j 的 calculateYakumanScore 对 n≥2 返回 SCORE0（复合役满变零点）
				// han 编码役满倍数（fu=0 标记役满），供结算界面/播报显示"N倍役满"
				return new WinResult(true, true, count, 0, names, ScoreVal.yakuman(isParent, count));
			}
			// 一般役：吃碰后役翻减半（kuisagari），宝牌在有役后另计（源项目同款）
			int han = 0;
			for (NormalYaku y : player.getNormalYakuList()) {
				if (y == NormalYaku.DORA)
					continue; // 宝牌在下方手计（手牌+副露+红宝牌），跳过 mahjong4j 内置项避免重复计翻
				names.add(y.name());
				han += open ? y.getKuisagari() : y.getHan();
			}
			// mahjong4j 的三色同顺/一气通贯判定在顺子列表中同数字/同花色顺子不相邻时漏判
			// （副露顺子固定追加在门清分解之后）——按全部面子分解自行补判
			for (org.mahjong4j.hands.MentsuComp mc : hands.getMentsuCompSet()) {
				List<Shuntsu> sh = mc.getShuntsuList();
				if (!names.contains(NormalYaku.SANSHOKUDOHJUN.name()) && detectSanshoku(sh)) {
					names.add(NormalYaku.SANSHOKUDOHJUN.name());
					han += open ? NormalYaku.SANSHOKUDOHJUN.getKuisagari() : NormalYaku.SANSHOKUDOHJUN.getHan();
				}
				if (!names.contains(NormalYaku.IKKITSUKAN.name()) && detectIttsu(sh)) {
					names.add(NormalYaku.IKKITSUKAN.name());
					han += open ? NormalYaku.IKKITSUKAN.getKuisagari() : NormalYaku.IKKITSUKAN.getHan();
				}
			}
			// 三暗刻规则：荣和时由和牌张凑成的刻不算暗刻——若无不含和牌张的 3 暗刻分解则撤销该役
			if (!tsumo && names.contains(NormalYaku.SANANKO.name())
					&& !hasAnkoExcludingWin(hands, toTile(last), 3)) {
				names.remove(NormalYaku.SANANKO.name());
				han -= open ? NormalYaku.SANANKO.getKuisagari() : NormalYaku.SANANKO.getHan();
			}
			// 平和规则：听坎张/边张/单骑不算平和（mahjong4j 不校验听形）——存在两面听分解才成立
			if (names.contains(NormalYaku.PINFU.name()) && !hasRyanmenDecomposition(handWithWinTile)) {
				names.remove(NormalYaku.PINFU.name());
				han -= open ? NormalYaku.PINFU.getKuisagari() : NormalYaku.PINFU.getHan();
			}
			if (han <= 0)
				return new WinResult(false, false, 0, 0, names, ScoreVal.ZERO);
			// 宝牌（含红宝牌简化：红五不算宝）
			int[] fullComp = hands.getHandsComp();
			for (Tile doraTile : general.getDora())
				han += fullComp[doraTile.ordinal()];
			// 符数取整：Mahjong4j 的 getFu() 可能返回未取整值（如 22/24），非法符会导致计分异常被吞
			int rawFu = Math.max(player.getFu(), 20);
			int fu = rawFu == 25 ? 25 : (rawFu + 9) / 10 * 10;
			// 副露底符 30：含明副露（非暗杠）的手牌不存在 20 符（喰い平和等按 30 计）
			if (melds.stream().anyMatch(f -> f.type != Fuuro.Type.ANKAN) && fu < 30)
				fu = 30;
			// 宝牌命中明细（含红宝牌/里宝牌，供播报按牌面聚合）；红五优先归入红宝牌
			List<Integer> allCodes = new ArrayList<>(handWithWinTile);
			for (Fuuro f : melds)
				for (int t : f.tiles)
					allCodes.add(t);
			List<Integer> doraHits = new ArrayList<>(), uraHits = new ArrayList<>(), redHits = new ArrayList<>();
			for (int c : allCodes) {
				if (c == 0 || c == 10 || c == 20)
					redHits.add(c);
				else if (doraIndicators.contains(c))
					doraHits.add(c);
				else if (riichi && flags.uraDoras().contains(c))
					uraHits.add(c);
			}
			// 红宝牌翻数：手牌+副露中的红五（0/10/20）每张 +1 翻
			int red = redHits.size();
			if (red > 0)
				han += red;
			// 里宝牌：立直和牌时按里指示牌计
			if (riichi && !flags.uraDoras().isEmpty()) {
				int ura = 0;
				for (Tile ind : toTiles(flags.uraDoras()))
					ura += fullComp[ind.ordinal()];
				if (ura > 0)
					han += ura;
			}
			// 海底捞月（最后一自摸）/ 河底捞鱼（最后一张荣和）
			if (flags.haitei() && tsumo) {
				han++;
				names.add("HAITEI");
			}
			if (flags.houtei() && !tsumo) {
				han++;
				names.add("HOUTEI");
			}
			// 累计役满（雀魂规则）：一般役累计翻数每满 13 翻按 1 个役满计（26 翻 = 双倍役满）；
			// mahjong4j 的 calculateScore 对 han≥13 一律只给单倍役满。han 编码役满倍数
			int k = han / 13;
			if (k > 0)
				return new WinResult(true, true, k, 0, names,
						ScoreVal.yakuman(isParent, k), doraHits, uraHits, redHits);
			ScoreVal s = ScoreVal.of(Score.calculateScore(isParent, han, fu));
			return new WinResult(true, false, han, fu, names, s, doraHits, uraHits, redHits);
		} catch (Exception e) {
			return new WinResult(false, false, 0, 0, List.of(), ScoreVal.ZERO);
		}
	}

	/**
	 * 平和两面听判定：枚举 4 顺子 + 1 对子分解，存在使和牌张所在顺子构成两面听的分解即成立。
	 * 坎张（和牌张居中）/ 边张（持 12 和 3 或持 89 和 7）/ 单骑（和牌张成对）均不两面。
	 */
	private static boolean hasRyanmenDecomposition(List<Integer> handWithWinTile) {
		Tile winTile = toTile(handWithWinTile.get(handWithWinTile.size() - 1));
		if (winTile.getNumber() == 0)
			return false; // 字牌不可能两面
		int[] comp = toComp(handWithWinTile);
		return ryanmenSearch(comp, winTile.ordinal(), 0, false);
	}

	/** 递归分解（仅顺子+对子；平和不含刻子）：winOrd 在对子里 = 单骑（不成立），在顺子里按位置判两面 */
	private static boolean ryanmenSearch(int[] comp, int winOrd, int from, boolean pairUsed) {
		int i = from;
		while (i < 34 && comp[i] == 0)
			i++;
		if (i == 34)
			return pairUsed; // 全部牌恰好分解为 4 顺子 + 1 对子
		// 对子分支：若对子含和牌张 = 单骑听，此分支必不两面；用对子后剩余同种牌仍可作顺子开头，故从 i 继续
		if (!pairUsed && comp[i] >= 2) {
			comp[i] -= 2;
			boolean ok = i != winOrd && ryanmenSearch(comp, winOrd, i, true);
			comp[i] += 2;
			if (ok)
				return true;
		}
		// 顺子分支
		if (i < 27 && i % 9 <= 6 && comp[i + 1] > 0 && comp[i + 2] > 0) {
			comp[i]--;
			comp[i + 1]--;
			comp[i + 2]--;
			boolean ok;
			if (winOrd < i || winOrd > i + 2) {
				ok = ryanmenSearch(comp, winOrd, i, pairUsed); // 和牌张不在本顺子
			} else if (winOrd == i + 1) {
				ok = false; // 坎张
			} else if (winOrd == i) {
				ok = i % 9 <= 5; // 持 (i+1,i+2) 和 i：边张仅 i%9==6（持 89 和 7）
			} else {
				ok = i % 9 >= 1; // 持 (i,i+1) 和 i+2：边张仅 i%9==0（持 12 和 3）
			}
			if (ok)
				ok = ryanmenSearch(comp, winOrd, i, pairUsed); // 和牌张所在顺子两面成立，仍需其余牌可分解
			comp[i]++;
			comp[i + 1]++;
			comp[i + 2]++;
			if (ok)
				return true;
		}
		return false;
	}

	/** 是否存在不含和牌张（荣和所成之刻不计）仍凑满 count 个暗刻的面子分解（暗杠恒计入） */
	private static boolean hasAnkoExcludingWin(Hands hands, Tile winTile, int count) {
		for (org.mahjong4j.hands.MentsuComp mc : hands.getMentsuCompSet()) {
			int anko = 0;
			for (Kotsu k : mc.getKotsuList())
				if (!k.isOpen() && k.getTile() != winTile)
					anko++;
			for (Kantsu k : mc.getKantsuList())
				if (!k.isOpen())
					anko++; // 暗杠恒为暗刻且不可能由荣和张凑成
			if (anko >= count)
				return true;
		}
		return false;
	}

	/** 老幺九牌种（1/9 数牌 + 全部字牌）的 Tile 序号 */
	private static boolean isYaochuu(int ord) {
		return ord == 0 || ord == 8 || ord == 9 || ord == 17 || ord == 18 || ord == 26 || ord >= 27;
	}

	/** 国士十三面听：扣去和牌张后 13 种老幺九各恰好 1 张（和牌张成对） */
	private static boolean isKokushi13(int[] comp, Tile winTile) {
		if (!isYaochuu(winTile.ordinal()))
			return false;
		int[] c = comp.clone();
		c[winTile.ordinal()]--;
		for (int i = 0; i < 34; i++)
			if (isYaochuu(i) ? c[i] != 1 : c[i] != 0)
				return false;
		return true;
	}

	/** 纯正九莲宝灯（九面听）：扣去和牌张后同花色 1112345678999（1/9 各 3 张、2-8 各 1 张） */
	private static boolean isChurenPure9(int[] comp, Tile winTile) {
		int suit = winTile.getType().ordinal();
		if (suit > 2)
			return false;
		int[] c = comp.clone();
		c[winTile.ordinal()]--;
		for (int d = 0; d < 9; d++)
			if (c[suit * 9 + d] != (d == 0 || d == 8 ? 3 : 1))
				return false;
		return true;
	}

	private static List<Tile> toTiles(List<Integer> codes) {
		List<Tile> tiles = new ArrayList<>();
		for (int c : codes)
			tiles.add(toTile(c));
		return tiles;
	}

	/** 三色同顺补判：某数字的顺子覆盖三种花色即成立（副露含明顺，吃碰减 1 翻由调用方处理） */
	private static boolean detectSanshoku(List<Shuntsu> shuntsu) {
		for (Shuntsu a : shuntsu) {
			int num = a.getTile().getNumber();
			int types = 0;
			for (Shuntsu b : shuntsu)
				if (b.getTile().getNumber() == num)
					types |= 1 << b.getTile().getType().ordinal();
			if (Integer.bitCount(types & 0b111) == 3) // 三种数牌花色齐
				return true;
		}
		return false;
	}

	/** 一气通贯补判：同花色 123/456/789 三顺齐即成立 */
	private static boolean detectIttsu(List<Shuntsu> shuntsu) {
		for (int suit = 0; suit < 3; suit++) {
			int mask = 0;
			for (Shuntsu s : shuntsu)
				if (s.getTile().getType().ordinal() == suit)
					mask |= 1 << (s.getTile().getNumber() - 1);
			if ((mask & 0b100100011) == 0b100100011) // 1、4、7 起始顺齐
				return true;
		}
		return false;
	}
}
