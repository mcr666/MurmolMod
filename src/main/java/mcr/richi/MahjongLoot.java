package mcr.richi;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

/**
 * 麻雀战利品支持：随机牌面 loot function。
 * 用法（战利品表 JSON）："function": "richi:set_mahjong_tile_code" —— 给 richi:mahjong_tile 随机写 0..36 牌面码。
 */
public final class MahjongLoot {
	public static final DeferredRegister<LootItemFunctionType<?>> TYPES = DeferredRegister
			.create(Registries.LOOT_FUNCTION_TYPE, MahjongItems.NAMESPACE);

	public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetMahjongTileCode>> SET_TILE_CODE = TYPES
			.register("set_mahjong_tile_code", () -> new LootItemFunctionType<>(SetMahjongTileCode.CODEC));

	private MahjongLoot() {
	}

	public static final class SetMahjongTileCode extends LootItemConditionalFunction {
		public static final MapCodec<SetMahjongTileCode> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder
				.mapCodec(i -> commonFields(i).apply(i, SetMahjongTileCode::new));

		private SetMahjongTileCode(java.util.List<LootItemCondition> conditions) {
			super(conditions);
		}

		@Override
		public LootItemFunctionType<SetMahjongTileCode> getType() {
			return SET_TILE_CODE.get();
		}

		@Override
		public ItemStack run(ItemStack stack, LootContext ctx) {
			// 随机牌面：0m..9m=0..9，0p..9p=10..19，0s..9s=20..29，1z..7z=30..36（无红宝牌）
			stack.set(MahjongComponents.TILE_CODE.get(), ctx.getRandom().nextInt(37));
			return stack;
		}
	}
}
