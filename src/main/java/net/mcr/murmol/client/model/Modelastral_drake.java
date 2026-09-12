package net.mcr.murmol.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import net.mcr.murmol.entity.AstralDrakeEntity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelastral_drake<T extends AstralDrakeEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelastral_drake"), "main");
	public final ModelPart root;
	public final ModelPart torso;
	public final ModelPart head;
	public final ModelPart left_wing;
	public final ModelPart right_wing;
	public final ModelPart left_leg;
	public final ModelPart right_leg;
	public final ModelPart tail;

	public Modelastral_drake(ModelPart root) {
		this.root = root.getChild("root");
		this.torso = this.root.getChild("torso");
		this.head = this.torso.getChild("head");
		this.left_wing = this.torso.getChild("left_wing");
		this.right_wing = this.torso.getChild("right_wing");
		this.left_leg = this.torso.getChild("left_leg");
		this.right_leg = this.torso.getChild("right_leg");
		this.tail = this.torso.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));
		PartDefinition torso = root.addOrReplaceChild("torso",
				CubeListBuilder.create().texOffs(2, 2).addBox(-4.0F, -2.5F, -3.5F, 8.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(39, 2).addBox(-3.0F, -3.5F, -6.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(64, 2)
						.addBox(-3.0F, -0.5F, -6.5F, 6.0F, 4.0F, 11.0F, new CubeDeformation(-0.01F)).texOffs(101, 2).addBox(-1.0F, -5.5F, -4.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(112, 2)
						.addBox(-1.0F, -4.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(2, 20).addBox(-1.0F, -4.5F, 2.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.01F)),
				PartPose.offset(0.0F, -4.5F, 0.5F));
		PartDefinition head = torso.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -4.8F, -7.0F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(67, 20).addBox(-3.0F, -2.8F, -10.0F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(92, 20)
						.addBox(-2.0F, -0.8F, -9.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(111, 20).addBox(-4.25F, -3.05F, -7.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(2, 36)
						.addBox(2.25F, -3.05F, -7.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -3.7F, -6.5F));
		PartDefinition right_ear_fin_r1 = head.addOrReplaceChild("right_ear_fin_r1", CubeListBuilder.create().texOffs(54, 36).addBox(-0.8F, -3.6F, -2.2F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(2.8F, -2.2F, -1.8F, 0.0F, 0.0F, 0.3491F));
		PartDefinition left_ear_fin_r1 = head.addOrReplaceChild("left_ear_fin_r1", CubeListBuilder.create().texOffs(39, 36).addBox(-1.2F, -3.6F, -2.2F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(-2.8F, -2.2F, -1.8F, 0.0F, 0.0F, -0.3491F));
		PartDefinition right_horn_r1 = head.addOrReplaceChild("right_horn_r1", CubeListBuilder.create().texOffs(26, 36).addBox(-0.6F, -3.4F, -1.4F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.6F, -3.4F, -2.6F, 0.4363F, 0.0F, -0.2094F));
		PartDefinition left_horn_r1 = head.addOrReplaceChild("left_horn_r1", CubeListBuilder.create().texOffs(13, 36).addBox(-1.4F, -3.4F, -1.4F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.6F, -3.4F, -2.6F, 0.4363F, 0.0F, 0.2094F));
		PartDefinition neck_r1 = head.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(15, 20).addBox(-2.0F, -2.8F, -2.8F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.8F, -0.2094F, 0.0F, 0.0F));
		PartDefinition left_wing = torso.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(-3.2F, -1.7F, -2.1F));
		PartDefinition left_wing_finger_r1 = left_wing.addOrReplaceChild("left_wing_finger_r1",
				CubeListBuilder.create().texOffs(50, 47).addBox(-4.8F, -0.8F, -2.3F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(2, 47).addBox(-7.8F, 0.2F, -1.3F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0F, 0.0F, -0.1F, 0.0F, 0.3142F, -0.2618F));
		PartDefinition left_wing_membrane_02_r1 = left_wing.addOrReplaceChild("left_wing_membrane_02_r1", CubeListBuilder.create().texOffs(25, 47).addBox(-7.8F, -0.3F, -1.3F, 9.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, 1.5F, 0.9F, 0.0F, 0.3491F, -0.4887F));
		PartDefinition left_wing_tip_r1 = left_wing.addOrReplaceChild("left_wing_tip_r1", CubeListBuilder.create().texOffs(94, 36).addBox(-8.8F, -1.0F, -1.8F, 10.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, 1.2F, 1.4F, 0.0F, 0.3491F, -0.4538F));
		PartDefinition left_wing_arm_r1 = left_wing.addOrReplaceChild("left_wing_arm_r1", CubeListBuilder.create().texOffs(69, 36).addBox(-7.6F, -1.2F, -1.3F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.2F, -0.6F, -0.1F, 0.0F, 0.3142F, -0.1396F));
		PartDefinition right_wing = torso.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(3.2F, -1.7F, -2.1F));
		PartDefinition right_wing_finger_r1 = right_wing.addOrReplaceChild("right_wing_finger_r1",
				CubeListBuilder.create().texOffs(50, 57).addBox(2.8F, -0.8F, -2.3F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(2, 57).addBox(-0.2F, 0.2F, -1.3F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, 0.0F, -0.1F, 0.0F, -0.3142F, 0.2618F));
		PartDefinition right_wing_membrane_02_r1 = right_wing.addOrReplaceChild("right_wing_membrane_02_r1", CubeListBuilder.create().texOffs(25, 57).addBox(-1.2F, -0.3F, -1.3F, 9.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.0F, 1.5F, 0.9F, 0.0F, -0.3491F, 0.4887F));
		PartDefinition right_wing_tip_r1 = right_wing.addOrReplaceChild("right_wing_tip_r1", CubeListBuilder.create().texOffs(86, 47).addBox(-1.2F, -1.0F, -1.8F, 10.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.0F, 1.2F, 1.4F, 0.0F, -0.3491F, 0.4538F));
		PartDefinition right_wing_arm_r1 = right_wing.addOrReplaceChild("right_wing_arm_r1", CubeListBuilder.create().texOffs(61, 47).addBox(-0.4F, -1.2F, -1.3F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.2F, -0.6F, -0.1F, 0.0F, -0.3142F, 0.1396F));
		PartDefinition left_leg = torso.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-2.8F, 2.5F, 1.7F));
		PartDefinition left_claw_02_r1 = left_leg.addOrReplaceChild("left_claw_02_r1", CubeListBuilder.create().texOffs(104, 57).addBox(-1.3F, -0.8F, -1.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.08F)),
				PartPose.offsetAndRotation(-0.9F, 2.8F, -2.8F, 0.0F, 0.0F, 0.1396F));
		PartDefinition left_claw_01_r1 = left_leg.addOrReplaceChild("left_claw_01_r1", CubeListBuilder.create().texOffs(95, 57).addBox(-0.8F, -0.8F, -1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.08F)),
				PartPose.offsetAndRotation(0.6F, 2.8F, -2.8F, 0.0F, 0.0F, -0.1396F));
		PartDefinition left_foot_r1 = left_leg.addOrReplaceChild("left_foot_r1", CubeListBuilder.create().texOffs(78, 57).addBox(-2.2F, -0.7F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.7F, -1.2F, 0.0F, 0.0F, -0.0698F));
		PartDefinition left_thigh_r1 = left_leg.addOrReplaceChild("left_thigh_r1", CubeListBuilder.create().texOffs(61, 57).addBox(-2.4F, -1.6F, -1.8F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.2F, -0.4F, 0.6F, -0.1745F, 0.0F, -0.1047F));
		PartDefinition right_leg = torso.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(2.8F, 2.5F, 1.7F));
		PartDefinition right_claw_02_r1 = right_leg.addOrReplaceChild("right_claw_02_r1", CubeListBuilder.create().texOffs(45, 68).addBox(-0.7F, -0.8F, -1.4F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.08F)),
				PartPose.offsetAndRotation(0.9F, 2.8F, -2.8F, 0.0F, 0.0F, -0.1396F));
		PartDefinition right_claw_01_r1 = right_leg.addOrReplaceChild("right_claw_01_r1", CubeListBuilder.create().texOffs(36, 68).addBox(-0.2F, -0.8F, -1.4F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.08F)),
				PartPose.offsetAndRotation(-0.6F, 2.8F, -2.8F, 0.0F, 0.0F, 0.1396F));
		PartDefinition right_foot_r1 = right_leg.addOrReplaceChild("right_foot_r1", CubeListBuilder.create().texOffs(19, 68).addBox(-0.8F, -0.7F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.7F, -1.2F, 0.0F, 0.0F, 0.0698F));
		PartDefinition right_thigh_r1 = right_leg.addOrReplaceChild("right_thigh_r1", CubeListBuilder.create().texOffs(2, 68).addBox(-0.6F, -1.6F, -1.8F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.2F, -0.4F, 0.6F, -0.1745F, 0.0F, 0.1047F));
		PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.1F, 4.5F));
		PartDefinition tail_fin_right_r1 = tail.addOrReplaceChild("tail_fin_right_r1", CubeListBuilder.create().texOffs(30, 80).addBox(0.0F, -1.8F, -0.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.2F, 11.6F, 0.0F, 0.0F, 0.4363F));
		PartDefinition tail_fin_left_r1 = tail.addOrReplaceChild("tail_fin_left_r1", CubeListBuilder.create().texOffs(15, 80).addBox(-3.0F, -1.8F, -0.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.2F, 11.6F, 0.0F, 0.0F, -0.4363F));
		PartDefinition tail_tip_r1 = tail.addOrReplaceChild("tail_tip_r1", CubeListBuilder.create().texOffs(2, 80).addBox(-1.0F, -0.9F, -0.6F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.3F, 10.6F, -0.4363F, 0.0F, 0.0F));
		PartDefinition tail_03_r1 = tail.addOrReplaceChild("tail_03_r1", CubeListBuilder.create().texOffs(100, 68).addBox(-1.0F, -0.6F, -0.6F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(0.0F, 1.0F, 7.6F, -0.3142F, 0.0F, 0.0F));
		PartDefinition tail_02_r1 = tail.addOrReplaceChild("tail_02_r1", CubeListBuilder.create().texOffs(77, 68).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(0.0F, 0.4F, 4.0F, -0.2094F, 0.0F, 0.0F));
		PartDefinition tail_01_r1 = tail.addOrReplaceChild("tail_01_r1", CubeListBuilder.create().texOffs(56, 68).addBox(-2.0F, -1.6F, 0.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(AstralDrakeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}