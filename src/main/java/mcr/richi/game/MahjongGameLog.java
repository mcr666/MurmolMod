package mcr.richi.game;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/**
 * 牌谱记录（纯追加文本，紧凑位置化格式，无键名）。
 * 写入 存档目录/data/mahjong/&lt;x&gt;_&lt;y&gt;_&lt;z&gt;.log（按风盘原点一桌一文件），IO 失败静默忽略。
 * <p>行格式（| 分隔，按位置解读）：</p>
 * <pre>
 * G|unix秒|总局数|名0|名1|名2|名3|点0|点1|点2|点3          （开局；AI 名带 (AI:流派)）
 * S|手0|手1|手2|手3|牌山|宝牌堆10张|岭上4张                  （每局开局，牌谱查看器回放用）
 * T|席|d|牌记法|旗                                       （切牌；旗 r=立直宣言，可空）
 * T|席|c/p/k|三张记法|来源席                              （吃/碰/明杠）
 * T|席|a/g|牌记法                                        （暗杠四张/加杠一张）
 * T|席|q|                                                （九种九牌宣言）
 * H|局号|r|和牌席|放铳席|翻|符|点数|役种|手牌|副露            （荣和）
 * H|局号|t|和牌席|翻|符|役种|手牌|副露                       （自摸）
 * H|局号|d|听牌掩码(0-15)|待张...（仅听牌席按席序）            （流局）
 * E|点0,点1,点2,点3                                    （终局；排名由此推导）
 * </pre>
 * <p>手牌/待张/副露/牌山为 {@link MahjongTileNotation} 记法；玩家名仅 G 行出现，其余行用席位号。
 * 回放推导：摸牌 = 牌山尾（吃碰后该家不摸、杠后岭上摸）；指示牌数 = 1 + 暗杠/加杠次数。</p>
 */
public final class MahjongGameLog {
	private MahjongGameLog() {
	}

	/** 追加一行到该桌的牌谱文件（不存在则创建，含目录） */
	public static synchronized void write(ServerLevel level, BlockPos origin, String line) {
		try {
			Path file = fileOf(level, origin);
			Files.createDirectories(file.getParent());
			Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Exception ignored) {
		}
	}

	/** G 行：开局（名称已含 AI 标注） */
	public static void gameStart(ServerLevel level, BlockPos origin, int totalHands, String[] names,
			int[] points) {
		StringBuilder sb = new StringBuilder("G|").append(System.currentTimeMillis() / 1000)
				.append('|').append(totalHands);
		for (String n : names)
			sb.append('|').append(n);
		for (int p : points)
			sb.append('|').append(p);
		write(level, origin, sb.toString());
	}

	/** H 行：荣和（ron=true，含放铳席与支付点）/ 自摸 */
	public static void win(ServerLevel level, BlockPos origin, int hand, boolean ron, int winSeat,
			int fromSeat, String yakuZh, int han, int fu, int pay, String handNotation, String melds) {
		StringBuilder sb = new StringBuilder("H|").append(hand).append(ron ? "|r|" : "|t|").append(winSeat);
		if (ron)
			sb.append('|').append(fromSeat);
		sb.append('|').append(han).append('|').append(fu);
		if (ron)
			sb.append('|').append(pay);
		sb.append('|').append(yakuZh).append('|').append(handNotation).append('|').append(melds);
		write(level, origin, sb.toString());
	}

	/** H 行：流局（tenpai=是否听牌；waits=听牌家待张记法，未听传空串） */
	public static void draw(ServerLevel level, BlockPos origin, int hand, boolean[] tenpai, String[] waits) {
		int mask = 0;
		for (int i = 0; i < 4; i++)
			if (tenpai[i])
				mask |= 1 << i;
		StringBuilder sb = new StringBuilder("H|").append(hand).append("|d|").append(mask);
		for (int i = 0; i < 4; i++)
			if (tenpai[i])
				sb.append('|').append(waits[i]);
		write(level, origin, sb.toString());
	}

	/** S 行：每局开局（四家 13 张手牌 + 剩余牌山 + 宝牌堆 + 岭上 4 张，供回放重建） */
	public static void handStart(ServerLevel level, BlockPos origin, String[] hands, String wall,
			String doraStack, String rinshan) {
		write(level, origin, "S|" + hands[0] + '|' + hands[1] + '|' + hands[2] + '|' + hands[3]
				+ '|' + wall + '|' + doraStack + '|' + rinshan);
	}

	/** T 行：过程事件（action: d=切牌 c=吃 p=碰 k=明杠 a=暗杠 g=加杠 q=九种九牌） */
	public static void turnEvent(ServerLevel level, BlockPos origin, int seat, String action,
			String detail) {
		write(level, origin, "T|" + seat + '|' + action + (detail.isEmpty() ? "" : "|" + detail));
	}

	/** E 行：终局四人点数（排名由此推导） */
	public static void gameEnd(ServerLevel level, BlockPos origin, int[] points) {
		write(level, origin, "E|" + points[0] + ',' + points[1] + ',' + points[2] + ',' + points[3]);
	}

	/** 读取该桌全部牌谱内容（不存在返回 null） */
	public static synchronized String read(ServerLevel level, BlockPos origin) {
		try {
			Path file = fileOf(level, origin);
			return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : null;
		} catch (Exception e) {
			return null;
		}
	}

	/** 桌对应文件：存档目录/data/mahjong/x_y_z.log */
	private static Path fileOf(ServerLevel level, BlockPos origin) {
		return level.getServer().getWorldPath(LevelResource.ROOT)
				.resolve("data/mahjong/" + origin.getX() + "_" + origin.getY() + "_" + origin.getZ() + ".log");
	}

	/**
	 * 列出全部牌谱（按对局开始时间倒序）：每项 "x_y_z|局序|开始毫秒"。
	 * 一桌一文件，文件内每个 G…E 段拆为一个条目（局序 = 文件内第几局，回放定位用）。
	 */
	public static synchronized java.util.List<String> list(ServerLevel level) {
		java.util.List<String> out = new java.util.ArrayList<>();
		try {
			Path dir = fileOf(level, new BlockPos(0, 0, 0)).getParent();
			if (!Files.isDirectory(dir))
				return out;
			try (var stream = Files.list(dir)) {
				for (Path p : stream.filter(f -> f.getFileName().toString().endsWith(".log")).toList()) {
					String base = p.getFileName().toString();
					base = base.substring(0, base.length() - 4);
					long mtime = Files.getLastModifiedTime(p).toMillis();
					int gameNo = 0;
					for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
						if (!line.startsWith("G|"))
							continue;
						long ts = mtime;
						try {
							ts = Long.parseLong(line.split("\\|")[1]) * 1000;
						} catch (Exception ignored) {
						}
						out.add(base + "|" + gameNo + "|" + ts);
						gameNo++;
					}
					if (gameNo == 0) // 不完整对局也按桌列出
						out.add(base + "|0|" + mtime);
				}
			}
			out.sort((a, b) -> Long.compare(
					Long.parseLong(b.substring(b.lastIndexOf('|') + 1)),
					Long.parseLong(a.substring(a.lastIndexOf('|') + 1))));
		} catch (Exception ignored) {
		}
		return out;
	}
}
