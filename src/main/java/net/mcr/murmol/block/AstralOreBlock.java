package net.mcr.murmol.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

import net.mcr.murmol.procedures.AstralOreShiTiZaiFangKuaiZhongPengZhuangShiProcedure;

public class AstralOreBlock extends Block {
	public AstralOreBlock() {
		super(BlockBehaviour.Properties.of().strength(20f, 10f).lightLevel(blockstate -> 4).requiresCorrectToolForDrops().hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true));
	}

	@Override
	public void entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity) {
		super.entityInside(blockstate, world, pos, entity);
		AstralOreShiTiZaiFangKuaiZhongPengZhuangShiProcedure.execute(entity);
	}

	@Override
	public void stepOn(Level world, BlockPos pos, BlockState blockstate, Entity entity) {
		super.stepOn(world, pos, blockstate, entity);
		AstralOreShiTiZaiFangKuaiZhongPengZhuangShiProcedure.execute(entity);
	}
}