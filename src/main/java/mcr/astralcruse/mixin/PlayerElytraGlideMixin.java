package mcr.astralcruse.mixin;

import mcr.murmol.feral.FeralFormManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 虚拟鞘翅启动（双端）：Player.tryToStartFallFlying 原版只认胸甲槽鞘翅。
 * 客户端 LocalPlayer.aiStep 与服务端收 START_FALL_FLYING 包都会虚分派到本方法，
 * 有虚拟鞘翅的形态复制原版前置条件后直接 startFallFlying，绕过装备检查。
 * 注意：客户端还需 LocalPlayerElytraGlideMixin 先通过 NeoForge 的 canElytraFly 前置门，
 * 否则 aiStep 在调用本方法之前就已跳走。
 */
@Mixin(Player.class)
public abstract class PlayerElytraGlideMixin {

	@Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
	private void murmol$virtualElytraStart(CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (self.onGround() || self.isFallFlying() || self.isInWater()
				|| self.hasEffect(MobEffects.LEVITATION))
			return;
		if (!FeralFormManager.useElytraFlight(self))
			return;
		self.startFallFlying();
		cir.setReturnValue(true);
	}
}
