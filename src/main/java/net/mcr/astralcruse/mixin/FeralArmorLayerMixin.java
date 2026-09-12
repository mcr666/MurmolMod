package net.mcr.astralcruse.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class FeralArmorLayerMixin {
    @Inject(
        method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void astralCruse$hideFeralBodyArmor(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        LivingEntity entity,
        EquipmentSlot slot,
        int packedLight,
        HumanoidModel<?> model,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        if (FeralBedrockPlayerAnimator.shouldAnimate(entity)
            && (slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET)) {
            ci.cancel();
        }
    }
}
