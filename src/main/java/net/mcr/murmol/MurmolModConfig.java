package net.mcr.murmol;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端配置。在 mod 构造函数中注册为 CLIENT 配置。
 */
public class MurmolModConfig {

	/** 客户端分组 */
	public static final ModConfigSpec.BooleanValue MODIFY_TITLE_SCREEN;
	/** 手动开关：强制显示在形态定义中被隐藏的第一人称形态手臂（默认关闭） */
	public static final ModConfigSpec.BooleanValue SHOW_HIDDEN_FERAL_ARM;
	/** 是否渲染本 mod 生物群系的环境粒子（星界侵蚀群系的 astral_effect，默认开启） */
	public static final ModConfigSpec.BooleanValue RENDER_BIOME_PARTICLES;

	public static final ModConfigSpec SPEC;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		builder.push("client");
		MODIFY_TITLE_SCREEN = builder
				.comment("Whether the mod should modify the title screen (panorama background, button style and main menu music). Default: false")
				.define("modifyTitleScreen", false);
		SHOW_HIDDEN_FERAL_ARM = builder
				.comment("Manual switch: force-render the first-person feral arm that the form definition hides. Default: false")
				.define("showHiddenFeralArm", false);
		RENDER_BIOME_PARTICLES = builder
				.comment("Whether the mod's biome ambient particles (astral_effect in the astral infection biome) should render. Default: true")
				.define("renderBiomeParticles", true);
		builder.pop();
		SPEC = builder.build();
	}
}
