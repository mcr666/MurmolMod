package mcr.richi;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import mcr.murmol.MurmolMod;

/**
 * 麻雀内容创造模式标签页。
 */
public class MahjongCreativeTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MahjongItems.NAMESPACE);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAHJONG_TEST = REGISTRY.register("mahjong_test",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("item_group.richi.mahjong_test"))
					.icon(() -> MahjongTileItem.create(21)) // 1s（一索）
					.displayItems((parameters, tabData) -> {
						// 麻雀牌：单个物品 + NBT 牌面代码（0..36），共 34 张（0m/0p/0s 为红五宝牌）
						for (int code : MahjongTileItem.validCodes())
							tabData.accept(MahjongTileItem.create(code));
						tabData.accept(MahjongTileItem.create(-1)); // 未知牌（背面）
						// 风盘（麻雀牌桌）
						tabData.accept(MahjongItems.FENG_PAN.get());
						// 点棒沿用独立物品
						for (int i = 0; i < MahjongItems.IDS.size(); i++)
							tabData.accept(MahjongItems.ITEMS.get(i).get());
					})
					.build());
}
