package net.mcr.murmol.procedures;

import net.minecraft.world.entity.Entity;

/**
 * 变身冲击（creative_shock）效果结束时执行。
 * 原实现在此恢复 mayBuild=true，但因客户端 handlePlayerAbilities 不同步 mayBuild，
 * 该恢复路径失效并造成轮廓永久消失；交互禁用已改由 PetrifyLockHandlers 实现，此过程保留为空。
 */
public class CreativeShockZhuangTaiXiaoGuoJieShuShiProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
	}
}
