package mcr.murmol.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import mcr.murmol.item.WaterBubbleHelmetItem;

/**
 * 水泡头盔仅令眼睛被判定浸水（气泡/缺氧/溺水由 isEyeInFluid 驱动），
 * 但身体不在真水中时玩家不能被当作全身在水下：
 * 否则客户端 isUnderWater() 恒真而脚下无真实水体，疾跑会在陆地/水岸反复切入游泳又弹回。
 * wasUnderwater 声明于 Player（@Shadow 不查父类），LocalPlayer 的重写会经 super 调用走到这里。
 */
@Mixin(Player.class)
public abstract class LocalPlayerWaterBubbleMixin {

	@Shadow
	protected boolean wasUnderwater;

	@Inject(method = "updateIsUnderwater", at = @At("HEAD"), cancellable = true)
	private void murmol$waterBubbleDryBody(CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (!self.isInWater()
				&& self.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof WaterBubbleHelmetItem) {
			this.wasUnderwater = false;
			cir.setReturnValue(false);
		}
	}
}
