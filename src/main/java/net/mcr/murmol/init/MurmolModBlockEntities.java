package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.mcr.murmol.block.entity.SpiritTableBlockEntity;
import net.mcr.murmol.MurmolMod;

public class MurmolModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MurmolMod.MODID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpiritTableBlockEntity>> SPIRIT_TABLE = REGISTRY.register("spirit_table",
			() -> BlockEntityType.Builder.of(SpiritTableBlockEntity::new, MurmolModBlocks.SPIRIT_TABLE.get()).build(null));
}
