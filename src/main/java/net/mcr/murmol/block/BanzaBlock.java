package net.mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.mcr.murmol.feral.FeralFormManager;

/**
 * 磐座：苔石基座（高度 4/16）。狛犬形态的玩家站在磐座上右键，可立即进入石像状态
 * （正常状态为原地静止 5 秒触发）。石像状态下站在磐座上会缓慢回血，贴图显示为原版苔石。
 */
public class BanzaBlock extends Block {

	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

	public BanzaBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		// 狛犬形态右键：吸附到磐座顶部中心并立即石化（不要求必须站在磐座上）
		if (!level.isClientSide() && FeralFormManager.getForm(player).hasStatueState()) {
			player.teleportTo(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
			FeralFormManager.tryActivateStatue(player);
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}
}
