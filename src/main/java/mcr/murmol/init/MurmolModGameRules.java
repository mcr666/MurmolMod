package mcr.murmol.init;

import net.minecraft.world.level.GameRules;

/**
 * 本 mod 游戏规则（/gamerule 可改）：
 * <ul>
 *   <li>{@link #REQUIRE_FORM_UNLOCKED}：变形前强制检查形态已通过残页解锁（默认关闭）</li>
 *   <li>{@link #SPIRIT_TABLE_XP_COST}：唤灵台变形消耗经验等级（默认开启；消耗 {@link #SPIRIT_TABLE_XP_AMOUNT} 级，创造免费）</li>
 *   <li>{@link #ALLOW_SCRAP_SOUL_RECIPE}：允许用古代知识残页在工作台合成形态灵魂物品（默认开启）</li>
 *   <li>{@link #FERAL_ELYTRA_FLIGHT}：会飞形态使用鞘翅式滑翔（等同隐形不占位鞘翅）替代按键悬停飞行（默认关闭）</li>
 * </ul>
 * 类在 mod 构造期加载，GameRules 静态注册在世界创建前完成。
 */
public final class MurmolModGameRules {
	private MurmolModGameRules() {
	}

	/** 唤灵台变形消耗的经验等级数 */
	public static final int SPIRIT_TABLE_XP_AMOUNT = 5;

	public static final GameRules.Key<GameRules.BooleanValue> REQUIRE_FORM_UNLOCKED =
			GameRules.register("murmolRequireFormUnlocked", GameRules.Category.PLAYER,
					GameRules.BooleanValue.create(false));

	public static final GameRules.Key<GameRules.BooleanValue> SPIRIT_TABLE_XP_COST =
			GameRules.register("murmolSpiritTableXpCost", GameRules.Category.PLAYER,
					GameRules.BooleanValue.create(true));

	public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SCRAP_SOUL_RECIPE =
			GameRules.register("murmolAllowScrapSoulRecipe", GameRules.Category.PLAYER,
					GameRules.BooleanValue.create(true));

	/** 会飞形态（月蛾/文鳐/狰/黯忿）的飞行机制：false=长按跳跃悬停上升；true=鞘翅式滑翔（隐形虚拟鞘翅） */
	public static final GameRules.Key<GameRules.BooleanValue> FERAL_ELYTRA_FLIGHT =
			GameRules.register("murmolFeralElytraFlight", GameRules.Category.PLAYER,
					GameRules.BooleanValue.create(false));

	/**
	 * 幻星瘟疫（astral_infection_biome）生成噪声气候范围等级 1~10：
	 * 1=几乎不生成（气候区间收窄到约 10%），10=满气候范围，默认 2=当前减半效果。
	 * 仅影响新世界的噪声预设构建（BiomeSourcePreset 初始化时读取默认值），
	 * 故放置在 PLAYER 分类以便出现在创建世界界面的「游戏规则」中；已生成区块不回溯。
	 */
	public static final GameRules.Key<GameRules.IntegerValue> ASTRAL_INFECTION_RANGE =
			GameRules.register("murmolAstralInfectionRange", GameRules.Category.PLAYER,
					GameRules.IntegerValue.create(2));

	/** 范围规则的上下限（界面/指令校验与预设缩放共用） */
	public static final int ASTRAL_INFECTION_RANGE_MIN = 1;
	public static final int ASTRAL_INFECTION_RANGE_MAX = 10;

	/** 预设构建期读取的范围值（越界钳制；GameRules 默认值即预设缩放依据） */
	public static int astralInfectionRangeForPreset(int raw) {
		return Math.max(ASTRAL_INFECTION_RANGE_MIN, Math.min(ASTRAL_INFECTION_RANGE_MAX, raw));
	}

	/**
	 * 范围等级 → 气候区间半宽系数：range=2 时 0.5（现行默认减半效果），
	 * range=10 时 1.0（满范围），range=1 时 0.1（极窄、几乎不生成）。线性映射 scale=range/4。
	 */
	public static float astralInfectionHalfWidth(int range) {
		return astralInfectionRangeForPreset(range) / 4.0F;
	}
}
