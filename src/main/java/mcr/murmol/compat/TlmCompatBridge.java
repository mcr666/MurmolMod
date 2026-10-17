package mcr.murmol.compat;

import java.lang.reflect.Method;

/**
 * TouhouLittleMaid 联动桥（核心包，零 TLM 类引用）：ModList 检测 + 惰性 Class.forName，
 * 仅在 TLM 已加载时才触碰 {@code mcr.murmol.compat.tlm.TlmCompat}；
 * 未安装 TLM 时本桥不加载任何 TLM 类（对照 {@code FPMCompat} 的软依赖范式）。
 */
public final class TlmCompatBridge {
	private static final String TLM_MODID = "touhou_little_maid";
	private static final String COMPAT_CLASS = "mcr.murmol.compat.tlm.TlmCompat";

	private static Boolean loaded;
	private static Method registerMethod;

	private TlmCompatBridge() {
	}

	public static boolean isTlmLoaded() {
		if (loaded == null) {
			var modList = net.neoforged.fml.ModList.get();
			loaded = modList != null && modList.isLoaded(TLM_MODID);
		}
		return loaded;
	}

	/** 主类构造时调用：TLM 在场才惰性加载兼容层（未安装时静默返回） */
	public static void init() {
		if (!isTlmLoaded())
			return;
		try {
			if (registerMethod == null)
				registerMethod = Class.forName(COMPAT_CLASS).getMethod("register");
			registerMethod.invoke(null);
		} catch (Throwable t) {
			mcr.murmol.MurmolMod.LOGGER.warn("[TLM Compat] register failed: {}", t);
		}
	}
}
