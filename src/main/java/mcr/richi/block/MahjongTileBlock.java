package mcr.richi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;

import mcr.richi.MahjongTileItem;
import mcr.richi.block.entity.MahjongTileBlockEntity;

/**
 * 麻将牌方块：牌面由方块实体的牌面代码决定，方块本身无模型（{@link RenderShape#INVISIBLE}），牌体由 BER 渲染。
 * 两种放置形态：点顶面 = 平放（占 x[2,14]、y[0,6]、z[0,16]，牌面朝上）；
 * 点侧面 = 竖放立牌（整格高，{@link #FACING} 为牌面朝外方向），点底面不可放。
 */
public class MahjongTileBlock extends Block implements EntityBlock {
	public static final MapCodec<MahjongTileBlock> CODEC = simpleCodec(MahjongTileBlock::new);

	/** 竖放立牌朝向（牌面朝外的水平方向）；平放时无意义 */
	public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
			HorizontalDirectionalBlock.FACING;
	/** true=竖放立牌，false=平放 */
	public static final BooleanProperty STANDING = BooleanProperty.create("standing");

	/** 平放碰撞/选中形状（厚 6/16，横向占满、两侧各收 2/16） */
	private static final VoxelShape FLAT_SHAPE = Block.box(2.0, 0.0, 0.0, 14.0, 6.0, 16.0);
	/** 竖放碰撞/选中形状：立牌 x[2,14] y[0,16] z[5,11]（关于中心对称，四朝向通用） */
	private static final VoxelShape STANDING_SHAPE = Block.box(2.0, 0.0, 5.0, 14.0, 16.0, 11.0);

	public MahjongTileBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(STANDING, false)
				.setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STANDING, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction face = ctx.getClickedFace();
		if (face == Direction.DOWN)
			return null;
		if (face == Direction.UP)
			return defaultBlockState().setValue(STANDING, false);
		// 点侧面：竖放立牌，牌面朝点击面外侧
		return defaultBlockState().setValue(STANDING, true).setValue(FACING, face);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(STANDING) ? STANDING_SHAPE : FLAT_SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MahjongTileBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// 放置时把麻将牌物品的牌面代码写入方块实体，并同步给客户端 BER
		if (level.getBlockEntity(pos) instanceof MahjongTileBlockEntity tile) {
			tile.setCode(MahjongTileItem.getCode(stack));
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// 战利品表为空：玩家非创造破坏时手动掉落带牌面代码的麻将牌物品
		if (!level.isClientSide() && !player.isCreative()
				&& level.getBlockEntity(pos) instanceof MahjongTileBlockEntity tile) {
			Block.popResource(level, pos, MahjongTileItem.create(tile.getCode()));
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
