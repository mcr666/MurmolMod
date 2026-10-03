package mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;

/**
 * 照妖镜方块：类似物品展示框，可贴在方块六个面上放置。
 * FACING = 镜面朝向（放置时取被点击面法线方向）；整体厚度 1 像素，无碰撞。
 */
public class TruthMirrorBlock extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	/** 各朝向的形状：镜面位于方块外侧 1px 厚度层 */
	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

	static {
		SHAPES.put(Direction.UP, Block.box(0, 0, 0, 16, 1, 16));
		SHAPES.put(Direction.DOWN, Block.box(0, 15, 0, 16, 16, 16));
		SHAPES.put(Direction.NORTH, Block.box(0, 0, 15, 16, 16, 16));
		SHAPES.put(Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 1));
		SHAPES.put(Direction.WEST, Block.box(15, 0, 0, 16, 16, 16));
		SHAPES.put(Direction.EAST, Block.box(0, 0, 0, 1, 16, 16));
	}

	public TruthMirrorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	/** 右键：给予 10s 显形效果（重复右键刷新时长）；期间形态被临时替换，结束复原 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			mcr.murmol.potion.RevealManager.apply(serverPlayer);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// 1px 薄板，无碰撞
		return Block.box(0, 0, 0, 0, 0, 0);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
