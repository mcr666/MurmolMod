package net.mcr.murmol.potion;

import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.player.Input;

/**
 * 石化移动锁定（仅客户端）：MovementInputUpdateEvent 是客户端专属事件，
 * 输入清零后玩家无法移动/跳跃；服务端速度清零见 PetrifyMobEffect.applyEffectTick。
 */
@EventBusSubscriber(Dist.CLIENT)
public class PetrifyMovementLock {

	@SubscribeEvent
	public static void onMovementInput(MovementInputUpdateEvent event) {
		Player player = event.getEntity();
		// 石化药水、被囚笼禁锢，或磐座上的狛犬石像（石像必须物理定身，否则走动会离开磐座导致状态被移动解除）
		if (PetrifyMobEffect.isPetrified(player)
				|| player.getData(net.mcr.murmol.network.MurmolModVariables.CAGED_STATE)
				|| player.getData(net.mcr.murmol.network.MurmolModVariables.PLAYER_VARIABLES).caged
				|| (net.mcr.murmol.feral.FeralFormManager.isInStatue(player)
						&& net.mcr.murmol.feral.FeralFormManager.isOnBanza(player))) {
			Input input = event.getInput();
			input.leftImpulse = 0.0F;
			input.forwardImpulse = 0.0F;
			input.up = false;
			input.down = false;
			input.left = false;
			input.right = false;
			input.jumping = false;
		}
	}
}
