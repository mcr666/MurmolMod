package mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 两仪：太极图样式的形态开关管理方块。
 * 右键打开形态开关界面（所有玩家可查看，修改需 3 级权限，校验在网络包服务端处理）。
 */
public class TwoPrinciplesBlock extends Block {
	public TwoPrinciplesBlock(Properties properties) {
		super(properties);
	}

	/** 薄板外形：1x0.25x1（16x4x16 像素） */
	private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE =
			Block.box(0, 0, 0, 16, 4, 16);

	@Override
	protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!level.isClientSide()) {
			// 服务端确认交互成功（1.21.1 无 SUCCESS_SERVER），客户端收到确认后自行打开界面
			return InteractionResult.SUCCESS;
		}
		mcr.murmol.client.screen.TwoPrinciplesScreen.open();
		return InteractionResult.SUCCESS;
	}
}
