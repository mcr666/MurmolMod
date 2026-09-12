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
public class Modelastralarmor_1<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelastralarmor_1"), "main");
	public final ModelPart body;
	public final ModelPart head;
	public final ModelPart RightHorn;
	public final ModelPart LeftHorn;
	public final ModelPart bone7;
	public final ModelPart LeftCrystals;
	public final ModelPart RightCrystals;

	public Modelastralarmor_1(ModelPart root) {
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.RightHorn = this.head.getChild("RightHorn");
		this.LeftHorn = this.head.getChild("LeftHorn");
		this.bone7 = this.head.getChild("bone7");
		this.LeftCrystals = this.head.getChild("LeftCrystals");
		this.RightCrystals = this.head.getChild("RightCrystals");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.25F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -12.0F, 0.0F));
		PartDefinition RightHorn = head.addOrReplaceChild("RightHorn", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 16.0F, 0.0F, 0.0F, 0.0F, -0.0436F));
		PartDefinition Horn_r1 = RightHorn.addOrReplaceChild("Horn_r1", CubeListBuilder.create().texOffs(56, 19).mirror().addBox(-3.0F, -31.3F, 18.8F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.48F, -0.1745F, 0.0F));
		PartDefinition Horn_r2 = RightHorn.addOrReplaceChild("Horn_r2", CubeListBuilder.create().texOffs(56, 9).addBox(-3.0F, -31.3F, 18.8F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(-0.3F, 7.0F, 1.8F, 0.48F, -0.1745F, 0.0F));
		PartDefinition Horn_r3 = RightHorn.addOrReplaceChild("Horn_r3", CubeListBuilder.create().texOffs(50, 54).addBox(-3.0F, -35.2F, 8.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.25F)),
				PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.2182F, -0.1745F, 0.0F));
		PartDefinition Horn_r4 = RightHorn.addOrReplaceChild("Horn_r4", CubeListBuilder.create().texOffs(32, 9).addBox(-3.0F, -30.75F, -19.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
				PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.5672F, -0.1745F, 0.0F));
		PartDefinition LeftHorn = head.addOrReplaceChild("LeftHorn", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0436F));
		PartDefinition Horn_r5 = LeftHorn.addOrReplaceChild("Horn_r5", CubeListBuilder.create().texOffs(56, 24).addBox(1.0F, -31.3F, 18.8F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(0.3F, 7.0F, 1.8F, 0.48F, 0.1745F, 0.0F));
		PartDefinition Horn_r6 = LeftHorn.addOrReplaceChild("Horn_r6", CubeListBuilder.create().texOffs(56, 19).addBox(1.0F, -31.3F, 18.8F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F)),
				PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.48F, 0.1745F, 0.0F));
		PartDefinition Horn_r7 = LeftHorn.addOrReplaceChild("Horn_r7", CubeListBuilder.create().texOffs(56, 14).addBox(1.0F, -35.2F, 8.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.25F)),
				PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.2182F, 0.1745F, 0.0F));
		PartDefinition Horn_r8 = LeftHorn.addOrReplaceChild("Horn_r8", CubeListBuilder.create().texOffs(0, 57).addBox(1.0F, -30.75F, -19.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
				PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.5672F, 0.1745F, 0.0F));
		PartDefinition bone7 = head.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(0.0F, -3.15F, 2.4F));
		PartDefinition cube_r1 = bone7.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)),
				PartPose.offsetAndRotation(0.0F, 2.075F, 0.175F, 0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r2 = bone7.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 1.3F, 1.0F, 0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r3 = bone7.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.2F, 0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r4 = bone7.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -1.7F, 1.2F, 0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r5 = bone7.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -3.4F, 1.2F, 0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r6 = bone7.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)),
				PartPose.offsetAndRotation(0.0F, -6.65F, 1.675F, -0.5236F, 0.0F, 0.0F));
		PartDefinition cube_r7 = bone7.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(62, 61).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -5.0F, 1.2F, 0.6545F, 0.0F, 0.0F));
		PartDefinition LeftCrystals = head.addOrReplaceChild("LeftCrystals", CubeListBuilder.create(), PartPose.offset(3.0F, -2.4F, -0.5F));
		PartDefinition Crystal_r1 = LeftCrystals.addOrReplaceChild("Crystal_r1", CubeListBuilder.create().texOffs(30, 62).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.15F)),
				PartPose.offsetAndRotation(0.1F, 0.0F, -2.2F, 0.3491F, 0.6109F, 0.0F));
		PartDefinition Crystal_r2 = LeftCrystals.addOrReplaceChild("Crystal_r2", CubeListBuilder.create().texOffs(62, 45).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.15F)),
				PartPose.offsetAndRotation(-0.1F, 0.7F, -2.4F, -0.2618F, 0.5236F, 0.0F));
		PartDefinition Crystal_r3 = LeftCrystals.addOrReplaceChild("Crystal_r3", CubeListBuilder.create().texOffs(62, 49).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.15F)),
				PartPose.offsetAndRotation(0.1F, 0.3F, -2.1F, 0.0873F, 0.6981F, 0.0F));
		PartDefinition Crystal_r4 = LeftCrystals.addOrReplaceChild("Crystal_r4", CubeListBuilder.create().texOffs(60, 54).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.1F, 0.3F, -0.6F, 0.0873F, 0.6981F, 0.0F));
		PartDefinition Crystal_r5 = LeftCrystals.addOrReplaceChild("Crystal_r5", CubeListBuilder.create().texOffs(54, 59).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(-0.1F, 0.9F, -1.1F, -0.2618F, 0.5236F, 0.0F));
		PartDefinition Crystal_r6 = LeftCrystals.addOrReplaceChild("Crystal_r6", CubeListBuilder.create().texOffs(46, 59).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.1F, -0.5F, -0.9F, 0.3491F, 0.6109F, 0.0F));
		PartDefinition RightCrystals = head.addOrReplaceChild("RightCrystals", CubeListBuilder.create(), PartPose.offset(-3.0F, -2.4F, -0.5F));
		PartDefinition Crystal_r7 = RightCrystals.addOrReplaceChild("Crystal_r7", CubeListBuilder.create().texOffs(62, 49).mirror().addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(-0.1F, 0.3F, -0.6F, 0.0873F, -0.6981F, 0.0F));
		PartDefinition Crystal_r8 = RightCrystals.addOrReplaceChild("Crystal_r8", CubeListBuilder.create().texOffs(62, 45).mirror().addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(0.1F, 0.9F, -1.1F, -0.2618F, -0.5236F, 0.0F));
		PartDefinition Crystal_r9 = RightCrystals.addOrReplaceChild("Crystal_r9", CubeListBuilder.create().texOffs(30, 62).mirror().addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)).mirror(false),
				PartPose.offsetAndRotation(-0.1F, -0.5F, -0.9F, 0.3491F, -0.6109F, 0.0F));
		PartDefinition Crystal_r10 = RightCrystals.addOrReplaceChild("Crystal_r10", CubeListBuilder.create().texOffs(62, 49).mirror().addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.15F)).mirror(false),
				PartPose.offsetAndRotation(-0.1F, 0.3F, -2.1F, 0.0873F, -0.6981F, 0.0F));
		PartDefinition Crystal_r11 = RightCrystals.addOrReplaceChild("Crystal_r11", CubeListBuilder.create().texOffs(62, 45).mirror().addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.15F)).mirror(false),
				PartPose.offsetAndRotation(0.1F, 0.7F, -2.4F, -0.2618F, -0.5236F, 0.0F));
		PartDefinition Crystal_r12 = RightCrystals.addOrReplaceChild("Crystal_r12", CubeListBuilder.create().texOffs(30, 62).mirror().addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.15F)).mirror(false),
				PartPose.offsetAndRotation(-0.1F, 0.0F, -2.2F, 0.3491F, -0.6109F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}