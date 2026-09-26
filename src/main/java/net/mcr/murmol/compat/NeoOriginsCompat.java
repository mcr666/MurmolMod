package net.mcr.murmol.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.murmol.feral.FeralForms;

/**
 * NeoOrigins 联动桥（软依赖）。
 *
 * 双向同步：
 * - 形态 → 起源：变形后（唤灵台/形态之魂等）以 GM 权限源执行
 *   {@code /neoorigins set <玩家> neoorigins:origin murmol:<form>}。
 *   admin set 路径不派发 OriginChangedEvent，不会回环。
 * - 起源 → 形态：监听 NeoOrigins 的 {@code OriginChangedEvent}（选择界面/
 *   起源之球路径派发），切到 murmol 起源则变形为对应形态，
 *   切到非 murmol 起源则变回人类。另有起源 JSON 的 chosen→execute_command
 *   兜底路径，两条路径经同形态 no-op 守卫幂等。
 *
 * 编译期引用 {@code com.cyberday1.neoorigins.api.event.OriginChangedEvent}
 * （src/apistubs 手写 stub，不打包）；运行时类加载仅发生在
 * {@link #register()} 且 NeoOrigins 已安装时，故未安装不受影响。
 */
public final class NeoOriginsCompat {

	/** NeoOrigins 的默认起源层 id */
	private static final String ORIGIN_LAYER = "neoorigins:origin";
	/** NeoOrigins 模组 id */
	private static final String NEOORIGINS_MODID = "neoorigins";

	private NeoOriginsCompat() {
	}

	/** 是否启用联动 */
	public static boolean isAvailable() {
		return ModList.get() != null && ModList.get().isLoaded(NEOORIGINS_MODID);
	}

	/**
	 * 注册起源变更监听。必须在确认 {@link #isAvailable()} 为 true 后调用
	 * （监听器类引用 NeoOrigins 事件类，未安装时不可加载）。
	 */
	public static void register() {
		if (isAvailable())
			NeoForge.EVENT_BUS.register(OriginSyncListener.class);
	}

	/**
	 * 变形后同步起源：把玩家的 NeoOrigins 起源切到 {@code murmol:<formId>}。
	 * 仅服务端玩家调用生效；NeoOrigins 未安装或目标为人类形态时静默跳过。
	 */
	public static void syncOrigin(ServerPlayer player, FeralForm form) {
		if (!isAvailable() || player == null || form == FeralForms.HUMAN)
			return;
		String command = "neoorigins set " + player.getGameProfile().getName()
				+ " " + ORIGIN_LAYER + " " + net.mcr.murmol.MurmolMod.MODID + ":" + form.getId();
		// 以 GM 权限源执行（/neoorigins set 要求权限 2）；withSuppressedOutput 避免刷屏
		var source = player.createCommandSourceStack()
				.withPermission(2)
				.withSuppressedOutput();
		player.server.getCommands().performPrefixedCommand(source, command);
	}

	/** 起源变更 → 形态同步监听（仅 OriginChangedEvent 可加载时才注册） */
	public static class OriginSyncListener {

		@SubscribeEvent
		public static void onOriginChanged(com.cyberday1.neoorigins.api.event.OriginChangedEvent event) {
			ResourceLocation layer = event.getLayer();
			if (!"neoorigins".equals(layer.getNamespace()) || !"origin".equals(layer.getPath()))
				return;
			ServerPlayer player = event.getEntity();
			ResourceLocation newOrigin = event.getNewOrigin();
			if (newOrigin.getNamespace().equals(net.mcr.murmol.MurmolMod.MODID)) {
				FeralForm form = FeralForms.byId(newOrigin.getPath());
				FeralFormManager.transformFromItem(player.level(), player.getX(), player.getY(), player.getZ(),
						player, form, ItemStack.EMPTY);
			} else if (FeralFormManager.getForm(player) != FeralForms.HUMAN) {
				// 切到非本 mod 起源：变回人类
				FeralFormManager.transformFromItem(player.level(), player.getX(), player.getY(), player.getZ(),
						player, FeralForms.HUMAN, ItemStack.EMPTY);
			}
		}
	}
}
