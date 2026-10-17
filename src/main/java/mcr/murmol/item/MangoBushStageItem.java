package mcr.murmol.item;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import mcr.murmol.init.MurmolModBiomes;
import mcr.murmol.init.MurmolModBlocks;
import mcr.murmol.init.MurmolModItems;
import mcr.murmol.block.MangoBushBlock;

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
		dropAstralMango(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}

	/** 在星幻感染群系中销毁时额外掉落一颗星辉芒果 */
	private static void dropAstralMango(LevelAccessor world, double x, double y, double z) {
		if (world.getBiome(BlockPos.containing(x, y, z)).is(MurmolModBiomes.ASTRAL_INFECTION_BIOME)) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(MurmolModItems.MANGO_ASTRAL.get()));
				entityToSpawn.setPickUpDelay(10);
				entityToSpawn.setUnlimitedLifetime();
				_level.addFreshEntity(entityToSpawn);
			}
		}
	}
}
