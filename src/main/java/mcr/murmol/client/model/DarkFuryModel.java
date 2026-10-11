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
 * 黯忿形态模型（Dark Fury）。层位置与 DarkFuryForm.getBodyLayer() 一致。
 */
public class DarkFuryModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "dark_fury"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public DarkFuryModel(ModelPart root) {
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

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 78).addBox(-4.0F, 4.8F, -2.6F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(28, 82).addBox(-4.5F, -0.2F, -3.2F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition horn_r1 = torso.addOrReplaceChild("horn_r1", CubeListBuilder.create().texOffs(60, 62).addBox(-0.99F, -2.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 5.25F, 2.5F, -2.6616F, 0.0F, 0.0F));

		PartDefinition horn_r2 = torso.addOrReplaceChild("horn_r2", CubeListBuilder.create().texOffs(60, 62).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 14.25F, 2.5F, -2.6616F, 0.0F, 0.0F));

		PartDefinition horn_r3 = torso.addOrReplaceChild("horn_r3", CubeListBuilder.create().texOffs(60, 62).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 11.25F, 2.5F, -2.6616F, 0.0F, 0.0F));

		PartDefinition horn_r4 = torso.addOrReplaceChild("horn_r4", CubeListBuilder.create().texOffs(60, 62).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 8.25F, 2.5F, -2.6616F, 0.0F, 0.0F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.3544F, 0.9216F, -1.0472F, 0.0F, 0.0F));

		PartDefinition TailFin_r1 = Tail.addOrReplaceChild("TailFin_r1", CubeListBuilder.create().texOffs(88, 17).addBox(-4.0F, 0.0F, 0.25F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 1.875F, 0.4302F, 1.5708F, 0.0F, 0.0F));

		PartDefinition TailFin_r2 = Tail.addOrReplaceChild("TailFin_r2", CubeListBuilder.create().texOffs(92, 38).addBox(-4.0F, 0.0F, 0.25F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 0.9637F, 0.9395F, 1.5708F, 0.0F, 0.0F));

		PartDefinition TailFin_r3 = Tail.addOrReplaceChild("TailFin_r3", CubeListBuilder.create().texOffs(92, 42).addBox(-4.0F, 4.0F, -0.75F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 1.7447F, 0.7663F, 1.5708F, 0.0F, 0.0F));

		PartDefinition Base_r1 = Tail.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(77, 29).addBox(-2.0F, 0.75F, -0.8F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5796F, -1.0239F, 0.9599F, 0.0F, 0.0F));

		PartDefinition TailFin_r4 = Tail.addOrReplaceChild("TailFin_r4", CubeListBuilder.create().texOffs(92, 17).addBox(-4.0F, 0.0F, 0.25F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -0.0126F, 1.1559F, 0.8892F, -0.4933F, -0.1676F));

		PartDefinition TailFin_r5 = Tail.addOrReplaceChild("TailFin_r5", CubeListBuilder.create().texOffs(92, 17).addBox(-4.0F, 0.0F, 0.25F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7F, 1.269F, -2.3035F, 0.9326F, 0.4818F, 0.1558F));

		PartDefinition Base_r2 = Tail.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(80, 41).addBox(-2.0F, -1.075F, 0.375F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.858F, 0.8093F, 1.7017F, 0.0F, 0.0F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.1803F, 0.1688F, -0.2182F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 3.25F, 7.25F));

		PartDefinition Base_r3 = TailSecondary.addOrReplaceChild("Base_r3", CubeListBuilder.create().texOffs(84, 51).addBox(-1.5F, -2.3553F, -0.5652F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.25F, 1.0F, 1.309F, 0.0F, 0.0F));

		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.5F, 4.5F));

		PartDefinition Base_r4 = TailTertiary.addOrReplaceChild("Base_r4", CubeListBuilder.create().texOffs(86, 60).addBox(-0.8314F, -8.0463F, -3.4448F, 1.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.175F, 3.25F, -0.8611F, 0.008F, 1.5605F));

		PartDefinition Base_r5 = TailTertiary.addOrReplaceChild("Base_r5", CubeListBuilder.create().texOffs(78, 55).mirror().addBox(-0.1686F, -6.0463F, -0.4448F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(86, 60).mirror().addBox(-0.1686F, -8.0463F, -3.4448F, 1.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.175F, 3.25F, -0.8611F, -0.008F, -1.5605F));

		PartDefinition Base_r6 = TailTertiary.addOrReplaceChild("Base_r6", CubeListBuilder.create().texOffs(78, 55).addBox(-0.82F, -6.0459F, -0.4369F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(72, 41).addBox(-0.82F, -8.0459F, -3.4369F, 1.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.175F, 3.25F, -0.8611F, -0.008F, 1.5811F));

		PartDefinition Base_r7 = TailTertiary.addOrReplaceChild("Base_r7", CubeListBuilder.create().texOffs(80, 17).addBox(-1.0F, -1.3212F, -0.5038F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.25F, 0.25F, 1.4835F, 0.0F, 0.0F));

		PartDefinition RightWing = torso.addOrReplaceChild("RightWing", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 8.5F, 3.3F, -0.0906F, 0.2143F, -0.6303F));

		PartDefinition rightWingRoot = RightWing.addOrReplaceChild("rightWingRoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = rightWingRoot.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(60, 21).addBox(-25.975F, -4.475F, 1.65F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, 0.0F, 0.0F, 1.2654F));

		PartDefinition cube_r2 = rightWingRoot.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(60, 0).addBox(-25.075F, -12.7F, 1.2F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r3 = rightWingRoot.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(64, 7).addBox(-12.775F, -19.75F, 1.2F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(2.0F, 20.0F, -2.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition rightSecondaries = rightWingRoot.addOrReplaceChild("rightSecondaries", CubeListBuilder.create().texOffs(42, 64).addBox(-0.2F, -0.475F, -0.3F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-7.3F, -7.0F, -0.5F, 0.0F, 0.0F, 0.5236F));

		PartDefinition cube_r4 = rightSecondaries.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(50, 65).addBox(1.025F, -22.55F, 1.2F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3F, 27.0F, -1.5F, 0.0F, 0.0F, -0.48F));

		PartDefinition cube_r5 = rightSecondaries.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(48, 11).addBox(-22.4F, 10.625F, 1.651F, 9.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3F, 27.0F, -1.5F, 0.0F, 0.0F, 1.8326F));

		PartDefinition cube_r6 = rightSecondaries.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(58, 47).addBox(-24.525F, -13.85F, 1.648F, 9.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3F, 27.0F, -1.5F, 0.0F, 0.0F, 0.7418F));

		PartDefinition rightTertiaries = rightSecondaries.addOrReplaceChild("rightTertiaries", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9599F));

		PartDefinition cube_r7 = rightTertiaries.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(22, 64).addBox(2.3F, -22.5F, 1.2F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 27.0F, -1.5F, 0.0F, 0.0F, -0.5236F));

		PartDefinition cube_r8 = rightTertiaries.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(54, 65).addBox(-10.15F, -26.2F, 1.2F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(9.0F, 27.0F, -1.5F, 0.0F, 0.0F, 0.0436F));

		PartDefinition cube_r9 = rightTertiaries.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(52, 32).addBox(-25.125F, -10.525F, 1.64F, 9.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 27.0F, -1.5F, 0.0F, 0.0F, 0.8727F));

		PartDefinition LeftWing = torso.addOrReplaceChild("LeftWing", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, 8.5F, 3.3F, -0.0906F, -0.2143F, 0.6303F));

		PartDefinition leftWingRoot = LeftWing.addOrReplaceChild("leftWingRoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r10 = leftWingRoot.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(60, 21).mirror().addBox(18.975F, -4.475F, 1.65F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, 0.0F, 0.0F, -1.2654F));

		PartDefinition cube_r11 = leftWingRoot.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(19.075F, -12.7F, 1.2F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r12 = leftWingRoot.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(64, 7).mirror().addBox(7.775F, -19.75F, 1.2F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 20.0F, -2.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition leftSecondaries = leftWingRoot.addOrReplaceChild("leftSecondaries", CubeListBuilder.create().texOffs(42, 64).mirror().addBox(-0.8F, -0.475F, -0.3F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(7.3F, -7.0F, -0.5F, 0.0F, 0.0F, -0.5236F));

		PartDefinition cube_r13 = leftSecondaries.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(50, 65).mirror().addBox(-2.025F, -22.55F, 1.2F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.3F, 27.0F, -1.5F, 0.0F, 0.0F, 0.48F));

		PartDefinition cube_r14 = leftSecondaries.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(48, 11).mirror().addBox(13.4F, 10.625F, 1.651F, 9.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.3F, 27.0F, -1.5F, 0.0F, 0.0F, -1.8326F));

		PartDefinition cube_r15 = leftSecondaries.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(58, 47).mirror().addBox(15.525F, -13.85F, 1.648F, 9.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.3F, 27.0F, -1.5F, 0.0F, 0.0F, -0.7418F));

		PartDefinition leftTertiaries = leftSecondaries.addOrReplaceChild("leftTertiaries", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9599F));

		PartDefinition cube_r16 = leftTertiaries.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(22, 64).mirror().addBox(-3.3F, -22.5F, 1.2F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.0F, 27.0F, -1.5F, 0.0F, 0.0F, 0.5236F));

		PartDefinition cube_r17 = leftTertiaries.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(54, 65).mirror().addBox(9.15F, -26.2F, 1.2F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.005F)).mirror(false), PartPose.offsetAndRotation(-9.0F, 27.0F, -1.5F, 0.0F, 0.0F, -0.0436F));

		PartDefinition cube_r18 = leftTertiaries.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(52, 32).mirror().addBox(16.125F, -10.525F, 1.64F, 9.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.0F, 27.0F, -1.5F, 0.0F, 0.0F, -0.8727F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 22).addBox(-1.5F, -1.0F, -5.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 3).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		head.addOrReplaceChild("horn_r5", CubeListBuilder.create().texOffs(60, 62).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -7.75F, 0.5F, -1.0908F, 0.0F, 0.0F));

		head.addOrReplaceChild("right ear_r1", CubeListBuilder.create().texOffs(60, 62).addBox(3.75F, -26.75F, -24.55F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 25.0F, 0.0F, -0.7418F, -0.3927F, -0.2618F));

		head.addOrReplaceChild("right horn_r1", CubeListBuilder.create().texOffs(0, 16).addBox(5.75F, -11.5F, -28.55F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 25.0F, 0.0F, -1.2654F, -0.3927F, -0.3491F));

		head.addOrReplaceChild("left ear_r1", CubeListBuilder.create().texOffs(16, 32).addBox(-5.75F, -26.75F, -24.55F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 25.0F, 0.0F, -0.7418F, 0.3927F, 0.2618F));

		head.addOrReplaceChild("left horn_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-7.75F, -11.5F, -28.55F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 25.0F, 0.0F, -1.2654F, 0.3927F, 0.3491F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 32).addBox(-2.98F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(-2.98F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(0, 32).addBox(-2.985F, 7.88F, -1.7076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-5.0F, -10.0F, 0.0F));

		right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.75F, -2.0F));

		right_arm.addOrReplaceChild("LeftFootPawBeans4", CubeListBuilder.create().texOffs(70, 91).addBox(0.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F))
		.texOffs(70, 87).addBox(1.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 85).addBox(0.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 89).addBox(-0.775F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)), PartPose.offset(-2.0F, -0.5F, 0.25F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 40).addBox(-1.02F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 40).addBox(-1.02F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(16, 40).addBox(-1.015F, 7.8757F, -1.6576F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 7.75F, -2.0F));

		left_arm.addOrReplaceChild("LeftFootPawBeans3", CubeListBuilder.create().texOffs(70, 91).addBox(0.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F))
		.texOffs(70, 87).addBox(1.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 85).addBox(0.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 89).addBox(-0.775F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)), PartPose.offset(0.0F, -0.5F, 0.25F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(27, 53).addBox(-2.5F, 10.0F, -3.2F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("RightThigh_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.5F, 0.0F, -0.2182F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("RightCalf_r1", CubeListBuilder.create().texOffs(46, 49).addBox(-1.99F, -0.125F, -2.9F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.75F, -1.5F, 0.8727F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("RightArch_r1", CubeListBuilder.create().texOffs(28, 60).addBox(-2.0F, -8.45F, -0.725F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-0.5F, 12.75F, -1.25F, -0.3491F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("LeftFootPawBeans2", CubeListBuilder.create().texOffs(70, 91).addBox(0.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F))
		.texOffs(70, 87).addBox(1.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 85).addBox(0.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 89).addBox(-0.775F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)), PartPose.offset(-1.5F, 1.5F, -0.75F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, 10.0F, -3.2F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("LeftThigh_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.5F, 0.0F, -0.2182F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("LeftCalf_r1", CubeListBuilder.create().texOffs(48, 22).addBox(-2.01F, -0.125F, -2.9F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 2.75F, -1.5F, 0.8727F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("LeftArch_r1", CubeListBuilder.create().texOffs(0, 61).addBox(-2.0F, -8.45F, -0.725F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.5F, 12.75F, -1.25F, -0.3491F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("LeftFootPawBeans", CubeListBuilder.create().texOffs(70, 91).addBox(0.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F))
		.texOffs(70, 87).addBox(1.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 85).addBox(0.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(70, 89).addBox(-0.775F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)), PartPose.offset(-0.5F, 1.5F, -0.75F));

		return LayerDefinition.create(meshdefinition, 96, 96);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
