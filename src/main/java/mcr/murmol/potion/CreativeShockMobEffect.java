package mcr.murmol.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class CreativeShockMobEffect extends MobEffect {
	public CreativeShockMobEffect() {
		super(MobEffectCategory.HARMFUL, -1);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	/**
	 * 变身冲击效果持续期间每刻执行：关闭玩家打开的容器界面。
	 * 注意：不得在此修改 mayBuild —— 客户端 handlePlayerAbilities 不同步该字段，
	 * 且客户端效果到期不触发事件，会导致方块轮廓/交互判定永久卡死（见 docs/PROJECT_MEMORY.md）。
	 * 交互禁用由 PetrifyLockHandlers.isLocked 判定 hasEffect(CREATIVE_SHOCK) 实现。
	 */
	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player _player)
			_player.closeContainer();
		return super.applyEffectTick(entity, amplifier);
	}
}