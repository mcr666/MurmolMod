package mcr.richi.game;

import java.util.ArrayList;
import java.util.List;

/**
 * 麻将牌谱记法：数字 + 花色字母（如 "12345m123p456s11z"）。
 * 字牌 z 用 1..7（东南西北中发白）；红五（宝牌）用 0 表示（0m/0p/0s）。
 * 与牌面代码互转：0m..9m=0..9，0p..9p=10..19，0s..9s=20..29，1z..7z=30..36。
 * 纯静态工具，无状态。
 */
public final class MahjongTileNotation {
	private MahjongTileNotation() {
	}

	/**
	 * 解析记法为牌面代码列表（顺序保留）。"?" = 未知牌占位（code -1，客户端渲染 unknown 牌面）。
	 * 其余未知字符抛 IllegalArgumentException。
	 */
	public static List<Integer> parse(String notation) {
		List<Integer> codes = new ArrayList<>();
		if (notation == null || notation.isEmpty())
			return codes;
		StringBuilder digits = new StringBuilder();
		for (char c : notation.toCharArray()) {
			if (c >= '0' && c <= '9') {
				digits.append(c);
				continue;
			}
			if (c == '?') {
				if (!digits.isEmpty())
					throw new IllegalArgumentException("牌谱数字缺失花色: " + notation);
				codes.add(-1); // 未知牌占位
				continue;
			}
			int base = switch (c) {
				case 'm' -> 0;
				case 'p' -> 10;
				case 's' -> 20;
				case 'z' -> 29;
				default -> throw new IllegalArgumentException("未知牌谱花色: " + c + " (in " + notation + ")");
			};
			if (digits.isEmpty())
				throw new IllegalArgumentException("牌谱数字缺失: " + notation);
			for (int i = 0; i < digits.length(); i++) {
				int d = digits.charAt(i) - '0';
				if (c == 'z' && (d < 1 || d > 7))
					throw new IllegalArgumentException("字牌编号须为 1..7: " + notation);
				codes.add(base + d);
			}
			digits.setLength(0);
		}
		if (!digits.isEmpty())
			throw new IllegalArgumentException("牌谱末尾缺少花色: " + notation);
		return codes;
	}

	/** 牌面代码列表 → 记法（按插入顺序、同花色连续段合并，如 "12345m123p456s11z"；-1 = 未知牌 → "?"）。 */
	public static String format(List<Integer> codes) {
		StringBuilder sb = new StringBuilder();
		StringBuilder digits = new StringBuilder();
		char lastSuit = 0;
		for (int code : codes) {
			if (code < 0) { // 未知牌：先结清当前花色段，再写独立 "?"
				if (digits.length() > 0) {
					sb.append(digits).append(lastSuit);
					digits.setLength(0);
				}
				lastSuit = 0;
				sb.append('?');
				continue;
			}
			char suit = code < 10 ? 'm' : code < 20 ? 'p' : code < 30 ? 's' : 'z';
			int d = suit == 'z' ? code - 29 : code % 10;
			if (suit != lastSuit && digits.length() > 0) {
				sb.append(digits).append(lastSuit);
				digits.setLength(0);
			}
			lastSuit = suit;
			digits.append(d);
		}
		if (digits.length() > 0)
			sb.append(digits).append(lastSuit);
		return sb.toString();
	}
}
