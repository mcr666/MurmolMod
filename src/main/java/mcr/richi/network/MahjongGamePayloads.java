package mcr.richi.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;
import mcr.richi.game.riichi.RiichiGame;

/**
 * 对局进行中的网络包。
 * <ul>
 * <li>ActionMessage（C→S）：玩家对局操作（打牌/吃碰杠/立直/荣和/自摸/过）。
 * action 字符串直接复用服务端选项格式；data 为参数（如 "chi" 的组合索引）。</li>
 * <li>EventMessage（S→C）：向客户端推送可执行选项列表（HUD 按钮渲染）。
 * data 格式 ";" 分隔：ron / tsumo / pon / minkan / riichi / chi:&lt;组合索引&gt; / ankan:&lt;code&gt; / kakan:&lt;code&gt;。</li>
 * </ul>
 * 客户端缓存为纯原始类型（该类服务端也会加载）。
 */
public class MahjongGamePayloads {
	/** 客户端缓存：当前可用选项（";" 分隔，空 = 清除）与所属桌原点 */
	public static volatile String clientOptions = "";
	public static volatile long clientOptionsOrigin;

	/** C→S：玩家操作。action ∈ discard/chi/pon/minkan/riichi/ron/tsumo/skip，data 视 action 而定 */
	public record ActionMessage(long originPacked, String action, String data) implements CustomPacketPayload {
		public static final Type<ActionMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_game_action"));

		public static final StreamCodec<RegistryFriendlyByteBuf, ActionMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, ActionMessage msg) -> {
					buf.writeLong(msg.originPacked());
					buf.writeUtf(msg.action(), 32);
					buf.writeUtf(msg.data(), 128);
				},
				(RegistryFriendlyByteBuf buf) -> new ActionMessage(buf.readLong(), buf.readUtf(32), buf.readUtf(128)));

		@Override
		public Type<ActionMessage> type() {
			return TYPE;
		}

		public static void handleData(final ActionMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (context.player() instanceof net.minecraft.server.level.ServerPlayer sp)
						RiichiGame.handleAction(sp, BlockPos.of(message.originPacked()),
								message.action(), message.data());
				});
			}
		}
	}

	/** S→C：选项推送（data 为 ";" 分隔选项，空串 = 清除该桌选项） */
	public record EventMessage(long originPacked, String data) implements CustomPacketPayload {
		public static final Type<EventMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_game_event"));

		public static final StreamCodec<RegistryFriendlyByteBuf, EventMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, EventMessage msg) -> {
					buf.writeLong(msg.originPacked());
					buf.writeUtf(msg.data(), 256);
				},
				(RegistryFriendlyByteBuf buf) -> new EventMessage(buf.readLong(), buf.readUtf(256)));

		@Override
		public Type<EventMessage> type() {
			return TYPE;
		}

		public static void handleData(final EventMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					if (message.data().isEmpty()) {
						clientOptions = "";
					} else {
						clientOptionsOrigin = message.originPacked();
						clientOptions = message.data();
					}
				});
			}
		}
	}

	/** 服务端便捷方法：给某玩家推选项（空串清除） */
	public static void sendOptions(net.minecraft.server.level.ServerPlayer player, BlockPos origin, String options) {
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
				new EventMessage(origin.asLong(), options));
	}

	public static void register() {
		MurmolMod.addNetworkMessage(ActionMessage.TYPE, ActionMessage.STREAM_CODEC, ActionMessage::handleData);
		MurmolMod.addNetworkMessage(EventMessage.TYPE, EventMessage.STREAM_CODEC, EventMessage::handleData);
	}
}
