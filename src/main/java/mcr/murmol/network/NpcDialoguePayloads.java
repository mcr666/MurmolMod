package mcr.murmol.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * Murmol NPC 对话叠加层网络包。
 * <ul>
 * <li>OpenMessage（S→C）：右键 NPC 时推送对话选项（title 为标题翻译键，
 * data 为 ";" 分隔的选项翻译键列表，服务端选项顺序即数字键 1..n 顺序）。</li>
 * <li>SelectMessage（C→S）：玩家按数字键选择第 index（1 起）个选项，服务端校验后执行。</li>
 * </ul>
 * 客户端缓存为纯原始类型（该类服务端也会加载）。
 */
public class NpcDialoguePayloads {
	/** 客户端缓存：当前对话 NPC 实体 id、标题翻译键、选项翻译键（";" 连接，空 = 无对话） */
	public static volatile int clientNpcId;
	public static volatile String clientTitle = "";
	public static volatile String clientOptions = "";

	/** S→C：打开/刷新 NPC 对话（options 为 ";" 分隔翻译键，服务端顺序 = 数字键顺序） */
	public record OpenMessage(int npcId, String title, String options) implements CustomPacketPayload {
		public static final Type<OpenMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "npc_dialogue_open"));

		public static final StreamCodec<RegistryFriendlyByteBuf, OpenMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, OpenMessage msg) -> {
					buf.writeVarInt(msg.npcId());
					buf.writeUtf(msg.title(), 128);
					buf.writeUtf(msg.options(), 256);
				},
				(RegistryFriendlyByteBuf buf) -> new OpenMessage(buf.readVarInt(), buf.readUtf(128), buf.readUtf(256)));

		@Override
		public Type<OpenMessage> type() {
			return TYPE;
		}

		public static void handleData(final OpenMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					clientNpcId = message.npcId();
					clientTitle = message.title();
					clientOptions = message.options();
				});
			}
		}
	}

	/** C→S：数字键选择（index 1 起；服务端按当前状态重算选项后校验执行） */
	public record SelectMessage(int npcId, int index) implements CustomPacketPayload {
		public static final Type<SelectMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "npc_dialogue_select"));

		public static final StreamCodec<RegistryFriendlyByteBuf, SelectMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, SelectMessage msg) -> {
					buf.writeVarInt(msg.npcId());
					buf.writeVarInt(msg.index());
				},
				(RegistryFriendlyByteBuf buf) -> new SelectMessage(buf.readVarInt(), buf.readVarInt()));

		@Override
		public Type<SelectMessage> type() {
			return TYPE;
		}

		public static void handleData(final SelectMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (context.player() instanceof net.minecraft.server.level.ServerPlayer sp)
						mcr.murmol.entity.MurmolNpcEntity.handleDialogueSelect(sp, message.npcId(), message.index());
				});
			}
		}
	}

	public static void register() {
		MurmolMod.addNetworkMessage(OpenMessage.TYPE, OpenMessage.STREAM_CODEC, OpenMessage::handleData);
		MurmolMod.addNetworkMessage(SelectMessage.TYPE, SelectMessage.STREAM_CODEC, SelectMessage::handleData);
	}
}
