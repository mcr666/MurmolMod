package mcr.richi.game;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/**
 * 麻雀段位战绩（天凤算法）：存档目录/data/mahjong/ranks.json，IO 失败静默忽略。
 * <p>pt = (素点 - 30000) / 1000 + 顺位马（{@link #UMA}，天凤四般：1位+20 / 2位+10 / 3位-10 / 4位-30）。
 * 每名非 AI 参战者一条记录（uuid 键），累计 pt、对局数与各顺位次数；查询按 pt 降序排名。</p>
 */
public final class MahjongRankStore {
	private MahjongRankStore() {
	}

	/** 顺位马（按最终顺位 1..4，天凤半庄战马点） */
	public static final int[] UMA = { 20, 10, -10, -30 };

	/** 一名玩家的战绩：累计 pt、对局数、顺位分布（ranks[0..3] = 1..4 位次数） */
	public record Entry(String name, double pt, int games, int[] ranks) {
	}

	/** 排行榜前 n 位（pt 降序） */
	public static synchronized List<Entry> top(ServerLevel level, int n) {
		List<Entry> all = all(level);
		return all.subList(0, Math.min(n, all.size()));
	}

	/** 全部记录（pt 降序） */
	public static synchronized List<Entry> all(ServerLevel level) {
		List<Entry> out = new ArrayList<>();
		JsonObject root = load(level);
		for (Map.Entry<String, com.google.gson.JsonElement> e : root.entrySet()) {
			if (!e.getValue().isJsonObject())
				continue;
			JsonObject o = e.getValue().getAsJsonObject();
			int[] ranks = new int[4];
			for (int i = 0; i < 4; i++)
				ranks[i] = optInt(o, "r" + (i + 1));
			out.add(new Entry(optString(o, "name", e.getKey()), optDouble(o, "pt"), optInt(o, "games"), ranks));
		}
		out.sort(Comparator.comparingDouble(Entry::pt).reversed());
		return out;
	}

	/** 按名查询（忽略大小写；null = 无记录） */
	public static synchronized Entry find(ServerLevel level, String name) {
		for (Entry e : all(level))
			if (e.name().equalsIgnoreCase(name))
				return e;
		return null;
	}

	/** 名次的 1 起始序号（pt 降序，同名次并列取最前） */
	public static synchronized int rankOf(ServerLevel level, Entry target) {
		double best = Double.NaN;
		int rank = 0;
		for (Entry e : all(level)) {
			if (Double.compare(e.pt(), best) != 0) {
				best = e.pt();
				rank++;
			}
			if (e == target || (e.name().equals(target.name()) && e.pt() == target.pt()))
				return rank;
		}
		return rank;
	}

	// ---- 存取 ----

	/** 终局结算（uuid 版）：uuids 按席序，AI 席传 null。pt 增量按对局长度缩放：
	 *  一局战 ×0、东风战 ×0.5、半庄战 ×1（gameLen 与 RichiTableState.gameLen 对应）。
	 *  返回各席 [顺位1..4, 马, pt]（pt 为缩放后小数） */
	public static double[][] recordEnd(ServerLevel level, String[] names, String[] uuids, int[] points, int gameLen) {
		double scale = switch (gameLen) {
			case 2 -> 1.0;
			case 1 -> 0.5;
			default -> 0.0;
		};
		Integer[] order = { 0, 1, 2, 3 };
		java.util.Arrays.sort(order, (a, b) -> Integer.compare(points[b], points[a]));
		JsonObject root = load(level);
		double[][] result = new double[4][3];
		for (int rank = 0; rank < 4; rank++) {
			int seat = order[rank];
			int uma = UMA[rank];
			double pt = Math.round(((points[seat] - 30000) / 1000 + uma) * scale * 10.0) / 10.0;
			result[seat] = new double[] { rank + 1, uma, pt };
			if (uuids[seat] == null || uuids[seat].isEmpty())
				continue;
			JsonObject o = root.has(uuids[seat]) ? root.getAsJsonObject(uuids[seat]) : new JsonObject();
			o.addProperty("name", names[seat]);
			o.addProperty("pt", round1(optDouble(o, "pt") + pt));
			o.addProperty("games", optInt(o, "games") + 1);
			String rk = "r" + (rank + 1);
			o.addProperty(rk, optInt(o, rk) + 1);
			root.add(uuids[seat], o);
		}
		save(level, root);
		return result;
	}

	private static JsonObject load(ServerLevel level) {
		try {
			Path file = fileOf(level);
			if (Files.isRegularFile(file))
				return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
		} catch (Exception ignored) {
		}
		return new JsonObject();
	}

	private static void save(ServerLevel level, JsonObject root) {
		try {
			Path file = fileOf(level);
			Files.createDirectories(file.getParent());
			Files.writeString(file, root.toString());
		} catch (Exception ignored) {
		}
	}

	private static Path fileOf(ServerLevel level) {
		return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data/mahjong/ranks.json");
	}

	private static int optInt(JsonObject o, String k) {
		return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsInt() : 0;
	}

	private static double optDouble(JsonObject o, String k) {
		return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsDouble() : 0.0;
	}

	private static String optString(JsonObject o, String k, String def) {
		return o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : def;
	}

	private static double round1(double v) {
		return Math.round(v * 10.0) / 10.0;
	}
}
