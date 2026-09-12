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
public class Modelleaftail<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelleaftail"), "main");
	public final ModelPart body;
	public final ModelPart torso;
	public final ModelPart Tail;
	public final ModelPart Tail3;
	public final ModelPart Tail4;
	public final ModelPart Tail5;

	public Modelleaftail(ModelPart root) {
		this.body = root.getChild("body");
		this.torso = this.body.getChild("torso");
		this.Tail = this.torso.getChild("Tail");
		this.Tail3 = this.Tail.getChild("Tail3");
		this.Tail4 = this.Tail3.getChild("Tail4");
		this.Tail5 = this.Tail4.getChild("Tail5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(56, 20).addBox(-2.5F, 0.1954F, -2.2669F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 16.442F, 0.4806F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Tail3 = Tail.addOrReplaceChild("Tail3", CubeListBuilder.create().texOffs(104, 0).addBox(-2.0F, -2.6827F, -2.2307F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 8.9454F, 0.4831F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Tail4 = Tail3.addOrReplaceChild("Tail4", CubeListBuilder.create().texOffs(101, 13).addBox(-1.5F, -2.3162F, -1.604F, 3.0F, 7.5F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 6.3173F, 0.0193F, 0.2618F, 0.0F, 0.0F));
		PartDefinition Tail5 = Tail4.addOrReplaceChild("Tail5",
				CubeListBuilder.create().texOffs(27, 19).addBox(-1.0F, -0.4817F, -1.662F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.1F)).texOffs(15, 15).addBox(-0.25F, 7.0183F, -1.362F, 0.5F, 2.0F, 0.5F, new CubeDeformation(0.0F)).texOffs(12, 43)
						.addBox(-0.5F, 7.0183F, -1.362F, 1.0F, 2.0F, 0.5F, new CubeDeformation(0.0F)).texOffs(12, 40).addBox(-0.75F, 7.0183F, -1.362F, 1.5F, 2.0F, 0.5F, new CubeDeformation(0.0F)).texOffs(0, 45)
						.addBox(-1.0F, 7.0183F, -1.362F, 2.0F, 2.0F, 0.5F, new CubeDeformation(0.0F)).texOffs(0, 40).addBox(0.0F, 11.5183F, -2.162F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 32)
						.addBox(1.0F, 6.5183F, -2.162F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 42).addBox(-2.0F, 11.5183F, -2.162F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, 32)
						.addBox(-2.0F, 6.5183F, -2.162F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 50).addBox(-1.0F, 6.5183F, -2.162F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(113, 13)
						.addBox(-2.0F, 6.5183F, -1.162F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(0.0F, 6.5183F, -0.662F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 4.1838F, 0.146F, 0.4363F, 0.0F, 0.0F));
		PartDefinition Petal1_r1 = Tail5.addOrReplaceChild("Petal1_r1", CubeListBuilder.create().texOffs(16, 100).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, -0.3491F, 0.0F, 0.0F));
		PartDefinition Petal2_r1 = Tail5.addOrReplaceChild("Petal2_r1", CubeListBuilder.create().texOffs(16, 100).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, 1.5708F, 0.3491F));
		PartDefinition Petal3_r1 = Tail5.addOrReplaceChild("Petal3_r1", CubeListBuilder.create().texOffs(16, 100).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 2.7925F, 0.0F, 3.1416F));
		PartDefinition Petal4_r1 = Tail5.addOrReplaceChild("Petal4_r1", CubeListBuilder.create().texOffs(16, 100).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, -1.5708F, -0.3491F));
		PartDefinition Leaf1_r1 = Tail5.addOrReplaceChild("Leaf1_r1", CubeListBuilder.create().texOffs(16, 100).addBox(-4.3299F, 1.8711F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, 0.0F, -0.1745F));
		PartDefinition Leaf2_r1 = Tail5.addOrReplaceChild("Leaf2_r1", CubeListBuilder.create().texOffs(16, 100).addBox(-3.7143F, 1.8711F, -3.835F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, 0.5236F, 0.1745F));
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