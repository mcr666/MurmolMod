package dev.tr7zw.firstperson.api;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;

/** 编译期 stub（不打包），运行时由 FPM 提供同 FQCN 真实接口。 */
public interface PlayerOffsetHandler {

	/**
	 * 自定义模型偏移。current 为 FPM 预计算值（或此前处理器结果）；不适用时应
	 * 原样返回 current。
	 */
	Vec3 applyOffset(AbstractClientPlayer entity, float delta, Vec3 original, Vec3 current);
}
