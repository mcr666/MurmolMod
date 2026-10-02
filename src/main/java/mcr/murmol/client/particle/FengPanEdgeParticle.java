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
 * 风盘边缘标记粒子：完全静止（无速度/重力/自转，尺寸不变），
 * 短生命周期内轻微淡出。贴图复用星幻冲击（astral_burst）粒子纹理。
 */
@OnlyIn(Dist.CLIENT)
public class FengPanEdgeParticle extends TextureSheetParticle {
	public static FengPanEdgeParticleProvider provider(SpriteSet spriteSet) {
		return new FengPanEdgeParticleProvider(spriteSet);
	}

	public static class FengPanEdgeParticleProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public FengPanEdgeParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new FengPanEdgeParticle(worldIn, x, y, z, this.spriteSet);
		}
	}

	protected FengPanEdgeParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteSet) {
		super(world, x, y, z);
		this.setSize(0.1f, 0.1f);
		this.quadSize = 0.35f;
		this.lifetime = 14; // 0.7s（与服务端 0.5s 一轮的刷新节奏衔接）
		this.gravity = 0.0f;
		this.hasPhysics = false;
		this.xd = 0;
		this.yd = 0;
		this.zd = 0;
		this.pickSprite(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		// 末段轻微淡出（quadSize 缩小模拟），本体不动
		this.quadSize = 0.35f * (1.0f - (float) this.age / (float) this.lifetime * 0.4f);
	}
}
