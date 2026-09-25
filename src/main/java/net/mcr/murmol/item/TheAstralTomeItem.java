package net.mcr.murmol.item;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

public class TheAstralTomeItem extends Item {
	private static final ResourceLocation BOOK_ID = ResourceLocation.fromNamespaceAndPath("murmol", "astral_tame");

	public TheAstralTomeItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack itemstack) {
		return new ItemStack(this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		// Patchouli 为必要前置（mods.toml type=required），无需再检查 ModList
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			PatchouliAPI.get().openBookGUI(serverPlayer, BOOK_ID);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
