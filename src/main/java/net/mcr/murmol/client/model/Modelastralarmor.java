package net.mcr.murmol.client.model;

import net.minecraft.world.entity.Entity;
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

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelastralarmor<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelastralarmor"), "main");
	public final ModelPart body;
	public final ModelPart torso;
	public final ModelPart head;
	public final ModelPart LeftEar;
	public final ModelPart LeftEarPivot;
	public final ModelPart RightEar;
	public final ModelPart RightEarPivot;
	public final ModelPart bone9;
	public final ModelPart right_arm;
	public final ModelPart left_arm;
	public final ModelPart RightFoot;
	public final ModelPart right_leg;
	public final ModelPart bone15;
	public final ModelPart LeftFoot;
	public final ModelPart left_leg;
	public final ModelPart bone16;

	public Modelastralarmor(ModelPart root) {
		this.body = root.getChild("body");
		this.torso = this.body.getChild("torso");
		this.head = this.body.getChild("head");
		this.LeftEar = this.head.getChild("LeftEar");
		this.LeftEarPivot = this.LeftEar.getChild("LeftEarPivot");
		this.RightEar = this.head.getChild("RightEar");
		this.RightEarPivot = this.RightEar.getChild("RightEarPivot");
		this.bone9 = this.head.getChild("bone9");
		this.right_arm = this.body.getChild("right_arm");
		this.left_arm = this.body.getChild("left_arm");
		this.RightFoot = this.body.getChild("RightFoot");
		this.right_leg = this.body.getChild("right_leg");
		this.bone15 = this.right_leg.getChild("bone15");
		this.LeftFoot = this.body.getChild("LeftFoot");
		this.left_leg = this.body.getChild("left_leg");
		this.bone16 = this.left_leg.getChild("bone16");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -1.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -11.0F, 0.0F));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -12.0F, 0.0F));
		PartDefinition LeftEar = head.addOrReplaceChild("LeftEar", CubeListBuilder.create(), PartPose.offset(3.0F, -8.2F, 0.0F));
		PartDefinition LeftEarPivot = LeftEar.addOrReplaceChild("LeftEarPivot",
				CubeListBuilder.create().texOffs(4, 38).addBox(-1.1F, -1.2F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(4, 38).addBox(-1.1F, -1.6F, -0.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.04F)).texOffs(4, 38)
						.addBox(-1.1F, -0.6F, 0.1F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.04F)).texOffs(4, 38).addBox(-1.1F, -2.3F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(19, 26)
						.addBox(-1.1F, -3.3F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(19, 26).addBox(-1.1F, -3.2F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(-0.5F, -1.25F, 0.0F, -0.1309F, -0.5236F, 0.3491F));
		PartDefinition RightEar = head.addOrReplaceChild("RightEar", CubeListBuilder.create(), PartPose.offset(-3.0F, -8.2F, 0.0F));
		PartDefinition RightEarPivot = RightEar.addOrReplaceChild("RightEarPivot",
				CubeListBuilder.create().texOffs(4, 38).addBox(-1.9F, -1.2F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(4, 38).addBox(-0.9F, -1.6F, -0.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.04F)).texOffs(4, 38)
						.addBox(-0.9F, -0.6F, 0.1F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.04F)).texOffs(4, 38).addBox(-0.9F, -2.3F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(19, 26)
						.addBox(0.1F, -3.2F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(19, 26).addBox(0.1F, -3.3F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(0.5F, -1.25F, 0.0F, -0.1309F, 0.5236F, -0.3491F));
		PartDefinition bone9 = head.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(4, 38).addBox(-1.0F, -5.1F, 3.9F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));
		PartDefinition cube_r1 = bone9.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 38).addBox(-8.0F, 0.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, 0.5F, 9.2F, 0.0F, -1.1345F, 1.5708F));
		PartDefinition cube_r2 = bone9.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 38).addBox(-8.0F, 0.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -5.0F, 0.6F, 0.0F, 0.8639F, 0.0F));
		PartDefinition cube_r3 = bone9.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(4, 38).addBox(2.0F, 0.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -5.0F, 0.6F, 0.0F, -0.8639F, 0.0F));
		PartDefinition cube_r4 = bone9.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(4, 38).addBox(-8.0F, 0.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, -4.3F, 0.2F, 0.0F, 1.1345F, 1.5708F));
		PartDefinition cube_r5 = bone9.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(4, 38).addBox(-4.0F, -1.0F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2F, -4.5F, 1.6F, 0.0F, 0.733F, 0.0F));
		PartDefinition cube_r6 = bone9.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(4, 38).addBox(-5.0F, -1.0F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -0.8F, 1.325F, 0.955F, 0.6005F, -0.2883F));
		PartDefinition cube_r7 = bone9.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(4, 38).addBox(2.0F, 0.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -5.0F, 0.6F, 0.0F, -0.8639F, 0.0F));
		PartDefinition cube_r8 = bone9.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(4, 38).addBox(-4.0F, 0.0F, -0.5F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(-3.1F, -2.5F, -3.9F, 0.0F, 1.0385F, 0.0F));
		PartDefinition cube_r9 = bone9.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(4, 38).addBox(-4.0F, -1.0F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.4F, -7.8F, 2.025F, -0.8735F, 0.6164F, 0.4374F));
		PartDefinition cube_r10 = bone9.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(4, 38).addBox(-8.0F, 0.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.3F, -5.0F, 0.6F, 0.0F, 0.8639F, 0.0F));
		PartDefinition cube_r11 = bone9.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(4, 38).addBox(-1.0F, -1.0F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.4F, -7.8F, 2.025F, -0.8735F, -0.6164F, -0.4374F));
		PartDefinition cube_r12 = bone9.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(4, 38).addBox(0.0F, -1.0F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, -0.8F, 1.325F, 0.955F, -0.6005F, 0.2883F));
		PartDefinition cube_r13 = bone9.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(4, 38).addBox(1.0F, 0.0F, -0.5F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(3.1F, -2.5F, -3.9F, 0.0F, -1.0385F, 0.0F));
		PartDefinition cube_r14 = bone9.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(4, 38).addBox(0.0F, -1.0F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2F, -4.5F, 1.6F, 0.0F, -0.733F, 0.0F));
		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 15).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(-5.0F, -10.0F, 0.0F));
		PartDefinition cube_r15 = right_arm.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(-4.35F, 3.0F, -1.0F, 0.0F, 0.0F, 0.7854F));
		PartDefinition cube_r16 = right_arm.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(-4.65F, 0.2F, -1.0F, 0.0F, 0.0F, 0.7854F));
		PartDefinition cube_r17 = right_arm.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(-4.45F, 2.2F, -1.0F, 0.0F, 0.0F, 0.7854F));
		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 15).mirror().addBox(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(5.0F, -10.0F, 0.0F));
		PartDefinition cube_r18 = left_arm.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 40).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(4.2F, 2.2F, -1.0F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r19 = left_arm.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 40).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(4.1F, 3.0F, -1.0F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r20 = left_arm.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 40).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(4.4F, 0.2F, -1.0F, 0.0F, 0.0F, -0.7854F));
		PartDefinition RightFoot = body.addOrReplaceChild("RightFoot", CubeListBuilder.create().texOffs(0, 36).addBox(-2.5F, 10.0F, -3.75F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-2.0F, 0.0F, 0.0F));
		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.25F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-2.0F, 0.0F, 0.0F));
		PartDefinition bone15 = right_leg.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5F, -1.5F, -2.25F, 0.2618F, 0.0F, 0.0F));
		PartDefinition cube_r21 = bone15.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(-1.6F, 4.0F, 0.1F, -0.1104F, -0.1886F, -1.0367F));
		PartDefinition cube_r22 = bone15.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 40).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(-1.6F, 6.5F, -0.45F, -0.1104F, -0.1886F, -1.0367F));
		PartDefinition LeftFoot = body.addOrReplaceChild("LeftFoot", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-1.5F, 10.0F, -3.75F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(2.0F, 0.0F, 0.0F));
		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.75F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(2.0F, 0.0F, 0.0F));
		PartDefinition bone16 = left_leg.addOrReplaceChild("bone16", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, -1.5F, -2.25F, 0.2618F, 0.0F, 0.0F));
		PartDefinition cube_r23 = bone16.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 39).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(1.6F, 6.5F, -0.45F, -0.1104F, 0.1886F, 1.0367F));
		PartDefinition cube_r24 = bone16.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 39).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(1.6F, 4.0F, 0.1F, -0.1104F, 0.1886F, 1.0367F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}