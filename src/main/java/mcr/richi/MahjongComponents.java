package mcr.richi;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;

import mcr.richi.MahjongItems;

/**
 * 麻将内容数据组件（1.21.1 中即物品的持久化 NBT）。
 */
public class MahjongComponents {
	public static final DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MahjongItems.NAMESPACE);

	/** 牌面代码：0m..9m=0..9，0p..9p=10..19，0s..9s=20..29，1z..7z=30..36；无组件视为未知牌 */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TILE_CODE =
			COMPONENTS.registerComponentType("mahjong_tile_code", builder -> builder
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT));
}
