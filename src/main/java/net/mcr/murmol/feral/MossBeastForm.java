package net.mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;

/**
 * 苔叶兽形态。
 * 身体模型 moss_beast，尾巴 moss_beast_tail，身体纹理 moss_beast.png，尾巴纹理 moss_beast_tail.png。
 */
public class MossBeastForm extends FeralForm {

	public static final String ID = "moss_beast";

	public MossBeastForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/moss_beast.png"),
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/moss_beast_tail.png"),
				ResourceLocation.fromNamespaceAndPath("murmol", "moss_beast"),
				ResourceLocation.fromNamespaceAndPath("murmol", "moss_beast_tail"),
				() -> new ItemStack(MurmolModItems.MOSS_BEAST_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "stands_a_loquat_tree"),
				Map.of(
					// 仅保留无条件加成；移速/跳跃/挖掘为洞穴条件加成，由 FeralFormManager 按 tick 环境切换
					Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 4, AttributeModifier.Operation.ADD_VALUE),
					Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(Blocks.BIG_DRIPLEAF),
							new ItemStack(Blocks.MOSS_BLOCK),
							new ItemStack(Blocks.VINE),
							new ItemStack(Blocks.SPORE_BLOSSOM)));
		// 该形态不显示第一人称手臂
		enableFirstPersonArm();
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/moss_beast_anim.json"));
	}
}
