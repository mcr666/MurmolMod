package mcr.richi;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;

import mcr.richi.block.FengPanBlock;

/**
 * 麻雀内容方块注册。
 */
public class MahjongBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(Registries.BLOCK, MahjongItems.NAMESPACE);

	/** 风盘：麻雀牌桌（不渲染方块模型，实体按每格 1x1x1 实心碰撞拼成 2x2x1 桌面；挖掘速度/类型同信标——无 mineable 标签、任意工具同速） */
	public static final DeferredHolder<Block, Block> FENG_PAN =
			REGISTRY.register("fengpan", () -> new FengPanBlock(
					Block.Properties.of()
								.strength(3.0f) // 与信标一致：任意工具同速挖掘、不要求正确工具掉落
								.sound(net.minecraft.world.level.block.SoundType.GLASS)
								.pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK) // 活塞/液体不可推毁
								.noOcclusion()));
}
