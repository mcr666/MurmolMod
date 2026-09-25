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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 狛犬（Komainu）形态模型。层位置与 KomainuForm 的 bodyLayer（murmol/komainu）一致。
 * 核心骨骼（torso/head/arms/legs）由通用 feral 动画驱动（与乘黄/落瓣春花一致），
 * 尾巴骨骼链（Tail → TailPrimary → TailSecondary → TailTertiary → TailQuaternary）由 tail_idle/tail_walk 驱动。
 */
public class KomainuModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "komainu"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public KomainuModel(ModelPart root) {
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

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(47, 78).addBox(-4.0F, 4.8F, -2.6F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(64, 62).addBox(-4.5F, -0.2F, -3.2F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.75F, 1.0F, -1.2654F, 0.0F, 0.0F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition Base_r1 = TailPrimary.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, 0.75F, -1.5F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0908F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.25F, 4.5F));

		PartDefinition fur_r1 = TailSecondary.addOrReplaceChild("fur_r1", CubeListBuilder.create().texOffs(39, 76).mirror().addBox(0.0F, -0.45F, 0.75F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 1.5708F, -0.2182F, -1.5708F));

		PartDefinition fur_r2 = TailSecondary.addOrReplaceChild("fur_r2", CubeListBuilder.create().texOffs(39, 76).addBox(0.0F, -0.45F, 0.75F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 1.5708F, 0.2182F, 1.5708F));

		PartDefinition fur_r3 = TailSecondary.addOrReplaceChild("fur_r3", CubeListBuilder.create().texOffs(33, 76).addBox(0.0F, -0.45F, 0.75F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(48, 0).addBox(-2.5F, -0.45F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 1.3526F, 0.0F, 0.0F));

		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, 4.5F));

		PartDefinition fur_r4 = TailTertiary.addOrReplaceChild("fur_r4", CubeListBuilder.create().texOffs(33, 66).mirror().addBox(-0.5F, -4.0F, 0.05F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0436F, -1.5708F));

		PartDefinition fur_r5 = TailTertiary.addOrReplaceChild("fur_r5", CubeListBuilder.create().texOffs(33, 66).addBox(0.5F, -4.0F, 0.05F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0436F, 1.5708F));

		PartDefinition fur_r6 = TailTertiary.addOrReplaceChild("fur_r6", CubeListBuilder.create().texOffs(33, 68).addBox(0.0F, -3.25F, 0.05F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition Base_r2 = TailTertiary.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(0, 32).addBox(-2.5F, 4.55F, -3.3F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.25F, -4.5F, 1.5272F, 0.0F, 0.0F));

		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 7.5F));

		PartDefinition Base_r3 = TailQuaternary.addOrReplaceChild("Base_r3", CubeListBuilder.create().texOffs(30, 60).addBox(-2.0F, 5.5F, -3.8F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(0.0F, -1.0F, -5.5F, 1.7017F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition Head2 = head.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.5F, -1.0F, -5.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 32).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.25F, 0.0F));

		PartDefinition Snout_r1 = Head2.addOrReplaceChild("Snout_r1", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0F, -29.625F, -0.95F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 26.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition LeftEar = Head2.addOrReplaceChild("LeftEar", CubeListBuilder.create(), PartPose.offset(2.5F, -5.0F, 0.0F));

		PartDefinition leftear_r1 = LeftEar.addOrReplaceChild("left ear_r1", CubeListBuilder.create().texOffs(46, 61).addBox(-7.75F, -34.75F, -1.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 30.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition RightEar = Head2.addOrReplaceChild("RightEar", CubeListBuilder.create(), PartPose.offset(-2.5F, -5.0F, 0.0F));

		PartDefinition rightear_r1 = RightEar.addOrReplaceChild("right ear_r1", CubeListBuilder.create().texOffs(61, 48).addBox(5.75F, -34.75F, -1.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 30.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition Hair = Head2.addOrReplaceChild("Hair", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -34.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
		.texOffs(24, 8).addBox(-4.5F, -34.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.3F)), PartPose.offset(0.5F, 26.0F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(25, 1).addBox(-3.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(25, 1).addBox(-3.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(25, 1).addBox(-3.0F, 7.3757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(25, 1).addBox(-3.0F, 7.3757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(108, 0).addBox(-3.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -10.0F, 0.0F));

		PartDefinition right_item = right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.75F, -2.0F));

		PartDefinition RightArm = right_arm.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(16, 41).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 65).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, -0.25F, 0.25F));

		PartDefinition RightPawBeans = RightArm.addOrReplaceChild("RightPawBeans", CubeListBuilder.create().texOffs(0, 87).mirror().addBox(-2.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F)).mirror(false)
		.texOffs(0, 83).mirror().addBox(-2.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)).mirror(false)
		.texOffs(0, 81).mirror().addBox(-1.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)).mirror(false)
		.texOffs(0, 85).mirror().addBox(-0.225F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)).mirror(false), PartPose.offset(0.0F, -0.15F, -0.5F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(25, 1).addBox(-1.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(25, 1).addBox(-1.0F, 7.3757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(25, 1).addBox(-1.0F, 7.3757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(25, 1).addBox(-1.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		PartDefinition left_item = left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 7.75F, -2.0F));

		PartDefinition LeftArm = left_arm.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(32, 44).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 66).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)).mirror(false), PartPose.offset(0.0F, -0.25F, 0.25F));

		PartDefinition LeftPawBeans = LeftArm.addOrReplaceChild("LeftPawBeans", CubeListBuilder.create().texOffs(8, 87).addBox(0.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F))
		.texOffs(8, 83).addBox(1.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(8, 81).addBox(0.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(8, 85).addBox(-0.775F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)), PartPose.offset(0.0F, -0.15F, -0.5F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		PartDefinition RightThigh_r1 = right_leg.addOrReplaceChild("RightThigh_r1", CubeListBuilder.create().texOffs(48, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.5F, 0.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition RightLowerLeg = right_leg.addOrReplaceChild("RightLowerLeg", CubeListBuilder.create(), PartPose.offset(-0.5F, 4.875F, -3.45F));

		PartDefinition RightCalf_r1 = RightLowerLeg.addOrReplaceChild("RightCalf_r1", CubeListBuilder.create().texOffs(48, 51).addBox(-1.99F, -0.125F, -2.9F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.125F, 1.95F, 0.8727F, 0.0F, 0.0F));

		PartDefinition RightFoot = RightLowerLeg.addOrReplaceChild("RightFoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, 7.175F));

		PartDefinition RightArch_r1 = RightFoot.addOrReplaceChild("RightArch_r1", CubeListBuilder.create().texOffs(56, 11).addBox(-2.0F, -8.45F, -0.725F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 7.075F, -4.975F, -0.3491F, 0.0F, 0.0F));

		PartDefinition RightPad = RightFoot.addOrReplaceChild("RightPad", CubeListBuilder.create().texOffs(52, 32).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.325F, -4.425F));

		PartDefinition RightFootPawBeans = RightPad.addOrReplaceChild("RightFootPawBeans", CubeListBuilder.create().texOffs(16, 88).mirror().addBox(-2.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F)).mirror(false)
		.texOffs(16, 84).mirror().addBox(-2.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)).mirror(false)
		.texOffs(16, 82).mirror().addBox(-1.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)).mirror(false)
		.texOffs(16, 86).mirror().addBox(-0.225F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)).mirror(false), PartPose.offset(1.0F, -8.5F, -0.05F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

		PartDefinition LeftThigh_r1 = left_leg.addOrReplaceChild("LeftThigh_r1", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.5F, 0.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition LeftLowerLeg = left_leg.addOrReplaceChild("LeftLowerLeg", CubeListBuilder.create(), PartPose.offset(0.5F, 4.875F, -3.45F));

		PartDefinition LeftCalf_r1 = LeftLowerLeg.addOrReplaceChild("LeftCalf_r1", CubeListBuilder.create().texOffs(48, 22).addBox(-2.01F, -0.125F, -2.9F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.125F, 1.95F, 0.8727F, 0.0F, 0.0F));

		PartDefinition LeftFoot = LeftLowerLeg.addOrReplaceChild("LeftFoot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, 7.175F));

		PartDefinition LeftArch_r1 = LeftFoot.addOrReplaceChild("LeftArch_r1", CubeListBuilder.create().texOffs(16, 57).addBox(-2.0F, -8.45F, -0.725F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 7.075F, -4.975F, -0.3491F, 0.0F, 0.0F));

		PartDefinition LeftPad = LeftFoot.addOrReplaceChild("LeftPad", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.325F, -4.425F));

		PartDefinition LeftFootPawBeans = LeftPad.addOrReplaceChild("LeftFootPawBeans", CubeListBuilder.create().texOffs(24, 88).addBox(0.0F, 9.475F, -0.375F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.075F))
		.texOffs(24, 84).addBox(1.8F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(24, 82).addBox(0.5F, 9.475F, -1.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F))
		.texOffs(24, 86).addBox(-0.775F, 9.475F, -1.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.075F)), PartPose.offset(-1.0F, -8.5F, -0.05F));

		return LayerDefinition.create(meshdefinition, 96, 96);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
