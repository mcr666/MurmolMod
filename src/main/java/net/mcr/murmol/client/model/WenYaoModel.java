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
 * 文鳐（WenYao）形态整体替换模型。层位置与 WenyaoForm 的 wholeModelLayer（murmol/wenyao）一致。
 * 纯鱼形模型，不包装 PlayerModel：渲染时直接取消原版玩家模型，
 * 由 FeralFormRenderer 渲染本模型根骨骼并驱动 fish_idle/fish_moving_land/fish_moving_swim/fish_flying 动画。
 */
public class WenYaoModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("murmol", "wenyao"), "main");

	private final ModelPart body;

	public WenYaoModel(ModelPart root) {
		this.body = root.getChild("body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(4, 0).addBox(-4.0F, -6.0F, 0.0F, 8.0F, 6.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(4, 19).addBox(-3.0F, -5.5F, 13.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 24.0F, -6.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 27).addBox(-3.0F, -5.5F, -5.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 0).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(1, 32).addBox(-1.5F, -2.5F, 6.0F, 3.0F, 5.0F, 7.0F, new CubeDeformation(-0.3F))
		.texOffs(18, 47).addBox(-2.0F, -2.5F, 3.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -3.0F, 21.0F));

		PartDefinition right_fin_r1 = tail.addOrReplaceChild("right_fin_r1", CubeListBuilder.create().texOffs(38, 37).mirror().addBox(3.75F, 13.0F, 12.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.75F, 3.0F, -18.0F, 0.9599F, 0.0F, -1.8675F));

		PartDefinition left_fin_r1 = tail.addOrReplaceChild("left_fin_r1", CubeListBuilder.create().texOffs(38, 37).addBox(-4.75F, 13.0F, 12.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 3.0F, -18.0F, 0.9599F, 0.0F, 1.8675F));

		PartDefinition tail_fin = tail.addOrReplaceChild("tail_fin", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 11.75F, -0.1396F, 0.0F, 0.0F));

		PartDefinition Base_r1 = tail_fin.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-0.5F, -8.1668F, -2.1179F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 19).addBox(-0.5F, -6.1668F, 0.8821F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 45).addBox(-0.5F, -6.1668F, -2.1179F, 1.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.75F, 2.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition Base_r2 = tail_fin.addOrReplaceChild("Base_r2", CubeListBuilder.create().texOffs(34, 45).addBox(-0.5F, -2.5538F, -1.8296F, 1.0F, 8.0F, 3.0F, new CubeDeformation(-0.05F))
		.texOffs(0, 0).addBox(-0.5F, 5.3462F, -1.8296F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(0.0F, 0.25F, 2.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition right_item = tail.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 49).addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.75F, -24.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition left_item = tail.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(0, 49).mirror().addBox(0.0F, -2.0F, -9.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -5.25F, -3.0F));

		PartDefinition back_fin = body.addOrReplaceChild("back_fin", CubeListBuilder.create().texOffs(26, 37).addBox(-0.5F, 2.25F, -1.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 44).addBox(-0.5F, -1.75F, -2.5F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(28, 19).addBox(-1.0F, 1.75F, -2.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(14, 36).addBox(-1.0F, -1.25F, -3.5F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 5.0F, 1.0472F, 0.0F, 0.0F));

		PartDefinition left_fin = body.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(42, 11).addBox(-0.0614F, -2.1772F, -1.675F, 1.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.0F, 5.0F, 0.9599F, 0.0F, 1.8675F));

		PartDefinition right_fin = body.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(42, 11).mirror().addBox(-0.9386F, -2.1772F, -1.675F, 1.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -2.0F, 5.0F, 0.9599F, 0.0F, -1.8675F));

		PartDefinition LeftFlight = body.addOrReplaceChild("LeftFlight", CubeListBuilder.create().texOffs(50, 43).addBox(0.0F, -6.0F, 0.0F, 6.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -3.0F, 7.75F, -1.6143F, -0.0437F, -0.0019F));

		PartDefinition LeftSubFlight = LeftFlight.addOrReplaceChild("LeftSubFlight", CubeListBuilder.create().texOffs(50, 43).addBox(0.0F, -6.0F, 0.25F, 6.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -2.0F, 0.0F));

		PartDefinition RightFlight = body.addOrReplaceChild("RightFlight", CubeListBuilder.create().texOffs(50, 43).mirror().addBox(-6.0F, -6.0F, 0.0F, 6.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.5F, -3.0F, 7.75F, -1.6143F, 0.0437F, 0.0019F));

		PartDefinition RightSubFlight = RightFlight.addOrReplaceChild("RightSubFlight", CubeListBuilder.create().texOffs(50, 43).mirror().addBox(-6.0F, -6.0F, 0.25F, 6.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, -2.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
