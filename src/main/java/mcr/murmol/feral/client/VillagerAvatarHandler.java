package mcr.murmol.feral.client;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import mcr.murmol.network.MurmolModVariables;

/**
 * 麻将 AI 形象的村民模式渲染（客户端）：玩家附件 feralFormId 为 {@link #FORM_ID} 哨兵时
 * （仅本 mod 客户端假玩家会写入），取消原版玩家渲染并手动绘制原版村民模型与贴图（静止站桩）。
 * 其余模式（human / 各形态 id）不走本类，human 由原版玩家渲染 + 注入 PlayerInfo 的皮肤处理。
 */
@EventBusSubscriber(Dist.CLIENT)
public final class VillagerAvatarHandler {

	/** 形态哨兵 id：客户端假玩家的 villager 模式写入玩家附件，渲染时据此拦截 */
	public static final String FORM_ID = "villager_avatar";

	/** 原版村民贴图 */
	private static final ResourceLocation VILLAGER_TEXTURE =
			ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

	private static VillagerModel<Player> model;

	private VillagerAvatarHandler() {
	}

	@SubscribeEvent
	public static void onRenderPlayer(net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre event) {
		AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
		if (!player.hasData(MurmolModVariables.PLAYER_VARIABLES))
			return;
		if (!FORM_ID.equals(player.getData(MurmolModVariables.PLAYER_VARIABLES).feralFormId))
			return;
		event.setCanceled(true);
		if (model == null)
			model = new VillagerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.VILLAGER));
		float partialTick = event.getPartialTick();
		float age = player.tickCount + partialTick;
		model.setupAnim(player, 0.0F, 0.0F, age, 0.0F, 0.0F);
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		// 待机轻微呼吸浮动（与旧 MahjongAvatarRenderer 一致）
		poseStack.translate(0, Math.sin(age * 0.07) * 0.012, 0);
		event.getRenderer().setupRotations(player, poseStack, age, player.getYRot(), partialTick, 1.0F);
		// 原版 LivingEntityRenderer 的标准上下翻转（缺失会导致村民头朝下），含 0.9375 缩放
		float s = 0.9375F;
		poseStack.scale(-s, -s, s);
		poseStack.translate(0.0D, -1.501D, 0.0D);
		VertexConsumer vc = event.getMultiBufferSource()
				.getBuffer(RenderType.entityCutoutNoCull(VILLAGER_TEXTURE));
		model.renderToBuffer(poseStack, vc, event.getPackedLight(),
				LivingEntityRenderer.getOverlayCoords(player, 0), -1);
		poseStack.popPose();
	}
}
