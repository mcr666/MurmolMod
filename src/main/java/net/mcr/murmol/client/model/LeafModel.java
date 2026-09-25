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
 * 苔藓兽形态模型（原 leaf）。层位置与 MossBeastForm.getBodyLayer() 一致。
 */
public class LeafModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "moss_beast"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart Tail;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public LeafModel(ModelPart root) {
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

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 4.8F, -2.6F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(28, 0).addBox(-4.5F, -0.2F, -3.2F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		torso.addOrReplaceChild("BlossomButton_r1", CubeListBuilder.create().texOffs(81, 77).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.25F, -1.1F, -1.5708F, -0.7854F, 0.0F));

		PartDefinition blomm2 = torso.addOrReplaceChild("blomm2", CubeListBuilder.create().texOffs(0, 52).addBox(-7.0F, -6.9127F, -7.0F, 14.0F, 8.0F, 14.0F, new CubeDeformation(-4.0F)), PartPose.offsetAndRotation(0.0F, 11.0127F, 6.5F, 1.5708F, 0.0F, -0.7854F));

		blomm2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 76).addBox(-16.0F, -7.7F, -16.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(-4.0F)), PartPose.offsetAndRotation(8.0F, -1.0127F, 5.1F, 0.3927F, 0.0F, 0.0F));

		blomm2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(184, 52).addBox(-16.0F, -7.7F, 0.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(-4.0F)), PartPose.offsetAndRotation(8.0F, -1.0127F, -5.1F, -0.3927F, 0.0F, 0.0F));

		blomm2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(120, 52).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(-4.0F)), PartPose.offsetAndRotation(3.707F, -1.3696F, 0.0F, 0.0F, -1.5708F, 0.3927F));

		blomm2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(56, 52).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(-4.0F)), PartPose.offsetAndRotation(-3.707F, -1.3696F, 0.0F, 0.0F, 1.5708F, -0.3927F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(56, 168).addBox(-2.5F, 0.1954F, -2.2669F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.442F, 0.4806F, 0.1745F, 0.0F, 0.0F));

		PartDefinition Tail3 = Tail.addOrReplaceChild("Tail3", CubeListBuilder.create().texOffs(104, 148).addBox(-2.0F, -2.6827F, -2.2307F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.9454F, 0.4831F, 0.1745F, 0.0F, 0.0F));

		PartDefinition Tail4 = Tail3.addOrReplaceChild("Tail4", CubeListBuilder.create().texOffs(101, 161).addBox(-1.5F, -2.3162F, -1.604F, 3.0F, 7.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.3173F, 0.0193F, 0.2618F, 0.0F, 0.0F));

		PartDefinition Tail5 = Tail4.addOrReplaceChild("Tail5", CubeListBuilder.create().texOffs(27, 167).addBox(-1.0F, -0.4817F, -1.662F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.1F))
		.texOffs(15, 163).addBox(-0.25F, 7.0183F, -1.362F, 0.5F, 2.0F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(12, 191).addBox(-0.5F, 7.0183F, -1.362F, 1.0F, 2.0F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(12, 188).addBox(-0.75F, 7.0183F, -1.362F, 1.5F, 2.0F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 193).addBox(-1.0F, 7.0183F, -1.362F, 2.0F, 2.0F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(0, 188).addBox(0.0F, 11.5183F, -2.162F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 180).addBox(1.0F, 6.5183F, -2.162F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 190).addBox(-2.0F, 11.5183F, -2.162F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 180).addBox(-2.0F, 6.5183F, -2.162F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 198).addBox(-1.0F, 6.5183F, -2.162F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(113, 161).addBox(-2.0F, 6.5183F, -1.162F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 148).addBox(0.0F, 6.5183F, -0.662F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.1838F, 0.146F, 0.4363F, 0.0F, 0.0F));

		Tail5.addOrReplaceChild("Petal1_r1", CubeListBuilder.create().texOffs(16, 248).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, -0.3491F, 0.0F, 0.0F));

		Tail5.addOrReplaceChild("Petal2_r1", CubeListBuilder.create().texOffs(16, 248).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, 1.5708F, 0.3491F));

		Tail5.addOrReplaceChild("Petal3_r1", CubeListBuilder.create().texOffs(16, 248).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 2.7925F, 0.0F, 3.1416F));

		Tail5.addOrReplaceChild("Petal4_r1", CubeListBuilder.create().texOffs(16, 248).addBox(-4.0F, 1.8794F, 0.684F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, -1.5708F, -0.3491F));

		Tail5.addOrReplaceChild("Leaf1_r1", CubeListBuilder.create().texOffs(16, 248).addBox(-4.3299F, 1.8711F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, 0.0F, -0.1745F));

		Tail5.addOrReplaceChild("Leaf2_r1", CubeListBuilder.create().texOffs(16, 248).addBox(-3.7143F, 1.8711F, -3.835F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 4.5183F, -0.662F, 0.0F, 0.5236F, 0.1745F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(140, 76).addBox(-2.0F, -8.75F, -1.75F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition snout = head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(46, 18).addBox(-2.0F, -15.75F, -15.55F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 18).addBox(-2.5F, -17.75F, -16.15F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.75F, 10.75F));

		snout.addOrReplaceChild("aHead_r1", CubeListBuilder.create().texOffs(56, 18).addBox(-2.0F, -29.625F, -0.95F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 11.25F, -10.15F, 0.1745F, 0.0F, 0.0F));

		PartDefinition Blossom_fl = head.addOrReplaceChild("Blossom_fl", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -8.95F, 2.5F, 0.3927F, 0.0F, 0.0F));

		PartDefinition BlossomBase5 = Blossom_fl.addOrReplaceChild("BlossomBase5", CubeListBuilder.create().texOffs(64, 43).addBox(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 3.1416F));

		BlossomBase5.addOrReplaceChild("Leaf10_r1", CubeListBuilder.create().texOffs(196, 43).addBox(-4.0F, -39.2F, -1.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-4.6071F, 36.7074F, 11.0242F, 0.5236F, -0.6981F, -0.1745F));

		BlossomBase5.addOrReplaceChild("Petal14_r1", CubeListBuilder.create().texOffs(164, 43).addBox(-4.0F, -39.9F, 5.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-18.5997F, 31.2544F, 15.607F, 2.618F, 0.8727F, 3.1416F));

		BlossomBase5.addOrReplaceChild("Petal13_r1", CubeListBuilder.create().texOffs(132, 43).addBox(-4.0F, -39.9F, 5.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(18.5997F, 31.2544F, 15.607F, 2.618F, -0.8727F, 3.1416F));

		BlossomBase5.addOrReplaceChild("Leaf9_r1", CubeListBuilder.create().texOffs(100, 43).addBox(-4.0F, -39.2F, -1.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(4.6071F, 36.7074F, 11.0242F, 0.5236F, 0.6981F, 0.1745F));

		BlossomBase5.addOrReplaceChild("Petal12_r1", CubeListBuilder.create().texOffs(68, 43).addBox(-4.0F, -39.5F, 5.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 31.308F, 24.0801F, 2.618F, 0.0F, 3.1416F));

		PartDefinition BlossomBase4 = Blossom_fl.addOrReplaceChild("BlossomBase4", CubeListBuilder.create().texOffs(164, 34).addBox(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.4F, -1.0F, 2.0944F, 5.0615F, 0.0F));

		BlossomBase4.addOrReplaceChild("Leaf8_r1", CubeListBuilder.create().texOffs(32, 43).addBox(-9.0F, -39.6F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(8.6485F, 38.2333F, -0.7861F, 0.0F, -0.6981F, -0.1745F));

		BlossomBase4.addOrReplaceChild("Petal11_r1", CubeListBuilder.create().texOffs(0, 43).addBox(-7.0F, -39.5F, 4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(11.3001F, 35.7498F, 13.3981F, 2.7925F, -0.8727F, 3.1416F));

		BlossomBase4.addOrReplaceChild("Leaf7_r1", CubeListBuilder.create().texOffs(200, 34).addBox(-9.0F, -39.6F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-5.1044F, 39.5635F, -7.2139F, 0.0F, 0.6981F, 0.1745F));

		BlossomBase4.addOrReplaceChild("Petal10_r1", CubeListBuilder.create().texOffs(168, 34).addBox(-7.0F, -39.5F, 4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-3.0F, 35.7498F, 17.2686F, 2.7925F, 0.0F, 3.1416F));

		PartDefinition BlossomBase3 = Blossom_fl.addOrReplaceChild("BlossomBase3", CubeListBuilder.create().texOffs(32, 34).addBox(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.4F, -1.0F, 2.0944F, 1.2217F, 0.0F));

		BlossomBase3.addOrReplaceChild("Leaf6_r1", CubeListBuilder.create().texOffs(132, 34).addBox(1.0F, -39.6F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(5.1044F, 39.5635F, -7.2139F, 0.0F, -0.6981F, -0.1745F));

		BlossomBase3.addOrReplaceChild("Petal9_r1", CubeListBuilder.create().texOffs(100, 34).addBox(-1.0F, -39.5F, 4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-11.3001F, 35.7498F, 13.3981F, 2.7925F, 0.8727F, -3.1416F));

		BlossomBase3.addOrReplaceChild("Leaf5_r1", CubeListBuilder.create().texOffs(68, 34).addBox(1.0F, -39.6F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-8.6485F, 38.2333F, -0.7861F, 0.0F, 0.6981F, 0.1745F));

		BlossomBase3.addOrReplaceChild("Petal8_r1", CubeListBuilder.create().texOffs(36, 34).addBox(-1.0F, -39.5F, 4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(3.0F, 35.7498F, 17.2686F, 2.7925F, 0.0F, 3.1416F));

		PartDefinition BlossomBase2 = Blossom_fl.addOrReplaceChild("BlossomBase2", CubeListBuilder.create().texOffs(110, 18).addBox(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.4F, -1.0F, 2.0944F, 0.0F, 0.0F));

		BlossomBase2.addOrReplaceChild("Leaf4_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, -39.6F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(6.8765F, 38.8984F, -4.0F, 0.0F, -0.6981F, -0.1745F));

		BlossomBase2.addOrReplaceChild("Petal7_r1", CubeListBuilder.create().texOffs(210, 18).addBox(-4.0F, -39.5F, 4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-13.2285F, 35.7498F, 11.1F, 2.7925F, 0.8727F, -3.1416F));

		BlossomBase2.addOrReplaceChild("Petal6_r1", CubeListBuilder.create().texOffs(178, 18).addBox(-4.0F, -39.5F, 4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(13.2285F, 35.7498F, 11.1F, 2.7925F, -0.8727F, 3.1416F));

		BlossomBase2.addOrReplaceChild("Leaf3_r1", CubeListBuilder.create().texOffs(146, 18).addBox(-4.0F, -39.6F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(-6.8765F, 38.8984F, -4.0F, 0.0F, 0.6981F, 0.1745F));

		BlossomBase2.addOrReplaceChild("Petal5_r1", CubeListBuilder.create().texOffs(114, 18).addBox(-4.0F, -39.0F, 3.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, 36.1219F, 15.1579F, 2.7925F, 0.0F, 3.1416F));

		PartDefinition RightEar = head.addOrReplaceChild("RightEar", CubeListBuilder.create(), PartPose.offset(-3.0F, -6.5F, -2.0F));

		RightEar.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(92, 76).addBox(-7.1F, -1.2F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.05F))
		.texOffs(100, 76).addBox(-6.1F, -1.6F, -0.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.04F))
		.texOffs(106, 76).addBox(-6.1F, -2.3F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.05F))
		.texOffs(112, 76).addBox(-5.1F, -3.1F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(4.7317F, -2.7902F, -2.6F, -0.1309F, 0.5236F, -0.3491F));

		PartDefinition LeftEar = head.addOrReplaceChild("LeftEar", CubeListBuilder.create(), PartPose.offset(3.0F, -6.5F, -2.0F));

		LeftEar.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(116, 76).mirror().addBox(-0.9F, -1.2F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-0.6628F, -1.3092F, -0.1F, -0.1309F, -0.5236F, 0.3491F));

		LeftEar.addOrReplaceChild("Back_r1", CubeListBuilder.create().texOffs(124, 76).mirror().addBox(-1.0F, -1.4F, -0.4F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.04F)).mirror(false), PartPose.offsetAndRotation(-0.5258F, -1.4704F, -0.0274F, -0.1309F, -0.5236F, 0.3491F));

		LeftEar.addOrReplaceChild("Part2_r1", CubeListBuilder.create().texOffs(130, 76).mirror().addBox(0.0F, 0.9F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-0.5063F, -4.6285F, -0.1883F, -0.1309F, -0.5236F, 0.3491F));

		LeftEar.addOrReplaceChild("Part3_r1", CubeListBuilder.create().texOffs(136, 76).mirror().addBox(1.0F, 1.1F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-1.0424F, -5.8787F, -0.5752F, -0.1309F, -0.5236F, 0.3491F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(212, 0).mirror().addBox(-3.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.09F)).mirror(false)
		.texOffs(64, 76).mirror().addBox(-3.0F, 7.3757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.08F)).mirror(false), PartPose.offset(-5.0F, -10.0F, 0.0F));

		PartDefinition legfur2 = right_arm.addOrReplaceChild("legfur2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.75F, 0.35F, 3.85F, 0.4363F, 0.0F, 0.0F));

		legfur2.addOrReplaceChild("legfur_r1", CubeListBuilder.create().texOffs(102, 18).mirror().addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));

		legfur2.addOrReplaceChild("legfur_r2", CubeListBuilder.create().texOffs(94, 18).mirror().addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));

		PartDefinition Right_claw2 = right_arm.addOrReplaceChild("Right_claw2", CubeListBuilder.create().texOffs(10, 100).addBox(-0.7F, 1.75F, 1.0F, 2.0F, 1.5F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(16, 100).addBox(-0.9F, 0.0F, 0.75F, 3.0F, 2.5F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.5F, 9.05F, -1.0F, -1.5708F, 0.0F, -1.5708F));

		Right_claw2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 100).addBox(-7.1F, -18.45F, 0.5F, 3.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Right_claw2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(242, 76).addBox(-6.6F, -16.55F, 0.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Right_claw2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(232, 76).addBox(-7.1F, -18.45F, -2.0F, 3.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		Right_claw2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(226, 76).addBox(-6.6F, -16.55F, -1.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(76, 0).addBox(-1.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(62, 18).addBox(-1.0F, 7.3757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.08F))
		.texOffs(228, 0).addBox(-1.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.09F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		PartDefinition legfur = left_arm.addOrReplaceChild("legfur", CubeListBuilder.create(), PartPose.offsetAndRotation(2.75F, 0.6F, 3.85F, 0.4363F, 0.0F, 0.0F));

		legfur.addOrReplaceChild("legfur_r3", CubeListBuilder.create().texOffs(86, 18).addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));

		legfur.addOrReplaceChild("legfur_r4", CubeListBuilder.create().texOffs(78, 18).addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));

		PartDefinition Left_claw2 = left_arm.addOrReplaceChild("Left_claw2", CubeListBuilder.create().texOffs(58, 100).addBox(-0.7F, 1.75F, 1.0F, 2.0F, 1.5F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(64, 100).addBox(-0.9F, 0.0F, 0.75F, 3.0F, 2.5F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-0.5F, 9.05F, -1.0F, -1.5708F, 0.0F, -1.5708F));

		Left_claw2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(48, 100).addBox(-7.1F, -18.45F, 0.5F, 3.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Left_claw2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(42, 100).addBox(-6.6F, -16.55F, 0.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Left_claw2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(32, 100).addBox(-7.1F, -18.45F, -2.0F, 3.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		Left_claw2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(26, 100).addBox(-6.6F, -16.55F, -1.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("RightCalf_r1_r1", CubeListBuilder.create().texOffs(108, 0).addBox(-4.49F, -9.375F, 0.4F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-3.0F, 7.6724F, 6.3425F, 2.2689F, 0.0F, 3.1416F));

		right_leg.addOrReplaceChild("RightArch_r1_r1", CubeListBuilder.create().texOffs(124, 0).addBox(-4.5F, -7.7F, -1.025F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-0.005F)), PartPose.offsetAndRotation(-3.0F, 12.5984F, -0.1515F, -2.7925F, 0.0F, 3.1416F));

		right_leg.addOrReplaceChild("RightPad_r1", CubeListBuilder.create().texOffs(138, 0).addBox(-4.5F, -2.0F, -1.8F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 12.0257F, -0.3326F, -3.1416F, 0.0F, 3.1416F));

		right_leg.addOrReplaceChild("RightThigh_r1_r1", CubeListBuilder.create().texOffs(92, 0).addBox(-4.5F, -13.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 11.8057F, -3.0295F, -0.2182F, 0.0F, 0.0F));

		PartDefinition Right_claw = right_leg.addOrReplaceChild("Right_claw", CubeListBuilder.create().texOffs(11, 100).addBox(-0.7F, 1.75F, 1.0F, 1.0F, 1.5F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(178, 76).addBox(-0.9F, 0.0F, 0.75F, 2.0F, 2.5F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.0F, 11.05F, -1.0F, -1.5708F, 0.0F, -1.5708F));

		Right_claw.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(166, 76).addBox(-7.1F, -18.45F, 0.5F, 2.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Right_claw.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(174, 76).addBox(-6.6F, -16.55F, 0.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Right_claw.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(154, 76).addBox(-7.1F, -18.45F, -2.0F, 2.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		Right_claw.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(162, 76).addBox(-6.6F, -16.55F, -1.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("LeftCalf_r1_r1", CubeListBuilder.create().texOffs(168, 0).mirror().addBox(0.49F, -9.375F, 0.4F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(3.0F, 7.6724F, 6.3425F, 2.2689F, 0.0F, 3.1416F));

		left_leg.addOrReplaceChild("LeftArch_r1_r1", CubeListBuilder.create().texOffs(184, 0).mirror().addBox(0.5F, -7.7F, -1.025F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-0.005F)).mirror(false), PartPose.offsetAndRotation(3.0F, 12.5984F, -0.1515F, -2.7925F, 0.0F, 3.1416F));

		left_leg.addOrReplaceChild("LeftPad_r1", CubeListBuilder.create().texOffs(198, 0).mirror().addBox(0.5F, -2.0F, -1.8F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, 12.0257F, -0.3326F, -3.1416F, 0.0F, 3.1416F));

		left_leg.addOrReplaceChild("LeftThigh_r1_r1", CubeListBuilder.create().texOffs(152, 0).mirror().addBox(0.5F, -13.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 11.8057F, -3.0295F, -0.2182F, 0.0F, 0.0F));

		PartDefinition Left_claw = left_leg.addOrReplaceChild("Left_claw", CubeListBuilder.create().texOffs(11, 100).addBox(-0.7F, 1.75F, 1.0F, 1.0F, 1.5F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(218, 76).addBox(-0.9F, 0.0F, 0.75F, 2.0F, 2.5F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.0F, 11.05F, -1.0F, -1.5708F, 0.0F, -1.5708F));

		Left_claw.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(206, 76).addBox(-7.1F, -18.45F, 0.5F, 2.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Left_claw.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(202, 76).addBox(-6.6F, -16.55F, 0.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, 4.7266F, 0.1745F, 0.0F, 0.0F));

		Left_claw.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(194, 76).addBox(-7.1F, -18.45F, -2.0F, 2.0F, 2.0F, 1.5F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.4302F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		Left_claw.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(190, 76).addBox(-6.6F, -16.55F, -1.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(6.1F, 18.1802F, -1.7266F, -0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}
