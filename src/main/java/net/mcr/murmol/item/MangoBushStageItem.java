package net.mcr.murmol.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;

import net.mcr.murmol.init.MurmolModBlocks;
import net.mcr.murmol.block.MangoBushBlock;
import net.mcr.murmol.procedures.MangoDiaoLuoWuXiaoShiShiProcedure;

/**
 * 指定生长阶段的芒果丛方块物品：右键放置时直接生成对应 age 阶段的芒果丛。
 * 不可食用（区别于芒果本身）。
 */
public class MangoBushStageItem extends BlockItem {
	private final int stage;

	public MangoBushStageItem(int stage) {
		super(MurmolModBlocks.MANGO_BUSH.get(), new Item.Properties());
		this.stage = stage;
	}

	@Override
	protected BlockState getPlacementState(BlockPlaceContext context) {
		BlockState state = super.getPlacementState(context);
		if (state == null) {
			return null;
		}
		return state.setValue(MangoBushBlock.AGE, Math.min(stage, MangoBushBlock.MAX_AGE));
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		return context.getLevel().setBlock(context.getClickedPos(), state, 26);
	}

	@Override
	public void onDestroyed(ItemEntity entity, DamageSource damagesource) {
		super.onDestroyed(entity, damagesource);
		MangoDiaoLuoWuXiaoShiShiProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}
}
