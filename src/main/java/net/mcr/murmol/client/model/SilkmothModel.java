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
 * 月蛾（蚕蛾）形态模型，偏人形。层位置与 SilkmothForm.getBodyLayer() 一致（silkmoth）。
 * 核心骨骼（torso/head/arms/legs）由 FeralBedrockPlayerAnimator 按 silkmoth_anim.json 驱动，
 * 未定义的核心骨骼保留原版玩家动画；翅膀（wings）与尾部（bep1）由 tail_idle/tail_walk 等附加动画驱动。
 */
public class SilkmothModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "silkmoth"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public SilkmothModel(ModelPart root) {
		this.body = root.getChild("body");
		this.torso = this.body.getChild("torso");
		this.head = this.body.getChild("head");
		this.right_arm = this.body.getChild("right_arm");
		this.left_arm = this.body.getChild("left_arm");
		this.left_leg = this.body.getChild("left_leg");
		this.right_leg = this.body.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		torso.addOrReplaceChild("bone_r1", CubeListBuilder.create().texOffs(96, 4).addBox(-5.5F, -4.5F, -1.0F, 11.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.25F, 0.5F, 1.5708F, 0.0F, -3.1416F));

		torso.addOrReplaceChild("bone_r2", CubeListBuilder.create().texOffs(0, 67).addBox(-5.5F, -2.5F, -7.5F, 10.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 7.5F, 2.25F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bep1 = torso.addOrReplaceChild("bep1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 14.2403F, 2.7809F, 1.2217F, 0.0F, 0.0F));

		bep1.addOrReplaceChild("cube_r6_r1", CubeListBuilder.create().texOffs(52, 67).addBox(-5.5F, -6.3458F, 0.0203F, 11.0F, 11.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.75F, 0.0F, 1.309F, 0.0F, -3.1416F));

		bep1.addOrReplaceChild("bone_r3", CubeListBuilder.create().texOffs(94, 3).addBox(-3.5F, -4.5F, -4.0F, 8.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.2597F, -0.2809F, 2.138F, 0.0F, -3.1416F));

		bep1.addOrReplaceChild("bone_r4", CubeListBuilder.create().texOffs(28, 93).addBox(-4.5F, -4.5F, -1.0F, 9.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.4903F, -0.2809F, 1.5708F, 0.0F, -3.1416F));

		PartDefinition wings = torso.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.5F, 20.0F, 8.0F, -1.5708F, 0.0F, 0.0F));

		wings.addOrReplaceChild("wingR", CubeListBuilder.create().texOffs(31, 31).mirror().addBox(-29.0F, 0.25F, -12.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 3.0F, -14.0F, 0.0F, 0.0F, 1.1781F));

		wings.addOrReplaceChild("wingL", CubeListBuilder.create().texOffs(31, 31).addBox(-1.0F, 0.25F, -12.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5F, 3.0F, -14.0F, 0.0F, 0.0F, -1.1781F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition h2 = head.addOrReplaceChild("h2", CubeListBuilder.create().texOffs(0, 15).addBox(-6.0F, -4.0F, -4.25F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -4.0F, 0.75F));

		h2.addOrReplaceChild("cube_r8_r1", CubeListBuilder.create().texOffs(0, 93).addBox(-5.0F, 0.0F, -5.0F, 4.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8419F, -7.4264F, -6.9958F, -0.9278F, 0.1321F, -0.1741F));

		h2.addOrReplaceChild("cube_r7_r1", CubeListBuilder.create().texOffs(78, 92).addBox(0.0F, 0.0F, -5.0F, 4.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1817F, -7.2547F, -6.8641F, -0.9278F, -0.1321F, 0.1741F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, -10.0F, 0.0F));

		right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-0.5F, 9.75F, -3.0F));

		PartDefinition cube_r6 = right_arm.addOrReplaceChild("cube_r6", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.9085F, 0.0F, 0.5915F, -2.7489F, 0.0F, -3.1416F));

		PartDefinition cube_r5 = cube_r6.addOrReplaceChild("cube_r5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, 0.8727F, -0.003F, 0.0023F));

		cube_r5.addOrReplaceChild("cube_r5_r1", CubeListBuilder.create().texOffs(18, 119).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		cube_r5.addOrReplaceChild("cube_r4_r1", CubeListBuilder.create().texOffs(78, 104).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.005F, 2.5226F, -5.1276F, 1.5725F, -0.9107F, -1.6228F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, -10.0F, 0.0F));

		left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 9.75F, -3.0F));

		PartDefinition cube_r4 = left_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.9085F, 0.0F, 0.5915F, -2.7489F, 0.0F, 3.1416F));

		PartDefinition cube_r3 = cube_r4.addOrReplaceChild("cube_r3", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.25F, 0.0F, 0.0F, 0.8727F, 0.003F, -0.0023F));

		cube_r3.addOrReplaceChild("cube_r5_r2", CubeListBuilder.create().texOffs(18, 119).mirror().addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		cube_r3.addOrReplaceChild("cube_r4_r2", CubeListBuilder.create().texOffs(78, 104).mirror().addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.005F, 2.5226F, -5.1276F, 1.5725F, 0.9107F, 1.6228F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.3441F, 2.0682F, -1.8007F));

		PartDefinition legr3 = left_leg.addOrReplaceChild("legr3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6303F, 0.3756F, -1.1038F));

		legr3.addOrReplaceChild("cube_r2_r1", CubeListBuilder.create().texOffs(0, 105).addBox(-9.2267F, -10.4887F, 4.5275F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.9465F, -0.4027F, 5.0016F, -2.8704F, 0.2351F, -1.2381F));

		legr3.addOrReplaceChild("cube_r3_r1", CubeListBuilder.create().texOffs(28, 111).addBox(5.4704F, -7.4361F, -8.158F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.7501F, -6.5653F, 2.9081F, 0.0F, -0.4625F, 1.1781F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition legr1 = right_leg.addOrReplaceChild("legr1", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3441F, 2.0682F, -1.8007F, -0.6303F, -0.3756F, 1.1038F));

		legr1.addOrReplaceChild("cube_r2_r2", CubeListBuilder.create().texOffs(0, 105).mirror().addBox(-2.7733F, -10.4887F, 4.5275F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.9465F, -0.4027F, 5.0016F, -2.8704F, -0.2351F, 1.2381F));

		legr1.addOrReplaceChild("cube_r3_r2", CubeListBuilder.create().texOffs(28, 111).mirror().addBox(-10.4704F, -7.4361F, -8.158F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.7501F, -6.5653F, 2.9081F, 0.0F, 0.4625F, -1.1781F));

		PartDefinition biped_left_leg = body.addOrReplaceChild("biped_left_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition rlegback2 = biped_left_leg.addOrReplaceChild("rlegback2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.5F, -7.75F, 0.5F, -0.0608F, -0.6301F, 0.1893F));

		rlegback2.addOrReplaceChild("leg_r2_r1", CubeListBuilder.create().texOffs(64, 112).mirror().addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.8687F, -2.0F, 0.4204F, 0.0F, 0.48F, 0.0F));

		rlegback2.addOrReplaceChild("leg_r1_r1", CubeListBuilder.create().texOffs(84, 108).mirror().addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.1399F, 1.2137F, -1.3348F, 2.758F, -0.202F, -2.2477F));

		PartDefinition biped_right_leg = body.addOrReplaceChild("biped_right_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition rlegback = biped_right_leg.addOrReplaceChild("rlegback", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, -7.75F, 0.5F, -0.0608F, 0.6301F, -0.1893F));

		rlegback.addOrReplaceChild("leg_r2_r2", CubeListBuilder.create().texOffs(64, 112).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8687F, -2.0F, 0.4204F, 0.0F, -0.48F, 0.0F));

		rlegback.addOrReplaceChild("leg_r1_r2", CubeListBuilder.create().texOffs(84, 108).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1399F, 1.2137F, -1.3348F, 2.758F, 0.202F, 2.2477F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
