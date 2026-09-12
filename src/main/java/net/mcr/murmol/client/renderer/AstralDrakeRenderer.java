package net.mcr.murmol.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.mcr.murmol.entity.AstralDrakeEntity;
import net.mcr.murmol.client.model.animations.astral_drakeAnimation;
import net.mcr.murmol.client.model.animations.AlfarAnimation;
import net.mcr.murmol.client.model.Modelastral_drake;

public class AstralDrakeRenderer extends MobRenderer<AstralDrakeEntity, Modelastral_drake<AstralDrakeEntity>> {
	private final ResourceLocation entityTexture = ResourceLocation.parse("murmol:textures/entities/astral_drake.png");

	public AstralDrakeRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(Modelastral_drake.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(AstralDrakeEntity entity) {
		return entityTexture;
	}

	private static final class AnimatedModel extends Modelastral_drake<AstralDrakeEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<AstralDrakeEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(AstralDrakeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animateWalk(astral_drakeAnimation.drake_fly, limbSwing, limbSwingAmount, 2f, 1f);
				this.animate(entity.animationState1, AlfarAnimation.alfar_idle, ageInTicks, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(AstralDrakeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}