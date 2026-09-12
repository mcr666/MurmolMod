package net.mcr.murmol.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

public class CobbledastralstoneBlock extends Block {
	public CobbledastralstoneBlock() {
		super(BlockBehaviour.Properties.of().strength(1.7f, 10f).requiresCorrectToolForDrops());
	}
}