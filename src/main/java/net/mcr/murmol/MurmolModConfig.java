package net.mcr.murmol;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端配置。在 mod 构造函数中注册为 CLIENT 配置。
 */
public class MurmolModConfig {

	/** 是否让 mod 修改标题界面（按钮样式与主菜单音乐，默认开启） */
	public static final ModConfigSpec.BooleanValue MODIFY_TITLE_SCREEN;
	/** 手动开关：强制显示在形态定义中被隐藏的第一人称形态手臂（默认关闭） */
	public static final ModConfigSpec.BooleanValue SHOW_HIDDEN_FERAL_ARM;

	public static final ModConfigSpec SPEC;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		MODIFY_TITLE_SCREEN = builder
				.comment("Whether the mod should modify the title screen (button style and main menu music). Default: true")
				.define("modifyTitleScreen", true);
		SHOW_HIDDEN_FERAL_ARM = builder
				.comment("Manual switch: force-render the first-person feral arm that the form definition hides. Default: false")
				.define("showHiddenFeralArm", false);
		SPEC = builder.build();
	}
}
