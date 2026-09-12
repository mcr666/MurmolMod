package net.mcr.murmol.item;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;

import net.mcr.murmol.procedures.KoubanxueProcedure;

import java.util.List;

public class MurmolTaleItem extends Item {
	public MurmolTaleItem() {
		super(new Item.Properties().fireResistant().rarity(Rarity.EPIC));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, list, flag);
		list.add(Component.translatable("item.murmol.murmol_tale.description_0"));
		list.add(Component.translatable("item.murmol.murmol_tale.description_1"));
		list.add(Component.translatable("item.murmol.murmol_tale.description_2"));
		list.add(Component.translatable("item.murmol.murmol_tale.description_3"));
		list.add(Component.translatable("item.murmol.murmol_tale.description_4"));
		list.add(Component.translatable("item.murmol.murmol_tale.description_5"));
		list.add(Component.translatable("item.murmol.murmol_tale.description_6"));
	}

	@Override
	public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity, InteractionHand hand) {
		boolean retval = super.onEntitySwing(itemstack, entity, hand);
		KoubanxueProcedure.execute(entity.level(), entity);
		return retval;
	}
}