package net.mcr.murmol.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

public class AstralBlockBlock extends Block {
	public AstralBlockBlock() {
		super(BlockBehaviour.Properties.of().strength(30f, 99f).lightLevel(blockstate -> 10).requiresCorrectToolForDrops().hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true));
	}
}