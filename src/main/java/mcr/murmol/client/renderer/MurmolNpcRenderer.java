package mcr.murmol.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import mcr.murmol.client.model.MurmolNpcModel;
import mcr.murmol.entity.MurmolNpcEntity;
import mcr.murmol.init.MurmolModModels;

/**
 * Murmol NPC 渲染：自定义 Blockbench 模型（玩家形骨架 + 尾巴，
 * 人形基础动画 + 尾部摇摆动画）+ 专属贴图 murmolnpc.png。
 */
public class MurmolNpcRenderer extends MobRenderer<MurmolNpcEntity, MurmolNpcModel> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("murmol", "textures/entity/murmolnpc.png");

	public MurmolNpcRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new MurmolNpcModel(ctx.bakeLayer(MurmolNpcModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(MurmolNpcEntity entity) {
		return TEXTURE;
	}
}
