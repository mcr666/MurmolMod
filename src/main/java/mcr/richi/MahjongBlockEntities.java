package mcr.richi;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import mcr.richi.block.entity.MahjongTileBlockEntity;

/**
 * 麻雀内容方块实体注册（richi 命名空间）。
 */
public class MahjongBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
			DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MahjongItems.NAMESPACE);

	/** 平放麻将牌方块实体（绑定 mahjong_tile 方块） */
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MahjongTileBlockEntity>> MAHJONG_TILE =
			REGISTRY.register("mahjong_tile",
					() -> BlockEntityType.Builder
							.of(MahjongTileBlockEntity::new, MahjongBlocks.MAHJONG_TILE_BLOCK.get())
							.build(null));
}
