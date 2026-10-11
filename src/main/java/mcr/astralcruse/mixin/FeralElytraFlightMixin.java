package mcr.astralcruse.mixin;

import mcr.murmol.feral.FeralFormManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 虚拟鞘翅（游戏规则 murmolFeralElytraFlight 开启时的会飞形态）的滑翔状态维持：
 * 原版 LivingEntity.updateFallFlying 每 tick 验证胸甲槽是鞘翅，否则把 sharedFlag(7) 清零，
 * 形态无装备会导致下一 tick 滑翔立即结束。本注入在有虚拟鞘翅时跳过原版验证，
 * 并在落地/入水/乘骑时按原版规则结束滑翔（双端，客户端用于消除专用服同步抖动）。
 * 启动由 PlayerTryStartFallFlyingMixin 与 LocalPlayerElytraGlideMixin 负责。
 */
@Mixin(LivingEntity.class)
public abstract class FeralElytraFlightMixin {

	@Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
	private void murmol$keepVirtualElytraFlying(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!FeralFormManager.useElytraFlight(self))
			return;
		if (self.onGround() || self.isInWater() || self.isPassenger()) {
			// 边沿检测：仅在确实处于滑翔的那一 tick 调用，避免每 tick 重复触发声音/状态包
			if (self.isFallFlying() && self instanceof Player player) {
				player.stopFallFlying();
			}
		}
		// 空中：不做任何事，保留 sharedFlag(7)=true；跳过原版胸甲验证与装备损耗
		ci.cancel();
	}
}
