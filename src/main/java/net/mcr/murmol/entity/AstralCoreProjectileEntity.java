package net.mcr.murmol.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;

import net.mcr.murmol.init.MurmolModItems;
import net.mcr.murmol.init.MurmolModMobEffects;
import net.mcr.murmol.init.MurmolModParticleTypes;
import net.mcr.murmol.init.MurmolModSounds;

/**
 * Astral Core 弹射物：有重力，可由物品直接抛出。
 * 命中实体/方块或 5 秒后触发星幻冲击：
 * 视觉闪电 + 不破坏方块的爆炸 + 连续产生的同心圆粒子螺旋（半径 1 → 4）；
 * 圆范围内实体被强力上抛一次并获得星幻感染 5 秒。
 */
public class AstralCoreProjectileEntity extends ThrowableItemProjectile {
	public static final int FUSE_TICKS = 100; // 5s
	public static final double RING_RADIUS_MIN = 1.0D;
	public static final double RING_RADIUS_MAX = 4.0D;
	public static final int BURST_DURATION = 24;   // 连续产生同心圆的持续 tick（生成速度快一倍）
	public static final int RING_INTERVAL = 2;     // 每隔几 tick 产生一个同心圆
	public static final int RING_PARTICLES = 32;   // 每个同心圆的粒子数

	private boolean triggered = false;
	private int burstStartTick = 0;
	private double burstX, burstY, burstZ;

	public AstralCoreProjectileEntity(EntityType<? extends AstralCoreProjectileEntity> type, Level world) {
		super(type, world);
	}

	public AstralCoreProjectileEntity(EntityType<? extends AstralCoreProjectileEntity> type, LivingEntity entity, Level world) {
		super(type, entity, world);
	}

	@Override
	protected Item getDefaultItem() {
		return MurmolModItems.ASTRAL_CORE.get();
	}

	@Override
	public void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!this.level().isClientSide() && !this.triggered) {
			this.triggered = true;
			triggerAstralBurst();
		}
	}

	@Override
	public void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		if (!this.level().isClientSide() && !this.triggered) {
			this.triggered = true;
			triggerAstralBurst();
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide())
			return;
		if (!this.triggered) {
			if (this.tickCount >= FUSE_TICKS) {
				this.triggered = true;
				this.burstStartTick = this.tickCount;
				triggerAstralBurst();
			}
		} else {
			// 连续产生多个半径逐渐增大的同心圆，结束后销毁
			int elapsed = this.tickCount - this.burstStartTick;
			if (elapsed > 0 && elapsed <= BURST_DURATION && elapsed % RING_INTERVAL == 0)
				spawnRing();
			if (elapsed >= BURST_DURATION)
				this.discard();
		}
	}

	private void triggerAstralBurst() {
		Level level = this.level();
		if (!(level instanceof ServerLevel server))
			return;
		// 记录触发点位置（所有特效以此为基准，之后不再变动）
		this.burstX = this.getX();
		this.burstY = this.getY();
		this.burstZ = this.getZ();
		double x = this.burstX, y = this.burstY, z = this.burstZ;

		// 停止运动并隐藏弹射物本体，进入特效阶段
		this.setDeltaMovement(Vec3.ZERO);
		this.setNoGravity(true);
		this.setInvisible(true);

		// 0) 播放星幻冲击音效（仅一次）
		server.playSound(null, x, y, z, MurmolModSounds.ASTRAL_BURST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

		// 1) 从空中劈下一道闪电（仅视觉，不造成伤害/起火）
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
		if (bolt != null) {
			bolt.moveTo(x, y, z);
			bolt.setVisualOnly(true);
			server.addFreshEntity(bolt);
		}

		// 2) 圆心处一次不破坏方块的爆炸（已调大一格）
		level.explode(this.getOwner(), x, y, z, 3.0F, Level.ExplosionInteraction.NONE);

		// 3) 第一个同心圆（半径 1），后续在 tick() 中连续产生更大的同心圆
		spawnRing();

		// 4) 最大圆范围内实体：强力上抛一次 + 星幻感染 5s
		AABB box = new AABB(x - RING_RADIUS_MAX - 0.5D, y - 1.0D, z - RING_RADIUS_MAX - 0.5D,
				x + RING_RADIUS_MAX + 0.5D, y + 3.0D, z + RING_RADIUS_MAX + 0.5D);
		for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, box)) {
			if (target.isSpectator())
				continue;
			target.setDeltaMovement(target.getDeltaMovement().x, 1.2D, target.getDeltaMovement().z);
			target.hurtMarked = true;
			target.addEffect(new MobEffectInstance(MurmolModMobEffects.ASTRAL_INFECTION, 100, 0));
		}
	}

	/** 在当前位置产生一个空心粒子圆，半径随触发后时间从 1 线性增大到 4，所有同心圆在同一高度生成 */
	private void spawnRing() {
		Level level = this.level();
		if (!(level instanceof ServerLevel server))
			return;
		double x = this.getX(), y = this.getY(), z = this.getZ();
		double progress = Math.min(1.0D, (double) (this.tickCount - this.burstStartTick) / BURST_DURATION);
		double radius = RING_RADIUS_MIN + (RING_RADIUS_MAX - RING_RADIUS_MIN) * progress;
		// 所有同心圆固定在原触发点的高度（不额外偏移）
		double ringY = this.burstY;
		for (int i = 0; i < RING_PARTICLES; i++) {
			double angle = Math.PI * 2 * i / RING_PARTICLES;
			// 每颗粒子在圆半径上有 ±1 单位的随机偏移（高度保持不变）
			double jitterX = (server.random.nextDouble() * 2.0D - 1.0D);
			double jitterZ = (server.random.nextDouble() * 2.0D - 1.0D);
			double px = x + Math.cos(angle) * radius + jitterX;
			double pz = z + Math.sin(angle) * radius + jitterZ;
			// 圆周切向初速度（角速度 0.07 rad/tick × 半径）
			double tx = -Math.sin(angle) * 0.07D * radius;
			double tz = Math.cos(angle) * 0.07D * radius;
			server.sendParticles(MurmolModParticleTypes.ASTRAL_BURST.get(), px, ringY, pz, 0, tx, 0.02D, tz, 1.0D);
		}
	}
}
