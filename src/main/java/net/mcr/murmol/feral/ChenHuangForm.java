package net.mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;

/**
 * 乘黄形态。
 * 身体模型 chen_huang，尾巴 chen_huang_tail，纹理 chen_huang.png。
 */
public class ChenHuangForm extends FeralForm {

	public static final String ID = "chen_huang";

	public ChenHuangForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/chen_huang.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "chen_huang"),
				ResourceLocation.fromNamespaceAndPath("murmol", "chen_huang_tail"),
				() -> new ItemStack(MurmolModItems.CHEN_HUANG_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_chenhuang"),
				Map.of(
							Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 4, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ARMOR, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 2, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(Items.GOLDEN_APPLE),
							new ItemStack(MurmolModItems.MANGO.get()),
							new ItemStack(Items.SUGAR),
							new ItemStack(Items.HONEY_BOTTLE)));
		// 该形态不显示第一人称手臂
		enableFirstPersonArm();
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/chen_huang_anim.json"));
	}
}
