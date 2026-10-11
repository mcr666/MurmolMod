package mcr.richi.game.ai.akochan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.mojang.logging.LogUtils;

/**
 * 单个 akochan 进程会话（一个座位一个实例：akochan pipe 模式单进程单视角 id）。
 * <p>stdin 逐行写 mjai 事件；stdout 逐行收响应（触发事件：本家 tsumo / 他家 dahai·kakan，各产出一行）。
 * 读取线程独立收行入队；{@link #request} = 清队 + 等待一行（严格"一触发一响应"消费纪律）。</p>
 */
final class AkochanSession {
	private final Process proc;
	private final BufferedWriter stdin;
	private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
	private volatile boolean healthy = true;
	private final int seatId;

	private AkochanSession(Process proc, int seatId) {
		this.proc = proc;
		this.seatId = seatId;
		this.stdin = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8));
		Thread t = new Thread(this::readLoop, "akochan-reader-" + seatId);
		t.setDaemon(true);
		t.start();
	}

	/** 启动 akochan pipe 模式进程；失败返回 null */
	static AkochanSession start(int seatId) {
		AkochanConfig cfg = AkochanConfig.get();
		try {
			Path work = cfg.workDir();
			Path exe = Path.of(cfg.exePath);
			if (!exe.isAbsolute())
				exe = work.resolve(exe).normalize();
			Path tactics = Path.of(cfg.tacticsPath);
			if (!tactics.isAbsolute())
				tactics = work.resolve(tactics).normalize();
			ProcessBuilder pb = new ProcessBuilder(exe.toString(), "pipe", tactics.toString(), String.valueOf(seatId));
			pb.directory(work.toFile());
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);
			return new AkochanSession(pb.start(), seatId);
		} catch (IOException e) {
			LogUtils.getLogger().error("[Akochan] 进程启动失败 seat={}", seatId, e);
			return null;
		}
	}

	boolean healthy() {
		return healthy && proc.isAlive();
	}

	/** 发送事件（无需响应的事件：start_kyoku / tsumo(他家) / chi / pon / 杠 / dora / reach 等） */
	void send(String json) {
		if (!healthy())
			return;
		try {
			synchronized (stdin) {
				stdin.write(json);
				stdin.write('\n');
				stdin.flush();
			}
		} catch (IOException e) {
			healthy = false;
			LogUtils.getLogger().error("[Akochan] 事件发送失败 seat={}", seatId, e);
		}
	}

	/**
	 * 决策请求：清空残留行后等待一响应（调用方保证上一触发已被消费）。
	 * 阻塞至响应/超时；在引擎后台线程调用。返回 null = 超时/不健康（回落本地 AI）。
	 */
	List<MjaiProtocolConverter.Action> request(long timeoutMs) {
		if (!healthy())
			return null;
		lines.clear(); // 残留丢弃（一触发一响应纪律下应为空）
		try {
			String line = lines.poll(timeoutMs, TimeUnit.MILLISECONDS);
			if (line == null) {
				LogUtils.getLogger().warn("[Akochan] 决策超时 seat={} ({}ms)", seatId, timeoutMs);
				return null;
			}
			return MjaiProtocolConverter.parseActions(line);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	/** 立即丢弃一行残留响应（fire-and-forget 触发：立直锁定摸切 / 九种九牌 / 自摸宣和等已终局场景） */
	void drainOne() {
		lines.poll();
	}

	void close() {
		healthy = false;
		proc.destroy();
	}

	private void readLoop() {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = r.readLine()) != null) {
				if (!line.isBlank())
					lines.offer(line);
			}
		} catch (IOException ignored) {
		} finally {
			healthy = false;
		}
	}
}
