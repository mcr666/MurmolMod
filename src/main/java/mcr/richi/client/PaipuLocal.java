package mcr.richi.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 客户端本地牌谱库（游戏根目录/richi/*.log，按桌一文件，文件名与服务端一致 x_y_z.log）。
 * 下载保存 / 本地牌谱列表 / 上传前读取。IO 失败以聊天框提示，不抛异常。
 */
public final class PaipuLocal {
	private PaipuLocal() {
	}

	/** 本地牌谱文件条目 */
	public record Entry(String name, long bytes) {
	}

	/** 本地牌谱目录：游戏根目录/richi */
	public static Path dir() {
		return FMLPaths.GAMEDIR.get().resolve("richi");
	}

	/** 列出本地牌谱（.log，按修改时间倒序） */
	public static List<Entry> list() {
		List<Entry> out = new ArrayList<>();
		try {
			if (!Files.isDirectory(dir()))
				return out;
			try (var stream = Files.list(dir())) {
				for (Path p : stream.filter(f -> f.getFileName().toString().endsWith(".log")).toList())
					out.add(new Entry(p.getFileName().toString(), Files.size(p)));
			}
			out.sort(Comparator.comparingLong((Entry e) -> {
				try {
					return Files.getLastModifiedTime(dir().resolve(e.name())).toMillis();
				} catch (Exception ex) {
					return 0L;
				}
			}).reversed());
		} catch (Exception ignored) {
		}
		return out;
	}

	/** 读取本地牌谱全文（文件名须为 x_y_z.log 形式；不存在/非法返回 null） */
	public static String read(String name) {
		try {
			if (!isValidName(name))
				return null;
			Path file = dir().resolve(name).normalize();
			if (!file.startsWith(dir()) || !Files.isRegularFile(file))
				return null;
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return null;
		}
	}

	/** 文件名 → 桌 origin packed（x_y_z.log；非法返回 0） */
	public static long packedOfName(String name) {
		try {
			String[] xyz = name.substring(0, name.length() - 4).split("_");
			return BlockPos.asLong(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
		} catch (Exception e) {
			return 0;
		}
	}

	/** 下载保存：服务端整桌牌谱写入 richi/x_y_z.log（覆盖），聊天框提示结果 */
	public static void save(long originPacked, String content) {
		var pos = BlockPos.of(originPacked);
		String name = pos.getX() + "_" + pos.getY() + "_" + pos.getZ() + ".log";
		if (content == null || content.isEmpty()) {
			msg(Component.translatable("gui.richi.paipu.none"));
			return;
		}
		try {
			Files.createDirectories(dir());
			Files.writeString(dir().resolve(name), content == null ? "" : content, StandardCharsets.UTF_8);
			msg(Component.translatable("gui.richi.paipu.saved", name));
		} catch (Exception e) {
			msg(Component.translatable("gui.richi.paipu.save_fail"));
		}
	}

	/** 上传结果提示（聊天框） */
	public static void uploadResult(boolean ok) {
		msg(Component.translatable(ok ? "gui.richi.paipu.uploaded" : "gui.richi.paipu.upload_fail"));
	}

	private static void msg(Component text) {
		var p = Minecraft.getInstance().player;
		if (p != null)
			p.displayClientMessage(text, false);
	}

	/** 文件名白名单：x_y_z.log（数字段），防路径穿越 */
	private static boolean isValidName(String name) {
		return name != null && name.matches("[0-9]+_[0-9]+_[0-9]+\\.log");
	}
}
