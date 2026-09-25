package net.mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 芒果丛：仿原版甜浆果丛的灌木作物。
 * 成熟后右键采收芒果；行走时减速并受轻微伤害（与甜浆果丛一致）。
 * 贴图暂用原版甜浆果丛。
 */
public class MangoBushBlock extends BushBlock implements BonemealableBlock {

	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final float HURT_SPEED = 0.003F;

	public MangoBushBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() {
		return simpleCodec(MangoBushBlock::new);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(Blocks.FARMLAND) || super.mayPlaceOn(state, level, pos);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return state.getValue(AGE) < MAX_AGE;
	}

	@Override
	public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 9 && net.minecraft.util.Mth.randomBetween(random, 5, 25) == 5) {
			level.setBlock(pos, state.setValue(AGE, age + 1), 2);
		}
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		int age = state.getValue(AGE);
		if (age > 1) {
			int amount = 1 + level.random.nextInt(2) + (age == MAX_AGE ? 1 : 0);
			popResource(level, pos, new ItemStack(net.mcr.murmol.init.MurmolModItems.MANGO.get(), amount));
			level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
					0.8F + level.random.nextFloat() * 0.4F);
			level.setBlock(pos, state.setValue(AGE, 1), 2);
			return InteractionResult.sidedSuccess(level.isClientSide());
		}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		// 与原版甜浆果丛一致：非创造玩家与狐狸以外的实体减速并受轻伤
		if (!(entity instanceof net.minecraft.world.entity.animal.Fox)) {
			boolean hurtful = !(entity instanceof Player player) || !player.getAbilities().mayfly;
			if (hurtful) {
				entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75D, 0.8F));
				if (!level.isClientSide() && state.getValue(AGE) > 0
						&& (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
					double dx = Math.abs(entity.getX() - entity.xOld);
					double dz = Math.abs(entity.getZ() - entity.zOld);
					if (dx >= HURT_SPEED || dz >= HURT_SPEED) {
						entity.hurt(level.damageSources().sweetBerryBush(), 1.0F);
					}
				}
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return new ItemStack(net.mcr.murmol.init.MurmolModItems.MANGO.get());
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(AGE) == 0)
			return;
		if (random.nextInt(5) != 0)
			return;
		Vec3 offset = state.getOffset(level, pos);
		double x = pos.getX() + 0.4375D + random.nextDouble() / 8.0D * (random.nextBoolean() ? 1.0D : -1.0D);
		double y = pos.getY() + 0.8D + random.nextDouble() / 8.0D;
		double z = pos.getZ() + 0.4375D + random.nextDouble() / 8.0D * (random.nextBoolean() ? 1.0D : -1.0D);
		level.addParticle(ParticleTypes.COMPOSTER, x + offset.x, y + offset.y, z + offset.z, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return state.getValue(AGE) < MAX_AGE;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(net.minecraft.server.level.ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		int newAge = Math.min(MAX_AGE, state.getValue(AGE) + 1);
		if (newAge != state.getValue(AGE)) {
			level.setBlock(pos, state.setValue(AGE, newAge), 2);
		}
	}
}
