package mcr.richi.game.ai.akochan;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * mod 牌码 ↔ mjai/akochan 牌串与事件互转（无状态纯工具）。
 * <p>mod 牌码：0=红5m，1..9=1m..9m，10=红5p，11..19，20=红5s，21..29，30..36=东南西北白发（1z..7z）。
 * mjai 牌串：1m..9m / 5mr（红）/ 1p..9p / 5pr / 1s..9s / 5sr / E S W N P F C。</p>
 * <p>事件格式对照 akochan 源码 main.cpp 的 pipe 模式与其 haifu 记录（start_kyoku 携带全量明牌 tehais）。</p>
 */
public final class MjaiProtocolConverter {
	private MjaiProtocolConverter() {
	}

	/** akochan 动作（响应数组元素） */
	public record Action(String type, int tile, int[] consumed, boolean tsumogiri) {
	}

	// ---- 牌码映射 ----

	/** mod 牌码 → mjai 牌串（-1 = 未知 "?"） */
	public static String tileOf(int code) {
		if (code < 0)
			return "?";
		if (code == 0)
			return "5mr";
		if (code < 10)
			return code + "m";
		if (code == 10)
			return "5pr";
		if (code < 20)
			return (code - 10) + "p";
		if (code == 20)
			return "5sr";
		if (code < 30)
			return (code - 20) + "s";
		return switch (code) {
			case 30 -> "E";
			case 31 -> "S";
			case 32 -> "W";
			case 33 -> "N";
			case 34 -> "P";
			case 35 -> "F";
			default -> "C";
		};
	}

	/** mjai 牌串 → mod 牌码（红五还原为 0/10/20；未知返回 -1） */
	public static int codeOf(String tile) {
		if (tile == null || tile.isEmpty())
			return -1;
		return switch (tile) {
			case "5mr" -> 0;
			case "5pr" -> 10;
			case "5sr" -> 20;
			case "E" -> 30;
			case "S" -> 31;
			case "W" -> 32;
			case "N" -> 33;
			case "P" -> 34;
			case "F" -> 35;
			case "C" -> 36;
			default -> {
				char c = tile.charAt(tile.length() - 1);
				int d;
				try {
					d = Integer.parseInt(tile.substring(0, tile.length() - 1));
				} catch (NumberFormatException e) {
					yield -1;
				}
				yield switch (c) {
					case 'm' -> d;
					case 'p' -> d + 10;
					case 's' -> d + 20;
					default -> -1;
				};
			}
		};
	}

	// ---- 事件构造 ----

	/** start_game（进程启动后发送一次；kyokuFirst：4=东风 8=半庄） */
	public static String startGame(String[] names, int kyokuFirst) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "start_game");
		o.addProperty("aka_flag", true);
		o.addProperty("kyoku_first", kyokuFirst);
		JsonArray arr = new JsonArray();
		for (String n : names)
			arr.add(n == null ? "AI" : n);
		o.add("names", arr);
		return o.toString();
	}

	/** start_kyoku（tehais 为 4 家全量手牌——akochan 服务端视角记录，自身仅用本家信息评估） */
	public static String startKyoku(int roundWind, int round, int honba, int kyotaku, int oya,
			int[] scores, int doraMarker, List<List<Integer>> tehais) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "start_kyoku");
		o.addProperty("bakaze", roundWind == 0 ? "E" : "S");
		o.addProperty("kyoku", round);
		o.addProperty("honba", honba);
		o.addProperty("kyotaku", kyotaku);
		o.addProperty("oya", oya);
		JsonArray sc = new JsonArray();
		for (int p : scores)
			sc.add(p);
		o.add("scores", sc);
		o.addProperty("dora_marker", tileOf(doraMarker));
		JsonArray ts = new JsonArray();
		for (List<Integer> hand : tehais) {
			JsonArray h = new JsonArray();
			for (int c : hand)
				h.add(tileOf(c));
			ts.add(h);
		}
		o.add("tehais", ts);
		return o.toString();
	}

	public static String tsumo(int actor, int code) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "tsumo");
		o.addProperty("actor", actor);
		o.addProperty("pai", tileOf(code));
		return o.toString();
	}

	public static String dahai(int actor, int code, boolean tsumogiri) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "dahai");
		o.addProperty("actor", actor);
		o.addProperty("pai", tileOf(code));
		o.addProperty("tsumogiri", tsumogiri);
		return o.toString();
	}

	public static String reach(int actor) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "reach");
		o.addProperty("actor", actor);
		return o.toString();
	}

	public static String reachAccepted(int actor) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "reach_accepted");
		o.addProperty("actor", actor);
		return o.toString();
	}

	/** 吃/碰/明杠（consumed：手牌中被取用的牌，不含声明牌） */
	public static String meld(String type, int actor, int target, int calledTile, int[] consumed) {
		JsonObject o = new JsonObject();
		o.addProperty("type", type);
		o.addProperty("actor", actor);
		o.addProperty("target", target);
		o.addProperty("pai", tileOf(calledTile));
		JsonArray arr = new JsonArray();
		for (int c : consumed)
			arr.add(tileOf(c));
		o.add("consumed", arr);
		return o.toString();
	}

	/** 暗杠/加杠 */
	public static String kong(String type, int actor, int code) {
		JsonObject o = new JsonObject();
		o.addProperty("type", type);
		o.addProperty("actor", actor);
		o.addProperty("pai", tileOf(code));
		return o.toString();
	}

	public static String dora(int markerCode) {
		JsonObject o = new JsonObject();
		o.addProperty("type", "dora");
		o.addProperty("dora_marker", tileOf(markerCode));
		return o.toString();
	}

	// ---- 动作解析 ----

	/** 解析 akochan 响应行（JSON 数组或单对象）→ 动作列表；解析失败返回空列表 */
	public static List<Action> parseActions(String line) {
		List<Action> out = new ArrayList<>();
		try {
			var root = JsonParser.parseString(line);
			JsonArray arr;
			if (root.isJsonArray())
				arr = root.getAsJsonArray();
			else {
				arr = new JsonArray();
				arr.add(root);
			}
			for (var el : arr) {
				if (!el.isJsonObject())
					continue;
				JsonObject o = el.getAsJsonObject();
				String type = o.has("type") ? o.get("type").getAsString() : "";
				int tile = o.has("pai") ? codeOf(o.get("pai").getAsString()) : -1;
				int[] consumed = null;
				if (o.has("consumed") && o.get("consumed").isJsonArray()) {
					var ca = o.getAsJsonArray("consumed");
					consumed = new int[ca.size()];
					for (int i = 0; i < ca.size(); i++)
						consumed[i] = codeOf(ca.get(i).getAsString());
				}
				boolean tsumogiri = o.has("tsumogiri") && o.get("tsumogiri").getAsBoolean();
				if (!type.isEmpty())
					out.add(new Action(type, tile, consumed, tsumogiri));
			}
		} catch (RuntimeException ignored) {
		}
		return out;
	}
}
