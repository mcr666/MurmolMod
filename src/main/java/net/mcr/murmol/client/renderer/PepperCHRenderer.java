package net.mcr.murmol.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.mcr.murmol.procedures.PepperCHHuiFangTiaoJian3Procedure;
import net.mcr.murmol.procedures.PepperCHHuiFangTiaoJian2Procedure;
import net.mcr.murmol.entity.PepperCHEntity;
import net.mcr.murmol.client.model.animations.unknownAnimation;
import net.mcr.murmol.client.model.Modelunknown;

public class PepperCHRenderer extends MobRenderer<PepperCHEntity, Modelunknown<PepperCHEntity>> {
	private final ResourceLocation entityTexture = ResourceLocation.parse("murmol:textures/entities/tartaric_acid.png");

	public PepperCHRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(Modelunknown.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(PepperCHEntity entity) {
		return entityTexture;
	}

	private static final class AnimatedModel extends Modelunknown<PepperCHEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<PepperCHEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(PepperCHEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				if (PepperCHHuiFangTiaoJian2Procedure.execute(entity))
					this.animateWalk(unknownAnimation.WALK_ANIMATION, limbSwing, limbSwingAmount, 1.1f, 1f);
				this.animate(entity.animationState1, unknownAnimation.IDLE_ANIMATION, ageInTicks, 1f);
				if (PepperCHHuiFangTiaoJian3Procedure.execute(entity))
					this.animateWalk(unknownAnimation.RUN_ANIMATION, limbSwing, limbSwingAmount, 1f, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(PepperCHEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}