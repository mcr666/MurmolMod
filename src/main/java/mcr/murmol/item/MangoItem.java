package mcr.murmol.item;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import mcr.murmol.init.MurmolModBiomes;
import mcr.murmol.init.MurmolModBlocks;
import mcr.murmol.init.MurmolModItems;

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
