package net.mcr.murmol.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.mcr.murmol.feral.FeralFormManager;

/**
 * 磐座：苔石基座。可像顶层雪一样用磐座物品堆叠，最多 4 层（每层 4/16 高）。
 * 狛犬形态的玩家右键磐座，可吸附到顶部并立即进入石像状态
 * （正常状态为原地静止 5 秒触发）。石像状态下站在磐座上会缓慢回血。
 */
public class BanzaBlock extends Block {

	/** 层数 1~4（每层 4/16 高，4 层为完整方块） */
	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 4);

	protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[] {
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
			Shapes.block() };

	public BanzaBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LAYERS, 1));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LAYERS);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(LAYERS) == 4 ? 0.2F : 1.0F;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return Block.isFaceFull(below.getCollisionShape(level, pos.below()), Direction.UP)
				|| below.is(this) && below.getValue(LAYERS) == 4;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
			LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		return !state.canSurvive(level, pos)
				? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
				: super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		int layers = state.getValue(LAYERS);
		if (!context.getItemInHand().is(this.asItem()) || layers >= 4) {
			return false;
		}
		return context.replacingClickedOnBlock() ? context.getClickedFace() == Direction.UP : true;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
		if (clicked.is(this)) {
			// 对已有磐座使用磐座物品：堆叠一层（最多 4 层）
			return clicked.setValue(LAYERS, Math.min(4, clicked.getValue(LAYERS) + 1));
		}
		return super.getStateForPlacement(context);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		// 狛犬形态右键：吸附到磐座顶部中心并立即石化（不要求必须站在磐座上；潜行时右键不触发，避免刚石化立即被 shift 解除）
		if (!level.isClientSide() && !player.isShiftKeyDown() && FeralFormManager.getForm(player).hasStatueState()) {
			double topOffset = 0.25D * state.getValue(LAYERS);
			player.teleportTo(pos.getX() + 0.5D, pos.getY() + topOffset, pos.getZ() + 0.5D);
			FeralFormManager.tryActivateStatue(player);
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}
}
