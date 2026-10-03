package mcr.richi;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.List;

/**
 * 麻雀内容物品注册（移植自 MahjongCraft，素材来自 19mahjong 资源包）。
 * 阶段一：仅注册 34 张麻雀牌 + 4 种点棒 + 未知牌，供创造模式测试标签页展示。
 * 麻雀内容使用独立命名空间 mahjong，assets/data 均位于 mahjong 目录下。
 */
public class MahjongItems {
	/** 麻雀内容独立命名空间 */
	public static final String NAMESPACE = "richi";

	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(Registries.ITEM, NAMESPACE);

	/** 点棒物品 id（不含前缀 mahjong_） */
	public static final List<String> IDS = new ArrayList<>();

	static {
		// 点棒（34 张牌 + 未知牌已由 mahjong_tile 单物品 + NBT 组件取代，旧独立物品已移除）
		// 仅保留 1000 点棒（立直宣言用）；100/5000/10000 点棒已移除
		IDS.add("mahjong_stick_1000");
	}

	/** 单个麻雀牌物品：牌面由数据组件 mahjong_tile_code（NBT）驱动（阶段二起为正式方案） */
	public static final DeferredHolder<Item, Item> MAHJONG_TILE =
			REGISTRY.register("mahjong_tile", () -> new MahjongTileItem(new Item.Properties().stacksTo(4)));

	/** 风盘（麻雀牌桌）的物品形式 */
	public static final DeferredHolder<Item, Item> FENG_PAN =
			REGISTRY.register("fengpan", () -> new net.minecraft.world.item.BlockItem(MahjongBlocks.FENG_PAN.get(), new Item.Properties()));

	public static final List<DeferredHolder<Item, Item>> ITEMS = IDS.stream()
			.map(id -> REGISTRY.register(id, () -> new Item(new Item.Properties())))
			.toList();
}
