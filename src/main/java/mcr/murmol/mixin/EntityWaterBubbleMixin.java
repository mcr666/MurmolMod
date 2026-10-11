package mcr.murmol.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;

import mcr.murmol.item.WaterBubbleHelmetItem;

/**
 * 水泡头盔：头戴时眼睛被判定为浸在水中（仅标签版眼睛检测，不影响 isInWater 的流体物理）。
 * 文鳐据此回满氧气；氧气消耗与溺水由 NeoForge 的 LivingBreatheEvent 负责（见 WaterBubbleHelmetItem），
 * 因为 NeoForge 用 getEyeInFluidType()/onLivingBreathe 重写了原版扣氧链路，标签判定只决定该事件是否触发。
 */
@Mixin(Entity.class)
public abstract class EntityWaterBubbleMixin {

	@Inject(method = "isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
	private void murmol$waterBubbleHelmet(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
		if (tag == FluidTags.WATER
				&& (Object) this instanceof LivingEntity living
				&& living.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof WaterBubbleHelmetItem) {
			cir.setReturnValue(true);
		}
	}
}
