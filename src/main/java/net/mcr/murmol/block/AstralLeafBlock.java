package net.mcr.murmol.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LeavesBlock;

public class AstralLeafBlock extends LeavesBlock {
	public AstralLeafBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.CHERRY_LEAVES).strength(2f, 10f).lightLevel(blockstate -> 3).requiresCorrectToolForDrops().noOcclusion().hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true)
				.isRedstoneConductor((bs, br, bp) -> false).isSuffocating((bs, br, bp) -> false).isViewBlocking((bs, br, bp) -> false));
	}
}