package mcr.murmol.client.particle;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * 立直宣言粒子：初速度为圆周切向方向。
 * 每 tick 将水平速度旋转固定角度（圆周运动），同时切向匀加速使半径逐渐增大，
 * 垂直方向匀加速上升形成螺旋，0.5 秒后销毁（独立于星幻冲击的短寿命版本）。
 */
@OnlyIn(Dist.CLIENT)
public class RiichiBurstParticle extends TextureSheetParticle {
	public static RiichiBurstParticleProvider provider(SpriteSet spriteSet) {
		return new RiichiBurstParticleProvider(spriteSet);
	}

	public static class RiichiBurstParticleProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public RiichiBurstParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new RiichiBurstParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

	private static final float SPIN = 0.07F;            // 角速度（弧度/tick）
	private static final float TANGENTIAL_ACCEL = 1.015F; // 切向加速度倍率（半径散开更快）
	private static final float RISE_ACCEL = 0.005F;     // 垂直匀加速量值（一半粒子上升，一半下降）

	private final float riseSign; // +1 上升，-1 下降

	protected RiichiBurstParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
		super(world, x, y, z);
		this.setSize(0.1f, 0.1f);
		this.quadSize = 0.45f;
		this.lifetime = 10; // 0.5s
		this.gravity = 0.0f;
		this.hasPhysics = false;
		this.xd = vx;
		this.zd = vz;
		// 一半粒子向上螺旋，一半向下
		this.riseSign = this.random.nextBoolean() ? 1.0f : -1.0f;
		this.yd = vy * this.riseSign;
		this.pickSprite(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		// 水平速度旋转 SPIN 弧度 → 圆周运动
		double cos = Math.cos(SPIN);
		double sin = Math.sin(SPIN);
		double nx = this.xd * cos - this.zd * sin;
		double nz = this.xd * sin + this.zd * cos;
		// 切向匀加速 → 圆周半径逐渐增大
		this.xd = nx * TANGENTIAL_ACCEL;
		this.zd = nz * TANGENTIAL_ACCEL;
		// 垂直匀加速（方向由 riseSign 决定）→ 螺旋上升/下降
		this.yd += RISE_ACCEL * this.riseSign;
		// 自转 + 末段缩小淡出
		this.oRoll = this.roll;
		this.roll += 0.1f;
		this.quadSize = 0.45f * (1.0f - (float) this.age / (float) this.lifetime * 0.7f);
	}
}
