package net.mcr.murmol.entity;

import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.mcr.murmol.block.CageBlock;
import net.mcr.murmol.network.MurmolModVariables;

/**
 * 贪食者史莱姆：以原版史莱姆为原型的大型敌对生物（体型 4，死亡不分裂）。
 * 成功伤害玩家后触发类似囚笼的"吞食"关押：玩家等比缩小并被吞入史莱姆体内，
 * 无法移动/用物品/攻击/交互（复用 CAGED_STATE 锁定链路，玩家变量 caged 同步客户端锁输入）；
 * 击杀史莱姆后释放（还原缩放并弹出体外）。
 */
public class GluttonySlimeEntity extends Slime {

	/** 玩家 persistentData 中记录吞噬者 UUID 的键 */
	public static final String SWALLOWED_BY_TAG = "MurmolSwallowedBy";
	/** 本实体 persistentData 中记录被吞玩家 UUID 的键 */
	private static final String SWALLOWED_PLAYER_TAG = "MurmolSwallowedPlayer";
	/** 玩家原始缩放缓存键（复用囚笼的还原约定） */
	private static final String ORIG_SCALE_TAG = "MurmolOrigScale";
	/** 吞食后玩家等比缩小的目标高度 */
	private static final double SWALLOW_TARGET_HEIGHT = 0.75D;

	public GluttonySlimeEntity(EntityType<? extends GluttonySlimeEntity> type, Level level) {
		super(type, level);
		this.setSize(4, true);
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes();
	}

	public static void init(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
	}

	/**
	 * 死亡不分裂：跳过原版 Slime.remove 的子史莱姆生成，
	 * 复刻 LivingEntity.remove 的等效路径（死亡效果触发 + 移除 + 清空记忆）。
	 */
	@Override
	public void remove(Entity.RemovalReason reason) {
		if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED)
			this.triggerOnDeathMobEffects(reason);
		this.setRemoved(reason);
		this.getBrain().clearMemories();
	}

	/** 接触伤害钩子：成功造成伤害且目标为玩家时触发吞食 */
	@Override
	public void playerTouch(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer) || this.isDealsDamage() == false)
			return;
		// 已被禁锢的玩家（含正被本实体吞食）不再重复伤害，防止吞后连击致死
		if (serverPlayer.getData(MurmolModVariables.CAGED_STATE))
			return;
		float healthBefore = serverPlayer.getHealth();
		this.dealDamage(serverPlayer);
		if (serverPlayer.getHealth() < healthBefore || serverPlayer.isDeadOrDying())
			this.swallow(serverPlayer);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide)
			this.maintainSwallowedPlayer();
	}

	/** 死亡释放：还原被吞玩家并弹出体外 */
	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (!this.level().isClientSide)
			this.releaseSwallowedPlayer(true);
	}

	/** 吞食玩家：等比缩小、传送进体内并按囚笼链路锁定 */
	private void swallow(ServerPlayer player) {
		if (this.getSwallowed() != null)
			return; // 一次只吞一人
		AttributeInstance scale = player.getAttribute(Attributes.SCALE);
		if (scale != null) {
			double curScale = scale.getBaseValue();
			double newScale = curScale * (SWALLOW_TARGET_HEIGHT / Math.max(player.getBbHeight(), 0.01D));
			if (newScale < curScale) {
				player.getPersistentData().putDouble(ORIG_SCALE_TAG, curScale);
				scale.setBaseValue(newScale);
			}
		}
		player.getPersistentData().putUUID(SWALLOWED_BY_TAG, this.getUUID());
		this.getPersistentData().putUUID(SWALLOWED_PLAYER_TAG, player.getUUID());
		player.setData(MurmolModVariables.CAGED_STATE, true);
		player.getData(MurmolModVariables.PLAYER_VARIABLES).caged = true;
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		// 骑乘实现"吞入体内"：原版乘客同步机制平滑跟随，无传送时延
		player.startRiding(this, true);
		this.level().playSound(null, this.blockPosition(), SoundEvents.SLIME_ATTACK, SoundSource.HOSTILE, 1.0F, 0.5F);
	}

	/** 每 tick 维护：保持被吞玩家定身于体内；标记失效时兜底释放 */
	private void maintainSwallowedPlayer() {
		ServerPlayer player = this.getSwallowed();
		if (player == null)
			return;
		CompoundTag data = player.getPersistentData();
		if (!player.isAlive() || !player.getData(MurmolModVariables.CAGED_STATE)
				|| !data.hasUUID(SWALLOWED_BY_TAG)
				|| !data.getUUID(SWALLOWED_BY_TAG).equals(this.getUUID())) {
			this.clearSwallowedTag();
			CageBlock.releaseEntity(player);
			return;
		}
		// 骑乘状态下由原版乘客同步跟随；若因异常下座则强制重新骑回
		if (!player.isPassenger() || player.getVehicle() != this)
			player.startRiding(this, true);
	}

	/** 释放被吞玩家：还原缩放并弹出体外；squish=true 时附带音效与粒子 */
	private void releaseSwallowedPlayer(boolean squish) {
		ServerPlayer player = this.getSwallowed();
		this.clearSwallowedTag();
		if (player == null)
			return;
		CageBlock.releaseEntity(player);
		if (player.isPassenger() && player.getVehicle() == this)
			player.stopRiding();
		player.teleportTo(this.getX() + 1.0D, this.getY() + 0.5D, this.getZ() + 1.0D);
		player.setDeltaMovement(Vec3.ZERO);
		if (squish) {
			this.level().playSound(null, this.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0F, 0.8F);
			((ServerLevel) this.level()).sendParticles(ParticleTypes.ITEM_SLIME,
					this.getX(), this.getY() + this.getBbHeight() / 2.0D, this.getZ(), 30,
					this.getBbWidth() / 4.0D, 0.25D, this.getBbWidth() / 4.0D, 0.05D);
		}
	}

	/**
	 * 被吞玩家骑乘点：半身没入（高度 × 0.5）。
	 * 注意必须让玩家眼睛略高于史莱姆碰撞箱顶端——原版 AABB.clip 从箱体内部
	 * 出发的射线返回空，眼睛在箱内会导致准星永远拾取不到本实体（无法攻击）。
	 */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, net.minecraft.world.entity.EntityDimensions dimensions, float f) {
		return new Vec3(0.0D, dimensions.height() * 0.5D, 0.0D);
	}

	/** 当前被本实体吞食的玩家（无则 null） */
	private ServerPlayer getSwallowed() {
		CompoundTag data = this.getPersistentData();
		if (this.level() instanceof ServerLevel serverLevel && data.hasUUID(SWALLOWED_PLAYER_TAG))
			return serverLevel.getEntity(data.getUUID(SWALLOWED_PLAYER_TAG)) instanceof ServerPlayer player ? player : null;
		return null;
	}

	private void clearSwallowedTag() {
		this.getPersistentData().remove(SWALLOWED_PLAYER_TAG);
	}

	/**
	 * 吞食关押校验（供 PetrifyLockHandlers 自愈逻辑调用）：
	 * 吞噬者仍存活返回 true（保持关押）；已消失则释放并返回 false。
	 */
	public static boolean validateSwallowLock(net.minecraft.world.entity.LivingEntity living) {
		if (!(living.level() instanceof ServerLevel serverLevel)
				|| !living.getPersistentData().hasUUID(SWALLOWED_BY_TAG))
			return false;
		UUID gluttonId = living.getPersistentData().getUUID(SWALLOWED_BY_TAG);
		if (serverLevel.getEntity(gluttonId) instanceof GluttonySlimeEntity slime && slime.isAlive())
			return true;
		living.getPersistentData().remove(SWALLOWED_BY_TAG);
		CageBlock.releaseEntity(living);
		return false;
	}
}
