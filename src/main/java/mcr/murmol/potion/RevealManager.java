package mcr.murmol.potion;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FeralForms;
import mcr.murmol.feral.FeralFormToggles;
import mcr.murmol.init.MurmolModMobEffects;
import mcr.murmol.network.MurmolModVariables;

/**
 * 显形效果逻辑：
 * - 右键照妖镜方块获得 10s 效果；重复右键刷新时长。
 * - 获得时记录原形态（PlayerVariables.revealOriginalFormId，持久附件）：
 *   变形者临时切为人类，人类临时变为随机形态
 *   （仅从两仪启用中的形态随机；全被禁用时退化为全形态随机）。
 * - 效果自然过期/被牛奶清除（MobEffectEvent Expired/Remove）时复原原形态；
 *   服务端 tick 兜底自愈：记录存在但效果已消失时立即复原（覆盖事件未触发/重登等情况）。
 * - 效果期间灵魂物品与照妖镜不可用（isLocked 由各物品入口检查）。
 */
@EventBusSubscriber
public final class RevealManager {

	private RevealManager() {
	}

	/** 效果持续期间为 true：灵魂物品与照妖镜禁止使用 */
	public static boolean isLocked(LivingEntity entity) {
		return entity != null && entity.hasEffect(MurmolModMobEffects.REVEAL);
	}

	/** 照妖镜方块右键入口（服务端）：首次记录原形态并临时变形；已有效果则刷新时长 */
	public static void apply(ServerPlayer player) {
		if (player.hasEffect(MurmolModMobEffects.REVEAL)) {
			// 刷新时长（不重掷临时形态；效果结束复原后再次获得才会随机到新形态）
			player.addEffect(new MobEffectInstance(MurmolModMobEffects.REVEAL, 200, 0, false, true));
			return;
		}
		beginReveal(player);
		// 给予 10s 显形效果（重复右键走上方刷新分支）
		player.addEffect(new MobEffectInstance(MurmolModMobEffects.REVEAL, 200, 0, false, true));
	}

	/** 记录原形态并临时替换（变形者→人类，人类→随机形态）。药水路径与方块路径共用。 */
	static void beginReveal(Player player) {
		FeralForm original = FeralFormManager.getForm(player);
		player.getData(MurmolModVariables.PLAYER_VARIABLES).revealOriginalFormId = original.getId();
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		FeralForm temp = original.isFeral() ? FeralForms.HUMAN : randomFeralForm(player);
		switchTemp(player, temp);
	}

	/** 效果结束（过期/移除）时复原原形态 */
	public static void onRevealEnd(net.minecraft.world.entity.Entity entity) {
		if (!(entity instanceof Player player))
			return; // 仅玩家
		restore(player);
	}

	/** tick 兜底：原形态记录存在但效果已消失 → 立即复原（不依赖 MobEffectEvent 触发） */
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
			return;
		if (!player.getData(MurmolModVariables.PLAYER_VARIABLES).revealOriginalFormId.isEmpty())
			restore(player);
	}

	/** 有记录且效果已消失时复原并清空记录；效果仍在则保持临时形态 */
	private static void restore(Player player) {
		String orig = player.getData(MurmolModVariables.PLAYER_VARIABLES).revealOriginalFormId;
		if (orig.isEmpty())
			return;
		if (player.hasEffect(MurmolModMobEffects.REVEAL))
			return; // 效果仍在（如刷新过时长），继续等待
		player.getData(MurmolModVariables.PLAYER_VARIABLES).revealOriginalFormId = "";
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		switchTemp(player, FeralForms.byId(orig));
	}

	private static void switchTemp(Player player, FeralForm form) {
		if (FeralFormManager.getForm(player) == form)
			return;
		FeralFormManager.setForm(player, form);
		if (player instanceof ServerPlayer serverPlayer && player.getServer() != null)
			mcr.murmol.compat.NeoOriginsCompat.syncOrigin(serverPlayer, form);
		player.level().playSound(null, player.blockPosition(),
				SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
	}

	/** 随机取一个形态：优先两仪启用中的非人类形态，全部禁用时退化为全形态随机 */
	private static FeralForm randomFeralForm(Player player) {
		var enabled = FeralFormToggles.formIds().stream()
				.map(FeralForms::byId)
				.filter(f -> FeralFormToggles.isEnabled(f, false))
				.toList();
		var pool = enabled.isEmpty()
				? FeralFormToggles.formIds().stream().map(FeralForms::byId).toList()
				: enabled;
		return pool.get(player.getRandom().nextInt(pool.size()));
	}
}
