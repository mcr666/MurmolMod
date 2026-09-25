package net.mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;

/**
 * 狛犬形态。
 * 身体模型 komainu（尾巴骨骼链在模型内），纹理 komainu.png。
 * 动画与乘黄/落瓣春花一致（通用 feral 动画 + tail_idle/tail_walk 尾巴动画）。
 * 特性：静止 5 秒进入石像状态（石头纹理、潜行动画、抗性提升、远处怪物无法察觉）。
 */
public class KomainuForm extends FeralForm {

	public static final String ID = "komainu";

	public KomainuForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/komainu.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "komainu"),
				null,
				() -> new ItemStack(MurmolModItems.KOMAINU_SOUL.get()),
				null,
				Map.of(),
				List.of(
							new ItemStack(Items.STONE),
							new ItemStack(Items.CHERRY_SAPLING),
							new ItemStack(Items.BONE),
							new ItemStack(Items.DIAMOND)));
		// 该形态不显示第一人称手臂
		disableFirstPersonArm();
		// 与乘黄共用同一套动画文件（通用 idle/walk/sneak + tail_idle/tail_walk）
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/chen_huang_anim.json"));
	}

	@Override
	public boolean hasStatueState() {
		return true;
	}
}
