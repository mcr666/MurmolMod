package mcr.astralcruse.mixin;

import mcr.murmol.feral.FeralFormManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 虚拟鞘翅客户端前置门：NeoForge 在 LocalPlayer.aiStep 中先调用
 * ItemStack.canElytraFly(entity)，返回 false 则直接跳过后续 tryToStartFallFlying
 * 与 START_FALL_FLYING 发包（没穿鞘翅时连启动方法都不会调用）。
 * 这里重定向该判定：有虚拟鞘翅的形态返回 true 让流程继续，
 * 随后 tryToStartFallFlying 由 PlayerElytraGlideMixin（双端）接管并真正进入滑翔。
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerElytraGlideMixin {

	@Redirect(
			method = "aiStep",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
	private boolean murmol$virtualElytraCanFly(ItemStack chestStack, LivingEntity entity) {
		if (FeralFormManager.useElytraFlight(entity))
			return true;
		return chestStack.canElytraFly(entity);
	}
}
