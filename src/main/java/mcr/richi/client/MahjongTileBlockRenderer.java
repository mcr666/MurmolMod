package mcr.richi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import mcr.richi.MahjongTileItem;
import mcr.richi.block.MahjongTileBlock;
import mcr.richi.block.entity.MahjongTileBlockEntity;

/**
 * 麻将牌方块的 BER：把对应牌面的麻将牌物品渲染为平放或竖放立牌。
 * 立牌模型元素 x[2,14] y[0,16] z[5,11]（像素，牌面=north）；
 * ItemRenderer.render 对 NONE context 在外部 pose 之后还会 translate(-0.5,-0.5,-0.5)。
 */
public class MahjongTileBlockRenderer implements BlockEntityRenderer<MahjongTileBlockEntity> {

	public MahjongTileBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(MahjongTileBlockEntity tile, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int packedLight, int packedOverlay) {
		ItemStack stack = MahjongTileItem.create(tile.getCode());
		pose.pushPose();
		if (tile.getBlockState().getValue(MahjongTileBlock.STANDING)) {
			// 竖放立牌：模型原样竖直（内部 -0.5 后 x/z 已中心对称），上移半格落地，按朝向绕中心 yaw
			pose.translate(0.0D, 0.5D, 0.0D);
			pose.mulPose(Axis.YP.rotationDegrees(facingYaw(tile.getBlockState().getValue(MahjongTileBlock.FACING))));
		} else {
			// 平放：绕模型中心倒 90°（牌面朝上）后下移 5/16 贴地；
			// 与内部 T(-0.5) 合并化简 = T(0.5,3/16,0.5)·Rx(90°)，牌体占 y[0,6]
			pose.translate(0.5D, 3.0D / 16.0D, 0.5D);
			pose.mulPose(Axis.XP.rotationDegrees(90.0F));
		}
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE,
				packedLight, packedOverlay, pose, buffers, tile.getLevel(), 0);
		pose.popPose();
	}

	/** FACING（牌面朝外方向）→ YP 旋转角：模型牌面默认朝 -z（north=0） */
	private static float facingYaw(Direction facing) {
		return switch (facing) {
			case SOUTH -> 180.0F;
			case WEST -> 90.0F;
			case EAST -> -90.0F;
			default -> 0.0F; // NORTH
		};
	}
}
