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
public class Modelfurtals<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "luohong_tail"), "main");
	public final ModelPart body;
	public final ModelPart torso;
	public final ModelPart Tail;
	public final ModelPart TailPrimary;
	public final ModelPart Base_r1;
	public final ModelPart TailSecondary;
	public final ModelPart Base_r2;
	public final ModelPart TailTertiary;
	public final ModelPart Base_r3;
	public final ModelPart TailQuaternary;
	public final ModelPart Base_r4;

	public Modelfurtals(ModelPart root) {
		this.body = root.getChild("body");
		this.torso = this.body.getChild("torso");
		this.Tail = this.torso.getChild("Tail");
		this.TailPrimary = this.Tail.getChild("TailPrimary");
		this.Base_r1 = this.TailPrimary.getChild("Base_r1");
		this.TailSecondary = this.TailPrimary.getChild("TailSecondary");
		this.Base_r2 = this.TailSecondary.getChild("Base_r2");
		this.TailTertiary = this.TailSecondary.getChild("TailTertiary");
		this.Base_r3 = this.TailTertiary.getChild("Base_r3");
		this.TailQuaternary = this.TailTertiary.getChild("TailQuaternary");
		this.Base_r4 = this.TailQuaternary.getChild("Base_r4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.3F, 1.0F, -1.7453F, 0.0F, 3.1416F));
		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.3892F, -7.8785F, -0.1309F, 0.0F, 0.0F));
		PartDefinition Base_r1 = TailPrimary.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(48, 45).addBox(-2.0F, 1.1364F, -2.6035F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1781F, 0.0F, 0.0F));
		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.25F, -4.5F));
		PartDefinition Base_r2 = TailSecondary.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(24, 16).addBox(-2.5F, -0.45F, -3.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.0F, 0.5F, -0.5F, -1.4835F, 0.0F, 0.0F));
		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, -6.5F));
		PartDefinition Base_r3 = TailTertiary.addOrReplaceChild("Base_r3", CubeListBuilder.create().texOffs(48, 54).addBox(-2.0F, -0.7F, -2.05F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, -1.5272F, 0.0F, 0.0F));
		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(0.0F, 2.5F, -2.0F));
		PartDefinition Base_r4 = TailQuaternary.addOrReplaceChild("Base_r4", CubeListBuilder.create().texOffs(30, 64).addBox(-1.0F, 0.3F, -1.05F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(-0.5F, -3.1F, -1.7F, -1.5272F, 0.0F, 0.0F));
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