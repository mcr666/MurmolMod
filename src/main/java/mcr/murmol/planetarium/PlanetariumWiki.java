package mcr.murmol.planetarium;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * 星象仪 wiki 数据加载器（客户端）。
 * 通过 Minecraft 资源管理器加载 assets/murmol/planetarium/ 下的 JSON。
 */
public final class PlanetariumWiki {

	public static class Category {
		public String id;
		public String title;
		public final List<PlanetariumEntry> entries = new ArrayList<>();
	}

	private static final Gson GSON = new Gson();
	private static final List<Category> CATEGORIES = new ArrayList<>();
	private static final Map<String, PlanetariumEntry> BY_ID = new LinkedHashMap<>();
	private static boolean loaded = false;
	private static String error = null;

	private PlanetariumWiki() {}

	/** 通过资源管理器读取输入流（不存在返回 null） */
	private static InputStream resource(String path) {
		try {
			ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("murmol", "planetarium/" + path);
			var opt = Minecraft.getInstance().getResourceManager().getResource(rl);
			if (opt.isEmpty())
				return null;
			return opt.get().open();
		} catch (Exception e) {
			return null;
		}
	}

	public static synchronized void loadIfNeeded() {
		if (loaded) return;
		CATEGORIES.clear();
		BY_ID.clear();
		error = null;
		try {
			InputStream is = resource("index.json");
			if (is == null) {
				error = "找不到 index.json";
				return;
			}
			JsonObject index = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
			JsonArray cats = index.getAsJsonArray("categories");
			for (var ce : cats) {
				JsonObject cat = ce.getAsJsonObject();
				Category c = new Category();
				c.id = cat.get("id").getAsString();
				c.title = cat.get("title").getAsString();
				JsonArray entryIds = cat.getAsJsonArray("entries");
				for (var eid : entryIds) {
					String entryId = eid.getAsString();
					InputStream eis = resource("entries/" + entryId + ".json");
					if (eis == null) continue;
					PlanetariumEntry entry = GSON.fromJson(
							new InputStreamReader(eis, StandardCharsets.UTF_8), PlanetariumEntry.class);
					c.entries.add(entry);
					BY_ID.put(entry.id, entry);
				}
				CATEGORIES.add(c);
			}
			loaded = true;
		} catch (Exception ex) {
			error = "加载失败: " + ex.getMessage();
			System.err.println("[Planetarium] " + error);
		}
	}

	public static List<Category> categories() {
		loadIfNeeded();
		return CATEGORIES;
	}

	public static PlanetariumEntry byId(String id) {
		loadIfNeeded();
		return BY_ID.get(id);
	}

	public static String error() { return error; }
}
