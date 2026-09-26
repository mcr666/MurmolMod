package net.mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.mcr.murmol.network.MurmolModVariables;

/**
 * 囚笼（Cage）：完整一格的实心金属笼。红石激活时，踩到笼顶的玩家或生物
 * 会被传送进笼内禁锢——无法移动、无法使用物品/攻击/交互；
 * 被困者只能挖掘脚下囚笼自救（挖掘时长等效黑曜石），或由外部断电/破坏方块释放。
 *
 * 碰撞细节：完整一格的判定是 stepOn 生效的前提（实体站笼顶时 onPos=笼块本身，
 * 1/16 底板时 onPos 会落到笼块下方导致永远无法触发）。
 * 被禁锢实体在所在笼块内的碰撞箱退化为底板，使其能站在笼内
 * （避免原版 pushOutOfBlocks 把实体挤出方块）。
 */
public class CageBlock extends Block {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	/** 笼内被禁锢实体的碰撞箱：底板（1/16 高） */
	private static final VoxelShape SLAB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

	/** 禁锢时等比缩小的目标高度（比囚笼一格低 4 像素，即 12/16） */
	private static final double CAGE_TARGET_HEIGHT = 0.75D;
	/** 原始缩放值暂存键（实体 persistentData） */
	private static final String ORIG_SCALE_TAG = "MurmolOrigScale";
	/** 每个囚笼方块最多同时禁锢的实体数 */
	public static final int MAX_CAGED_PER_CAGE = 1;

	public CageBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.FALSE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	/** 轮廓（准星目标/渲染外框）恒为完整一格：被困者从笼内也能选中笼块挖掘自救 */
	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return net.minecraft.world.phys.shapes.Shapes.block();
	}

	/** 碰撞：默认完整一格；被禁锢实体在所在笼块内退化为底板（站在笼内不被挤出） */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Entity entity = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext entityContext
				? entityContext.getEntity()
				: null;
		if (entity instanceof LivingEntity living
				&& living.getData(MurmolModVariables.CAGED_STATE)
				&& living.blockPosition().equals(pos))
			return SLAB;
		return net.minecraft.world.phys.shapes.Shapes.block();
	}

	/** 踩到激活的囚笼笼顶：等比缩小到能被装下，传送进笼内并禁锢 */
	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (!level.isClientSide() && state.getValue(POWERED)
				&& entity instanceof LivingEntity living
				&& !living.getData(MurmolModVariables.CAGED_STATE)) {
			// 上限检查：该囚笼内已禁锢实体达到上限时不再捕获
			long cagedCount = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos)).stream()
					.filter(e -> e.getData(MurmolModVariables.CAGED_STATE)).count();
			if (cagedCount >= MAX_CAGED_PER_CAGE)
				return;
			// 等比缩小：按当前碰撞箱高度缩放至略低于一格（先记录原始缩放，释放时还原）
			if (living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE) != null) {
				double curScale = living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
				double newScale = curScale * (CAGE_TARGET_HEIGHT / Math.max(living.getBbHeight(), 0.01D));
				if (newScale < curScale) {
					living.getPersistentData().putDouble(ORIG_SCALE_TAG, curScale);
					living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE).setBaseValue(newScale);
				}
			}
			// 传送进笼内中心（底板顶面 y + 1/16）
			entity.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D / 16.0D, pos.getZ() + 0.5D);
			entity.setDeltaMovement(Vec3.ZERO);
			living.setData(MurmolModVariables.CAGED_STATE, true);
			if (living instanceof Mob mob)
				mob.setNoAi(true);
			// 玩家是客户端权威移动：需显式传送客户端，并通过 PlayerVariables 自同步让客户端锁输入
			if (living instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				serverPlayer.getData(MurmolModVariables.PLAYER_VARIABLES).caged = true;
				serverPlayer.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
				serverPlayer.connection.teleport(pos.getX() + 0.5D, pos.getY() + 1.0D / 16.0D, pos.getZ() + 0.5D,
						serverPlayer.getYRot(), serverPlayer.getXRot());
			}
			level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.6F);
		}
		super.stepOn(level, pos, state, entity);
	}

	/** 红石信号变化：记录 POWERED；断电时释放并弹出笼内实体 */
	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			net.minecraft.core.BlockPos fromPos, boolean isMoving) {
		if (!level.isClientSide()) {
			boolean powered = level.hasNeighborSignal(pos);
			if (powered != state.getValue(POWERED)) {
				level.setBlock(pos, state.setValue(POWERED, powered), 3);
				if (!powered)
					freeEntities(level, pos, true);
			}
		}
		super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
	}

	/** 方块被破坏时释放笼内实体（笼已消失，原地释放即可） */
	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!level.isClientSide() && newState.getBlock() != this)
			freeEntities(level, pos, false);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	/** 释放囚笼位置所有被禁锢的实体；ejectAbove=true 时弹到笼顶（方块还在） */
	private static void freeEntities(Level level, BlockPos pos, boolean ejectAbove) {
		AABB box = new AABB(pos);
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
			if (living.getData(MurmolModVariables.CAGED_STATE)) {
				releaseEntity(living);
				if (ejectAbove) {
					living.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
					living.setDeltaMovement(Vec3.ZERO);
				}
			}
		}
	}

	/**
	 * 释放单个被禁锢实体：清除标记、还原缩放、恢复生物 AI。
	 * 供 CageBlock 与 PetrifyLockHandlers 的自愈逻辑共用（含方块被破坏后标记残留的情况）。
	 */
	public static void releaseEntity(LivingEntity living) {
		living.setData(MurmolModVariables.CAGED_STATE, false);
		if (living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE) != null) {
			net.minecraft.nbt.CompoundTag tag = living.getPersistentData();
			if (tag.contains(ORIG_SCALE_TAG))
				living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE)
						.setBaseValue(tag.getDouble(ORIG_SCALE_TAG));
			tag.remove(ORIG_SCALE_TAG);
		}
		if (living instanceof Mob mob)
			mob.setNoAi(false);
		if (living instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			serverPlayer.getData(MurmolModVariables.PLAYER_VARIABLES).caged = false;
			serverPlayer.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		}
	}
}
