package net.mcr.murmol.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.ItemStackHandler;

import net.mcr.murmol.init.MurmolModBlockEntities;

public class SpiritTableBlockEntity extends BlockEntity {
	private final ItemStackHandler inventory = new ItemStackHandler(4);

	public SpiritTableBlockEntity(BlockPos pos, BlockState state) {
		super(MurmolModBlockEntities.SPIRIT_TABLE.get(), pos, state);
	}

	public ItemStackHandler getInventory() {
		return this.inventory;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("inventory", this.inventory.serializeNBT(registries));
	}

	@Override
	protected void load(CompoundTag tag, HolderLookup.Provider registries) {
		super.load(tag, registries);
		if (tag.contains("inventory"))
			this.inventory.deserializeNBT(registries, tag.getCompound("inventory"));
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
