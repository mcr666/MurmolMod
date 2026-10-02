package mcr.murmol.potion;

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
		// 磐座石像（右键激活）：清零移动/跳跃但保留 shift（shift 是磐座石像的解除方式）
		boolean statueOnBanza = mcr.murmol.feral.FeralFormManager.isInBanzaStatue(player);
		// 石化药水、被囚笼禁锢：输入全禁；吞食骑乘场景禁用潜行（潜行会触发原版下座）
		boolean fullyLocked = PetrifyMobEffect.isPetrified(player)
				|| player.getData(mcr.murmol.network.MurmolModVariables.CAGED_STATE)
				|| player.getData(mcr.murmol.network.MurmolModVariables.PLAYER_VARIABLES).caged;
		if (fullyLocked || statueOnBanza) {
			Input input = event.getInput();
			input.leftImpulse = 0.0F;
			input.forwardImpulse = 0.0F;
			input.up = false;
			input.down = false;
			input.left = false;
			input.right = false;
			input.jumping = false;
			if (fullyLocked)
				input.shiftKeyDown = false;
		}
	}
}
