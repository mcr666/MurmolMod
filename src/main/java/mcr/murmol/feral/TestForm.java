package mcr.murmol.feral;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 测试形态（开发用）。
 * 不隐藏玩家模型、不渲染任何额外模型（无整体替换层/身体层/尾巴层/纹理），
 * 仅由 Bedrock 动画直接驱动原版玩家模型（动画与乘黄共用同一套文件）。
 * 无变形配方、无古代知识残页、无灵魂物品，唯一变形途径是 /murmol tf 指令。
 */
public class TestForm extends FeralForm {

	public static final String ID = "test";

	@Override
	public boolean isTestForm() {
		return true;
	}

	public TestForm() {
		super(ID,
				null,
				null,
				null,
				null,
				() -> ItemStack.EMPTY,
				null,
				null,
				null);
		// 保留原版第一人称手臂（无自定义身体模型，走原版手臂渲染）
		disableFirstPersonArm();
		// 动画沿用原 AstralCruse 项目的原版模型专用动画文件（按原版 PlayerModel 枢轴制作，
		// 不可用乘黄等自定义模型动画——其 body 位移补偿会把原版模型抬离地面）
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("astral_cruse", "player_animations/feral_anim.json"));
	}
}
