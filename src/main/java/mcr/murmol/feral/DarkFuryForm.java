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
 * 黯忿形态（Dark Fury）。
 * 通体漆黑的翼兽，飞行机制同月蛾/狰（悬停飞行：长按跳跃上升，松手下落）。
 * 祭品：幻翼膜 + 黑曜石 + 龙息 + 末影之眼。
 */
public class DarkFuryForm extends FeralForm {

	public static final String ID = "dark_fury";

	public DarkFuryForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/dark_fury.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "dark_fury"),
				null,
				() -> new ItemStack(MurmolModItems.DARK_FURY_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_dark_fury"),
				Map.of(
							Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 10, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ARMOR, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 4, AttributeModifier.Operation.ADD_VALUE),
							Attributes.SCALE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.6, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 1.5, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE),
							Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.01, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(Items.PHANTOM_MEMBRANE),
							new ItemStack(Items.OBSIDIAN),
							new ItemStack(Items.DRAGON_BREATH),
							new ItemStack(Items.ENDER_EYE)));
		// 与狰一致：不显示第一人称手臂
		enableFirstPersonArm();
		// 悬停飞行
		enableHoverFlight();
		// 专属动画文件（feral_* 核心姿态 + tail_idle/tail_walk/wings_idle/wings_flying）
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/dark_fury_anim.json"));
	}
}
