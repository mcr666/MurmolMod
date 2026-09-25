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
 * 落瓣春花形态。
 * 身体模型 luohong，尾巴 luohong_tail，纹理 luohong.png。
 */
public class LuohongForm extends FeralForm {

	public static final String ID = "luohong";

	public LuohongForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/luohong.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "luohong"),
				ResourceLocation.fromNamespaceAndPath("murmol", "luohong_tail"),
				() -> new ItemStack(MurmolModItems.LUOHONG_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "witnessof_petal_spring"),
				Map.of(
							Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), -4, AttributeModifier.Operation.ADD_VALUE),
							Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.025, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.3, AttributeModifier.Operation.ADD_VALUE),
							Attributes.JUMP_STRENGTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.2, AttributeModifier.Operation.ADD_VALUE),
							Attributes.SCALE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), -0.25, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(MurmolModItems.PETAL.get()),
							new ItemStack(Items.SWEET_BERRIES),
							new ItemStack(Items.GLISTERING_MELON_SLICE),
							new ItemStack(Items.APPLE)));
		// 该形态不显示第一人称手臂
		disableFirstPersonArm();
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/luohong_anim.json"));
	}
}
