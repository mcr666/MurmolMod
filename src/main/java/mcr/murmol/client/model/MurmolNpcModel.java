package mcr.murmol.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import mcr.murmol.entity.MurmolNpcEntity;

/**
 * Murmol NPC 模型（Blockbench 导出，96x96）：玩家形骨架 + 四段尾巴与发丝。
 * 动画 = 玩家（人形）基础动画（行走摆臂摆腿、头部转动）+ 尾部摇摆关键帧动画。
 */
public class MurmolNpcModel extends HierarchicalModel<MurmolNpcEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("murmol", "murmolnpc"), "main");

	private final ModelPart root;
	private final ModelPart Waist;
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart TailPrimary;
	private final ModelPart TailSecondary;
	private final ModelPart TailTertiary;
	private final ModelPart TailQuaternary;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public MurmolNpcModel(ModelPart root) {
		this.root = root;
		this.Waist = root.getChild("Waist");
		this.Head = this.Waist.getChild("Head");
		this.Body = this.Waist.getChild("Body");
		ModelPart tail = this.Body.getChild("Tail");
		this.TailPrimary = tail.getChild("TailPrimary");
		this.TailSecondary = this.TailPrimary.getChild("TailSecondary");
		this.TailTertiary = this.TailSecondary.getChild("TailTertiary");
		this.TailQuaternary = this.TailTertiary.getChild("TailQuaternary");
		this.rightArm = this.Waist.getChild("Right Arm");
		this.leftArm = this.Waist.getChild("Left Arm");
		this.rightLeg = root.getChild("Right Leg");
		this.leftLeg = root.getChild("Left Leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Waist = partdefinition.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition Head = Waist.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition Body = Waist.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.01F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition Tail = Body.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 10.25F, -1.5F, -0.1745F, 0.0F, 0.0F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.5F, 2.0F));

		TailPrimary.addOrReplaceChild("TailBase_r1", CubeListBuilder.create().texOffs(52, 64).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, -0.2313F, 0.9764F, 0.8727F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.9F, 2.6F));

		TailSecondary.addOrReplaceChild("TailBase_r2", CubeListBuilder.create().texOffs(80, 65).addBox(-2.0F, -1.75F, 12.6F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 13.85F, -4.8F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 2.25F, 5.5F));

		TailTertiary.addOrReplaceChild("TailBase_r3", CubeListBuilder.create().texOffs(31, 69).addBox(-2.0F, 9.0F, 10.3F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.4F, -10.5F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.25F, 6.5F));

		TailQuaternary.addOrReplaceChild("TailBase_r4", CubeListBuilder.create().texOffs(84, 87).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 10.35F, -12.4F, 1.7017F, 0.0F, 0.0F));

		TailQuaternary.addOrReplaceChild("TailBase_r5", CubeListBuilder.create().texOffs(4, 70).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(0.0F, 11.05F, -17.9F, 1.7017F, 0.0F, 0.0F));

		PartDefinition Hair2 = TailQuaternary.addOrReplaceChild("Hair2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -4.0F));

		Hair2.addOrReplaceChild("TailBase_r6", CubeListBuilder.create().texOffs(84, 75).addBox(1.48F, 20.3F, 7.1F, 0.02F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.1F, 0.2F, -8.5F, 0.4363F, 0.0F, -1.5708F));

		Hair2.addOrReplaceChild("TailBase_r7", CubeListBuilder.create().texOffs(84, 75).addBox(1.48F, 20.3F, 7.1F, 0.02F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.9F, -2.8F, -8.5F, 0.4363F, 0.0F, 1.5708F));

		Hair2.addOrReplaceChild("TailBase_r8", CubeListBuilder.create().texOffs(84, 75).addBox(-0.02F, -1.5F, -3.0F, 0.02F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 0.6891F, 10.1168F, -0.1745F, 0.0F, 0.0F));

		Hair2.addOrReplaceChild("TailBase_r9", CubeListBuilder.create().texOffs(84, 75).addBox(1.48F, 20.3F, 7.1F, 0.02F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -17.8F, -8.5F, 0.4363F, 0.0F, 0.0F));

		Waist.addOrReplaceChild("Right Arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, -10.0F, 0.0F));

		Waist.addOrReplaceChild("Left Arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		partdefinition.addOrReplaceChild("Right Leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		partdefinition.addOrReplaceChild("Left Leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 96, 96);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(MurmolNpcEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 头部转动
		this.Head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.Head.xRot = headPitch * Mth.DEG_TO_RAD;

		// 人形基础动画：行走摆臂摆腿 + 手臂自然摆动（同 HumanoidModel）
		this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F
				+ Mth.cos(ageInTicks * 0.0662F) * 0.025F;
		this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F
				+ Mth.cos(ageInTicks * 0.0662F + (float) Math.PI) * 0.025F;
		this.rightArm.zRot = Mth.cos(ageInTicks * 0.0662F) * 0.05F;
		this.leftArm.zRot = Mth.cos(ageInTicks * 0.0662F + (float) Math.PI) * 0.05F;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;

		// 尾部摇摆动画（等价于 Blockbench 4 秒循环关键帧：0→-A→0→+A→0，A=2.5°/5°）
		float tailPhase = ageInTicks * Mth.PI / 40.0F; // 周期 4 秒（80 tick）
		this.TailPrimary.yRot = Mth.sin(tailPhase) * -2.5F * Mth.DEG_TO_RAD;
		this.TailSecondary.yRot = Mth.sin(tailPhase) * -2.5F * Mth.DEG_TO_RAD;
		this.TailTertiary.yRot = Mth.sin(tailPhase) * -5.0F * Mth.DEG_TO_RAD;
		this.TailQuaternary.yRot = Mth.sin(tailPhase) * -5.0F * Mth.DEG_TO_RAD;
	}
}
