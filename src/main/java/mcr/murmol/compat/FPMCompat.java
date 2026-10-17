package mcr.murmol.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import dev.tr7zw.firstperson.api.FirstPersonAPI;
import dev.tr7zw.firstperson.api.PlayerOffsetHandler;

import mcr.murmol.feral.FeralFormManager;

/**
 * FirstPersonModel（FPM）联动桥（软依赖，仅客户端）。
 *
 * <p>目标：第一人称显示完整兽身时——头部已由 FeralFormRenderer /
 * WenyaoPlayerRenderer 隐藏；此处通过 FPM 官方 {@link PlayerOffsetHandler}
 * 在世界空间把兽身再向后推 0.5 格（远离相机），方向与 FPM 自身的后向偏移
 * 一致（{@code (sin(yaw), 0, -cos(yaw))}），避免模型空间缩放造成的符号歧义。
 *
 * <p>未安装 FPM 时 register() 直接返回，不触碰任何 FPM 类（延迟加载安全）。
 */
public final class FPMCompat {

	private static final String FPM_MODID = "firstperson";

	/** 第一人称下兽身额外后移的格数（scale=1、水平视角基准，实际按体型 scale 缩放） */
	private static final double BACK_OFFSET_BLOCKS = 0.35D;
	/** 视角抬到竖直（90°）时在基础量上线性追加的后移格数 */
	private static final double PITCH_BACK_GAIN = 0.35D;

	private FPMCompat() {
	}

	public static boolean isAvailable() {
		return ModList.get() != null && ModList.get().isLoaded(FPM_MODID);
	}

	/** FPM 正在渲染第一人称玩家身体时为 true（背包界面等自绘玩家模型路径不满足）。 */
	public static boolean isRenderingPlayerFirstPerson() {
		if (!isAvailable()) {
			return false;
		}
		try {
			return FirstPersonAPI.isRenderingPlayer();
		} catch (Throwable t) {
			return false;
		}
	}

	/** 必须在客户端、且确认 {@link #isAvailable()} 后调用。 */
	public static void register() {
		if (isAvailable()) {
			FirstPersonAPI.registerPlayerHandler(new FeralOffsetHandler());
		}
	}

	/**
	 * 变形（兽形）时把第一人称兽身沿 FPM 后向偏移方向再推 0.35 格（随仰角增加）。
	 */
	public static class FeralOffsetHandler implements PlayerOffsetHandler {
		@Override
		public Vec3 applyOffset(AbstractClientPlayer entity, float delta, Vec3 original, Vec3 current) {
			if (!FeralFormManager.getForm(entity).isFeral())
				return current;
			float realYaw = Mth.rotLerp(delta, entity.yBodyRotO, entity.yBodyRot);
			double rad = Math.toRadians(realYaw);
			// 位移量随体型 scale 变化（与渲染器的 entity.getScale() 一致）
			double scale = entity.getScale();
			// 仰角越高，额外后移越多：水平（pitch≈0）时 0.35 格刚好离开视角，仰角越大越向后
			double lookUp = Math.max(0.0D, -entity.getXRot()) / 90.0D; // 0=水平，1=竖直向上
			double back = (BACK_OFFSET_BLOCKS + PITCH_BACK_GAIN * lookUp) * scale;
			return current.add(Math.sin(rad) * back, 0.0D, -Math.cos(rad) * back);
		}
	}
}
