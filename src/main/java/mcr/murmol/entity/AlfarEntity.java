package mcr.murmol.entity;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;

import mcr.murmol.MurmolMod;

public class AlfarEntity extends Monster implements RangedAttackMob {
	public static final EntityDataAccessor<Boolean> DATA_spattack = SynchedEntityData.defineId(AlfarEntity.class, EntityDataSerializers.BOOLEAN);
	public final AnimationState animationState0 = new AnimationState();
	public final AnimationState animationState2 = new AnimationState();
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.GREEN, ServerBossEvent.BossBarOverlay.PROGRESS);

	public AlfarEntity(EntityType<AlfarEntity> type, Level world) {
		super(type, world);
		xpReward = 1024;
		setNoAi(false);
		setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_spattack, true);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, true, true) {
			@Override
			public boolean canUse() {
				double x = AlfarEntity.this.getX();
				double y = AlfarEntity.this.getY();
				double z = AlfarEntity.this.getZ();
				Entity entity = AlfarEntity.this;
				Level world = AlfarEntity.this.level();
				return super.canUse() && AlfarEntity.targetDistanceAtLeast(entity);
			}

			@Override
			public boolean canContinueToUse() {
				double x = AlfarEntity.this.getX();
				double y = AlfarEntity.this.getY();
				double z = AlfarEntity.this.getZ();
				Entity entity = AlfarEntity.this;
				Level world = AlfarEntity.this.level();
				return super.canContinueToUse() && AlfarEntity.targetDistanceAtLeast(entity);
			}
		});
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
			}

			@Override
			public boolean canUse() {
				double x = AlfarEntity.this.getX();
				double y = AlfarEntity.this.getY();
				double z = AlfarEntity.this.getZ();
				Entity entity = AlfarEntity.this;
				Level world = AlfarEntity.this.level();
				return super.canUse() && AlfarEntity.targetDistanceAtLeast(entity);
			}

			@Override
			public boolean canContinueToUse() {
				double x = AlfarEntity.this.getX();
				double y = AlfarEntity.this.getY();
				double z = AlfarEntity.this.getZ();
				Entity entity = AlfarEntity.this;
				Level world = AlfarEntity.this.level();
				return super.canContinueToUse() && AlfarEntity.targetDistanceAtLeast(entity);
			}

		});
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(5, new FloatGoal(this));
		this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10f) {
			@Override
			public boolean canContinueToUse() {
				return this.canUse();
			}
		});
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float f) {
		return super.getPassengerAttachmentPoint(entity, dimensions, f).add(0, 1.8f, 0);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
	}

	@Override
	public boolean hurt(DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.is(DamageTypes.FALL))
			return false;
		if (damagesource.is(DamageTypes.CACTUS))
			return false;
		if (damagesource.is(DamageTypes.DROWN))
			return false;
		if (damagesource.is(DamageTypes.LIGHTNING_BOLT))
			return false;
		if (damagesource.is(DamageTypes.EXPLOSION) || damagesource.is(DamageTypes.PLAYER_EXPLOSION))
			return false;
		if (damagesource.is(DamageTypes.DRAGON_BREATH))
			return false;
		if (damagesource.is(DamageTypes.WITHER) || damagesource.is(DamageTypes.WITHER_SKULL))
			return false;
		return super.hurt(damagesource, amount);
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		grantKillAchievement(source.getEntity());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean("Dataspattack", this.entityData.get(DATA_spattack));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Dataspattack"))
			this.entityData.set(DATA_spattack, compound.getBoolean("Dataspattack"));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.animationState0.animateWhen(isSwinging(this), this.tickCount);
			this.animationState2.animateWhen(true, this.tickCount);
		}
	}

	@Override
	public void baseTick() {
		super.baseTick();
		specialAttack();
	}

	@Override
	public void performRangedAttack(LivingEntity target, float flval) {
		AlfaSpitProjEntity.shoot(this, target);
	}

	@Override
	public boolean canDrownInFluidType(FluidType type) {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		Level world = this.level();
		Entity entity = this;
		return false;
	}

	@Override
	public boolean isPushedByFluid() {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		Level world = this.level();
		Entity entity = this;
		return false;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public void customServerAiStep() {
		super.customServerAiStep();
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.2);
		builder = builder.add(Attributes.MAX_HEALTH, 300);
		builder = builder.add(Attributes.ARMOR, 10);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 8);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 0.8);
		builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
		builder = builder.add(Attributes.ATTACK_KNOCKBACK, 1.8);
		return builder;
	}

	/** AI 判定：目标距离 ≥3（用于追击/攻击目标的选择） */
	private static boolean targetDistanceAtLeast(Entity entity) {
		if (entity instanceof Mob mob && mob.getTarget() != null) {
			return mob.getTarget().position().distanceTo(entity.position()) >= 3;
		}
		return true;
	}

	/** 动画判定：是否正在挥砍 */
	private static boolean isSwinging(Entity entity) {
		return entity instanceof LivingEntity liv && liv.swinging;
	}

	/** 死亡时授予击杀者 killalfar 成就 */
	private static void grantKillAchievement(Entity sourceentity) {
		if (sourceentity instanceof ServerPlayer _player) {
			AdvancementHolder _adv = _player.server.getAdvancements().get(ResourceLocation.parse("murmol:killalfar"));
			if (_adv != null) {
				AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
				if (!_ap.isDone()) {
					for (String criteria : _ap.getRemainingCriteria())
						_player.getAdvancements().award(_adv, criteria);
				}
			}
		}
	}

	/** 特殊攻击：DATA_spattack 期间随机触发，向目标连续召唤 5 道延时闪电 */
	private void specialAttack() {
		if (!this.getEntityData().get(AlfarEntity.DATA_spattack)) {
			return;
		}
		if (Mth.nextInt(RandomSource.create(), 1, 150) != 1) {
			return;
		}
		LivingEntity target = this.getTarget();
		if (target == null) {
			return;
		}
		this.getEntityData().set(AlfarEntity.DATA_spattack, false);
		double i = 20;
		for (int _i1 = 0; _i1 < 5; _i1++) {
			MurmolMod.queueServerWork((int) i, () -> {
				LivingEntity tgt = this.getTarget();
				if (tgt == null)
					return;
				if (this.level() instanceof ServerLevel _level) {
					LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
					entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(tgt.getX(), tgt.getY(), tgt.getZ())));
					_level.addFreshEntity(entityToSpawn);
				}
			});
			i = i + 20;
		}
		MurmolMod.queueServerWork(200, () -> this.getEntityData().set(AlfarEntity.DATA_spattack, true));
	}
}