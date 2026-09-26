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
 * 蚕蛾形态模型。层位置与 SilkmothForm.getBodyLayer() 一致（silkmoth）。
 * 骨骼动画由 FeralBedrockPlayerAnimator 按 silkmoth_anim.json 驱动（含翅膀与尾部）。
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
		this.right_leg = this.body.getChild("right_leg");
		this.left_leg = this.body.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		torso.addOrReplaceChild("bone_r1", CubeListBuilder.create().texOffs(0, 67).addBox(-5.5F, -5.5F, -7.5F, 11.0F, 11.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.5F, 2.5F, -1.5708F, 0.0F, 0.0F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offset(0.0F, 16.3544F, 0.9216F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offset(0.25F, -0.3022F, 4.3392F));

		TailPrimary.addOrReplaceChild("bone_r2", CubeListBuilder.create().texOffs(28, 93).addBox(-4.5F, -4.5F, -1.0F, 9.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 2.4478F, -2.7608F, -1.5708F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(-0.25F, 0.1881F, -3.4799F));

		TailSecondary.addOrReplaceChild("cube_r6_r1", CubeListBuilder.create().texOffs(52, 67).addBox(-5.5F, -6.3458F, 0.0203F, 11.0F, 11.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 1.0F, -1.8326F, 0.0F, 0.0F));

		PartDefinition L_leg_mid2 = torso.addOrReplaceChild("L_leg_mid2", CubeListBuilder.create(), PartPose.offsetAndRotation(10.202F, 9.9668F, -2.5704F, -1.5708F, 0.0F, 0.0F));

		L_leg_mid2.addOrReplaceChild("leg_r7_r1", CubeListBuilder.create().texOffs(46, 112).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9099F, -1.8204F, -0.0332F, 3.1416F, -0.0436F, -3.1416F));

		L_leg_mid2.addOrReplaceChild("leg_r6_r1", CubeListBuilder.create().texOffs(28, 107).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9099F, 1.8204F, 0.0332F, -3.1416F, 0.0F, -2.3998F));

		PartDefinition rlegmid = torso.addOrReplaceChild("rlegmid", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.202F, 9.5332F, -2.5704F, -1.5708F, 0.0F, 0.0873F));

		rlegmid.addOrReplaceChild("leg_r4_r1", CubeListBuilder.create().texOffs(82, 112).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9099F, -1.8204F, 0.0332F, 0.0F, 0.0436F, 0.0F));

		rlegmid.addOrReplaceChild("leg_r3_r1", CubeListBuilder.create().texOffs(56, 108).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9099F, 1.8204F, -0.0332F, 3.1416F, 0.0F, 2.3998F));

		PartDefinition wings = torso.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.5F, 20.0F, 8.0F, -1.5708F, 0.0F, 0.0F));

		wings.addOrReplaceChild("wingR", CubeListBuilder.create().texOffs(-31, 31).addBox(-29.0F, 0.0F, -23.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		wings.addOrReplaceChild("wingL", CubeListBuilder.create().texOffs(31, 31).addBox(-1.0F, 0.0F, -23.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(11.0F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition h2 = head.addOrReplaceChild("h2", CubeListBuilder.create().texOffs(52, 92).addBox(-5.5F, -3.5F, -3.0F, 7.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.5F, 0.25F));

		h2.addOrReplaceChild("cube_r8_r1", CubeListBuilder.create().texOffs(0, 93).addBox(-5.0F, 0.0F, -5.0F, 4.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.342F, -5.4264F, -5.2458F, -0.9278F, 0.1321F, -0.1741F));

		h2.addOrReplaceChild("cube_r7_r1", CubeListBuilder.create().texOffs(78, 92).addBox(0.0F, 0.0F, -5.0F, 4.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6817F, -5.2547F, -5.1141F, -0.9278F, -0.1321F, 0.1741F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, -10.0F, 0.0F));

		right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.75F, -2.0F));

		PartDefinition L_leg_front = right_arm.addOrReplaceChild("L_leg_front", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 2.0F, 2.5F, 0.0F, -3.098F, 0.0F));

		L_leg_front.addOrReplaceChild("cube_r5_r1", CubeListBuilder.create().texOffs(18, 119).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.549F, -2.0F, 0.134F, 0.0F, -0.5236F, 0.0F));

		L_leg_front.addOrReplaceChild("cube_r4_r1", CubeListBuilder.create().texOffs(78, 104).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8916F, 1.2726F, 2.6354F, -2.8362F, 0.4363F, -2.3998F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, -10.0F, 0.0F));

		left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 7.75F, -2.0F));

		PartDefinition rlegfront = left_arm.addOrReplaceChild("rlegfront", CubeListBuilder.create(), PartPose.offsetAndRotation(3.75F, 2.0F, 4.0F, 0.0F, -3.098F, 0.0F));

		rlegfront.addOrReplaceChild("cube_r1_r1", CubeListBuilder.create().texOffs(0, 113).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8687F, -2.0F, 1.5797F, 3.1416F, -0.48F, -3.1416F));

		rlegfront.addOrReplaceChild("leg_r5_r1", CubeListBuilder.create().texOffs(0, 109).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.8916F, 1.2726F, 4.3854F, -2.8362F, -0.4363F, 2.3998F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		PartDefinition rlegback = right_leg.addOrReplaceChild("rlegback", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5F, 3.75F, -2.5F, 0.0F, 0.9425F, 0.0F));

		rlegback.addOrReplaceChild("leg_r2_r1", CubeListBuilder.create().texOffs(64, 112).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8687F, -2.0F, 0.4203F, 0.0F, -0.48F, 0.0F));

		rlegback.addOrReplaceChild("leg_r1_r1", CubeListBuilder.create().texOffs(84, 108).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6399F, 1.4637F, -2.5848F, 2.7673F, 0.4687F, 2.3332F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

		PartDefinition L_leg_back = left_leg.addOrReplaceChild("L_leg_back", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, 3.75F, -0.5F, 0.0F, -0.9425F, 0.0F));

		L_leg_back.addOrReplaceChild("cube_r3_r1", CubeListBuilder.create().texOffs(28, 111).addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8687F, -2.0F, -1.5797F, 0.0F, 0.48F, 0.0F));

		L_leg_back.addOrReplaceChild("cube_r2_r1", CubeListBuilder.create().texOffs(0, 105).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6399F, 1.4637F, -4.5848F, 2.7673F, -0.4687F, -2.3332F));

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
