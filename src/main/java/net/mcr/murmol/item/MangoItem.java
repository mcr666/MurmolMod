package net.mcr.murmol.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;

import net.mcr.murmol.init.MurmolModBlocks;
import net.mcr.murmol.procedures.MangoDiaoLuoWuXiaoShiShiProcedure;

/**
 * 芒果：可直接食用，也可像甜浆果一样右键泥土种植为芒果丛。
 */
public class MangoItem extends BlockItem {
	public MangoItem() {
		super(MurmolModBlocks.MANGO_BUSH.get(), new Item.Properties().food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.3f).build()));
	}

	/** BlockItem 默认取方块的翻译键（会显示成"芒果丛"），改为使用物品自己的键 */
	@Override
	public String getDescriptionId() {
		return "item.murmol.mango";
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
