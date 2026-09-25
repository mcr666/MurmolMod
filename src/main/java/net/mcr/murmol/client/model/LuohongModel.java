package net.mcr.murmol.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 落瓣春花狐龙模型。
 * 层位置与 LuohongForm.getBodyLayer() 一致。
 */
public class LuohongModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "luohong"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart Tail;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public LuohongModel(ModelPart root) {
		this.body = root.getChild("body");
		this.torso = this.body.getChild("torso");
		this.Tail = this.torso.getChild("Tail");
		this.head = this.body.getChild("head");
		this.right_arm = this.body.getChild("right_arm");
		this.left_arm = this.body.getChild("left_arm");
		this.right_leg = this.body.getChild("right_leg");
		this.left_leg = this.body.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(87, 36).addBox(-4.0F, 4.8F, -2.6F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(56, 96).addBox(-4.5F, -0.2F, -3.2F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition hair = torso.addOrReplaceChild("hair", CubeListBuilder.create(), PartPose.offset(0.0F, 5.8F, 3.0F));

		hair.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(56, 43).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -4.3935F, 0.2114F, 0.7854F, 0.0F, 0.0F));

		hair.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(56, 41).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -5.1935F, 0.2114F, 0.7854F, 0.0F, 0.0F));

		hair.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(56, 41).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -6.2071F, -1.9071F, 0.7854F, 0.0F, 0.0F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.3F, 1.0F, -1.7453F, 0.0F, 3.1416F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.6392F, -8.8785F, -0.1309F, 0.0F, 0.0F));

		TailPrimary.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(48, 45).addBox(-2.0F, 1.1364F, -2.6035F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.314F, 1.027F, -1.1781F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.564F, -3.473F));

		TailSecondary.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(24, 16).addBox(-2.5F, -0.45F, -3.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.5F, -0.5F, -1.4835F, 0.0F, 0.0F));

		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, -6.5F));

		TailTertiary.addOrReplaceChild("Base_r3", CubeListBuilder.create().texOffs(48, 54).addBox(-2.0F, -0.7F, -2.05F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, -1.5272F, 0.0F, 0.0F));

		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(-0.5F, -0.35F, -4.2F));

		TailQuaternary.addOrReplaceChild("Base_r4", CubeListBuilder.create().texOffs(30, 64).addBox(-1.0F, 0.3F, -1.05F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, -0.25F, 0.5F, -1.5272F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.05F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(44, 27).addBox(-1.5F, -25.5F, 4.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 8).addBox(-2.0F, -27.5F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.5F, -0.05F, 0.0F, 3.1416F, 0.0F));

		head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(29, 94).mirror().addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(29, 94).addBox(-5.5F, -1.0F, -1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, -7.5F, -2.45F, 0.5672F, 0.0F, 0.0F));

		head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(29, 93).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -2.25F, -1.45F, 0.1309F, 0.1745F, 0.3927F));

		head.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(29, 93).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.25F, -1.45F, 0.1309F, -0.1745F, -0.3927F));

		PartDefinition ear = head.addOrReplaceChild("ear", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0506F, 1.6F, -1.5465F, 3.1416F, -0.7854F, -3.1416F));

		PartDefinition bone9 = ear.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offsetAndRotation(4.85F, -0.1F, 4.75F, 0.0F, -1.5708F, 0.0F));

		bone9.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(62, 70).addBox(-2.6482F, -8.547F, 2.3269F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.925F, -3.65F, 0.75F, 1.0472F, 0.4962F, 0.04F));

		bone9.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(72, 70).addBox(-2.8982F, -8.547F, 2.2769F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7F, -4.975F, 0.475F, 1.0472F, 0.4962F, 0.04F));

		bone9.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(60, 14).addBox(-2.6482F, -12.547F, 1.3269F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.1F, 4.4F, 1.0472F, 0.4962F, 0.04F));

		bone9.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(66, 0).addBox(-2.6482F, -12.547F, 2.3269F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.55F, 4.1F, 1.0472F, 0.4962F, 0.04F));

		PartDefinition bone8 = ear.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(-4.85F, 0.1F, -4.75F));

		bone8.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(18, 73).addBox(0.8982F, -8.747F, 2.2269F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, -5.025F, 0.325F, 1.0472F, -0.4962F, -0.04F));

		bone8.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(46, 71).addBox(0.6482F, -8.547F, 2.3269F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.975F, -3.7F, 0.6F, 1.0472F, -0.4962F, -0.04F));

		bone8.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 62).addBox(0.6482F, -12.547F, 1.3269F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.1F, 4.4F, 1.0472F, -0.4962F, -0.04F));

		bone8.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(18, 65).addBox(1.6482F, -12.547F, 2.3269F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -0.55F, 4.1F, 1.0472F, -0.4962F, -0.04F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(49, 1).addBox(-3.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(49, 1).addBox(-3.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-5.0F, -10.0F, 0.0F));

		right_arm.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(33, 98).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.65F, -1.6698F, 1.0716F, -0.1309F, 0.0F, -2.8362F));

		right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(102, -11).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.75F, -2.0F));

		right_arm.addOrReplaceChild("paw2", CubeListBuilder.create().texOffs(72, 65).addBox(4.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 73).addBox(1.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(74, 0).addBox(2.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(74, 2).addBox(3.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 62).addBox(2.0F, -0.58F, -2.28F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 9.6602F, -1.1484F, 0.0F, 3.1416F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 29).addBox(-1.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(49, 8).addBox(-1.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(49, 8).addBox(-1.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		left_arm.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(33, 98).mirror().addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.65F, -1.6698F, 1.0716F, -0.1309F, 0.0F, 2.8362F));

		left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(98, -11).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 7.75F, -2.0F));

		left_arm.addOrReplaceChild("paw1", CubeListBuilder.create().texOffs(72, 41).addBox(-4.0F, -0.58F, -2.28F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(38, 72).addBox(-3.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(10, 72).addBox(-4.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(38, 70).addBox(-5.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(66, 12).addBox(-2.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.0F, 9.6602F, -1.1484F, 0.0F, 3.1416F, 0.0F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(18, 39).mirror().addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 4.1257F, -0.9576F, -0.3491F, 0.0F, 0.3054F));

		right_leg.addOrReplaceChild("RightThigh_r1", CubeListBuilder.create().texOffs(44, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.3743F, -0.1076F, 2.9234F, 3.1416F, -3.1416F));

		PartDefinition RightLowerLeg = right_leg.addOrReplaceChild("RightLowerLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5F, 5.0007F, -3.6576F, 0.0F, 3.1416F, 0.0F));

		RightLowerLeg.addOrReplaceChild("RightCalf_r1", CubeListBuilder.create().texOffs(32, 45).addBox(-1.99F, -0.125F, -1.1F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.125F, -1.95F, -0.8727F, 0.0F, 0.0F));

		PartDefinition RightFoot = RightLowerLeg.addOrReplaceChild("RightFoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, -7.175F));

		RightFoot.addOrReplaceChild("RightArch_r1", CubeListBuilder.create().texOffs(32, 55).addBox(-2.0F, -8.45F, -2.275F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 7.075F, 4.975F, 0.3491F, 0.0F, 0.0F));

		PartDefinition RightPad = RightFoot.addOrReplaceChild("RightPad", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.325F, 4.425F));

		RightPad.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(74, 39).addBox(-10.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(74, 44).addBox(-13.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(52, 27).addBox(-12.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(56, 14).addBox(-11.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 30).addBox(-12.0F, 9.1F, -1.9F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(11.0F, -8.0F, 0.55F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(18, 39).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 4.1257F, -0.9576F, -0.3491F, 0.0F, -0.3054F));

		left_leg.addOrReplaceChild("LeftThigh_r1", CubeListBuilder.create().texOffs(16, 45).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.3743F, -0.1076F, 2.9234F, 3.1416F, -3.1416F));

		PartDefinition LeftLowerLeg = left_leg.addOrReplaceChild("LeftLowerLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 5.0007F, -3.6576F, 0.0F, 3.1416F, 0.0F));

		LeftLowerLeg.addOrReplaceChild("LeftCalf_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-2.01F, -0.125F, -1.1F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.125F, -1.95F, -0.8727F, 0.0F, 0.0F));

		PartDefinition LeftFoot = LeftLowerLeg.addOrReplaceChild("LeftFoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, -7.175F));

		LeftFoot.addOrReplaceChild("LeftArch_r1", CubeListBuilder.create().texOffs(16, 56).addBox(-2.0F, -8.45F, -2.275F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 7.075F, 4.975F, 0.3491F, 0.0F, 0.0F));

		PartDefinition LeftPad = LeftFoot.addOrReplaceChild("LeftPad", CubeListBuilder.create().texOffs(48, 7).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.325F, 4.425F));

		LeftPad.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(74, 37).addBox(12.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(38, 74).addBox(9.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(48, 14).addBox(10.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(52, 14).addBox(11.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 20).addBox(10.0F, 9.1F, -1.9F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-11.0F, -8.0F, 0.55F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}
