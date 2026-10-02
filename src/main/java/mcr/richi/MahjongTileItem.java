package mcr.richi;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 麻将牌物品：单个物品，牌面由数据组件 mahjong_tile_code（即 NBT）驱动。
 * 模型切换：客户端 ItemProperties "code" + 模型 overrides（见 MahjongClient）。
 * 牌面代码：0m..9m=0..9，0p..9p=10..19，0s..9s=20..29，1z..7z=30..36；无组件 = 未知牌。
 */
public class MahjongTileItem extends Item {
	/** 牌面代码 → 牌名后缀（如 1m、7z），按注册顺序排列 */
	public static final Map<Integer, String> CODE_TO_NAME = new LinkedHashMap<>();

	static {
		for (String suit : new String[] { "m", "p", "s" }) {
			CODE_TO_NAME.put(CODE_TO_NAME.size(), "0" + suit);
			for (int i = 1; i <= 9; i++)
				CODE_TO_NAME.put(CODE_TO_NAME.size(), i + suit);
		}
		for (int i = 1; i <= 7; i++)
			CODE_TO_NAME.put(CODE_TO_NAME.size(), i + "z");
	}

	public MahjongTileItem(Properties properties) {
		super(properties);
	}

	/** 创建指定牌面的物品堆（无牌面代码 = 未知牌） */
	public static ItemStack create(int code) {
		ItemStack stack = new ItemStack(MahjongItems.MAHJONG_TILE.get());
		if (code >= 0)
			stack.set(MahjongComponents.TILE_CODE.get(), code);
		return stack;
	}

	/** 读取牌面代码，无组件时返回 -1（未知牌） */
	public static int getCode(ItemStack stack) {
		Integer code = stack.get(MahjongComponents.TILE_CODE.get());
		return code == null ? -1 : code;
	}

	/** 按牌面显示名称（复用旧 34 张牌的翻译键） */
	@Override
	public Component getName(ItemStack stack) {
		int code = getCode(stack);
		String name = CODE_TO_NAME.get(code);
		if (name != null)
			return Component.translatable("item.richi.mahjong_" + name);
		return Component.translatable("item.richi.mahjong_tile_unknown");
	}

	/** 牌面代码列表（工具方法，供标签页/模型生成遍历） */
	public static List<Integer> validCodes() {
		return List.copyOf(CODE_TO_NAME.keySet());
	}
}
