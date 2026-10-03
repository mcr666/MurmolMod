package mcr.murmol.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 显形：绿色粒子。持续期间玩家被临时切换形态（变形者→人类，人类→随机形态），
 * 效果结束后复原。期间无法使用灵魂物品与照妖镜。仅对玩家生效。
 * 形态替换入口：照妖镜方块走 {@link RevealManager#apply}，药水（饮用/喷溅）走 onEffectStarted。
 */
public class RevealMobEffect extends MobEffect {
	public RevealMobEffect() {
		super(MobEffectCategory.NEUTRAL, 0x39FF6A);
	}

	@Override
	public void onEffectStarted(net.minecraft.world.entity.LivingEntity entity, int amplifier) {
		// 药水路径：首次获得效果时记录原形态并临时替换（已有记录说明是照妖镜方块路径，跳过）
		if (!entity.level().isClientSide() && entity instanceof net.minecraft.server.level.ServerPlayer player
				&& player.getData(mcr.murmol.network.MurmolModVariables.PLAYER_VARIABLES).revealOriginalFormId.isEmpty())
			RevealManager.beginReveal(player);
	}
}
