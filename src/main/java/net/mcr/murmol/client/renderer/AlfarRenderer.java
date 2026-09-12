package net.mcr.murmol.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.mcr.murmol.entity.AlfarEntity;
import net.mcr.murmol.client.model.animations.AlfarAnimation;
import net.mcr.murmol.client.model.Modelforest_colossus;

import com.mojang.blaze3d.vertex.PoseStack;

public class AlfarRenderer extends MobRenderer<AlfarEntity, Modelforest_colossus<AlfarEntity>> {
	private final ResourceLocation entityTexture = ResourceLocation.parse("murmol:textures/entities/alfar.png");

	public AlfarRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(Modelforest_colossus.LAYER_LOCATION)), 2f);
	}

	@Override
	protected void scale(AlfarEntity entity, PoseStack poseStack, float f) {
		poseStack.scale(0.5f, 0.5f, 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(AlfarEntity entity) {
		return entityTexture;
	}

	private static final class AnimatedModel extends Modelforest_colossus<AlfarEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<AlfarEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(AlfarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animate(entity.animationState0, AlfarAnimation.alfar_bite, ageInTicks, 1f);
				this.animateWalk(AlfarAnimation.alfar_walk, limbSwing, limbSwingAmount, 2f, 2f);
				this.animate(entity.animationState2, AlfarAnimation.alfar_idle, ageInTicks, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(AlfarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}