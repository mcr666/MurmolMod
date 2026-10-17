package mcr.murmol;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.server.TickTask;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

import mcr.murmol.network.MurmolModVariables;
import mcr.murmol.init.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

@Mod("murmol")
public class MurmolMod {
	public static final Logger LOGGER = LogManager.getLogger(MurmolMod.class);
	public static final String MODID = "murmol";

	public MurmolMod(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
		// Start of user code block mod constructor
		modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, MurmolModConfig.SPEC);
		// 客户端注册配置界面（模组菜单中的"配置"按钮）
		if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
			modContainer.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
					net.neoforged.neoforge.client.gui.ConfigurationScreen::new);
		}
		// End of user code block mod constructor
		// 触发游戏规则静态注册（须在世界创建前完成）
		mcr.murmol.init.MurmolModGameRules.class.getName();
		NeoForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::registerNetworking);
		// TLM 联动延后到 CommonSetup：构造阶段触碰 TLM 实体类会在注册表冻结前后竞态崩溃
		modEventBus.addListener(this::onCommonSetup);
		MurmolModSounds.REGISTRY.register(modEventBus);
		MurmolModBlocks.REGISTRY.register(modEventBus);
		MurmolModBlockEntities.REGISTRY.register(modEventBus);
		MurmolModItems.REGISTRY.register(modEventBus);
		MurmolModEntities.REGISTRY.register(modEventBus);
		MurmolModTabs.REGISTRY.register(modEventBus);
		MurmolModVariables.ATTACHMENT_TYPES.register(modEventBus);
		MurmolModMobEffects.REGISTRY.register(modEventBus);
		MurmolModPotions.REGISTRY.register(modEventBus);
		MurmolModMenus.REGISTRY.register(modEventBus);
		MurmolModParticleTypes.REGISTRY.register(modEventBus);
		MurmolModAttributes.REGISTRY.register(modEventBus);
		MurmolModRecipeSerializers.REGISTRY.register(modEventBus);
		// 麻雀内容（移植测试，注册在 mcr.mahjong 包）
		mcr.richi.MahjongItems.REGISTRY.register(modEventBus);
		mcr.richi.MahjongBlocks.REGISTRY.register(modEventBus);
		mcr.richi.MahjongBlockEntities.REGISTRY.register(modEventBus);
		mcr.richi.MahjongComponents.COMPONENTS.register(modEventBus);
		mcr.richi.MahjongLoot.TYPES.register(modEventBus); // 随机牌面 loot function（战利品表用）
		// 麻雀回合倒计时叠加层 payload（须在 RegisterPayloadHandlersEvent 前注册）
		mcr.richi.network.MahjongCountdownPayload.register();
		// 风盘点播唱片（右击风盘临时压制原版 BGM 播放唱片内容）
		mcr.richi.network.FengPanMusicPayload.register();
		// 麻雀大厅（等待队列/管理）与对局进行 payload
		mcr.richi.network.MahjongLobbyPayloads.register();
		mcr.richi.network.MahjongGamePayloads.register();
		// 麻雀结算界面（和牌结算/终局结算）+ 点数变化界面
		mcr.richi.network.MahjongSettlementPayload.register();
		mcr.richi.network.MahjongPointDeltaPayload.register();
		// 麻雀牌局渲染状态包（服务端过滤后整桌快照，客户端自建显示实体）
		mcr.richi.network.MahjongTablePayload.register();
		mcr.richi.network.PaipuPayload.register();
		// Murmol NPC 对话叠加层 payload
		mcr.murmol.network.NpcDialoguePayloads.register();
		// 灵气指南针目标同步（服务端定位最近幻星瘟疫群系）
		mcr.murmol.network.AstralCompassPayload.register();
		// 形态飞行机制游戏规则（悬停飞行/鞘翅滑翔）同步
		mcr.murmol.network.FeralFlightPayloads.register();
		// 两仪「游戏规则」设置界面（请求/切换/同步）
		mcr.murmol.network.MurmolGameRulePayloads.register();
		// 塑魂之约确认对话（打开/确认绑定）
		mcr.murmol.network.SoulbindPayloads.register();
		// Start of user code block mod init
		// NeoOrigins 联动：起源 ↔ 形态双向同步（软依赖，未安装时跳过注册）
		mcr.murmol.compat.NeoOriginsCompat.register();
		// FirstPersonModel 联动（客户端，软依赖）：第一人称兽身头部隐藏 + 后向偏移 0.5 格
		if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
			mcr.murmol.compat.FPMCompat.register();
		// TouhouLittleMaid 联动（软依赖）：女仆可参与麻雀牌局；未安装时全部联动逻辑静默跳过
		// 改为在 CommonSetup 阶段执行（见 onCommonSetup），构造阶段不能触碰 TLM 实体类
		// End of user code block mod init
	}

	private void onCommonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
		event.enqueueWork(mcr.murmol.compat.TlmCompatBridge::init);
	}

	// Start of user code block mod methods
	// End of user code block mod methods
	private static boolean networkingRegistered = false;
	private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

	private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
	}

	public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
		if (networkingRegistered)
			throw new IllegalStateException("Cannot register new network messages after networking has been registered");
		MESSAGES.put(id, new NetworkMessage<>(reader, handler));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void registerNetworking(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(MODID);
		MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
		networkingRegistered = true;
	}

	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	@SubscribeEvent
	public void tick(ServerTickEvent.Post event) {
		int currentTick = event.getServer().getTickCount();
		IntObjectPair<Runnable> work;
		while ((work = workToBeScheduled.poll()) != null) {
			workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
		}
		while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
			workQueue.poll().run();
		}
	}
}