package net.mcr.murmol.feral;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;

/**
 * 蚕蛾形态（Silkmoth）。
 * 特性：悬停飞行——长按跳跃键缓慢上升，松手自然下落，无摔落伤害。
 */
public class SilkmothForm extends FeralForm {

	public static final String ID = "silkmoth";

	public SilkmothForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/silkmoth.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "silkmoth"),
				null,
				() -> new ItemStack(MurmolModItems.SILKMOTH_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_silkmoth"),
				null,
				List.of(
						new ItemStack(Items.PHANTOM_MEMBRANE),
						new ItemStack(Items.HONEY_BOTTLE),
						new ItemStack(Items.GLOWSTONE_DUST),
						new ItemStack(Items.STRING)));
		enableHoverFlight();
		disableFirstPersonArm();
		// 蚕蛾模型整体额外下移 3.25 个模型单位
		setBodyYOffset(-1.0F);
		// 专属动画文件：只含附加动画（wings_idle/wings_flying/moth_walk/tail_idle/tail_walk），
		// 核心骨骼保持 Blockbench 烘焙姿态，由 animateCoreBones=false 控制
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/silkmoth_anim.json"));
		setAnimateCoreBones(false);
		// 月蛾保持原版玩家碰撞箱/眼高
		setModifiesHitbox(false);
		// 月蛾允许穿戴胸甲
		setCanWearChestArmor(true);
	}
}
