package mcr.murmol.planetarium;

import java.util.ArrayList;
import java.util.List;

/**
 * 星象仪 wiki 条目数据模型。一个 JSON 文件对应一条。
 * type: "form" | "item" | "page"
 */
public class PlanetariumEntry {
	public String id;
	public String type = "page";
	public String title = "";
	public String icon = "minecraft:paper";
	public String category = "";
	public int sort = 0;
	/** 解锁该条目所需的 advancement（如 "murmol:unlock/dark_fury"）；空字符串表示始终可见 */
	public String advancement = "";

	// ---- 通用 ----
	public String text = ""; // markdown 正文

	// ---- 形态模板 ----
	public List<String> attributes = new ArrayList<>(); // 属性/能力列表
	public String[][] offering = new String[0][0];     // 祭物配方 2x2（物品 id）
	public String biography = "";                        // 传记长文

	// ---- 物品模板 ----
	public String craftingPattern = ""; // 有序配方网格（如 "GGG\nIRI\n I "）
	public String craftingKey = "{}";  // key 映射 JSON

	public boolean isForm()  { return "form".equals(type); }
	public boolean isItem()  { return "item".equals(type); }
}
