package net.mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.mcr.murmol.init.MurmolModMobEffects;
import net.mcr.murmol.potion.PetrifyMobEffect;

/**
 * 诅咒石头：配方 4×石头 + 粘液球。玩家触碰到（踩上）后立即碎裂，
 * 并给予 20 秒石化效果（PetrifyMobEffect）。创造模式玩家免疫。
 */
public class CursedStoneBlock extends Block {

	/** 石化时长（tick）：20 秒 */
	private static final int PETRIFY_TICKS = 400;

	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

	public CursedStoneBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (!level.isClientSide() && entity instanceof LivingEntity living
				&& !(living instanceof Player player && player.getAbilities().invulnerable)
				&& !PetrifyMobEffect.isPetrified(living)) {
			// 碎裂：石头破坏粒子 + 音效，方块消失不掉落
			playShatterEffects(level, pos);
			level.destroyBlock(pos, false);
			// 石化 20 秒
			living.addEffect(new MobEffectInstance(MurmolModMobEffects.PETRIFY, PETRIFY_TICKS, 0));
		}
		super.stepOn(level, pos, state, entity);
	}

	/** 侧面触发：玩家挖掘完成后同样碎裂并石化（正常掉落保留） */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !player.getAbilities().instabuild) {
			playShatterEffects(level, pos);
			player.addEffect(new MobEffectInstance(MurmolModMobEffects.PETRIFY, PETRIFY_TICKS, 0));
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	private void playShatterEffects(Level level, BlockPos pos) {
		var state = Blocks.STONE.defaultBlockState();
		var option = new BlockParticleOption(ParticleTypes.BLOCK, state);
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			serverLevel.sendParticles(option, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
					40, 0.4, 0.4, 0.4, 0.1);
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F,
					0.8F + level.getRandom().nextFloat() * 0.4F);
		}
	}
}
