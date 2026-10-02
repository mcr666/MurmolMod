package mcr.astralcruse.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import mcr.astralcruse.client.animation.FeralBedrockPlayerAnimator;
import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if (!FeralBedrockPlayerAnimator.shouldAnimate(entity)) {
            return;
        }
        if (slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
            ci.cancel();
        }
    }

    /**
     * 兽形头盔跟随兽头。
     *
     * 兽形身体由 FeralFormRenderer.renderHumanoid 用独立 bodyModel 渲染：在图层
     * poseStack 基础上额外乘 scale(-0.938*entityScale) 与 translate(0,-1.5,0)，
     * 并把 BODY_Y/Z_OFFSET 烘进骨骼坐标；原版盔甲层完全不感知这些变换，直接渲染
     * 会浮在原版头位置。这里包住 renderModel：渲染头盔前把图层 poseStack 复刻身体
     * 变换链，并把 bodyModel 中被动画驱动的头部件姿态绝对同步到盔甲模型
     * （两者头盒均为标准 8px 头盒，坐标空间一致后即可精确重合）。
     */
    @WrapOperation(
        method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V"
        )
    )
    private void astralCruse$renderFeralHeadArmor(
        HumanoidArmorLayer<?, ?, ?> instance,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        Model model,
        int color,
        ResourceLocation texture,
        Operation<Void> original,
        PoseStack enclosingPoseStack,
        MultiBufferSource enclosingBufferSource,
        LivingEntity entity,
        EquipmentSlot slot,
        int enclosingPackedLight,
        HumanoidModel<?> enclosingModel,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        PlayerModel<?> bodyModel = null;
        if (slot == EquipmentSlot.HEAD && FeralBedrockPlayerAnimator.shouldAnimate(entity)
            && model instanceof HumanoidModel<?> armor) {
            FeralForm form = FeralFormManager.getForm(entity);
            if (form != null && form.getBodyModel() != null) {
                bodyModel = form.getBodyModel();
                // 图层空间与身体模型空间近似一致（同 FeralItemInHandLayerMixin 的做法），
                // 只需把 bodyModel 中被动画驱动的头部件姿态同步到盔甲模型
                copyPose(bodyModel.head, armor.head);
                copyPose(bodyModel.head, armor.hat);
                // 石像/石化状态：姿态做绝对快照定死（进石像那一刻），彻底消除呼吸/插值等残余运动
                boolean statueFrozen = FeralFormManager.isInStatue(entity)
                    || mcr.murmol.potion.PetrifyMobEffect.isPetrified(entity);
                if (statueFrozen) {
                    float[] frozen = ASTRAL_CRUSE$FROZEN_ARMOR_POSES.computeIfAbsent(entity.getUUID(),
                        k -> new float[] {
                            armor.head.x, armor.head.y, armor.head.z,
                            armor.head.xRot, armor.head.yRot, armor.head.zRot,
                            armor.head.xScale, armor.head.yScale, armor.head.zScale
                        });
                    astralCruse$applyFrozenPose(armor.head, frozen);
                    astralCruse$applyFrozenPose(armor.hat, frozen);
                } else {
                    ASTRAL_CRUSE$FROZEN_ARMOR_POSES.remove(entity.getUUID());
                }
            }
        }
        original.call(instance, poseStack, bufferSource, packedLight, model, color, texture);
    }

    /** 石像状态下冻结的头盔姿态快照（按玩家 UUID，x/y/z + 三轴旋转 + 三轴缩放） */
    @Unique
    private static final java.util.Map<java.util.UUID, float[]> ASTRAL_CRUSE$FROZEN_ARMOR_POSES = new java.util.HashMap<>();

    @Unique
    private static void astralCruse$applyFrozenPose(ModelPart part, float[] frozen) {
        part.x = frozen[0];
        part.y = frozen[1];
        part.z = frozen[2];
        part.xRot = frozen[3];
        part.yRot = frozen[4];
        part.zRot = frozen[5];
        part.xScale = frozen[6];
        part.yScale = frozen[7];
        part.zScale = frozen[8];
    }

    @Unique
    private static void copyPose(ModelPart from, ModelPart to) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
        // 不同步 visible：兽形身体渲染管线可能隐藏部件，
        // 直接复制会导致头盔模型一并被隐藏
        to.visible = true;
    }
}
