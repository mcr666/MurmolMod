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

	/** 和牌结算结果（点数以庄家/闲家语义给出；doraHits/uraHits/redHits = 命中宝牌的牌面码，供播报聚合） */
	public record WinResult(boolean win, boolean yakuman, int han, int fu,
			List<String> yakuNames, Score score, List<Integer> doraHits, List<Integer> uraHits,
			List<Integer> redHits) {
		public WinResult(boolean win, boolean yakuman, int han, int fu, List<String> yakuNames, Score score) {
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
				return new WinResult(false, false, 0, 0, List.of(), Score.SCORE0);

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
				for (Yakuman y : yakumanList)
					names.add(y.name());
				Score s = Score.calculateYakumanScore(isParent, yakumanList.size());
				return new WinResult(true, true, 0, 0, names, s);
			}
			// 一般役：吃碰后役翻减半（kuisagari），宝牌在有役后另计（源项目同款）
			int han = 0;
			for (NormalYaku y : player.getNormalYakuList()) {
				if (y == NormalYaku.DORA)
					continue; // 宝牌在下方手计（手牌+副露+红宝牌），跳过 mahjong4j 内置项避免重复计翻
				names.add(y.name());
				han += open ? y.getKuisagari() : y.getHan();
			}
			if (han <= 0)
				return new WinResult(false, false, 0, 0, names, Score.SCORE0);
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
			Score s = Score.calculateScore(isParent, han, fu);
			return new WinResult(true, false, han, fu, names, s, doraHits, uraHits, redHits);
		} catch (Exception e) {
			return new WinResult(false, false, 0, 0, List.of(), Score.SCORE0);
		}
	}

	private static List<Tile> toTiles(List<Integer> codes) {
		List<Tile> tiles = new ArrayList<>();
		for (int c : codes)
			tiles.add(toTile(c));
		return tiles;
	}
}
