package mcr.murmol.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 薛定谔附魔：拥有 murmol:schrodinger 的装备在任何情况下都不渲染穿戴外观。
 * HumanoidArmorLayer 的 12 参 renderArmorPiece 是真正的绘制入口
 * （render(...) 与 6 参重载都委托到它），在此取消即可隐藏单件护甲外观。
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class SchrodingerArmorInvisibleMixin {

	private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> SCHRODINGER =
			ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("murmol:schrodinger"));

	@Inject(
			method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
			at = @At("HEAD"), cancellable = true)
	private void murmol$hideSchrodingerArmor(PoseStack poseStack, MultiBufferSource buffer,
			LivingEntity entity, EquipmentSlot slot, int i, HumanoidModel<?> model,
			float a, float b, float c, float d, float e, float f, CallbackInfo ci) {
		ItemStack stack = entity.getItemBySlot(slot);
		if (!stack.isEmpty() && hasSchrodinger(stack, entity)) {
			ci.cancel();
		}
	}

	private static boolean hasSchrodinger(ItemStack stack, LivingEntity entity) {
		var holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SCHRODINGER);
		return stack.getEnchantmentLevel(holder) > 0;
	}
}
