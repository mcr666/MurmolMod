package mcr.murmol.compat.tlm;

import java.util.UUID;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import mcr.murmol.MurmolMod;
import mcr.richi.block.FengPanBlock;
import mcr.richi.game.MahjongLobby;
import mcr.richi.game.MahjongParticipantRegistry;

/**
 * TouhouLittleMaid 联动兼容层（软依赖）：仅当 TLM 已加载时由
 * {@code TlmCompatBridge} 惰性加载（Class.forName），未安装 TLM 时本类不触碰。
 * <ul>
 * <li>注册表：女仆实体类型 → {@link TlmMaidParticipant}（入座/钉位/释放走泛化后的麻雀大厅）。</li>
 * <li>每 tick 驱动参牌女仆钉位，并清理已死/卸载/跨维度女仆的牌桌座位（含魂符收回 discard）。</li>
 * <li>入座触发（一）：工作状态设为"雀士"（{@link TlmMaidExtension} 注册的任务）→ 自动走向牌桌入座。</li>
 * <li>入座触发（二）：手持装有女仆的魂符（smart_slab）右键风盘 → 直接召唤并入队。</li>
 * </ul>
 */
public final class TlmCompat {
	public static final String TLM_MODID = "touhou_little_maid";

	private TlmCompat() {
	}

	public static boolean isAvailable() {
		return ModList.get() != null && ModList.get().isLoaded(TLM_MODID);
	}

	/** 仅在 isAvailable() 后调用（TlmCompatBridge 惰性触发） */
	public static void register() {
		if (!isAvailable())
			return;
		// 1) 注册表：女仆 → TlmMaidParticipant 包装（npcJoinTable/pinNpcSeats/leaveNpc 均经此解析）
		MahjongParticipantRegistry.register(EntityMaid.TYPE, e -> TlmMaidParticipant.of((EntityMaid) e));
		// 2) 每 tick 驱动钉位 + 清理（女仆入座后由这里钉在座位，不受大脑/驯服干扰；
		//    魂符收回(discard)/死亡/卸载也在 tickLevel 的 stale 清理中移除座位，不再监听 EntityLeaveLevelEvent——
		//    该事件触发面过宽（chunk 迁移、跨维度等），对局钉位 moveTo 可能误触发，导致女仆退桌。）
		NeoForge.EVENT_BUS.addListener(TlmCompat::onServerTick);
		// 3) 手持带女仆的魂符右键风盘 → 召唤并入队
		NeoForge.EVENT_BUS.addListener(TlmCompat::onRightClickBlock);
		MurmolMod.LOGGER.info("[TLM Compat] TouhouLittleMaid integration registered");
	}

	// ==================================================================
	// 每 tick 驱动钉位 + 清理
	// ==================================================================

	private static void onServerTick(ServerTickEvent.Post event) {
		for (ServerLevel level : event.getServer().getAllLevels())
			TlmMaidParticipant.tickLevel(level);
	}

	// ==================================================================
	// 魂符（smart_slab）右键风盘：召唤并入队
	// ==================================================================

	private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.isCanceled())
			return;
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		ServerLevel level = sp.serverLevel();
		BlockPos origin = event.getPos();
		if (!(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		ItemStack stack = event.getItemStack();
		if (!stack.has(InitDataComponent.MAID_INFO.get()))
			return; // 不是装有女仆的魂符
		event.setCanceled(true); // 接管：不让 TLM 把女仆放到风盘上
		summonAndQueue(sp, origin, stack, event.getHand());
	}

	/** 从魂符数据召唤女仆到桌旁并入队（复用 TLM 存储格式：MAID_INFO 组件 + "Owner" 字段） */
	private static void summonAndQueue(ServerPlayer sp, BlockPos origin, ItemStack stack, InteractionHand hand) {
		ServerLevel level = sp.serverLevel();
		CustomData data = stack.get(InitDataComponent.MAID_INFO.get());
		if (data == null)
			return;
		CompoundTag tag = data.copyTag();
		UUID owner = tag.getUUID("Owner");
		if (!sp.getUUID().equals(owner)) {
			sp.sendSystemMessage(
					Component.translatable("tooltips.touhou_little_maid.smart_slab.not_your_maid"));
			return;
		}
		EntityMaid maid = InitEntities.MAID.get().create(level);
		if (maid == null)
			return;
		maid.load(tag);
		maid.setHomeModeEnable(false);
		// 放到 0 号座位站位（桌外 3.5 格，开阔站立点；入队后由 pinWaiting 钉在当前点）
		Vec3 pos = FengPanBlock.seatStandPos(level, origin, 0);
		maid.moveTo(pos, 0.0f, 0.0f);
		level.addFreshEntity(maid);
		maid.spawnExplosionParticle();
		boolean queued = MahjongLobby.npcJoinTable(level, maid, origin);
		sp.sendSystemMessage(Component.literal("§6<幻星麻雀>§r 女仆已召唤并"
				+ (queued ? "加入等候队列" : "释放（牌桌满/进行中，未入队）")));
		// 魂符转空并加冷却（同 TLM 释放语义）
		sp.setItemInHand(hand, InitItems.SMART_SLAB_EMPTY.get().getDefaultInstance());
		sp.getCooldowns().addCooldown(InitItems.SMART_SLAB_EMPTY.get(), 20);
	}
}
