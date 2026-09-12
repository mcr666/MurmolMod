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

// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelferal_cat<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelferal_cat"), "main");
	public final ModelPart body;
	public final ModelPart head;
	public final ModelPart headr;
	public final ModelPart torso;
	public final ModelPart bone;
	public final ModelPart tails;
	public final ModelPart rightArm;
	public final ModelPart leftArm;
	public final ModelPart rightLeg;
	public final ModelPart bone2;
	public final ModelPart LeftPaw;
	public final ModelPart LeftLowerLeg;
	public final ModelPart LeftFoot2;
	public final ModelPart LeftPad2;
	public final ModelPart leftLeg;
	public final ModelPart bone3;
	public final ModelPart RightPaw;
	public final ModelPart RightLowerLeg;
	public final ModelPart RightFoot2;
	public final ModelPart RightPad2;

	public Modelferal_cat(ModelPart root) {
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.headr = this.head.getChild("headr");
		this.torso = this.body.getChild("torso");
		this.bone = this.torso.getChild("bone");
		this.tails = this.bone.getChild("tails");
		this.rightArm = this.body.getChild("rightArm");
		this.leftArm = this.body.getChild("leftArm");
		this.rightLeg = this.body.getChild("rightLeg");
		this.bone2 = this.rightLeg.getChild("bone2");
		this.LeftPaw = this.rightLeg.getChild("LeftPaw");
		this.LeftLowerLeg = this.LeftPaw.getChild("LeftLowerLeg");
		this.LeftFoot2 = this.LeftLowerLeg.getChild("LeftFoot2");
		this.LeftPad2 = this.LeftFoot2.getChild("LeftPad2");
		this.leftLeg = this.body.getChild("leftLeg");
		this.bone3 = this.leftLeg.getChild("bone3");
		this.RightPaw = this.leftLeg.getChild("RightPaw");
		this.RightLowerLeg = this.RightPaw.getChild("RightLowerLeg");
		this.RightFoot2 = this.RightLowerLeg.getChild("RightFoot2");
		this.RightPad2 = this.RightFoot2.getChild("RightPad2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));
		PartDefinition headr = head.addOrReplaceChild("headr", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5F, -1.2475F, -0.2676F, 0.0F, 3.1416F, 0.0F));
		PartDefinition cube_r1 = headr.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 0).addBox(0.0F, -0.9F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.2074F, 0.0F, 0.0F, -0.049F, -0.6504F, -0.0002F));
		PartDefinition cube_r2 = headr.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 0).addBox(-0.4F, -0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4F, -1.9622F, 0.5058F, -0.2355F, -0.3829F, 0.0894F));
		PartDefinition cube_r3 = headr.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-1.5F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.4F, -1.2525F, -0.2324F, 0.0F, 0.829F, 0.0F));
		PartDefinition cube_r4 = headr.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.4F, -0.9F, 2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.2074F, 0.0F, 0.0F, -0.039F, -0.0472F, 0.0317F));
		PartDefinition cube_r5 = headr.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(-1.0F, -0.9F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.2074F, 0.0F, 0.0F, -0.049F, 0.6504F, 0.0002F));
		PartDefinition cube_r6 = headr.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-0.6F, -0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.4F, -1.9622F, 0.5058F, -0.2355F, 0.3829F, -0.0894F));
		PartDefinition cube_r7 = headr.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -0.9F, 2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.2074F, 0.0F, 0.0F, -0.039F, 0.0472F, -0.0317F));
		PartDefinition cube_r8 = headr.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4F, -1.2525F, -0.2324F, 0.0F, -0.829F, 0.0F));
		PartDefinition Snout_r1 = headr.addOrReplaceChild("Snout_r1", CubeListBuilder.create().texOffs(15, 0).addBox(-1.5F, -0.6978F, -0.7497F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.5F, 0.5453F, 4.7173F, -3.1416F, 0.0F, 0.0F));
		PartDefinition Snout_r2 = headr.addOrReplaceChild("Snout_r2", CubeListBuilder.create().texOffs(21, 5).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.5F, -0.3569F, 4.9676F, 2.9671F, 0.0F, 0.0F));
		PartDefinition rightear_r1 = headr.addOrReplaceChild("rightear_r1", CubeListBuilder.create().texOffs(54, 58).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-6.9105F, -7.5165F, 0.7676F, 0.0F, -1.5708F, -0.3054F));
		PartDefinition rightear_r2 = headr.addOrReplaceChild("rightear_r2", CubeListBuilder.create().texOffs(48, 56).mirror().addBox(-0.5F, -2.1F, -2.1F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.6092F, -4.2626F, 1.1676F, 0.0F, -1.5708F, -0.6109F));
		PartDefinition rightear_r3 = headr.addOrReplaceChild("rightear_r3", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-0.1F, -1.0F, 0.2F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-5.9105F, -9.5165F, 0.7676F, 0.0F, -1.5708F, -0.3054F));
		PartDefinition leftear_r1 = headr.addOrReplaceChild("leftear_r1", CubeListBuilder.create().texOffs(0, 15).addBox(-0.9F, -1.0F, 0.2F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0895F, -9.5165F, 0.7676F, 0.0F, 1.5708F, 0.3054F));
		PartDefinition leftear_r2 = headr.addOrReplaceChild("leftear_r2", CubeListBuilder.create().texOffs(48, 56).addBox(-0.5F, -2.1F, -2.1F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.6092F, -4.2626F, 1.1676F, 0.0F, 1.5708F, 0.6109F));
		PartDefinition leftear_r3 = headr.addOrReplaceChild("leftear_r3", CubeListBuilder.create().texOffs(54, 58).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0895F, -7.5165F, 0.7676F, 0.0F, 1.5708F, 0.3054F));
		PartDefinition FeralHead_r1 = headr.addOrReplaceChild("FeralHead_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.5F, 1.2475F, 0.2676F, 0.0F, 3.1416F, 0.0F));
		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));
		PartDefinition bone = torso.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(36, 31).addBox(-4.0F, -5.7143F, -5.3F, 8.0F, 15.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(16, 23)
				.addBox(-3.5F, 5.7857F, -5.0F, 7.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(30, 0).addBox(-5.0F, -6.7143F, -5.6F, 10.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 6.7143F, -2.7F, 0.0F, 3.1416F, 0.0F));
		PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9",
				CubeListBuilder.create().texOffs(46, 53).addBox(-6.0F, -2.0F, -4.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(44, 53).addBox(-6.0F, -2.0F, -4.0F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.9F, -3.7143F, 4.0F, 0.0F, 0.0F, -1.5708F));
		PartDefinition cube_r10 = bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(46, 53).mirror().addBox(-1.0F, -2.0F, -4.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(44, 53).mirror()
				.addBox(-3.0F, -2.0F, -4.0F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.9F, -3.7143F, 4.0F, 0.0F, 0.0F, 1.5708F));
		PartDefinition tails = bone.addOrReplaceChild("tails", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 3.1416F, 0.0F));
		PartDefinition tail_r1 = tails.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(9, 40).addBox(-3.15F, -7.1681F, -4.0176F, 2.3F, 2.1F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 24.5857F, 4.75F, 1.1781F, 0.0F, 0.0F));
		PartDefinition tail_r2 = tails.addOrReplaceChild("tail_r2", CubeListBuilder.create().texOffs(2, 18).addBox(-2.45F, -3.5206F, -5.184F, 1.9F, 1.5F, 5.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 19.3857F, 3.25F, 1.2217F, 0.0F, 0.0F));
		PartDefinition tail_r3 = tails.addOrReplaceChild("tail_r3", CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, -2.4F, -3.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 16.1096F, 2.4824F, 1.3875F, 0.0F, 0.0F));
		PartDefinition tail_base_r1 = tails.addOrReplaceChild("tail_base_r1", CubeListBuilder.create().texOffs(0, 7).addBox(-1.4F, -1.3F, -2.35F, 2.8F, 2.6F, 4.7F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 11.6533F, 1.6724F, 1.5708F, 0.0F, 0.0F));
		PartDefinition rightArm = body.addOrReplaceChild("rightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, -6.0F, 0.0F));
		PartDefinition RightPaw_r1 = rightArm.addOrReplaceChild("RightPaw_r1",
				CubeListBuilder.create().texOffs(20, 7).addBox(-1.0F, -0.4272F, -0.2967F, 0.9F, 1.3F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 7).addBox(0.7F, -0.4272F, -0.2967F, 0.9F, 1.3F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.8F, 9.8272F, -1.7033F, -1.8064F, 0.0F, 0.0F));
		PartDefinition RightPaw_r2 = rightArm.addOrReplaceChild("RightPaw_r2", CubeListBuilder.create().texOffs(20, 7).addBox(-0.5F, -0.65F, -0.5F, 1.0F, 1.3F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.6487F, 8.5282F, 2.3826F, 0.4363F, 0.0F, -0.2618F));
		PartDefinition RightPaw_r3 = rightArm.addOrReplaceChild("RightPaw_r3", CubeListBuilder.create().texOffs(28, 14).addBox(-2.0F, 0.0F, -3.1F, 4.2F, 3.3F, 5.1F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.9F, 7.3418F, 0.486F, 0.0873F, 0.0F, 0.0F));
		PartDefinition RightArm_r1 = rightArm.addOrReplaceChild("RightArm_r1", CubeListBuilder.create().texOffs(48, 14).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0262F, 0.0F, 0.0F));
		PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create(), PartPose.offset(5.0F, -6.0F, 0.0F));
		PartDefinition LeftPaw_r1 = leftArm.addOrReplaceChild("LeftPaw_r1", CubeListBuilder.create().texOffs(21, 7).mirror().addBox(-0.6F, -0.4272F, -0.2967F, 0.9F, 1.3F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(21, 7).mirror()
				.addBox(1.1F, -0.4272F, -0.2967F, 0.9F, 1.3F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.8F, 9.8272F, -1.7033F, -1.8064F, 0.0F, 0.0F));
		PartDefinition LeftPaw_r2 = leftArm.addOrReplaceChild("LeftPaw_r2", CubeListBuilder.create().texOffs(20, 6).mirror().addBox(-0.5F, -0.65F, -0.5F, 1.0F, 1.3F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-1.4487F, 8.6952F, 2.3929F, 0.4363F, 0.0F, 0.2618F));
		PartDefinition LeftPaw_r3 = leftArm.addOrReplaceChild("LeftPaw_r3", CubeListBuilder.create().texOffs(28, 14).mirror().addBox(-2.0F, 0.3F, -3.1F, 4.0F, 3.0F, 5.1F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-0.9F, 7.3418F, 0.486F, 0.0873F, 0.0F, 0.0F));
		PartDefinition LeftPaw_r4 = leftArm.addOrReplaceChild("LeftPaw_r4", CubeListBuilder.create().texOffs(48, 14).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0262F, 0.0F, 0.0F));
		PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 4.0F, 0.0F));
		PartDefinition bone2 = rightLeg.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(-0.05F, 11.6728F, -4.1735F));
		PartDefinition cube_r11 = bone2.addOrReplaceChild("cube_r11",
				CubeListBuilder.create().texOffs(16, 5).addBox(-1.55F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 5).addBox(0.55F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.138F, 0.0F, 0.0F));
		PartDefinition LeftPaw = rightLeg.addOrReplaceChild("LeftPaw", CubeListBuilder.create(), PartPose.offsetAndRotation(0.8F, -1.5F, 0.0F, 0.0436F, 0.0F, 0.0F));
		PartDefinition LeftThigh_r1 = LeftPaw.addOrReplaceChild("LeftThigh_r1", CubeListBuilder.create().texOffs(33, 53).addBox(-2.5F, 0.1305F, -2.9914F, 3.5F, 7.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
		PartDefinition LeftLowerLeg = LeftPaw.addOrReplaceChild("LeftLowerLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.375F, -3.45F, -0.1309F, 0.0F, 0.0F));
		PartDefinition LeftCalf_r1 = LeftLowerLeg.addOrReplaceChild("LeftCalf_r1", CubeListBuilder.create().texOffs(0, 27).addBox(-2.51F, -0.8623F, -3.5756F, 3.5F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -2.125F, 1.95F, 0.8727F, 0.0F, 0.0F));
		PartDefinition LeftFoot2 = LeftLowerLeg.addOrReplaceChild("LeftFoot2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, 7.175F));
		PartDefinition LeftArch_r1 = LeftFoot2.addOrReplaceChild("LeftArch_r1", CubeListBuilder.create().texOffs(0, 38).addBox(-2.2F, -8.0673F, -1.6489F, 2.9F, 6.0F, 2.6F, new CubeDeformation(0.005F)),
				PartPose.offsetAndRotation(0.0F, 7.075F, -4.975F, -0.3491F, 0.0F, 0.0F));
		PartDefinition LeftPad2 = LeftFoot2.addOrReplaceChild("LeftPad2", CubeListBuilder.create().texOffs(19, 36).addBox(-2.7F, -1.0436F, -3.499F, 3.9F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 4.325F, -4.425F, 0.0873F, 0.0F, 0.0F));
		PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 4.0F, 0.0F));
		PartDefinition bone3 = leftLeg.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.05F, 11.6728F, -4.1735F));
		PartDefinition cube_r12 = bone3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(16, 5).mirror().addBox(-1.55F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(16, 5).mirror()
				.addBox(0.55F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.138F, 0.0F, 0.0F));
		PartDefinition RightPaw = leftLeg.addOrReplaceChild("RightPaw", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -1.5F, 0.0F, 0.0436F, 0.0F, 0.0F));
		PartDefinition RightThigh_r1 = RightPaw.addOrReplaceChild("RightThigh_r1", CubeListBuilder.create().texOffs(33, 53).mirror().addBox(-1.0F, 0.1305F, -2.9914F, 3.5F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
		PartDefinition RightLowerLeg = RightPaw.addOrReplaceChild("RightLowerLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.375F, -3.45F, -0.1309F, 0.0F, 0.0F));
		PartDefinition RightCalf_r1 = RightLowerLeg.addOrReplaceChild("RightCalf_r1", CubeListBuilder.create().texOffs(0, 27).mirror().addBox(-0.99F, -0.8623F, -3.5756F, 3.5F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, -2.125F, 1.95F, 0.8727F, 0.0F, 0.0F));
		PartDefinition RightFoot2 = RightLowerLeg.addOrReplaceChild("RightFoot2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.8F, 7.175F));
		PartDefinition RightArch_r1 = RightFoot2.addOrReplaceChild("RightArch_r1", CubeListBuilder.create().texOffs(0, 39).mirror().addBox(-0.7F, -8.0673F, -1.6489F, 2.9F, 6.0F, 2.6F, new CubeDeformation(0.005F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 7.075F, -4.975F, -0.3491F, 0.0F, 0.0F));
		PartDefinition RightPad2 = RightFoot2.addOrReplaceChild("RightPad2", CubeListBuilder.create().texOffs(19, 36).mirror().addBox(-1.2F, -1.0436F, -3.499F, 3.9F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 4.325F, -4.425F, 0.0873F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
		this.head.xRot = headPitch / (180F / (float) Math.PI);
	}
}