package mcr.murmol.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;
import mcr.murmol.feral.FeralFormToggles;
import mcr.murmol.feral.FeralForms;

import java.util.HashMap;
import java.util.Map;

/**
 * 两仪物品（形态开关 GUI）的网络同步：
 * - Sync（服务端→客户端）：全量同步各形态启用状态，登录/修改/打开 GUI 时发送；
 *   客户端缓存于 CLIENT_ENABLED，供屏幕渲染与变形入口本地判定
 * - Toggle（客户端→服务端）：管理员请求切换某形态，服务端做 3 级权限校验
 */
public class FormTogglePayloads {
	/** 客户端缓存：形态 id -> 是否启用（缺失视为启用） */
	public static final Map<String, Boolean> CLIENT_ENABLED = new HashMap<>();

	// ==================== S -> C 全量同步 ====================

	public record SyncMessage(Map<String, Boolean> states) implements CustomPacketPayload {
		public static final Type<SyncMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "form_toggles_sync"));

		public static final StreamCodec<RegistryFriendlyByteBuf, SyncMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, SyncMessage msg) -> {
					buf.writeVarInt(msg.states().size());
					msg.states().forEach((id, enabled) -> {
						buf.writeUtf(id);
						buf.writeBoolean(enabled);
					});
				},
				(RegistryFriendlyByteBuf buf) -> {
					int size = buf.readVarInt();
					Map<String, Boolean> states = new HashMap<>();
					for (int i = 0; i < size; i++)
						states.put(buf.readUtf(), buf.readBoolean());
					return new SyncMessage(states);
				});

		@Override
		public Type<SyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final SyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> CLIENT_ENABLED.putAll(message.states()));
			}
		}
	}

	// ==================== C -> S 切换请求 ====================

	public record ToggleMessage(String formId) implements CustomPacketPayload {
		public static final Type<ToggleMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "form_toggles_toggle"));

		public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, ToggleMessage msg) -> buf.writeUtf(msg.formId()),
				(RegistryFriendlyByteBuf buf) -> new ToggleMessage(buf.readUtf()));

		@Override
		public Type<ToggleMessage> type() {
			return TYPE;
		}

		public static void handleData(final ToggleMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player) {
				context.enqueueWork(() -> {
					// 仅管理员（3 级权限）可修改
					if (!player.hasPermissions(3))
						return;
					// 校验形态 id 合法
					if (FeralFormToggles.formIds().contains(message.formId()) && FeralForms.byId(message.formId()) != null) {
						FeralFormToggles.serverToggle(player, message.formId());
					}
				});
			}
		}
	}

	/** 服务端向所有玩家发送全量开关状态 */
	public static void syncToAll(MinecraftServer server) {
		PacketDistributor.sendToAllPlayers(new SyncMessage(FeralFormToggles.serverStates(server)));
	}

	/** 服务端向单个玩家发送全量开关状态（登录/打开 GUI） */
	public static void syncTo(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new SyncMessage(FeralFormToggles.serverStates(player.server)));
	}

	public static void register() {
		MurmolMod.addNetworkMessage(SyncMessage.TYPE, SyncMessage.STREAM_CODEC, SyncMessage::handleData);
		MurmolMod.addNetworkMessage(ToggleMessage.TYPE, ToggleMessage.STREAM_CODEC, ToggleMessage::handleData);
	}
}
