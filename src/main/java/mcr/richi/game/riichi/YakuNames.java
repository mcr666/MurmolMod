package mcr.richi.game.riichi;

import java.util.Map;

/**
 * 役种名中文翻译：Mahjong4j 枚举名 → 中文（用于聊天播报与结算界面）。
 * 未收录的名称原样返回。
 */
public final class YakuNames {
	private YakuNames() {
	}

	private static final Map<String, String> ZH = Map.ofEntries(
			// 一般役
			Map.entry("TANYAO", "断幺九"),
			Map.entry("TSUMO", "门清自摸"),
			Map.entry("PINFU", "平胡"),
			Map.entry("IPEIKO", "一杯口"),
			Map.entry("HAKU", "役牌·白"),
			Map.entry("HATSU", "役牌·发"),
			Map.entry("CHUN", "役牌·中"),
			Map.entry("JIKAZE", "自风牌"),
			Map.entry("BAKAZE", "场风牌"),
			Map.entry("IPPATSU", "一发"),
			Map.entry("HOUTEI", "河底捞鱼"),
			Map.entry("HAITEI", "海底捞月"),
			Map.entry("REACH", "立直"),
			Map.entry("DORA", "宝牌"),
			Map.entry("URADORA", "里宝牌"),
			Map.entry("RINSHANKAIHOH", "岭上开花"),
			Map.entry("CHANKAN", "抢杠"),
			Map.entry("DOUBLE_REACH", "两立直"),
			Map.entry("CHANTA", "混全带幺九"),
			Map.entry("HONROHTOH", "混老头"),
			Map.entry("SANSHOKUDOHJUN", "三色同顺"),
			Map.entry("IKKITSUKAN", "一气通贯"),
			Map.entry("TOITOIHO", "对对和"),
			Map.entry("SANSHOKUDOHKO", "三色同刻"),
			Map.entry("SANANKO", "三暗刻"),
			Map.entry("SANKANTSU", "三杠子"),
			Map.entry("SHOSANGEN", "小三元"),
			Map.entry("CHITOITSU", "七对子"),
			Map.entry("RYANPEIKO", "二杯口"),
			Map.entry("JUNCHAN", "纯全带幺九"),
			Map.entry("HONITSU", "混一色"),
			Map.entry("CHINITSU", "清一色"),
			// 役满
			Map.entry("KOKUSHIMUSO", "国士无双"),
			Map.entry("SUANKO", "四暗刻"),
			Map.entry("CHURENPOHTO", "九莲宝灯"),
			Map.entry("DAISANGEN", "大三元"),
			Map.entry("TSUISO", "字一色"),
			Map.entry("SHOSUSHI", "小四喜"),
			Map.entry("DAISUSHI", "大四喜"),
			Map.entry("RYUISO", "绿一色"),
			Map.entry("CHINROTO", "清老头"),
			Map.entry("SUKANTSU", "四杠子"),
			Map.entry("RENHO", "人和"),
			Map.entry("TENHO", "天和"),
			Map.entry("CHIHO", "地和"),
			Map.entry("RED_DORA", "红宝牌"));

	public static String zh(String name) {
		return ZH.getOrDefault(name, name);
	}
}
