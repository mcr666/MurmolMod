package mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import mcr.murmol.init.MurmolModItems;

/**
 * 狰形态（Ferocious）。
 * 飞行机制与月蛾一致（悬停飞行：长按跳跃上升，松手下落），其余同乘黄形态。
 * 模型与贴图待接入：body 层 ferocious，纹理 ferocious.png。
 */
public class FerociousForm extends FeralForm {

	public static final String ID = "ferocious";

	public FerociousForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/ferocious.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "ferocious"),
				null,
				() -> new ItemStack(MurmolModItems.FEROCIOUS_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_ferocious"),
				Map.of(
							Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 10, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ARMOR, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 5, AttributeModifier.Operation.ADD_VALUE),
							Attributes.SCALE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(Items.PHANTOM_MEMBRANE),
							new ItemStack(Items.BLAZE_POWDER),
							new ItemStack(Items.NETHER_WART),
							new ItemStack(Items.RABBIT_HIDE)));
		// 与乘黄一致：不显示第一人称手臂
		enableFirstPersonArm();
		// 与月蛾一致：悬停飞行
		enableHoverFlight();
		// 专属动画文件（feral_* 四足姿态 + tail_idle/tail_walk/wings_idle/wings_flying）
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/ferocious_anim.json"));
	}
}
