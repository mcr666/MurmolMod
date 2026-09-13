package net.mcr.murmol.jei;

import net.mcr.murmol.feral.FeralForm;

/**
 * 幻星秘典形态配方（仅 JEI 展示用）：
 * 4 格材料 → 对应形态灵魂物品。
 */
public class FormTransformRecipe {

	private final FeralForm form;

	public FormTransformRecipe(FeralForm form) {
		this.form = form;
	}

	public FeralForm getForm() {
		return form;
	}
}
