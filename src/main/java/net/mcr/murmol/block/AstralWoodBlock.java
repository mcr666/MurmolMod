package net.mcr.murmol.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class AstralWoodBlock extends Block {
	public AstralWoodBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(3f, 10f).requiresCorrectToolForDrops());
	}
}