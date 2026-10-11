package mcr.richi.game.ai.akochan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * akochan 外部 AI 配置（config/richi/akochan/config.json）。
 * <p>akochan（critter-mj/akochan，开源强 AI）以外部进程运行：{@code akochan.exe pipe <tactics.json> <id>}，
 * stdin 逐行收 mjai 事件、stdout 逐行回动作。用户手动下载 akochan 可执行文件与学好权重，
 * tactics.json 内指向权重文件（相对路径以本目录为工作目录解析）。</p>
 */
public final class AkochanConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path DIR = Path.of("config", "richi", "akochan");
	private static final Path FILE = DIR.resolve("config.json");

	/** akochan 可执行文件（相对游戏目录） */
	public String exePath = "config/richi/akochan/akochan.exe";
	/** tactics 配置（含权重文件路径，相对本目录解析） */
	public String tacticsPath = "config/richi/akochan/tactics.json";
	/** 总开关（false 时 akochan 流派回落本地 AI） */
	public boolean enabled = true;
	/** 单次决策超时（毫秒）；超时回落 RiichiBot 本地决策 */
	public int decisionTimeoutMs = 20000;

	private static AkochanConfig instance;

	public static synchronized AkochanConfig get() {
		if (instance != null)
			return instance;
		AkochanConfig cfg = new AkochanConfig();
		try {
			if (Files.exists(FILE))
				cfg = GSON.fromJson(Files.readString(FILE), AkochanConfig.class);
			else {
				Files.createDirectories(DIR);
				Files.writeString(FILE, GSON.toJson(cfg));
			}
		} catch (IOException | RuntimeException e) {
			com.mojang.logging.LogUtils.getLogger().error("[Akochan] 配置读取失败，使用默认值", e);
		}
		instance = cfg == null ? new AkochanConfig() : cfg;
		return instance;
	}

	/** 进程工作目录（tactics.json 内相对权重路径的基准） */
	public Path workDir() {
		return DIR;
	}

	/** 配置可用性：返回 null = 可用，否则为缺失原因描述 */
	public String validate() {
		if (!enabled)
			return "disabled";
		if (!Files.isRegularFile(Path.of(exePath)) && !Files.isRegularFile(workDir().resolve(exePath)))
			return "exe 缺失: " + exePath;
		if (!Files.isRegularFile(Path.of(tacticsPath)) && !Files.isRegularFile(workDir().resolve(tacticsPath)))
			return "tactics.json 缺失: " + tacticsPath;
		return null;
	}
}
