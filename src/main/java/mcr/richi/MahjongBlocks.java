package mcr.richi;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;

import mcr.richi.block.FengPanBlock;

/**
 * 麻将内容方块注册。
 */
public class MahjongBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(Registries.BLOCK, MahjongItems.NAMESPACE);

	/** 风盘：麻将牌桌（不渲染方块模型，实体按每格 1x1x1 实心碰撞拼成 2x2x1 桌面；硬度同工作台 2.5，斧头挖掘） */
	public static final DeferredHolder<Block, Block> FENG_PAN =
			REGISTRY.register("fengpan", () -> new FengPanBlock(
					Block.Properties.of()
								.strength(2.5f)
								.sound(net.minecraft.world.level.block.SoundType.WOOD)
								.requiresCorrectToolForDrops()
								.pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK) // 活塞/液体不可推毁
								.noOcclusion()));
}
