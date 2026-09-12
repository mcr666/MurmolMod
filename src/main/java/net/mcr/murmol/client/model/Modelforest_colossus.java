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
public class Modelforest_colossus<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelforest_colossus"), "main");
	public final ModelPart root;
	public final ModelPart body;
	public final ModelPart torso;
	public final ModelPart chest;
	public final ModelPart abdomen;
	public final ModelPart back_armor;
	public final ModelPart spikes;
	public final ModelPart effects;
	public final ModelPart rune_left;
	public final ModelPart rune_right;
	public final ModelPart tail_01;
	public final ModelPart tail_02;
	public final ModelPart tail_03;
	public final ModelPart tail_04;
	public final ModelPart tail_tip;
	public final ModelPart neck_01;
	public final ModelPart neck_02;
	public final ModelPart head;
	public final ModelPart jaw_lower;
	public final ModelPart eyes;
	public final ModelPart horns;
	public final ModelPart mane;
	public final ModelPart mouth_glow;
	public final ModelPart front_leg_left;
	public final ModelPart front_forearm_left;
	public final ModelPart front_paw_left;
	public final ModelPart front_leg_right;
	public final ModelPart front_forearm_right;
	public final ModelPart front_paw_right;
	public final ModelPart rear_leg_left;
	public final ModelPart rear_shin_left;
	public final ModelPart rear_paw_left;
	public final ModelPart front_paw_left2;
	public final ModelPart rear_leg_right;
	public final ModelPart rear_shin_right;
	public final ModelPart rear_paw_right;
	public final ModelPart front_paw_right2;

	public Modelforest_colossus(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.torso = this.body.getChild("torso");
		this.chest = this.torso.getChild("chest");
		this.abdomen = this.torso.getChild("abdomen");
		this.back_armor = this.torso.getChild("back_armor");
		this.spikes = this.back_armor.getChild("spikes");
		this.effects = this.torso.getChild("effects");
		this.rune_left = this.effects.getChild("rune_left");
		this.rune_right = this.effects.getChild("rune_right");
		this.tail_01 = this.torso.getChild("tail_01");
		this.tail_02 = this.tail_01.getChild("tail_02");
		this.tail_03 = this.tail_02.getChild("tail_03");
		this.tail_04 = this.tail_03.getChild("tail_04");
		this.tail_tip = this.tail_04.getChild("tail_tip");
		this.neck_01 = this.body.getChild("neck_01");
		this.neck_02 = this.neck_01.getChild("neck_02");
		this.head = this.neck_02.getChild("head");
		this.jaw_lower = this.head.getChild("jaw_lower");
		this.eyes = this.head.getChild("eyes");
		this.horns = this.head.getChild("horns");
		this.mane = this.head.getChild("mane");
		this.mouth_glow = this.head.getChild("mouth_glow");
		this.front_leg_left = this.body.getChild("front_leg_left");
		this.front_forearm_left = this.front_leg_left.getChild("front_forearm_left");
		this.front_paw_left = this.front_forearm_left.getChild("front_paw_left");
		this.front_leg_right = this.body.getChild("front_leg_right");
		this.front_forearm_right = this.front_leg_right.getChild("front_forearm_right");
		this.front_paw_right = this.front_forearm_right.getChild("front_paw_right");
		this.rear_leg_left = this.body.getChild("rear_leg_left");
		this.rear_shin_left = this.rear_leg_left.getChild("rear_shin_left");
		this.rear_paw_left = this.rear_shin_left.getChild("rear_paw_left");
		this.front_paw_left2 = this.rear_paw_left.getChild("front_paw_left2");
		this.rear_leg_right = this.body.getChild("rear_leg_right");
		this.rear_shin_right = this.rear_leg_right.getChild("rear_shin_right");
		this.rear_paw_right = this.rear_shin_right.getChild("rear_paw_right");
		this.front_paw_right2 = this.rear_paw_right.getChild("front_paw_right2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -48.0F, 0.0F));
		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(2, 2).addBox(-19.0F, -13.0F, 4.0F, 38.0F, 30.0F, 56.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -32.0F));
		PartDefinition chest = torso.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(194, 2).addBox(-22.0F, -22.0F, -26.0F, 44.0F, 34.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition right_shoulder_bark_plate_r1 = chest.addOrReplaceChild("right_shoulder_bark_plate_r1", CubeListBuilder.create().texOffs(174, 236).addBox(20.0F, -24.0F, -20.0F, 13.0F, 18.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2182F));
		PartDefinition left_shoulder_bark_plate_r1 = chest.addOrReplaceChild("left_shoulder_bark_plate_r1", CubeListBuilder.create().texOffs(174, 236).addBox(-33.0F, -24.0F, -20.0F, 13.0F, 18.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2182F));
		PartDefinition chest_keel_plate_r1 = chest.addOrReplaceChild("chest_keel_plate_r1", CubeListBuilder.create().texOffs(70, 428).addBox(-10.0F, 6.0F, -28.0F, 20.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));
		PartDefinition abdomen = torso.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(310, 94).addBox(-17.0F, -16.0F, -12.0F, 34.0F, 27.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 54.0F));
		PartDefinition right_haunch_plate_r1 = abdomen.addOrReplaceChild("right_haunch_plate_r1", CubeListBuilder.create().texOffs(2, 290).addBox(17.0F, -22.0F, -2.0F, 13.0F, 16.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
		PartDefinition left_haunch_plate_r1 = abdomen.addOrReplaceChild("left_haunch_plate_r1", CubeListBuilder.create().texOffs(2, 290).addBox(-30.0F, -22.0F, -2.0F, 13.0F, 16.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));
		PartDefinition back_armor = torso.addOrReplaceChild("back_armor", CubeListBuilder.create().texOffs(2, 94).addBox(-14.0F, -10.0F, -24.0F, 28.0F, 12.0F, 58.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 32.0F));
		PartDefinition back_plate_rear_r1 = back_armor.addOrReplaceChild("back_plate_rear_r1", CubeListBuilder.create().texOffs(54, 386).addBox(-11.0F, -18.0F, 22.0F, 22.0F, 12.0F, 22.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));
		PartDefinition back_plate_mid_right_r1 = back_armor.addOrReplaceChild("back_plate_mid_right_r1", CubeListBuilder.create().texOffs(226, 386).addBox(6.0F, -22.0F, -6.0F, 11.0F, 12.0F, 22.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1047F));
		PartDefinition back_plate_mid_left_r1 = back_armor.addOrReplaceChild("back_plate_mid_left_r1", CubeListBuilder.create().texOffs(226, 386).addBox(-17.0F, -22.0F, -6.0F, 11.0F, 12.0F, 22.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1047F));
		PartDefinition back_plate_front_right_r1 = back_armor.addOrReplaceChild("back_plate_front_right_r1", CubeListBuilder.create().texOffs(404, 386).addBox(5.0F, -18.0F, -32.0F, 11.0F, 10.0F, 20.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));
		PartDefinition back_plate_front_left_r1 = back_armor.addOrReplaceChild("back_plate_front_left_r1", CubeListBuilder.create().texOffs(404, 386).addBox(-16.0F, -18.0F, -32.0F, 11.0F, 10.0F, 20.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));
		PartDefinition spikes = back_armor.addOrReplaceChild("spikes", CubeListBuilder.create().texOffs(2, 236).addBox(-6.0F, -30.0F, -8.0F, 12.0F, 32.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));
		PartDefinition spine_back_placeholder_r1 = spikes.addOrReplaceChild("spine_back_placeholder_r1", CubeListBuilder.create().texOffs(342, 290).addBox(-5.0F, -24.0F, -8.0F, 10.0F, 26.0F, 16.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 4.0F, 30.0F, -0.1745F, 0.0F, 0.0F));
		PartDefinition spine_front_placeholder_r1 = spikes.addOrReplaceChild("spine_front_placeholder_r1", CubeListBuilder.create().texOffs(346, 340).addBox(-5.0F, -24.0F, -6.0F, 10.0F, 26.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -2.0F, -30.0F, 0.2182F, 0.0F, 0.0F));
		PartDefinition effects = torso.addOrReplaceChild("effects", CubeListBuilder.create(), PartPose.offset(0.0F, -10.0F, 32.0F));
		PartDefinition rune_left = effects.addOrReplaceChild("rune_left",
				CubeListBuilder.create().texOffs(296, 386).addBox(-2.0F, -6.0F, -10.0F, 4.0F, 12.0F, 22.0F, new CubeDeformation(0.0F)).texOffs(70, 459).addBox(-5.0F, -3.0F, -4.0F, 3.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-21.0F, -2.0F, -8.0F));
		PartDefinition rune_right = effects.addOrReplaceChild("rune_right",
				CubeListBuilder.create().texOffs(296, 386).addBox(-2.0F, -6.0F, -10.0F, 4.0F, 12.0F, 22.0F, new CubeDeformation(0.0F)).texOffs(70, 459).addBox(2.0F, -3.0F, -4.0F, 3.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(21.0F, -2.0F, -8.0F));
		PartDefinition tail_01 = torso.addOrReplaceChild("tail_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 76.0F));
		PartDefinition tail_01_placeholder_r1 = tail_01.addOrReplaceChild("tail_01_placeholder_r1", CubeListBuilder.create().texOffs(240, 170).addBox(-12.0F, -10.0F, 0.0F, 24.0F, 20.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
		PartDefinition tail_02 = tail_01.addOrReplaceChild("tail_02", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 26.0F));
		PartDefinition tail_02_placeholder_r1 = tail_02.addOrReplaceChild("tail_02_placeholder_r1", CubeListBuilder.create().texOffs(260, 236).addBox(-10.0F, -6.0F, -2.0F, 20.0F, 17.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));
		PartDefinition tail_03 = tail_02.addOrReplaceChild("tail_03", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 24.0F));
		PartDefinition tail_03_placeholder_r1 = tail_03.addOrReplaceChild("tail_03_placeholder_r1", CubeListBuilder.create().texOffs(250, 290).addBox(-8.0F, -4.0F, -2.0F, 16.0F, 14.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
		PartDefinition tail_04 = tail_03.addOrReplaceChild("tail_04", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 24.0F));
		PartDefinition tail_04_placeholder_r1 = tail_04.addOrReplaceChild("tail_04_placeholder_r1", CubeListBuilder.create().texOffs(186, 340).addBox(-6.0F, -4.0F, -2.0F, 12.0F, 12.0F, 28.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
		PartDefinition tail_tip = tail_04.addOrReplaceChild("tail_tip", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 26.0F));
		PartDefinition tail_tip_spike_right_r1 = tail_tip.addOrReplaceChild("tail_tip_spike_right_r1", CubeListBuilder.create().texOffs(134, 428).addBox(6.0F, -6.0F, 6.0F, 12.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3142F));
		PartDefinition tail_tip_spike_left_r1 = tail_tip.addOrReplaceChild("tail_tip_spike_left_r1", CubeListBuilder.create().texOffs(134, 428).addBox(-18.0F, -6.0F, 6.0F, 12.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3142F));
		PartDefinition tail_tip_spike_top_r1 = tail_tip.addOrReplaceChild("tail_tip_spike_top_r1", CubeListBuilder.create().texOffs(352, 386).addBox(-4.0F, -20.0F, 6.0F, 8.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
		PartDefinition tail_tip_placeholder_r1 = tail_tip.addOrReplaceChild("tail_tip_placeholder_r1", CubeListBuilder.create().texOffs(2, 340).addBox(-10.0F, -6.0F, -4.0F, 20.0F, 14.0F, 26.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));
		PartDefinition neck_01 = body.addOrReplaceChild("neck_01", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, -25.0F));
		PartDefinition neck_01_placeholder_r1 = neck_01.addOrReplaceChild("neck_01_placeholder_r1", CubeListBuilder.create().texOffs(154, 290).addBox(-12.0F, -12.0F, -22.0F, 24.0F, 20.0F, 22.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -5.0F, -2.0F, 0.2182F, 0.0F, 0.0F));
		PartDefinition neck_02 = neck_01.addOrReplaceChild("neck_02", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -22.0F));
		PartDefinition neck_02_placeholder_r1 = neck_02.addOrReplaceChild("neck_02_placeholder_r1", CubeListBuilder.create().texOffs(98, 340).addBox(-10.0F, -12.0F, -20.0F, 20.0F, 18.0F, 22.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
		PartDefinition head = neck_02
				.addOrReplaceChild("head",
						CubeListBuilder.create().texOffs(2, 170).addBox(-18.0F, -18.0F, -30.0F, 36.0F, 28.0F, 32.0F, new CubeDeformation(0.0F)).texOffs(398, 290).addBox(-12.0F, -12.0F, -52.0F, 24.0F, 15.0F, 26.0F, new CubeDeformation(0.0F))
								.texOffs(430, 428).addBox(-10.0F, 0.0F, -48.0F, 4.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(430, 428).addBox(6.0F, 0.0F, -48.0F, 4.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
						PartPose.offset(0.0F, -6.0F, -12.0F));
		PartDefinition root_beard_center_r1 = head.addOrReplaceChild("root_beard_center_r1", CubeListBuilder.create().texOffs(190, 428).addBox(-5.0F, 8.0F, -44.0F, 10.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
		PartDefinition snout_bridge_r1 = head.addOrReplaceChild("snout_bridge_r1", CubeListBuilder.create().texOffs(146, 386).addBox(-6.0F, -18.0F, -50.0F, 12.0F, 8.0F, 26.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
		PartDefinition cheek_right_armor_r1 = head.addOrReplaceChild("cheek_right_armor_r1", CubeListBuilder.create().texOffs(270, 340).addBox(14.0F, -10.0F, -34.0F, 10.0F, 14.0F, 26.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1396F, 0.0F));
		PartDefinition cheek_left_armor_r1 = head.addOrReplaceChild("cheek_left_armor_r1", CubeListBuilder.create().texOffs(270, 340).addBox(-24.0F, -10.0F, -34.0F, 10.0F, 14.0F, 26.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1396F, 0.0F));
		PartDefinition brow_right_heavy_r1 = head.addOrReplaceChild("brow_right_heavy_r1", CubeListBuilder.create().texOffs(298, 428).addBox(6.0F, -18.0F, -32.0F, 14.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1396F, -0.1396F));
		PartDefinition brow_left_heavy_r1 = head.addOrReplaceChild("brow_left_heavy_r1", CubeListBuilder.create().texOffs(298, 428).addBox(-20.0F, -18.0F, -32.0F, 14.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1396F, 0.1396F));
		PartDefinition jaw_lower = head
				.addOrReplaceChild("jaw_lower",
						CubeListBuilder.create().texOffs(398, 340).addBox(-12.0F, -2.0F, -26.0F, 24.0F, 11.0F, 26.0F, new CubeDeformation(0.0F)).texOffs(2, 428).addBox(-5.0F, 4.0F, -22.0F, 10.0F, 3.0F, 22.0F, new CubeDeformation(0.0F))
								.texOffs(458, 428).addBox(-10.0F, -8.0F, -22.0F, 4.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(458, 428).addBox(6.0F, -8.0F, -22.0F, 4.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
						PartPose.offset(0.0F, 4.0F, -24.0F));
		PartDefinition eyes = head.addOrReplaceChild("eyes",
				CubeListBuilder.create().texOffs(128, 459).addBox(-14.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(128, 459).addBox(10.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -15.0F, -29.0F));
		PartDefinition horns = head.addOrReplaceChild("horns", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, -10.0F));
		PartDefinition horn_right_placeholder_r1 = horns.addOrReplaceChild("horn_right_placeholder_r1", CubeListBuilder.create().texOffs(2, 386).addBox(-2.0F, -20.0F, -8.0F, 8.0F, 20.0F, 16.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(12.0F, 0.0F, 0.0F, 0.3927F, 0.0F, -0.2182F));
		PartDefinition horn_left_placeholder_r1 = horns.addOrReplaceChild("horn_left_placeholder_r1", CubeListBuilder.create().texOffs(2, 386).addBox(-6.0F, -20.0F, -8.0F, 8.0F, 20.0F, 16.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-12.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.2182F));
		PartDefinition mane = head.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(178, 94).addBox(-8.0F, -16.0F, -18.0F, 16.0F, 18.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 4.0F));
		PartDefinition mouth_glow = head.addOrReplaceChild("mouth_glow",
				CubeListBuilder.create().texOffs(234, 428).addBox(-9.0F, -5.0F, -10.0F, 18.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(354, 428).addBox(-5.0F, -7.0F, -14.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 4.0F, -44.0F));
		PartDefinition front_leg_left = body.addOrReplaceChild("front_leg_left", CubeListBuilder.create(), PartPose.offset(-14.0F, -6.0F, -36.0F));
		PartDefinition front_leg_left_upper_placeholder_r1 = front_leg_left.addOrReplaceChild("front_leg_left_upper_placeholder_r1",
				CubeListBuilder.create().texOffs(422, 170).addBox(-12.0F, -3.0F, -10.0F, 16.0F, 30.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, -0.0873F));
		PartDefinition front_forearm_left = front_leg_left.addOrReplaceChild("front_forearm_left", CubeListBuilder.create(), PartPose.offset(-4.0F, 26.0F, 0.0F));
		PartDefinition front_leg_left_lower_placeholder_r1 = front_forearm_left.addOrReplaceChild("front_leg_left_lower_placeholder_r1",
				CubeListBuilder.create().texOffs(88, 290).addBox(-7.0F, -2.0F, -10.0F, 13.0F, 26.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
		PartDefinition front_paw_left = front_forearm_left.addOrReplaceChild("front_paw_left",
				CubeListBuilder.create().texOffs(360, 236).addBox(-13.0F, -4.0F, -14.0F, 22.0F, 16.0F, 28.0F, new CubeDeformation(0.0F)).texOffs(2, 459).addBox(-13.0F, 5.0F, -19.0F, 22.0F, 6.0F, 10.0F, new CubeDeformation(-0.1F)).texOffs(100, 459)
						.addBox(2.0F, 4.0F, -22.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)).texOffs(398, 428).addBox(-5.0F, 3.0F, -24.0F, 5.0F, 9.0F, 9.0F, new CubeDeformation(-0.1F)).texOffs(100, 459)
						.addBox(-12.0F, 4.0F, -22.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(2.0F, 16.0F, -2.0F));
		PartDefinition front_leg_right = body.addOrReplaceChild("front_leg_right", CubeListBuilder.create(), PartPose.offset(14.0F, -6.0F, -36.0F));
		PartDefinition front_leg_right_upper_placeholder_r1 = front_leg_right.addOrReplaceChild("front_leg_right_upper_placeholder_r1",
				CubeListBuilder.create().texOffs(422, 170).addBox(-4.0F, -3.0F, -10.0F, 16.0F, 30.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0873F));
		PartDefinition front_forearm_right = front_leg_right.addOrReplaceChild("front_forearm_right", CubeListBuilder.create(), PartPose.offset(4.0F, 26.0F, 0.0F));
		PartDefinition front_leg_right_lower_placeholder_r1 = front_forearm_right.addOrReplaceChild("front_leg_right_lower_placeholder_r1",
				CubeListBuilder.create().texOffs(88, 290).addBox(-6.0F, -2.0F, -10.0F, 13.0F, 26.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
		PartDefinition front_paw_right = front_forearm_right.addOrReplaceChild("front_paw_right",
				CubeListBuilder.create().texOffs(360, 236).addBox(-9.0F, -4.0F, -14.0F, 22.0F, 16.0F, 28.0F, new CubeDeformation(0.0F)).texOffs(2, 459).addBox(-9.0F, 5.0F, -19.0F, 22.0F, 6.0F, 10.0F, new CubeDeformation(-0.1F)).texOffs(100, 459)
						.addBox(-7.0F, 4.0F, -22.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)).texOffs(398, 428).addBox(0.0F, 3.0F, -24.0F, 5.0F, 9.0F, 9.0F, new CubeDeformation(-0.1F)).texOffs(100, 459)
						.addBox(7.0F, 4.0F, -22.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(-2.0F, 16.0F, -2.0F));
		PartDefinition rear_leg_left = body.addOrReplaceChild("rear_leg_left", CubeListBuilder.create(), PartPose.offset(-15.0F, -6.0F, 30.0F));
		PartDefinition rear_leg_left_thigh_placeholder_r1 = rear_leg_left.addOrReplaceChild("rear_leg_left_thigh_placeholder_r1", CubeListBuilder.create().texOffs(142, 170).addBox(-15.0F, -6.0F, -8.0F, 21.0F, 32.0F, 26.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, 0.0F, -0.0873F));
		PartDefinition rear_shin_left = rear_leg_left.addOrReplaceChild("rear_shin_left", CubeListBuilder.create(), PartPose.offset(-5.0F, 22.0F, 6.0F));
		PartDefinition rear_leg_left_shin_placeholder_r1 = rear_shin_left.addOrReplaceChild("rear_leg_left_shin_placeholder_r1", CubeListBuilder.create().texOffs(348, 170).addBox(-7.0F, -2.0F, -8.0F, 15.0F, 28.0F, 20.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
		PartDefinition rear_paw_left = rear_shin_left.addOrReplaceChild("rear_paw_left", CubeListBuilder.create().texOffs(62, 236).addBox(-13.0F, -4.0F, -16.0F, 24.0F, 16.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -2.0F));
		PartDefinition front_paw_left2 = rear_paw_left.addOrReplaceChild("front_paw_left2",
				CubeListBuilder.create().texOffs(2, 459).addBox(-13.0F, 5.0F, -19.0F, 22.0F, 6.0F, 10.0F, new CubeDeformation(-0.1F)).texOffs(100, 459).addBox(2.0F, 4.0F, -22.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)).texOffs(398, 428)
						.addBox(-5.0F, 3.0F, -24.0F, 5.0F, 9.0F, 9.0F, new CubeDeformation(-0.1F)).texOffs(100, 459).addBox(-12.0F, 4.0F, -22.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(2.0F, 0.0F, -2.0F));
		PartDefinition rear_leg_right = body.addOrReplaceChild("rear_leg_right", CubeListBuilder.create(), PartPose.offset(16.0F, -6.0F, 30.0F));
		PartDefinition rear_leg_right_thigh_placeholder_r1 = rear_leg_right.addOrReplaceChild("rear_leg_right_thigh_placeholder_r1",
				CubeListBuilder.create().texOffs(142, 170).addBox(-6.0F, -6.0F, -8.0F, 21.0F, 32.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0873F));
		PartDefinition rear_shin_right = rear_leg_right.addOrReplaceChild("rear_shin_right", CubeListBuilder.create(), PartPose.offset(5.0F, 22.0F, 6.0F));
		PartDefinition rear_leg_right_shin_placeholder_r1 = rear_shin_right.addOrReplaceChild("rear_leg_right_shin_placeholder_r1",
				CubeListBuilder.create().texOffs(348, 170).addBox(-8.0F, -2.0F, -8.0F, 15.0F, 28.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
		PartDefinition rear_paw_right = rear_shin_right.addOrReplaceChild("rear_paw_right", CubeListBuilder.create().texOffs(62, 236).addBox(-11.0F, -4.0F, -16.0F, 24.0F, 16.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -2.0F));
		PartDefinition front_paw_right2 = rear_paw_right.addOrReplaceChild("front_paw_right2",
				CubeListBuilder.create().texOffs(2, 459).addBox(-9.0F, 5.0F, -20.0F, 22.0F, 6.0F, 10.0F, new CubeDeformation(-0.1F)).texOffs(100, 459).addBox(-7.0F, 4.0F, -23.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)).texOffs(398, 428)
						.addBox(0.0F, 3.0F, -25.0F, 5.0F, 9.0F, 9.0F, new CubeDeformation(-0.1F)).texOffs(100, 459).addBox(7.0F, 4.0F, -23.0F, 5.0F, 8.0F, 7.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(-1.25F, 0.0F, -1.0F));
		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}