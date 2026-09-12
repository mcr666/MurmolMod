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
public class Modelastraltail<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelastraltail"), "main");
	public final ModelPart Tail;
	public final ModelPart TailPrimary;
	public final ModelPart bone12;
	public final ModelPart TailSecondary;
	public final ModelPart bone10;
	public final ModelPart bone13;
	public final ModelPart TailTertiary;
	public final ModelPart bone11;

	public Modelastraltail(ModelPart root) {
		this.Tail = root.getChild("Tail");
		this.TailPrimary = this.Tail.getChild("TailPrimary");
		this.bone12 = this.TailPrimary.getChild("bone12");
		this.TailSecondary = this.TailPrimary.getChild("TailSecondary");
		this.bone10 = this.TailSecondary.getChild("bone10");
		this.bone13 = this.bone10.getChild("bone13");
		this.TailTertiary = this.TailSecondary.getChild("TailTertiary");
		this.bone11 = this.TailTertiary.getChild("bone11");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition Tail = partdefinition.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.1309F, 0.0F, 0.0F));
		PartDefinition Base_r1 = TailPrimary.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, 1.1364F, -1.3965F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.2F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, 0.0F));
		PartDefinition bone12 = TailPrimary.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.0F));
		PartDefinition cube_r1 = bone12.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.4F, -1.8F, 6.8F, -1.5708F, -0.8727F, 0.0F));
		PartDefinition cube_r2 = bone12.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 3.1F, 6.8F, -0.576F, 0.0F, 0.0F));
		PartDefinition cube_r3 = bone12.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.4F, -1.8F, 6.8F, -1.5708F, 0.8727F, 0.0F));
		PartDefinition cube_r4 = bone12.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -1.0F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -3.0F, 6.8F, 0.576F, 0.0F, 0.0F));
		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.25F, 4.5F));
		PartDefinition Base_r2 = TailSecondary.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(0, 45).addBox(-2.5F, -0.45F, -2.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.3F)),
				PartPose.offsetAndRotation(0.0F, 0.5F, 0.5F, 1.4835F, 0.0F, 0.0F));
		PartDefinition bone10 = TailSecondary.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, -9.5F));
		PartDefinition bone13 = bone10.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 8.0F));
		PartDefinition cube_r5 = bone13.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, -1.0F, 6.8F, -1.5708F, 0.8727F, 0.0F));
		PartDefinition cube_r6 = bone13.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.5F, -1.8F, 6.8F, -1.5708F, -0.8727F, 0.0F));
		PartDefinition cube_r7 = bone13.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, -1.0F, 6.8F, -1.5708F, -0.8727F, 0.0F));
		PartDefinition cube_r8 = bone13.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 3.1F, 6.8F, -0.576F, 0.0F, 0.0F));
		PartDefinition cube_r9 = bone13.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.5F, -1.8F, 6.8F, -1.5708F, 0.8727F, 0.0F));
		PartDefinition cube_r10 = bone13.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -1.0F, -1.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, -2.9F, 6.8F, 0.576F, 0.0F, 0.0F));
		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, -0.5F));
		PartDefinition Base_r3 = TailTertiary.addOrReplaceChild("Base_r3", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, -0.7F, -1.95F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 8.8F, 1.5272F, 0.0F, 0.0F));
		PartDefinition bone11 = TailTertiary.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 7.0F));
		PartDefinition Base_r4 = bone11.addOrReplaceChild("Base_r4",
				CubeListBuilder.create().texOffs(0, 45).addBox(0.0F, 1.3F, -1.95F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.3F)).texOffs(0, 45).addBox(0.2F, 1.3F, -1.95F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.3F)),
				PartPose.offsetAndRotation(-0.6F, -1.6F, 6.7F, 1.5272F, 0.0F, 0.0F));
		PartDefinition Base_r5 = bone11.addOrReplaceChild("Base_r5", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, 1.3F, -1.95F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)),
				PartPose.offsetAndRotation(1.0F, -1.2F, 4.3F, 1.5272F, 0.0F, 0.0F));
		PartDefinition cube_r11 = bone11.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, -1.0F, 3.8F, -1.5708F, -0.8727F, 0.0F));
		PartDefinition cube_r12 = bone11.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, 3.4F, 3.8F, -0.576F, 0.0F, 0.0F));
		PartDefinition cube_r13 = bone11.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, -1.0F, 3.8F, -1.5708F, 0.8727F, 0.0F));
		PartDefinition cube_r14 = bone11.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -1.0F, -1.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, -2.0F, 3.8F, 0.576F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		Tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}