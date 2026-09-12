package net.mcr.murmol.feral;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

/**
 * 人类形态（默认形态）。无自定义模型、无属性修饰符、无变形材料。
 */
public class HumanForm extends FeralForm {

	public static final int ID = 0;

	public HumanForm() {
		super(ID, null, null, null, null,
				() -> ItemStack.EMPTY,
				null,
				null,
				null);
	}
}
