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

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelunknown<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("murmol", "modelunknown"), "main");
	public final ModelPart Root;
	public final ModelPart AllHead;
	public final ModelPart body;
	public final ModelPart bone10;
	public final ModelPart bone3;
	public final ModelPart fur3;
	public final ModelPart back;
	public final ModelPart backcorn3;
	public final ModelPart backcorn4;
	public final ModelPart backcorn;
	public final ModelPart backcorn2;
	public final ModelPart ElytraLocator;
	public final ModelPart back2;
	public final ModelPart fur4;
	public final ModelPart bone11;
	public final ModelPart Tail;
	public final ModelPart TailPrimary;
	public final ModelPart fur6;
	public final ModelPart TailSecondary;
	public final ModelPart fur5;
	public final ModelPart TailTertiary;
	public final ModelPart tailfur1;
	public final ModelPart TailQuaternary;
	public final ModelPart neck;
	public final ModelPart ViewLocator;
	public final ModelPart NeckFur;
	public final ModelPart Head;
	public final ModelPart aHead;
	public final ModelPart LeftHandLocator;
	public final ModelPart headfur2;
	public final ModelPart RightHandLocator;
	public final ModelPart CheekFur;
	public final ModelPart RightCheek;
	public final ModelPart LeftCheek;
	public final ModelPart RightEar;
	public final ModelPart RightEarPivot;
	public final ModelPart furRight;
	public final ModelPart LeftEar;
	public final ModelPart LeftEarPivot;
	public final ModelPart furLeft;
	public final ModelPart frontlegright;
	public final ModelPart flright;
	public final ModelPart bone2;
	public final ModelPart legfur2;
	public final ModelPart bone15;
	public final ModelPart PawBeans2;
	public final ModelPart frontlegleft;
	public final ModelPart flleft;
	public final ModelPart bone5;
	public final ModelPart legfur;
	public final ModelPart bone14;
	public final ModelPart PawBeans;
	public final ModelPart backlegleft;
	public final ModelPart bone;
	public final ModelPart bone4;
	public final ModelPart bone6;
	public final ModelPart legfur4;
	public final ModelPart bone13;
	public final ModelPart PawBeans3;
	public final ModelPart backlegright;
	public final ModelPart bone7;
	public final ModelPart bone8;
	public final ModelPart bone9;
	public final ModelPart legfur3;
	public final ModelPart bone12;
	public final ModelPart PawBeans4;
	public final ModelPart molang;

	public Modelunknown(ModelPart root) {
		this.Root = root.getChild("Root");
		this.AllHead = this.Root.getChild("AllHead");
		this.body = this.AllHead.getChild("body");
		this.bone10 = this.body.getChild("bone10");
		this.bone3 = this.bone10.getChild("bone3");
		this.fur3 = this.bone10.getChild("fur3");
		this.back = this.body.getChild("back");
		this.backcorn3 = this.back.getChild("backcorn3");
		this.backcorn4 = this.back.getChild("backcorn4");
		this.backcorn = this.back.getChild("backcorn");
		this.backcorn2 = this.back.getChild("backcorn2");
		this.ElytraLocator = this.back.getChild("ElytraLocator");
		this.back2 = this.back.getChild("back2");
		this.fur4 = this.back2.getChild("fur4");
		this.bone11 = this.back2.getChild("bone11");
		this.Tail = this.bone11.getChild("Tail");
		this.TailPrimary = this.Tail.getChild("TailPrimary");
		this.fur6 = this.TailPrimary.getChild("fur6");
		this.TailSecondary = this.TailPrimary.getChild("TailSecondary");
		this.fur5 = this.TailSecondary.getChild("fur5");
		this.TailTertiary = this.TailSecondary.getChild("TailTertiary");
		this.tailfur1 = this.TailTertiary.getChild("tailfur1");
		this.TailQuaternary = this.TailTertiary.getChild("TailQuaternary");
		this.neck = this.body.getChild("neck");
		this.ViewLocator = this.neck.getChild("ViewLocator");
		this.NeckFur = this.neck.getChild("NeckFur");
		this.Head = this.neck.getChild("Head");
		this.aHead = this.Head.getChild("aHead");
		this.LeftHandLocator = this.aHead.getChild("LeftHandLocator");
		this.headfur2 = this.aHead.getChild("headfur2");
		this.RightHandLocator = this.aHead.getChild("RightHandLocator");
		this.CheekFur = this.aHead.getChild("CheekFur");
		this.RightCheek = this.CheekFur.getChild("RightCheek");
		this.LeftCheek = this.CheekFur.getChild("LeftCheek");
		this.RightEar = this.aHead.getChild("RightEar");
		this.RightEarPivot = this.RightEar.getChild("RightEarPivot");
		this.furRight = this.RightEarPivot.getChild("furRight");
		this.LeftEar = this.aHead.getChild("LeftEar");
		this.LeftEarPivot = this.LeftEar.getChild("LeftEarPivot");
		this.furLeft = this.LeftEarPivot.getChild("furLeft");
		this.frontlegright = this.Root.getChild("frontlegright");
		this.flright = this.frontlegright.getChild("flright");
		this.bone2 = this.flright.getChild("bone2");
		this.legfur2 = this.bone2.getChild("legfur2");
		this.bone15 = this.bone2.getChild("bone15");
		this.PawBeans2 = this.bone15.getChild("PawBeans2");
		this.frontlegleft = this.Root.getChild("frontlegleft");
		this.flleft = this.frontlegleft.getChild("flleft");
		this.bone5 = this.flleft.getChild("bone5");
		this.legfur = this.bone5.getChild("legfur");
		this.bone14 = this.bone5.getChild("bone14");
		this.PawBeans = this.bone14.getChild("PawBeans");
		this.backlegleft = this.Root.getChild("backlegleft");
		this.bone = this.backlegleft.getChild("bone");
		this.bone4 = this.bone.getChild("bone4");
		this.bone6 = this.bone4.getChild("bone6");
		this.legfur4 = this.bone6.getChild("legfur4");
		this.bone13 = this.bone6.getChild("bone13");
		this.PawBeans3 = this.bone13.getChild("PawBeans3");
		this.backlegright = this.Root.getChild("backlegright");
		this.bone7 = this.backlegright.getChild("bone7");
		this.bone8 = this.bone7.getChild("bone8");
		this.bone9 = this.bone8.getChild("bone9");
		this.legfur3 = this.bone9.getChild("legfur3");
		this.bone12 = this.bone9.getChild("bone12");
		this.PawBeans4 = this.bone12.getChild("PawBeans4");
		this.molang = this.Root.getChild("molang");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition Root = partdefinition.addOrReplaceChild("Root", CubeListBuilder.create(), PartPose.offset(0.0F, 7.75F, -4.0F));
		PartDefinition AllHead = Root.addOrReplaceChild("AllHead", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition body = AllHead.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition bone10 = body.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, -1.0F));
		PartDefinition bone10_r1 = bone10.addOrReplaceChild("bone10_r1", CubeListBuilder.create().texOffs(53, 90).mirror().addBox(-3.0F, -1.0588F, -3.8522F, 6.0F, 8.75F, 6.25F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));
		PartDefinition bone3 = bone10.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, 3.5F, -5.0F));
		PartDefinition bone3_r1 = bone3.addOrReplaceChild("bone3_r1", CubeListBuilder.create().texOffs(78, 0).addBox(-2.0F, -3.444F, -3.2886F, 4.0F, 3.5F, 3.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, 0.0F));
		PartDefinition fur3 = bone10.addOrReplaceChild("fur3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.5F, 3.75F, -1.0734F, 0.0F, 0.0F));
		PartDefinition fur3_r1 = fur3.addOrReplaceChild("fur3_r1", CubeListBuilder.create().texOffs(18, 48).addBox(-1.0F, -2.0F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 5.0F, -2.4F, -0.2356F, 0.0F, 0.0F));
		PartDefinition fur3_r2 = fur3.addOrReplaceChild("fur3_r2", CubeListBuilder.create().texOffs(18, 43).addBox(-1.0F, -4.0F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 5.0F, -1.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition back = body.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, -1.25F));
		PartDefinition back_r1 = back.addOrReplaceChild("back_r1", CubeListBuilder.create().texOffs(31, 61).addBox(-3.25F, -0.5F, -1.0F, 6.5F, 9.0F, 13.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
		PartDefinition backcorn3 = back.addOrReplaceChild("backcorn3", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, 2.0F, 4.0F, 0.0F, 0.0698F, 0.0F));
		PartDefinition backcorn3_r1 = backcorn3.addOrReplaceChild("backcorn3_r1", CubeListBuilder.create().texOffs(43, 47).addBox(-0.4F, -1.2F, -0.4F, 0.8F, 1.2F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -6.6F, 5.25F, -0.8727F, 0.0F, 0.0F));
		PartDefinition backcorn3_r2 = backcorn3.addOrReplaceChild("backcorn3_r2", CubeListBuilder.create().texOffs(40, 45).addBox(-0.7F, -3.0F, -0.7F, 1.4F, 3.0F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -4.0F, 4.05F, -0.4363F, 0.0F, 0.0F));
		PartDefinition backcorn3_r3 = backcorn3.addOrReplaceChild("backcorn3_r3", CubeListBuilder.create().texOffs(37, 43).addBox(-1.0F, -2.3F, -1.0F, 2.0F, 2.3F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -2.5F, 3.65F, -0.2618F, 0.0F, 0.0F));
		PartDefinition backcorn4 = back.addOrReplaceChild("backcorn4", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.75F, 2.0F, 4.0F, 0.0F, -0.0698F, 0.0F));
		PartDefinition backcorn4_r1 = backcorn4.addOrReplaceChild("backcorn4_r1", CubeListBuilder.create().texOffs(43, 47).addBox(-0.4F, -1.2F, -0.4F, 0.8F, 1.2F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -6.6F, 5.25F, -0.8727F, 0.0F, 0.0F));
		PartDefinition backcorn4_r2 = backcorn4.addOrReplaceChild("backcorn4_r2", CubeListBuilder.create().texOffs(40, 45).addBox(-0.7F, -3.0F, -0.7F, 1.4F, 3.0F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -4.0F, 4.05F, -0.4363F, 0.0F, 0.0F));
		PartDefinition backcorn4_r3 = backcorn4.addOrReplaceChild("backcorn4_r3", CubeListBuilder.create().texOffs(37, 43).addBox(-1.0F, -2.3F, -1.0F, 2.0F, 2.3F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -2.5F, 3.65F, -0.2618F, 0.0F, 0.0F));
		PartDefinition backcorn = back.addOrReplaceChild("backcorn", CubeListBuilder.create().texOffs(39, 45).addBox(0.3F, -2.75F, 2.45F, 2.4F, 2.0F, 2.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, 0.0F, 0.0698F, 0.0F));
		PartDefinition backcorn_r1 = backcorn.addOrReplaceChild("backcorn_r1", CubeListBuilder.create().texOffs(43, 47).addBox(-0.4F, -1.2F, -0.4F, 0.8F, 1.2F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -6.6F, 5.25F, -0.8727F, 0.0F, 0.0F));
		PartDefinition backcorn_r2 = backcorn.addOrReplaceChild("backcorn_r2", CubeListBuilder.create().texOffs(40, 45).addBox(-0.7F, -3.0F, -0.7F, 1.4F, 3.0F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -4.0F, 4.05F, -0.4363F, 0.0F, 0.0F));
		PartDefinition backcorn_r3 = backcorn.addOrReplaceChild("backcorn_r3", CubeListBuilder.create().texOffs(37, 43).addBox(-1.0F, -2.3F, -1.0F, 2.0F, 2.3F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -2.5F, 3.65F, -0.2618F, 0.0F, 0.0F));
		PartDefinition backcorn2 = back.addOrReplaceChild("backcorn2", CubeListBuilder.create().texOffs(39, 45).addBox(0.3F, -2.75F, 2.45F, 2.4F, 2.0F, 2.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.85F, 0.0F, 0.0F, 0.0F, -0.0698F, 0.0F));
		PartDefinition backcorn2_r1 = backcorn2.addOrReplaceChild("backcorn2_r1", CubeListBuilder.create().texOffs(43, 47).addBox(-0.4F, -1.2F, -0.4F, 0.8F, 1.2F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -6.6F, 5.25F, -0.8727F, 0.0F, 0.0F));
		PartDefinition backcorn2_r2 = backcorn2.addOrReplaceChild("backcorn2_r2", CubeListBuilder.create().texOffs(40, 45).addBox(-0.7F, -3.0F, -0.7F, 1.4F, 3.0F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -4.0F, 4.05F, -0.4363F, 0.0F, 0.0F));
		PartDefinition backcorn2_r3 = backcorn2.addOrReplaceChild("backcorn2_r3", CubeListBuilder.create().texOffs(37, 43).addBox(-1.0F, -2.3F, -1.0F, 2.0F, 2.3F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -2.5F, 3.65F, -0.2618F, 0.0F, 0.0F));
		PartDefinition ElytraLocator = back.addOrReplaceChild("ElytraLocator", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.25F, 1.0F, 1.3526F, 0.0F, 0.0F));
		PartDefinition back2 = back.addOrReplaceChild("back2", CubeListBuilder.create(), PartPose.offset(0.0F, -1.25F, 12.75F));
		PartDefinition back2_r1 = back2.addOrReplaceChild("back2_r1", CubeListBuilder.create().texOffs(36, 82).addBox(-2.5F, -0.25F, -1.0F, 5.0F, 8.75F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));
		PartDefinition fur4 = back2.addOrReplaceChild("fur4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, 2.5F, -0.9338F, 0.0F, 0.0F));
		PartDefinition fur4_r1 = fur4.addOrReplaceChild("fur4_r1", CubeListBuilder.create().texOffs(15, 48).addBox(-1.5F, -2.0F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 3.75F, -2.15F, -0.2356F, 0.0F, 0.0F));
		PartDefinition fur4_r2 = fur4.addOrReplaceChild("fur4_r2", CubeListBuilder.create().texOffs(15, 43).addBox(-1.5F, -4.0F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 3.75F, -1.25F, -0.2356F, 0.0F, 0.0F));
		PartDefinition fur4_r3 = fur4.addOrReplaceChild("fur4_r3", CubeListBuilder.create().texOffs(15, 43).addBox(-1.5F, -2.0F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.75F, 0.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition bone11 = back2.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, 3.0F));
		PartDefinition Tail = bone11.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 2.25F, -0.1F, -0.1745F, 0.0F, 0.0F));
		PartDefinition TailPrimary = Tail.addOrReplaceChild("TailPrimary", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.1F, 0.1309F, 0.0F, 0.0F));
		PartDefinition TailPrimary_r1 = TailPrimary.addOrReplaceChild("TailPrimary_r1", CubeListBuilder.create().texOffs(41, 86).mirror().addBox(-2.0F, 0.75F, -1.5F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, 0.0F));
		PartDefinition fur6 = TailPrimary.addOrReplaceChild("fur6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.5F, 2.5F, -0.9338F, 0.0F, 0.0F));
		PartDefinition fur6_r1 = fur6.addOrReplaceChild("fur6_r1", CubeListBuilder.create().texOffs(15, 43).addBox(-1.5F, -4.0F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 2.0F, 0.75F, -0.2356F, 0.0F, 0.0F));
		PartDefinition fur6_r2 = fur6.addOrReplaceChild("fur6_r2", CubeListBuilder.create().texOffs(15, 48).addBox(-1.5F, -2.0F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 1.25F, -0.15F, -0.2356F, 0.0F, 0.0F));
		PartDefinition TailSecondary = TailPrimary.addOrReplaceChild("TailSecondary", CubeListBuilder.create(), PartPose.offset(0.0F, 1.25F, 5.6F));
		PartDefinition TailSecondary_r1 = TailSecondary.addOrReplaceChild("TailSecondary_r1", CubeListBuilder.create().texOffs(41, 63).mirror().addBox(-2.5F, -0.45F, -2.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.7F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 1.4835F, 0.0F, 0.0F));
		PartDefinition fur5 = TailSecondary.addOrReplaceChild("fur5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.75F, 5.9F, -0.9338F, 0.0F, 0.0F));
		PartDefinition fur5_r1 = fur5.addOrReplaceChild("fur5_r1", CubeListBuilder.create().texOffs(15, 48).addBox(-1.5F, -1.7899F, 0.562F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 4.0F, -2.65F, -0.2356F, 0.0F, 0.0F));
		PartDefinition fur5_r2 = fur5.addOrReplaceChild("fur5_r2", CubeListBuilder.create().texOffs(15, 43).addBox(-1.5F, -3.7899F, 0.562F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 4.0F, -1.25F, -0.2356F, 0.0F, 0.0F));
		PartDefinition fur5_r3 = fur5.addOrReplaceChild("fur5_r3", CubeListBuilder.create().texOffs(15, 43).addBox(-1.5F, -1.7899F, 0.562F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.75F, 0.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition TailTertiary = TailSecondary.addOrReplaceChild("TailTertiary", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, 7.1F));
		PartDefinition TailTertiary_r1 = TailTertiary.addOrReplaceChild("TailTertiary_r1", CubeListBuilder.create().texOffs(41, 64).mirror().addBox(-2.0F, -0.7F, -1.95F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.6F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 1.8326F, 0.0F, 0.0F));
		PartDefinition tailfur1 = TailTertiary.addOrReplaceChild("tailfur1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.0F, 2.75F, -0.7418F, 0.0F, 0.0F));
		PartDefinition tailfur1_r1 = tailfur1.addOrReplaceChild("tailfur1_r1", CubeListBuilder.create().texOffs(17, 48).addBox(-1.5F, -1.6865F, 0.5116F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.5F, -0.6F, -0.2356F, 0.0F, 0.0F));
		PartDefinition TailQuaternary = TailTertiary.addOrReplaceChild("TailQuaternary", CubeListBuilder.create(), PartPose.offset(0.0F, -0.6F, 1.55F));
		PartDefinition TailQuaternary_r1 = TailQuaternary.addOrReplaceChild("TailQuaternary_r1", CubeListBuilder.create().texOffs(37, 63).addBox(-1.0F, -4.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, -0.25F, 7.25F, 0.7679F, 0.0F, 0.0F));
		PartDefinition TailQuaternary_r2 = TailQuaternary.addOrReplaceChild("TailQuaternary_r2", CubeListBuilder.create().texOffs(41, 86).mirror().addBox(-2.0F, -1.2F, -1.95F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, -0.7F, 3.0F, 2.0071F, 0.0F, 0.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, -2.5F));
		PartDefinition neck_r1 = neck.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(53, 63).mirror().addBox(-2.9F, -2.4189F, -5.8157F, 5.8F, 6.55F, 4.25F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.5236F, 0.0F, 0.0F));
		PartDefinition ViewLocator = neck.addOrReplaceChild("ViewLocator", CubeListBuilder.create(), PartPose.offset(0.0F, 3.25F, -6.0F));
		PartDefinition NeckFur = neck.addOrReplaceChild("NeckFur", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.75F, -4.4F, 0.4363F, 0.0F, 0.0F));
		PartDefinition NeckFur_r1 = NeckFur.addOrReplaceChild("NeckFur_r1", CubeListBuilder.create().texOffs(40, 79).addBox(-1.8F, -2.5F, 0.0F, 4.6F, 2.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 5.0F, -2.4F, -0.2356F, 0.0F, 0.0F));
		PartDefinition NeckFur_r2 = NeckFur.addOrReplaceChild("NeckFur_r2", CubeListBuilder.create().texOffs(40, 79).addBox(-1.8F, -2.5F, 0.0F, 4.6F, 2.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition Head = neck.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.75F, -2.85F));
		PartDefinition aHead = Head.addOrReplaceChild("aHead", CubeListBuilder.create().texOffs(0, 16).addBox(-3.8F, -6.9671F, 1.5226F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(72, 60)
				.addBox(-1.3F, -1.9671F, 0.1226F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(87, 77).addBox(-0.8F, 0.0329F, 0.7226F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 1.2171F, -6.9226F));
		PartDefinition aHead_r1 = aHead.addOrReplaceChild("aHead_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-1.0F, -29.625F, -0.95F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7F, 27.0329F, 6.1226F, 0.1745F, 0.0F, 0.0F));
		PartDefinition LeftHandLocator = aHead.addOrReplaceChild("LeftHandLocator", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2F, -7.7171F, 9.4726F, 3.0543F, 3.1416F, 0.0F));
		PartDefinition headfur2 = aHead.addOrReplaceChild("headfur2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2F, -7.7171F, 7.7726F, -1.0036F, 0.0F, 0.0F));
		PartDefinition headfur2_r1 = headfur2.addOrReplaceChild("headfur2_r1", CubeListBuilder.create().texOffs(60, 65).addBox(-1.0F, -2.0F, 0.0F, 3.0F, 2.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 5.0F, -2.4F, -0.2356F, 0.0F, 0.0F));
		PartDefinition headfur2_r2 = headfur2.addOrReplaceChild("headfur2_r2", CubeListBuilder.create().texOffs(60, 65).addBox(-1.0F, -4.0F, 0.0F, 3.0F, 2.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 5.0F, -1.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition headfur2_r3 = headfur2.addOrReplaceChild("headfur2_r3", CubeListBuilder.create().texOffs(60, 65).addBox(-1.0F, -2.0F, 0.0F, 3.0F, 2.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 3.75F, -1.75F, -0.2356F, 0.0F, 0.0F));
		PartDefinition RightHandLocator = aHead.addOrReplaceChild("RightHandLocator", CubeListBuilder.create(), PartPose.offset(0.2F, 0.0329F, 0.5226F));
		PartDefinition CheekFur = aHead.addOrReplaceChild("CheekFur", CubeListBuilder.create(), PartPose.offset(-1.8F, 11.0329F, -7.4774F));
		PartDefinition RightCheek = CheekFur.addOrReplaceChild("RightCheek", CubeListBuilder.create(), PartPose.offset(-1.0F, 1.25F, 0.0F));
		PartDefinition RightCheek_r1 = RightCheek.addOrReplaceChild("RightCheek_r1", CubeListBuilder.create().texOffs(34, 85).mirror().addBox(-0.45F, -2.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-0.85F, -12.0F, 10.2F, -0.1047F, -0.4451F, 0.0F));
		PartDefinition RightCheek_r2 = RightCheek.addOrReplaceChild("RightCheek_r2", CubeListBuilder.create().texOffs(34, 85).mirror().addBox(-0.45F, -3.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-0.95F, -11.9F, 11.5F, 0.1309F, -0.3403F, 0.0F));
		PartDefinition LeftCheek = CheekFur.addOrReplaceChild("LeftCheek", CubeListBuilder.create(), PartPose.offset(5.0F, 1.25F, 0.0F));
		PartDefinition LeftCheek_r1 = LeftCheek.addOrReplaceChild("LeftCheek_r1", CubeListBuilder.create().texOffs(34, 85).addBox(0.45F, -3.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.95F, -11.9F, 11.5F, 0.1309F, 0.3403F, 0.0F));
		PartDefinition LeftCheek_r2 = LeftCheek.addOrReplaceChild("LeftCheek_r2", CubeListBuilder.create().texOffs(34, 85).addBox(0.45F, -2.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.85F, -12.0F, 10.2F, -0.1047F, 0.4451F, 0.0F));
		PartDefinition RightEar = aHead.addOrReplaceChild("RightEar", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.9F, -6.7671F, 4.4726F, -0.2618F, 0.0873F, -0.3054F));
		PartDefinition RightEarPivot = RightEar.addOrReplaceChild("RightEarPivot",
				CubeListBuilder.create().texOffs(64, 42).addBox(-2.9F, -2.2F, -1.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(3, 65).addBox(-2.2959F, 0.1264F, -0.3118F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.04F)).texOffs(2, 67)
						.addBox(-1.8998F, -3.1579F, -1.0187F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(2, 67).addBox(-0.8998F, -4.1579F, -1.0187F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(5, 67)
						.addBox(-1.2998F, -1.8079F, -0.3187F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.04F)),
				PartPose.offsetAndRotation(0.5F, -1.15F, 0.0F, -0.1309F, 0.5236F, -0.3491F));
		PartDefinition furRight = RightEarPivot.addOrReplaceChild("furRight", CubeListBuilder.create(), PartPose.offset(-0.5F, 1.75F, 0.3F));
		PartDefinition furRight_r1 = furRight.addOrReplaceChild("furRight_r1", CubeListBuilder.create().texOffs(41, 66).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)),
				PartPose.offsetAndRotation(-0.9F, -1.1F, -0.9F, 0.0F, -1.5708F, 0.0F));
		PartDefinition furRight_r2 = furRight.addOrReplaceChild("furRight_r2", CubeListBuilder.create().texOffs(41, 63).addBox(-1.0F, -2.0F, -1.0415F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)),
				PartPose.offsetAndRotation(0.2F, -1.1F, -0.3F, 0.0F, 0.8727F, 0.0F));
		PartDefinition furRight_r3 = furRight.addOrReplaceChild("furRight_r3", CubeListBuilder.create().texOffs(41, 60).addBox(-1.0F, -2.0F, -1.3415F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)),
				PartPose.offsetAndRotation(-1.2F, -1.1F, -0.1F, 0.0F, -0.8727F, 0.0F));
		PartDefinition furRight_r4 = furRight.addOrReplaceChild("furRight_r4", CubeListBuilder.create().texOffs(41, 66).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)),
				PartPose.offsetAndRotation(-0.9F, -0.7F, -1.2F, 0.0F, -1.5708F, 0.0F));
		PartDefinition furRight_r5 = furRight.addOrReplaceChild("furRight_r5", CubeListBuilder.create().texOffs(41, 66).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)),
				PartPose.offsetAndRotation(-0.9F, -0.3F, -1.5F, 0.0F, -1.5708F, 0.0F));
		PartDefinition furRight_r6 = furRight.addOrReplaceChild("furRight_r6", CubeListBuilder.create().texOffs(41, 63).addBox(-1.0F, -2.0F, -0.9878F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)),
				PartPose.offsetAndRotation(0.3F, -0.7F, -0.5F, 0.0F, 0.8727F, 0.0F));
		PartDefinition furRight_r7 = furRight.addOrReplaceChild("furRight_r7", CubeListBuilder.create().texOffs(41, 63).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)),
				PartPose.offsetAndRotation(0.4F, -0.2F, -0.6F, 0.0F, 0.8727F, 0.0F));
		PartDefinition furRight_r8 = furRight.addOrReplaceChild("furRight_r8", CubeListBuilder.create().texOffs(41, 60).addBox(-1.0F, -2.0F, -1.2876F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)),
				PartPose.offsetAndRotation(-1.3F, -0.7F, -0.3F, 0.0F, -0.8727F, 0.0F));
		PartDefinition furRight_r9 = furRight.addOrReplaceChild("furRight_r9", CubeListBuilder.create().texOffs(41, 60).addBox(-1.0F, -2.0F, -1.3F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)),
				PartPose.offsetAndRotation(-1.4F, -0.2F, -0.4F, 0.0F, -0.8727F, 0.0F));
		PartDefinition LeftEar = aHead.addOrReplaceChild("LeftEar", CubeListBuilder.create(), PartPose.offsetAndRotation(3.3F, -6.6671F, 4.4726F, -0.2618F, -0.0873F, 0.3054F));
		PartDefinition LeftEarPivot = LeftEar.addOrReplaceChild("LeftEarPivot",
				CubeListBuilder.create().texOffs(54, 42).addBox(-1.1F, -2.2F, -1.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(3, 65).addBox(-0.6041F, 0.2264F, -0.3118F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.04F)).texOffs(2, 67)
						.addBox(-1.0502F, -3.1579F, -1.0187F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(2, 67).addBox(-1.0502F, -4.1579F, -1.0187F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(5, 64)
						.addBox(-0.6502F, -1.8079F, -0.3187F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.04F)),
				PartPose.offsetAndRotation(-0.5F, -1.25F, 0.0F, -0.1309F, -0.5236F, 0.3491F));
		PartDefinition furLeft = LeftEarPivot.addOrReplaceChild("furLeft", CubeListBuilder.create(), PartPose.offset(0.5F, 1.65F, 0.3F));
		PartDefinition furLeft_r1 = furLeft.addOrReplaceChild("furLeft_r1", CubeListBuilder.create().texOffs(41, 66).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)).mirror(false),
				PartPose.offsetAndRotation(0.9F, -1.1F, -0.9F, 0.0F, 1.5708F, 0.0F));
		PartDefinition furLeft_r2 = furLeft.addOrReplaceChild("furLeft_r2", CubeListBuilder.create().texOffs(41, 63).mirror().addBox(-1.0F, -2.0F, -1.0415F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)).mirror(false),
				PartPose.offsetAndRotation(-0.2F, -1.1F, -0.3F, 0.0F, -0.8727F, 0.0F));
		PartDefinition furLeft_r3 = furLeft.addOrReplaceChild("furLeft_r3", CubeListBuilder.create().texOffs(41, 60).mirror().addBox(-1.0F, -2.0F, -1.3415F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3002F)).mirror(false),
				PartPose.offsetAndRotation(1.2F, -1.1F, -0.1F, 0.0F, 0.8727F, 0.0F));
		PartDefinition furLeft_r4 = furLeft.addOrReplaceChild("furLeft_r4", CubeListBuilder.create().texOffs(41, 66).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)).mirror(false),
				PartPose.offsetAndRotation(0.9F, -0.7F, -1.2F, 0.0F, 1.5708F, 0.0F));
		PartDefinition furLeft_r5 = furLeft.addOrReplaceChild("furLeft_r5", CubeListBuilder.create().texOffs(41, 66).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)).mirror(false),
				PartPose.offsetAndRotation(0.9F, -0.3F, -1.5F, 0.0F, 1.5708F, 0.0F));
		PartDefinition furLeft_r6 = furLeft.addOrReplaceChild("furLeft_r6", CubeListBuilder.create().texOffs(41, 63).mirror().addBox(-1.0F, -2.0F, -0.9878F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)).mirror(false),
				PartPose.offsetAndRotation(-0.3F, -0.7F, -0.5F, 0.0F, -0.8727F, 0.0F));
		PartDefinition furLeft_r7 = furLeft.addOrReplaceChild("furLeft_r7", CubeListBuilder.create().texOffs(41, 63).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)).mirror(false),
				PartPose.offsetAndRotation(-0.4F, -0.2F, -0.6F, 0.0F, -0.8727F, 0.0F));
		PartDefinition furLeft_r8 = furLeft.addOrReplaceChild("furLeft_r8", CubeListBuilder.create().texOffs(41, 60).mirror().addBox(-1.0F, -2.0F, -1.2876F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3001F)).mirror(false),
				PartPose.offsetAndRotation(1.3F, -0.7F, -0.3F, 0.0F, 0.8727F, 0.0F));
		PartDefinition furLeft_r9 = furLeft.addOrReplaceChild("furLeft_r9", CubeListBuilder.create().texOffs(41, 60).mirror().addBox(-1.0F, -2.0F, -1.3F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.3F)).mirror(false),
				PartPose.offsetAndRotation(1.4F, -0.2F, -0.4F, 0.0F, 0.8727F, 0.0F));
		PartDefinition frontlegright = Root.addOrReplaceChild("frontlegright", CubeListBuilder.create(), PartPose.offset(-2.25F, 2.75F, -3.5F));
		PartDefinition flright = frontlegright.addOrReplaceChild("flright", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition flright_r1 = flright.addOrReplaceChild("flright_r1", CubeListBuilder.create().texOffs(41, 90).addBox(-3.15F, -6.2567F, -2.6025F, 3.55F, 8.0F, 3.45F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.0543F, 0.0F, 0.0F));
		PartDefinition bone2 = flright.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(42, 86).addBox(-1.65F, -0.75F, -0.95F, 3.05F, 7.5F, 3.95F, new CubeDeformation(0.0F)), PartPose.offset(-1.25F, 6.0F, 0.75F));
		PartDefinition legfur2 = bone2.addOrReplaceChild("legfur2", CubeListBuilder.create(), PartPose.offsetAndRotation(1.75F, -2.5F, 4.5F, 0.4363F, 0.0F, 0.0F));
		PartDefinition legfur2_r1 = legfur2.addOrReplaceChild("legfur2_r1", CubeListBuilder.create().texOffs(42, 81).addBox(-2.5F, -1.5F, 0.0F, 2.25F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));
		PartDefinition legfur2_r2 = legfur2.addOrReplaceChild("legfur2_r2", CubeListBuilder.create().texOffs(42, 81).addBox(-2.5F, -1.5F, 0.0F, 2.25F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition bone15 = bone2.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(6, 90).addBox(-1.75F, -0.5F, -3.0F, 3.25F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.25F));
		PartDefinition PawBeans2 = bone15.addOrReplaceChild(
				"PawBeans2", CubeListBuilder.create().texOffs(90, 65).addBox(-0.37F, -0.775F, -0.625F, 2.0F, 0.75F, 2.0F, new CubeDeformation(-0.1F)).texOffs(90, 63).addBox(-1.025F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F))
						.texOffs(90, 59).addBox(0.12F, -0.775F, -1.875F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)).texOffs(90, 61).addBox(1.3F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(-0.75F, 1.75F, -1.0F));
		PartDefinition frontlegleft = Root.addOrReplaceChild("frontlegleft", CubeListBuilder.create(), PartPose.offset(2.25F, 2.75F, -3.5F));
		PartDefinition flleft = frontlegleft.addOrReplaceChild("flleft", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition flleft_r1 = flleft.addOrReplaceChild("flleft_r1", CubeListBuilder.create().texOffs(41, 90).addBox(-0.4F, -6.2567F, -2.6025F, 3.55F, 8.0F, 3.45F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.0543F, 0.0F, 0.0F));
		PartDefinition bone5 = flleft.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(42, 86).addBox(-1.4F, -0.75F, -0.95F, 3.05F, 7.5F, 3.95F, new CubeDeformation(0.0F)), PartPose.offset(1.25F, 6.0F, 0.75F));
		PartDefinition legfur = bone5.addOrReplaceChild("legfur", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -2.5F, 4.5F, 0.4363F, 0.0F, 0.0F));
		PartDefinition legfur_r1 = legfur.addOrReplaceChild("legfur_r1", CubeListBuilder.create().texOffs(42, 81).addBox(-2.5F, -1.5F, 0.0F, 2.25F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));
		PartDefinition legfur_r2 = legfur.addOrReplaceChild("legfur_r2", CubeListBuilder.create().texOffs(42, 81).addBox(-2.5F, -1.5F, 0.0F, 2.25F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition bone14 = bone5.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(7, 90).addBox(-1.5F, -0.5F, -3.0F, 3.25F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.25F));
		PartDefinition PawBeans = bone14.addOrReplaceChild(
				"PawBeans", CubeListBuilder.create().texOffs(90, 65).addBox(-0.37F, -0.775F, -0.625F, 2.0F, 0.75F, 2.0F, new CubeDeformation(-0.1F)).texOffs(90, 63).addBox(-1.025F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F))
						.texOffs(90, 59).addBox(0.12F, -0.775F, -1.875F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)).texOffs(90, 61).addBox(1.3F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(-0.5F, 1.75F, -1.0F));
		PartDefinition backlegleft = Root.addOrReplaceChild("backlegleft", CubeListBuilder.create(), PartPose.offset(2.75F, 0.0F, 13.25F));
		PartDefinition bone = backlegleft.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-1.75F, 0.5F, 2.0F));
		PartDefinition bone_r1 = bone.addOrReplaceChild("bone_r1", CubeListBuilder.create().texOffs(41, 90).addBox(0.0F, -8.1277F, -1.4331F, 3.25F, 9.75F, 6.75F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.9671F, 0.0F, 0.0F));
		PartDefinition bone4 = bone.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(1.5F, 7.0F, -6.25F));
		PartDefinition bone4_r1 = bone4.addOrReplaceChild("bone4_r1", CubeListBuilder.create().texOffs(72, 83).addBox(-1.65F, -2.8934F, -0.0752F, 3.55F, 3.0F, 10.95F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));
		PartDefinition bone6 = bone4.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(42, 87).addBox(-1.9F, -1.0F, -1.45F, 4.05F, 6.25F, 3.45F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.75F, 9.5F));
		PartDefinition legfur4 = bone6.addOrReplaceChild("legfur4", CubeListBuilder.create(), PartPose.offsetAndRotation(2.25F, -2.75F, 3.5F, 0.4363F, 0.0F, 0.0F));
		PartDefinition legfur4_r1 = legfur4.addOrReplaceChild("legfur4_r1", CubeListBuilder.create().texOffs(42, 81).addBox(-3.0F, -1.5F, 0.0F, 2.75F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));
		PartDefinition legfur4_r2 = legfur4.addOrReplaceChild("legfur4_r2", CubeListBuilder.create().texOffs(42, 81).addBox(-3.0F, -1.5F, 0.0F, 2.75F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition bone13 = bone6.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(2, 90).addBox(-1.75F, -0.75F, -3.25F, 3.75F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.25F, 4.75F, -0.25F));
		PartDefinition PawBeans3 = bone13.addOrReplaceChild(
				"PawBeans3", CubeListBuilder.create().texOffs(90, 65).addBox(-0.37F, -0.775F, -0.625F, 2.0F, 0.75F, 2.0F, new CubeDeformation(-0.1F)).texOffs(90, 63).addBox(-1.025F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F))
						.texOffs(90, 59).addBox(0.12F, -0.775F, -1.875F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)).texOffs(90, 61).addBox(1.3F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(-0.5F, 1.5F, -1.25F));
		PartDefinition backlegright = Root.addOrReplaceChild("backlegright", CubeListBuilder.create(), PartPose.offset(-2.75F, 0.0F, 13.25F));
		PartDefinition bone7 = backlegright.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(1.75F, 0.5F, 2.0F));
		PartDefinition bone7_r1 = bone7.addOrReplaceChild("bone7_r1", CubeListBuilder.create().texOffs(41, 90).addBox(-3.25F, -8.1277F, -1.4331F, 3.25F, 9.75F, 6.75F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.9671F, 0.0F, 0.0F));
		PartDefinition bone8 = bone7.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(-1.5F, 7.0F, -6.25F));
		PartDefinition bone8_r1 = bone8.addOrReplaceChild("bone8_r1", CubeListBuilder.create().texOffs(72, 83).addBox(-1.9F, -2.8934F, -0.0752F, 3.55F, 3.0F, 10.95F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));
		PartDefinition bone9 = bone8.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(42, 87).addBox(-2.15F, -1.0F, -1.45F, 4.05F, 6.25F, 3.45F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.75F, 9.5F));
		PartDefinition legfur3 = bone9.addOrReplaceChild("legfur3", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -2.75F, 3.5F, 0.4363F, 0.0F, 0.0F));
		PartDefinition legfur3_r1 = legfur3.addOrReplaceChild("legfur3_r1", CubeListBuilder.create().texOffs(42, 81).addBox(-3.0F, -1.5F, 0.0F, 2.75F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 4.0F, -3.15F, -0.2356F, 0.0F, 0.0F));
		PartDefinition legfur3_r2 = legfur3.addOrReplaceChild("legfur3_r2", CubeListBuilder.create().texOffs(42, 81).addBox(-3.0F, -1.5F, 0.0F, 2.75F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 2.5F, -2.5F, -0.2356F, 0.0F, 0.0F));
		PartDefinition bone12 = bone9.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(9, 90).addBox(-2.0F, -0.75F, -3.25F, 3.75F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 4.75F, -0.25F));
		PartDefinition PawBeans4 = bone12.addOrReplaceChild(
				"PawBeans4", CubeListBuilder.create().texOffs(90, 65).addBox(-0.37F, -0.775F, -0.625F, 2.0F, 0.75F, 2.0F, new CubeDeformation(-0.1F)).texOffs(90, 63).addBox(-1.025F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F))
						.texOffs(90, 59).addBox(0.12F, -0.775F, -1.875F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)).texOffs(90, 61).addBox(1.3F, -0.775F, -1.625F, 1.0F, 0.75F, 1.0F, new CubeDeformation(-0.1F)),
				PartPose.offset(-0.75F, 1.5F, -1.25F));
		PartDefinition molang = Root.addOrReplaceChild("molang", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 96, 96);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		Root.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}