package net.mcr.murmol.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

/**
 * 变身冲击（creative_shock）效果持续期间每刻执行：关闭玩家打开的容器界面。
 * 注意：不得在此修改 mayBuild —— 客户端 handlePlayerAbilities 不同步该字段，
 * 且客户端效果到期不触发事件，会导致方块轮廓/交互判定永久卡死（见 docs/PROJECT_MEMORY.md）。
 * 交互禁用由 PetrifyLockHandlers.isLocked 判定 hasEffect(CREATIVE_SHOCK) 实现。
 */
public class CreativeShockXiaoGuoChiXuShiMeiKeFaShengProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _player)
			_player.closeContainer();
	}
}
