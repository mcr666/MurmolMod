package mcr.murmol.item;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

/**
 * 灵气指南针：以原版指南针为底材锻造而成，针尖指向最近的幻星瘟疫群系。
 * 指针角度由 AstralCompassClient 注册的 "angle" 物品属性驱动（复用原版
 * CompassItemPropertyFunction 的帧映射与乱转逻辑），模型复用原版指南针帧贴图。
 */
public class AstralCompassItem extends Item {
	public AstralCompassItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}
}
