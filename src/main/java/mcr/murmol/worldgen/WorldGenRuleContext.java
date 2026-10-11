package mcr.murmol.worldgen;

/**
 * 世界生成参数列表构建期的游戏规则线程上下文。
 * 群系噪声预设（MultiNoiseBiomeSourceParameterList.Preset 的 SourceProvider）在构建
 * ParameterList 时无法访问 WorldData 的 GameRules（该对象只是静态预设常量），
 * 故由 MultiNoiseBiomeSourceParameterListMixin 在「世界创建、且能取到 WorldData 规则」
 * 的构造路径中压入当前规则值；预设 provider 用 {@link #currentInfectionRange()} 读取，
 * 构建结束立即 clear。线程隔离避免多世界并行构建相互污染。
 */
public final class WorldGenRuleContext {
	private static final ThreadLocal<Integer> INFECTION_RANGE = new ThreadLocal<>();

	private WorldGenRuleContext() {
	}

	public static void setInfectionRange(int range) {
		INFECTION_RANGE.set(range);
	}

	public static void clear() {
		INFECTION_RANGE.remove();
	}

	/** 当前线程的幻星瘟疫范围等级；无上下文（数据包/静态引用）返回 2（现行默认） */
	public static int currentInfectionRange() {
		Integer value = INFECTION_RANGE.get();
		return value == null ? 2 : value;
	}
}
