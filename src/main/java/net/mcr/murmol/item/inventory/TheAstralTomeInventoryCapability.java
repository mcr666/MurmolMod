package net.mcr.murmol.item.inventory;

import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;

import net.mcr.murmol.world.inventory.AstralBookMenu;
import net.mcr.murmol.init.MurmolModItems;

import javax.annotation.Nonnull;

@EventBusSubscriber
public class TheAstralTomeInventoryCapability extends ComponentItemHandler {
	@SubscribeEvent
	public static void onItemDropped(ItemTossEvent event) {
		if (event.getEntity().getItem().getItem() == MurmolModItems.THE_ASTRAL_TOME.get()) {
			Player player = event.getPlayer();
			if (player.containerMenu instanceof AstralBookMenu)
				player.closeContainer();
		}
	}

	public TheAstralTomeInventoryCapability(MutableDataComponentHolder parent) {
		super(parent, DataComponents.CONTAINER, 4);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return stack.getItem() != MurmolModItems.THE_ASTRAL_TOME.get();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return super.getStackInSlot(slot).copy();
	}
}