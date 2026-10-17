package dev.tr7zw.firstperson.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 手写编译期 stub（不打包，jar 排除 dev/tr7zw/**）。
 * 运行时由 FirstPersonModel（1.21.1-neoforge）提供同 FQCN 真实类，形状一致。
 */
public final class FirstPersonAPI {

	private static final List<PlayerOffsetHandler> playerOffsetHandlers = new ArrayList<>();
	private static final List<ActivationHandler> activationHandlers = new ArrayList<>();

	private FirstPersonAPI() {
	}

	/** 当前 FPM 是否启用 */
	public static boolean isEnabled() {
		return false;
	}

	/** 模拟 F6 热键切换启用状态 */
	public static void setEnabled(boolean enabled) {
	}

	/** 第一人称玩家渲染期间返回 true（此时不应渲染头/头盔/帽子） */
	public static boolean isRenderingPlayer() {
		return false;
	}

	/** 注册处理器，支持 {@link PlayerOffsetHandler} 与 {@link ActivationHandler} */
	public static void registerPlayerHandler(Object handler) {
		if (handler instanceof PlayerOffsetHandler offset) {
			playerOffsetHandlers.add(offset);
		}
		if (handler instanceof ActivationHandler activation) {
			activationHandlers.add(activation);
		}
	}

	public static List<PlayerOffsetHandler> getPlayerOffsetHandlers() {
		return playerOffsetHandlers;
	}

	public static List<ActivationHandler> getActivationHandlers() {
		return activationHandlers;
	}
}
