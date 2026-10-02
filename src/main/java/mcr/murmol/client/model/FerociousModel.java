package mcr.murmol.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 狰形态模型（Ferocious）。层位置与 FerociousForm.getBodyLayer() 一致。
 */
public class FerociousModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "ferocious"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public FerociousModel(ModelPart root) {
		this.body = root.getChild("body");
		this.torso = this.body.getChild("torso");
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
		.texOffs(88, 13).addBox(-4.5F, -0.2F, -3.2F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition hair = torso.addOrReplaceChild("hair", CubeListBuilder.create(), PartPose.offset(0.0F, 5.8F, 3.0F));

		hair.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(56, 43).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -4.3935F, 0.2114F, 0.7854F, 0.0F, 0.0F));

		hair.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(56, 41).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -5.1935F, 0.2114F, 0.7854F, 0.0F, 0.0F));

		hair.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(56, 41).addBox(-6.0F, 0.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -6.2071F, -1.9071F, 0.7854F, 0.0F, 0.0F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.0F, -0.75F, -1.309F, 0.0F, 0.0F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		TailPrimary.addOrReplaceChild("TailBase_r1", CubeListBuilder.create().texOffs(84, 96).addBox(-2.5F, -8.25F, 11.6F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 15.75F, 0.0F, 1.0036F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 0.6F));

		TailSecondary.addOrReplaceChild("TailBase_r2", CubeListBuilder.create().texOffs(108, 95).addBox(-2.0F, -1.75F, 12.6F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 15.35F, -0.8F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 6.5F));

		TailTertiary.addOrReplaceChild("TailBase_r3", CubeListBuilder.create().texOffs(97, 117).addBox(-2.0F, 9.0F, 10.3F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.15F, -7.5F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.5F));

		TailQuaternary.addOrReplaceChild("TailBase_r4", CubeListBuilder.create().texOffs(116, 119).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 10.35F, -8.4F, 1.7017F, 0.0F, 0.0F));

		TailQuaternary.addOrReplaceChild("TailBase_r5", CubeListBuilder.create().texOffs(70, 118).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(0.0F, 11.05F, -13.9F, 1.7017F, 0.0F, 0.0F));

		PartDefinition Hair2 = TailQuaternary.addOrReplaceChild("Hair2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		Hair2.addOrReplaceChild("TailBase_r6", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.1F, 0.2F, -8.5F, 0.4363F, 0.0F, -1.5708F));

		Hair2.addOrReplaceChild("TailBase_r7", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.9F, -2.8F, -8.5F, 0.4363F, 0.0F, 1.5708F));

		Hair2.addOrReplaceChild("TailBase_r8", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -17.8F, -8.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition RightWing = torso.addOrReplaceChild("RightWing", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 8.5F, 4.0F, -0.2546F, 0.4114F, -0.577F));

		PartDefinition rightWingRoot = RightWing.addOrReplaceChild("rightWingRoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		rightWingRoot.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(58, 79).addBox(-25.975F, -4.475F, 1.66F, 7.0F, 5.0F, 0.05F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, 0.0F, 0.0F, 1.2654F));

		rightWingRoot.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 97).addBox(-30.075F, -12.7F, 1.2F, 11.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, 0.0F, 0.0F, 0.7854F));

		rightWingRoot.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(62, 95).addBox(-12.775F, -19.75F, 1.2F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition rightSecondaries2 = rightWingRoot.addOrReplaceChild("rightSecondaries2", CubeListBuilder.create(), PartPose.offsetAndRotation(-7.3F, -7.0F, -0.5F, 0.0F, 0.0F, 0.5236F));

		rightSecondaries2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(58, 84).addBox(1.025F, -30.55F, 1.2F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3F, 27.0F, -1.5F, 0.0F, 0.0F, -0.48F));

		rightSecondaries2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 100).addBox(-5.5F, -2.5F, 0.0F, 10.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0551F, 6.3129F, 0.151F, 0.0F, 0.0F, 1.5272F));

		rightSecondaries2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 114).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-3.6495F, 5.6238F, 0.2F, 0.0F, 0.0F, 0.0262F));

		rightSecondaries2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 107).addBox(-24.525F, -13.85F, 1.648F, 9.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3F, 27.0F, -1.5F, 0.0F, 0.0F, 0.7418F));

		PartDefinition rightTertiaries2 = rightSecondaries2.addOrReplaceChild("rightTertiaries2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9599F));

		rightTertiaries2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(24, 114).addBox(2.3F, -28.5F, 1.2F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9F, 28.4F, -1.5F, 0.0F, 0.0F, -0.48F));

		rightTertiaries2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(20, 100).addBox(-26.125F, -10.525F, 1.64F, 10.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 28.8F, -1.5F, 0.0F, 0.0F, 0.8727F));

		PartDefinition LeftWing = torso.addOrReplaceChild("LeftWing", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 8.5F, 4.0F, -0.2546F, -0.4114F, 0.577F));

		PartDefinition leftWingRoot = LeftWing.addOrReplaceChild("leftWingRoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		leftWingRoot.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(58, 79).mirror().addBox(18.975F, -4.475F, 1.66F, 7.0F, 5.0F, 0.05F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, 0.0F, 0.0F, -1.2654F));

		leftWingRoot.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(24, 97).mirror().addBox(19.075F, -12.7F, 1.2F, 11.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, 0.0F, 0.0F, -0.7854F));

		leftWingRoot.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(62, 95).mirror().addBox(7.775F, -19.75F, 1.2F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition leftSecondaries2 = leftWingRoot.addOrReplaceChild("leftSecondaries2", CubeListBuilder.create(), PartPose.offsetAndRotation(7.3F, -7.0F, -0.5F, 0.0F, 0.0F, -0.5236F));

		leftSecondaries2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(58, 84).mirror().addBox(-2.025F, -30.55F, 1.2F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.3F, 27.0F, -1.5F, 0.0F, 0.0F, 0.48F));

		leftSecondaries2.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 100).mirror().addBox(-4.5F, -2.5F, 0.0F, 10.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.0551F, 6.3129F, 0.151F, 0.0F, 0.0F, -1.5272F));

		leftSecondaries2.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(28, 114).mirror().addBox(-0.5F, -6.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(3.6495F, 5.6238F, 0.2F, 0.0F, 0.0F, -0.0262F));

		leftSecondaries2.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 107).mirror().addBox(15.525F, -13.85F, 1.648F, 9.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.3F, 27.0F, -1.5F, 0.0F, 0.0F, -0.7418F));

		PartDefinition leftTertiaries2 = leftSecondaries2.addOrReplaceChild("leftTertiaries2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9599F));

		leftTertiaries2.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(24, 114).mirror().addBox(-3.3F, -28.5F, 1.2F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.9F, 28.4F, -1.5F, 0.0F, 0.0F, 0.48F));

		leftTertiaries2.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(20, 100).mirror().addBox(16.125F, -10.525F, 1.64F, 10.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.0F, 28.8F, -1.5F, 0.0F, 0.0F, -0.8727F));

		PartDefinition Tail_e1 = torso.addOrReplaceChild("Tail_e1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.0F, -0.75F, -1.3281F, 0.0992F, -0.3806F));

		PartDefinition TailPrimary2 = Tail_e1.addOrReplaceChild("TailPrimary2", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		TailPrimary2.addOrReplaceChild("TailBase_r9", CubeListBuilder.create().texOffs(84, 96).addBox(-2.5F, -8.25F, 11.6F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 15.75F, 0.0F, 1.0036F, 0.0F, 0.0F));

		PartDefinition TailSecondary2 = TailPrimary2.addOrReplaceChild("TailSecondary2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 0.6F));

		TailSecondary2.addOrReplaceChild("TailBase_r10", CubeListBuilder.create().texOffs(108, 95).addBox(-2.0F, -1.75F, 12.6F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 15.35F, -0.8F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailTertiary2 = TailSecondary2.addOrReplaceChild("TailTertiary2", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 6.5F));

		TailTertiary2.addOrReplaceChild("TailBase_r11", CubeListBuilder.create().texOffs(97, 117).addBox(-2.0F, 9.0F, 10.3F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.15F, -7.5F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailQuaternary2 = TailTertiary2.addOrReplaceChild("TailQuaternary2", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.5F));

		TailQuaternary2.addOrReplaceChild("TailBase_r12", CubeListBuilder.create().texOffs(116, 119).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 10.35F, -8.4F, 1.7017F, 0.0F, 0.0F));

		TailQuaternary2.addOrReplaceChild("TailBase_r13", CubeListBuilder.create().texOffs(70, 118).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(0.0F, 11.05F, -13.9F, 1.7017F, 0.0F, 0.0F));

		PartDefinition Hair3 = TailQuaternary2.addOrReplaceChild("Hair3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		Hair3.addOrReplaceChild("TailBase_r14", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.1F, 0.2F, -8.5F, 0.4363F, 0.0F, -1.5708F));

		Hair3.addOrReplaceChild("TailBase_r15", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.9F, -2.8F, -8.5F, 0.4363F, 0.0F, 1.5708F));

		Hair3.addOrReplaceChild("TailBase_r16", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -17.8F, -8.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition Tail_e5 = torso.addOrReplaceChild("Tail_e5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.0F, -0.75F, -1.3281F, -0.0992F, 0.3806F));

		PartDefinition TailPrimary5 = Tail_e5.addOrReplaceChild("TailPrimary5", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		TailPrimary5.addOrReplaceChild("TailBase_r17", CubeListBuilder.create().texOffs(84, 96).mirror().addBox(-2.5F, -8.25F, 11.6F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 15.75F, 0.0F, 1.0036F, 0.0F, 0.0F));

		PartDefinition TailSecondary5 = TailPrimary5.addOrReplaceChild("TailSecondary5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 0.6F));

		TailSecondary5.addOrReplaceChild("TailBase_r18", CubeListBuilder.create().texOffs(108, 95).mirror().addBox(-2.0F, -1.75F, 12.6F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 15.35F, -0.8F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailTertiary5 = TailSecondary5.addOrReplaceChild("TailTertiary5", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 6.5F));

		TailTertiary5.addOrReplaceChild("TailBase_r19", CubeListBuilder.create().texOffs(97, 117).mirror().addBox(-2.0F, 9.0F, 10.3F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 12.15F, -7.5F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailQuaternary5 = TailTertiary5.addOrReplaceChild("TailQuaternary5", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.5F));

		TailQuaternary5.addOrReplaceChild("TailBase_r20", CubeListBuilder.create().texOffs(116, 119).mirror().addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.35F, -8.4F, 1.7017F, 0.0F, 0.0F));

		TailQuaternary5.addOrReplaceChild("TailBase_r21", CubeListBuilder.create().texOffs(70, 118).mirror().addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.15F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.05F, -13.9F, 1.7017F, 0.0F, 0.0F));

		PartDefinition Hair6 = TailQuaternary5.addOrReplaceChild("Hair6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		Hair6.addOrReplaceChild("TailBase_r22", CubeListBuilder.create().texOffs(112, 105).mirror().addBox(-1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(17.1F, 0.2F, -8.5F, 0.4363F, 0.0F, 1.5708F));

		Hair6.addOrReplaceChild("TailBase_r23", CubeListBuilder.create().texOffs(112, 105).mirror().addBox(-1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.9F, -2.8F, -8.5F, 0.4363F, 0.0F, -1.5708F));

		Hair6.addOrReplaceChild("TailBase_r24", CubeListBuilder.create().texOffs(112, 105).mirror().addBox(-1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.6F, -17.8F, -8.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition Tail_e6 = torso.addOrReplaceChild("Tail_e6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.0F, -0.75F, -0.9288F, -0.005F, -0.2404F));

		PartDefinition TailPrimary6 = Tail_e6.addOrReplaceChild("TailPrimary6", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		TailPrimary6.addOrReplaceChild("TailBase_r25", CubeListBuilder.create().texOffs(84, 96).addBox(-2.5F, -8.25F, 11.6F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 15.75F, 0.0F, 1.0036F, 0.0F, 0.0F));

		PartDefinition TailSecondary6 = TailPrimary6.addOrReplaceChild("TailSecondary6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 0.6F));

		TailSecondary6.addOrReplaceChild("TailBase_r26", CubeListBuilder.create().texOffs(108, 95).addBox(-2.0F, -1.75F, 12.6F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 15.35F, -0.8F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailTertiary6 = TailSecondary6.addOrReplaceChild("TailTertiary6", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 6.5F));

		TailTertiary6.addOrReplaceChild("TailBase_r27", CubeListBuilder.create().texOffs(97, 117).addBox(-2.0F, 9.0F, 10.3F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.15F, -7.5F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailQuaternary6 = TailTertiary6.addOrReplaceChild("TailQuaternary6", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.5F));

		TailQuaternary6.addOrReplaceChild("TailBase_r28", CubeListBuilder.create().texOffs(116, 119).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 10.35F, -8.4F, 1.7017F, 0.0F, 0.0F));

		TailQuaternary6.addOrReplaceChild("TailBase_r29", CubeListBuilder.create().texOffs(70, 118).addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(0.0F, 11.05F, -13.9F, 1.7017F, 0.0F, 0.0F));

		PartDefinition Hair7 = TailQuaternary6.addOrReplaceChild("Hair7", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		Hair7.addOrReplaceChild("TailBase_r30", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.1F, 0.2F, -8.5F, 0.4363F, 0.0F, -1.5708F));

		Hair7.addOrReplaceChild("TailBase_r31", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.9F, -2.8F, -8.5F, 0.4363F, 0.0F, 1.5708F));

		Hair7.addOrReplaceChild("TailBase_r32", CubeListBuilder.create().texOffs(112, 105).addBox(1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -17.8F, -8.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition Tail_e9 = torso.addOrReplaceChild("Tail_e9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.0F, -0.75F, -0.9288F, 0.005F, 0.2404F));

		PartDefinition TailPrimary8 = Tail_e9.addOrReplaceChild("TailPrimary8", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		TailPrimary8.addOrReplaceChild("TailBase_r33", CubeListBuilder.create().texOffs(84, 96).mirror().addBox(-2.5F, -8.25F, 11.6F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 15.75F, 0.0F, 1.0036F, 0.0F, 0.0F));

		PartDefinition TailSecondary8 = TailPrimary8.addOrReplaceChild("TailSecondary8", CubeListBuilder.create(), PartPose.offset(0.0F, 0.4F, 0.6F));

		TailSecondary8.addOrReplaceChild("TailBase_r34", CubeListBuilder.create().texOffs(108, 95).mirror().addBox(-2.0F, -1.75F, 12.6F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 15.35F, -0.8F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailTertiary8 = TailSecondary8.addOrReplaceChild("TailTertiary8", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 6.5F));

		TailTertiary8.addOrReplaceChild("TailBase_r35", CubeListBuilder.create().texOffs(97, 117).mirror().addBox(-2.0F, 9.0F, 10.3F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 12.15F, -7.5F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailQuaternary8 = TailTertiary8.addOrReplaceChild("TailQuaternary8", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 5.5F));

		TailQuaternary8.addOrReplaceChild("TailBase_r36", CubeListBuilder.create().texOffs(116, 119).mirror().addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.35F, -8.4F, 1.7017F, 0.0F, 0.0F));

		TailQuaternary8.addOrReplaceChild("TailBase_r37", CubeListBuilder.create().texOffs(70, 118).mirror().addBox(-1.5F, 17.3F, 7.1F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.15F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.05F, -13.9F, 1.7017F, 0.0F, 0.0F));

		PartDefinition Hair9 = TailQuaternary8.addOrReplaceChild("Hair9", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		Hair9.addOrReplaceChild("TailBase_r38", CubeListBuilder.create().texOffs(112, 105).mirror().addBox(-1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(17.1F, 0.2F, -8.5F, 0.4363F, 0.0F, 1.5708F));

		Hair9.addOrReplaceChild("TailBase_r39", CubeListBuilder.create().texOffs(112, 105).mirror().addBox(-1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.9F, -2.8F, -8.5F, 0.4363F, 0.0F, -1.5708F));

		Hair9.addOrReplaceChild("TailBase_r40", CubeListBuilder.create().texOffs(112, 105).mirror().addBox(-1.5F, 20.3F, 7.1F, 0.1F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.6F, -17.8F, -8.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.05F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(44, 27).addBox(-1.5F, -25.5F, 4.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 8).addBox(-2.0F, -27.5F, 4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.5F, -0.05F, 0.0F, 3.1416F, 0.0F));

		head.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(93, 60).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -2.25F, -1.45F, 0.1309F, 0.1745F, 0.3927F));

		head.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(93, 60).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.25F, -1.45F, 0.1309F, -0.1745F, -0.3927F));

		PartDefinition ear = head.addOrReplaceChild("ear", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0506F, 2.6F, -1.5465F, 3.1416F, -0.7854F, -3.1416F));

		PartDefinition bone8 = ear.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(-4.85F, 0.1F, -4.75F));

		bone8.addOrReplaceChild("cube_r4_r1", CubeListBuilder.create().texOffs(18, 65).addBox(0.4945F, -0.9911F, -2.4089F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(46, 62).addBox(-1.222F, -0.6544F, -2.5339F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(46, 71).addBox(-1.2569F, -1.6644F, -1.5177F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(18, 73).addBox(-1.264F, -2.6695F, -0.4988F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2532F, -11.6074F, 0.9308F, 0.4161F, 0.4029F, 0.1116F));

		PartDefinition ear2 = head.addOrReplaceChild("ear2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0506F, 2.6F, -1.5465F, 3.1416F, 0.7854F, 3.1416F));

		PartDefinition bone2 = ear2.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(4.85F, 0.1F, -4.75F));

		bone2.addOrReplaceChild("cube_r4_r2", CubeListBuilder.create().texOffs(18, 65).mirror().addBox(-1.4945F, -0.9911F, -2.4089F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(46, 62).mirror().addBox(-0.778F, -0.6544F, -2.5339F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(46, 71).mirror().addBox(-0.7431F, -1.6644F, -1.5177F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(18, 73).mirror().addBox(0.264F, -2.6695F, -0.4988F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.2532F, -11.6074F, 0.9308F, 0.4161F, -0.4029F, -0.1116F));

		PartDefinition jiao = head.addOrReplaceChild("jiao", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -10.617F, -5.308F, 1.0036F, 0.0F, 0.0F));

		jiao.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(45, 126).addBox(1.575F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.8552F, 0.0F, 0.0F));

		jiao.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(43, 114).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.05F, 2.817F, -0.492F, 1.0472F, 0.0F, 0.0F));

		jiao.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(56, 124).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.05F, 2.817F, -0.392F, 1.1345F, 0.0F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(49, 1).addBox(-3.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(49, 1).addBox(-3.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-5.0F, -10.0F, 0.0F));

		right_arm.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(97, 65).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.65F, -1.6698F, 1.0716F, -0.1309F, 0.0F, -2.8362F));

		right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(102, -11).addBox(-1.0F, -2.0F, -9.5F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.75F, -2.0F));

		right_arm.addOrReplaceChild("paw2", CubeListBuilder.create().texOffs(72, 65).addBox(4.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 73).addBox(1.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(74, 0).addBox(2.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(74, 2).addBox(3.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 62).addBox(2.0F, -0.58F, -2.28F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 9.6602F, -1.1484F, 0.0F, 3.1416F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 29).addBox(-1.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(49, 8).addBox(-1.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(49, 8).addBox(-1.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		left_arm.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(97, 65).mirror().addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.65F, -1.6698F, 1.0716F, -0.1309F, 0.0F, 2.8362F));

		left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(98, -11).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 7.75F, -2.0F));

		left_arm.addOrReplaceChild("paw1", CubeListBuilder.create().texOffs(72, 41).addBox(-4.0F, -0.58F, -2.28F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(38, 72).addBox(-3.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(10, 72).addBox(-4.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(38, 70).addBox(-5.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(66, 12).addBox(-2.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.0F, 9.6602F, -1.1484F, 0.0F, 3.1416F, 0.0F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(18, 39).mirror().addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 4.1257F, -0.9576F, -0.3491F, 0.0F, 0.3054F));

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

		left_leg.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(18, 39).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 4.1257F, -0.9576F, -0.3491F, 0.0F, -0.3054F));

		left_leg.addOrReplaceChild("LeftThigh_r1", CubeListBuilder.create().texOffs(16, 45).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.3743F, -0.1076F, 2.9234F, 3.1416F, -3.1416F));

		PartDefinition LeftLowerLeg = left_leg.addOrReplaceChild("LeftLowerLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 5.0007F, -3.6576F, 0.0F, 3.1416F, 0.0F));

		LeftLowerLeg.addOrReplaceChild("LeftCalf_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-2.01F, -0.125F, -1.1F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.125F, -1.95F, -0.8727F, 0.0F, 0.0F));

		PartDefinition LeftFoot = LeftLowerLeg.addOrReplaceChild("LeftFoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, -7.175F));

		LeftFoot.addOrReplaceChild("LeftArch_r1", CubeListBuilder.create().texOffs(16, 56).mirror().addBox(-2.0F, -8.45F, -2.275F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)).mirror(false), PartPose.offsetAndRotation(0.0F, 7.075F, 4.975F, 0.3491F, 0.0F, 0.0F));

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
