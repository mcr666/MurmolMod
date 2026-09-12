package net.mcr.murmol.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.mcr.murmol.entity.FeralCatEntity;
import net.mcr.murmol.client.model.animations.feral_catAnimation;
import net.mcr.murmol.client.model.Modelferal_cat;

import com.mojang.blaze3d.vertex.PoseStack;

public class FeralCatRenderer extends MobRenderer<FeralCatEntity, Modelferal_cat<FeralCatEntity>> {
	private final ResourceLocation entityTexture = ResourceLocation.parse("murmol:textures/entities/form_feral_cat_sp.png");

	public FeralCatRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(Modelferal_cat.LAYER_LOCATION)), 0.4f);
	}

	@Override
	protected void scale(FeralCatEntity entity, PoseStack poseStack, float f) {
		poseStack.scale(entity.getAgeScale(), entity.getAgeScale(), entity.getAgeScale());
	}

	@Override
	public ResourceLocation getTextureLocation(FeralCatEntity entity) {
		return entityTexture;
	}

	private static final class AnimatedModel extends Modelferal_cat<FeralCatEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<FeralCatEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(FeralCatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animate(entity.animationState0, feral_catAnimation.form_feral_common_attack, ageInTicks, 1f);
				this.animateWalk(feral_catAnimation.form_feral_common_walk, limbSwing, limbSwingAmount, 2f, 1f);
				this.animate(entity.animationState2, feral_catAnimation.form_feral_common_idle, ageInTicks, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(FeralCatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}