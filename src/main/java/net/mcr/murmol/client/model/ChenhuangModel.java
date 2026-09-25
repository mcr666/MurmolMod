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
 * 尘煌形态模型（原 Chenhuang）。层位置与 ChenHuangForm.getBodyLayer() 一致。
 */
public class ChenhuangModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "chen_huang"), "main");

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart Tail;
	private final ModelPart head;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public ChenhuangModel(ModelPart root) {
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

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(48, 0).addBox(-4.0F, 4.8F, -2.6F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(76, 0).addBox(-4.5F, -0.2F, -3.2F, 9.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition backcorn = torso.addOrReplaceChild("backcorn", CubeListBuilder.create().texOffs(104, 120).addBox(-0.3F, -2.5F, 2.45F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.4F, 4.75F, 2.75F, -1.5708F, 0.0698F, -0.0436F));

		backcorn.addOrReplaceChild("backcorn_r1", CubeListBuilder.create().texOffs(104, 120).addBox(-0.6F, -2.0F, -0.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.35F, -5.55F, 4.85F, -0.8727F, 0.0F, 0.0F));

		backcorn.addOrReplaceChild("backcorn_r2", CubeListBuilder.create().texOffs(104, 120).addBox(-1.3F, -3.0F, -0.7F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(1.5F, -3.5F, 3.95F, -0.4363F, 0.0F, 0.0F));

		backcorn.addOrReplaceChild("backcorn_r3", CubeListBuilder.create().texOffs(104, 120).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(1.75F, -1.75F, 3.4F, -0.2618F, 0.0F, 0.0F));

		PartDefinition backcorn2 = torso.addOrReplaceChild("backcorn2", CubeListBuilder.create().texOffs(104, 120).addBox(-0.3F, -2.5F, 2.45F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.85F, 4.75F, 2.75F, -1.5708F, -0.0698F, 0.0436F));

		backcorn2.addOrReplaceChild("backcorn_r4", CubeListBuilder.create().texOffs(104, 120).addBox(-0.6F, -2.0F, -0.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.35F, -5.55F, 4.85F, -0.8727F, 0.0F, 0.0F));

		backcorn2.addOrReplaceChild("backcorn_r5", CubeListBuilder.create().texOffs(104, 120).addBox(-1.3F, -3.0F, -0.7F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(1.5F, -3.5F, 3.95F, -0.4363F, 0.0F, 0.0F));

		backcorn2.addOrReplaceChild("backcorn_r6", CubeListBuilder.create().texOffs(104, 120).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(1.75F, -1.75F, 3.4F, -0.2618F, 0.0F, 0.0F));

		PartDefinition backcorn3 = torso.addOrReplaceChild("backcorn3", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1F, 8.5F, 1.25F, -1.6581F, 0.0698F, -0.0873F));

		backcorn3.addOrReplaceChild("backcorn_r7", CubeListBuilder.create().texOffs(104, 120).addBox(-0.6F, -2.0F, -0.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.35F, -5.55F, 4.85F, -0.8727F, 0.0F, 0.0F));

		backcorn3.addOrReplaceChild("backcorn_r8", CubeListBuilder.create().texOffs(104, 120).addBox(-1.3F, -3.0F, -0.7F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(1.5F, -3.5F, 3.95F, -0.4363F, 0.0F, 0.0F));

		backcorn3.addOrReplaceChild("backcorn_r9", CubeListBuilder.create().texOffs(104, 120).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(1.75F, -1.75F, 3.4F, -0.2618F, 0.0F, 0.0F));

		PartDefinition backcorn4 = torso.addOrReplaceChild("backcorn4", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.35F, 8.3F, 1.25F, -1.6581F, -0.0698F, 0.0873F));

		backcorn4.addOrReplaceChild("backcorn_r10", CubeListBuilder.create().texOffs(104, 120).addBox(-0.6F, -2.0F, -0.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.35F, -5.55F, 4.85F, -0.8727F, 0.0F, 0.0F));

		backcorn4.addOrReplaceChild("backcorn_r11", CubeListBuilder.create().texOffs(104, 120).addBox(-1.3F, -3.0F, -0.7F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(1.5F, -3.5F, 3.95F, -0.4363F, 0.0F, 0.0F));

		backcorn4.addOrReplaceChild("backcorn_r12", CubeListBuilder.create().texOffs(104, 120).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(1.75F, -1.75F, 3.4F, -0.2618F, 0.0F, 0.0F));

		PartDefinition Tail = torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.3544F, 0.9216F, -1.0472F, 0.0F, 0.0F));

		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		TailPrimary.addOrReplaceChild("TailPrimary_r1", CubeListBuilder.create().texOffs(0, 89).addBox(-2.0F, 0.75F, -1.5F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, 0.0F));

		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.25F, 5.6F));

		TailSecondary.addOrReplaceChild("TailSecondary_r1", CubeListBuilder.create().texOffs(16, 89).addBox(-2.5F, -0.45F, -2.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.7F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 1.4835F, 0.0F, 0.0F));

		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, 7.1F));

		TailTertiary.addOrReplaceChild("TailTertiary_r1", CubeListBuilder.create().texOffs(36, 89).addBox(-2.0F, -0.7F, -1.95F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 1.8326F, 0.0F, 0.0F));

		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(0.0F, -0.6F, 1.55F));

		TailQuaternary.addOrReplaceChild("TailQuaternary_r1", CubeListBuilder.create().texOffs(52, 89).addBox(-2.0F, -1.2F, -1.95F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, -0.7F, 3.0F, 2.0071F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(90, 45).addBox(-1.0F, -1.0F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 45).addBox(-1.5F, -3.0F, -5.4F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 45).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		head.addOrReplaceChild("aHead_r1", CubeListBuilder.create().texOffs(96, 45).addBox(-1.0F, -29.625F, -0.95F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 26.0F, 0.6F, 0.1745F, 0.0F, 0.0F));

		PartDefinition LeftEarPivot = head.addOrReplaceChild("LeftEarPivot", CubeListBuilder.create().texOffs(0, 67).addBox(-1.8085F, -13.4613F, 7.591F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 67).addBox(-1.3126F, -11.035F, 8.2792F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.04F))
		.texOffs(18, 67).addBox(-1.7587F, -14.4192F, 7.5723F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 67).addBox(-1.7587F, -15.4192F, 7.5723F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 67).addBox(-1.3587F, -13.0692F, 8.2723F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.04F)), PartPose.offsetAndRotation(2.9611F, 0.2402F, -11.5213F, -0.4454F, -0.4957F, 0.7974F));

		PartDefinition furLeft = LeftEarPivot.addOrReplaceChild("furLeft", CubeListBuilder.create(), PartPose.offset(0.5F, 1.65F, 0.3F));

		furLeft.addOrReplaceChild("furLeft_r1", CubeListBuilder.create().texOffs(86, 67).mirror().addBox(-9.591F, -13.2613F, -1.7085F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)).mirror(false), PartPose.offsetAndRotation(0.9F, -1.1F, -0.9F, 0.0F, 1.5708F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r2", CubeListBuilder.create().texOffs(80, 67).mirror().addBox(5.1257F, -13.2613F, 5.0235F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)).mirror(false), PartPose.offsetAndRotation(-0.2F, -1.1F, -0.3F, 0.0F, -0.8727F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r3", CubeListBuilder.create().texOffs(74, 67).mirror().addBox(-8.0365F, -13.2613F, 3.6379F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)).mirror(false), PartPose.offsetAndRotation(1.2F, -1.1F, -0.1F, 0.0F, 0.8727F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r4", CubeListBuilder.create().texOffs(68, 67).mirror().addBox(-9.591F, -13.2613F, -1.7085F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)).mirror(false), PartPose.offsetAndRotation(0.9F, -0.7F, -1.2F, 0.0F, 1.5708F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r5", CubeListBuilder.create().texOffs(62, 67).mirror().addBox(-9.591F, -13.2613F, -1.7085F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(0.9F, -0.3F, -1.5F, 0.0F, 1.5708F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r6", CubeListBuilder.create().texOffs(56, 67).mirror().addBox(5.1257F, -13.2613F, 5.0772F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -0.7F, -0.5F, 0.0F, -0.8727F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r7", CubeListBuilder.create().texOffs(50, 67).mirror().addBox(5.1257F, -13.2613F, 5.065F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(-0.4F, -0.2F, -0.6F, 0.0F, -0.8727F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r8", CubeListBuilder.create().texOffs(44, 67).mirror().addBox(-8.0365F, -13.2613F, 3.6918F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)).mirror(false), PartPose.offsetAndRotation(1.3F, -0.7F, -0.3F, 0.0F, 0.8727F, 0.0F));

		furLeft.addOrReplaceChild("furLeft_r9", CubeListBuilder.create().texOffs(38, 67).mirror().addBox(-8.0365F, -13.2613F, 3.6794F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(1.4F, -0.2F, -0.4F, 0.0F, 0.8727F, 0.0F));

		PartDefinition RightEarPivot = head.addOrReplaceChild("RightEarPivot", CubeListBuilder.create().texOffs(32, 61).addBox(-2.1915F, -13.4613F, 7.591F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 61).addBox(-1.5874F, -11.135F, 8.2792F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.04F))
		.texOffs(50, 61).addBox(-1.1913F, -14.4192F, 7.5723F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 61).addBox(-0.1913F, -15.4192F, 7.5723F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 61).addBox(-0.5913F, -13.0692F, 8.2723F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.04F)), PartPose.offsetAndRotation(-2.9342F, 0.233F, -11.5471F, -0.4454F, 0.4957F, -0.7974F));

		PartDefinition furRight = RightEarPivot.addOrReplaceChild("furRight", CubeListBuilder.create(), PartPose.offset(-0.5F, 1.75F, 0.3F));

		furRight.addOrReplaceChild("furRight_r1", CubeListBuilder.create().texOffs(118, 61).addBox(7.591F, -13.2613F, -1.7085F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)), PartPose.offsetAndRotation(-0.9F, -1.1F, -0.9F, 0.0F, -1.5708F, 0.0F));

		furRight.addOrReplaceChild("furRight_r2", CubeListBuilder.create().texOffs(112, 61).addBox(-7.1257F, -13.2613F, 5.0235F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)), PartPose.offsetAndRotation(0.2F, -1.1F, -0.3F, 0.0F, 0.8727F, 0.0F));

		furRight.addOrReplaceChild("furRight_r3", CubeListBuilder.create().texOffs(106, 61).addBox(6.0365F, -13.2613F, 3.6379F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)), PartPose.offsetAndRotation(-1.2F, -1.1F, -0.1F, 0.0F, -0.8727F, 0.0F));

		furRight.addOrReplaceChild("furRight_r4", CubeListBuilder.create().texOffs(100, 61).addBox(7.591F, -13.2613F, -1.7085F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)), PartPose.offsetAndRotation(-0.9F, -0.7F, -1.2F, 0.0F, -1.5708F, 0.0F));

		furRight.addOrReplaceChild("furRight_r5", CubeListBuilder.create().texOffs(94, 61).addBox(7.591F, -13.2613F, -1.7085F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(-0.9F, -0.3F, -1.5F, 0.0F, -1.5708F, 0.0F));

		furRight.addOrReplaceChild("furRight_r6", CubeListBuilder.create().texOffs(88, 61).addBox(-7.1257F, -13.2613F, 5.0772F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)), PartPose.offsetAndRotation(0.3F, -0.7F, -0.5F, 0.0F, 0.8727F, 0.0F));

		furRight.addOrReplaceChild("furRight_r7", CubeListBuilder.create().texOffs(82, 61).addBox(-7.1257F, -13.2613F, 5.065F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.4F, -0.2F, -0.6F, 0.0F, 0.8727F, 0.0F));

		furRight.addOrReplaceChild("furRight_r8", CubeListBuilder.create().texOffs(76, 61).addBox(6.0365F, -13.2613F, 3.6918F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)), PartPose.offsetAndRotation(-1.3F, -0.7F, -0.3F, 0.0F, -0.8727F, 0.0F));

		furRight.addOrReplaceChild("furRight_r9", CubeListBuilder.create().texOffs(70, 61).addBox(6.0365F, -13.2613F, 3.6794F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(-1.4F, -0.2F, -0.4F, 0.0F, -0.8727F, 0.0F));

		PartDefinition CheekFur = head.addOrReplaceChild("CheekFur", CubeListBuilder.create(), PartPose.offset(-2.0F, 19.25F, -23.75F));

		PartDefinition RightCheek = CheekFur.addOrReplaceChild("RightCheek", CubeListBuilder.create(), PartPose.offset(-1.0F, 1.25F, 0.0F));

		RightCheek.addOrReplaceChild("RightCheek_r1", CubeListBuilder.create().texOffs(8, 61).mirror().addBox(-0.45F, -2.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.85F, -21.25F, 20.95F, -0.1047F, -0.4451F, 0.0F));

		RightCheek.addOrReplaceChild("RightCheek_r2", CubeListBuilder.create().texOffs(0, 61).mirror().addBox(-0.45F, -3.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.95F, -21.15F, 22.25F, 0.1309F, -0.3403F, 0.0F));

		PartDefinition LeftCheek = CheekFur.addOrReplaceChild("LeftCheek", CubeListBuilder.create(), PartPose.offset(5.0F, 1.25F, 0.0F));

		LeftCheek.addOrReplaceChild("LeftCheek_r1", CubeListBuilder.create().texOffs(24, 61).addBox(0.45F, -3.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.95F, -21.15F, 22.25F, 0.1309F, 0.3403F, 0.0F));

		LeftCheek.addOrReplaceChild("LeftCheek_r2", CubeListBuilder.create().texOffs(16, 61).addBox(0.45F, -2.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.85F, -21.25F, 20.95F, -0.1047F, 0.4451F, 0.0F));

		PartDefinition headfur2 = head.addOrReplaceChild("headfur2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.45F, -8.25F, -1.0036F, 0.0F, 0.0F));

		headfur2.addOrReplaceChild("headfur2_r1", CubeListBuilder.create().texOffs(116, 45).addBox(-1.0F, -14.1758F, -5.2462F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 5.0F, -2.4F, -0.2356F, 0.0F, 0.0F));

		headfur2.addOrReplaceChild("headfur2_r2", CubeListBuilder.create().texOffs(108, 45).addBox(-1.0F, -16.1758F, -5.2462F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 5.0F, -1.5F, -0.2356F, 0.0F, 0.0F));

		headfur2.addOrReplaceChild("headfur2_r3", CubeListBuilder.create().texOffs(100, 45).addBox(-1.0F, -14.1758F, -5.2462F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 3.75F, -1.75F, -0.2356F, 0.0F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(108, 0).addBox(-3.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(-3.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(16, 45).addBox(-3.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(16, 73).addBox(-3.0F, 7.3757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(0, 73).addBox(-3.0F, 7.3757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-5.0F, -10.0F, 0.0F));

		right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.75F, -2.0F));

		right_arm.addOrReplaceChild("paw2", CubeListBuilder.create().texOffs(16, 18).addBox(4.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(20, 18).addBox(1.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(24, 18).addBox(2.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(28, 18).addBox(3.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(32, 18).addBox(2.0F, -0.58F, -2.28F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 9.6602F, -1.1484F, 0.0F, 3.1416F, 0.0F));

		PartDefinition legfur2 = right_arm.addOrReplaceChild("legfur2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.75F, 0.35F, 3.85F, 0.4363F, 0.0F, 0.0F));

		legfur2.addOrReplaceChild("legfur_r1", CubeListBuilder.create().texOffs(56, 73).addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));

		legfur2.addOrReplaceChild("legfur_r2", CubeListBuilder.create().texOffs(48, 73).addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -2.0197F, -1.6284F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(80, 18).addBox(-1.0F, 7.8757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(108, 67).addBox(-1.0F, 7.3757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(92, 67).addBox(-1.0F, 7.3757F, -2.3076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(32, 45).addBox(-1.0F, 7.8757F, -1.8076F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -10.0F, 0.0F));

		left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 7.75F, -2.0F));

		left_arm.addOrReplaceChild("paw1", CubeListBuilder.create().texOffs(56, 18).addBox(-4.0F, -0.58F, -2.28F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 18).addBox(-3.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(68, 18).addBox(-4.0F, -0.48F, 0.12F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(72, 18).addBox(-5.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(76, 18).addBox(-2.0F, -0.48F, -0.48F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.0F, 9.6602F, -1.1484F, 0.0F, 3.1416F, 0.0F));

		PartDefinition legfur = left_arm.addOrReplaceChild("legfur", CubeListBuilder.create(), PartPose.offsetAndRotation(2.75F, 0.6F, 3.85F, 0.4363F, 0.0F, 0.0F));

		legfur.addOrReplaceChild("legfur_r3", CubeListBuilder.create().texOffs(40, 73).addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));

		legfur.addOrReplaceChild("legfur_r4", CubeListBuilder.create().texOffs(32, 73).addBox(-2.75F, -1.5F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("RightCalf_r1_r1", CubeListBuilder.create().texOffs(112, 18).addBox(-4.49F, -9.375F, 0.4F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-3.0F, 7.6724F, 6.3425F, 2.2689F, 0.0F, 3.1416F));

		right_leg.addOrReplaceChild("RightArch_r1_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-4.5F, -7.7F, -1.025F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-0.005F)), PartPose.offsetAndRotation(-3.0F, 12.5984F, -0.1515F, -2.7925F, 0.0F, 3.1416F));

		right_leg.addOrReplaceChild("RightPad_r1", CubeListBuilder.create().texOffs(14, 34).addBox(-4.5F, -2.0F, -1.8F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 12.0257F, -0.3326F, -3.1416F, 0.0F, 3.1416F));

		right_leg.addOrReplaceChild("RightThigh_r1_r1", CubeListBuilder.create().texOffs(96, 18).addBox(-4.5F, -13.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 11.8057F, -3.0295F, -0.2182F, 0.0F, 0.0F));

		right_leg.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(32, 34).addBox(-10.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(36, 34).addBox(-13.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(40, 34).addBox(-12.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(44, 34).addBox(-11.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(48, 34).addBox(-12.0F, 9.1F, -1.9F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, 2.0257F, -1.5826F, -3.1416F, 0.0F, 3.1416F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("LeftCalf_r1_r1", CubeListBuilder.create().texOffs(72, 34).addBox(0.49F, -9.375F, 0.4F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(3.0F, 7.6724F, 6.3425F, 2.2689F, 0.0F, 3.1416F));

		left_leg.addOrReplaceChild("LeftArch_r1_r1", CubeListBuilder.create().texOffs(88, 34).addBox(0.5F, -7.7F, -1.025F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-0.005F)), PartPose.offsetAndRotation(3.0F, 12.5984F, -0.1515F, -2.7925F, 0.0F, 3.1416F));

		left_leg.addOrReplaceChild("LeftPad_r1", CubeListBuilder.create().texOffs(102, 34).addBox(0.5F, -2.0F, -1.8F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 12.0257F, -0.3326F, -3.1416F, 0.0F, 3.1416F));

		left_leg.addOrReplaceChild("LeftThigh_r1_r1", CubeListBuilder.create().texOffs(56, 34).addBox(0.5F, -13.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 11.8057F, -3.0295F, -0.2182F, 0.0F, 0.0F));

		left_leg.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(120, 34).addBox(12.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(124, 34).addBox(9.0F, 9.2F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(0, 45).addBox(10.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(4, 45).addBox(11.0F, 9.2F, 0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
		.texOffs(8, 45).addBox(10.0F, 9.1F, -1.9F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5F, 2.0257F, -1.5826F, -3.1416F, 0.0F, 3.1416F));

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
