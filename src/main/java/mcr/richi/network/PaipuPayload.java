package mcr.richi.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * 牌谱查看器网络包：C→S 请求某桌牌谱（服务端读 data/mahjong/&lt;x&gt;_&lt;y&gt;_&lt;z&gt;.log 全文），
 * S→C 返回内容（无记录时 content 为空串，客户端显示"无牌谱"）。content 上限 64K 字符。
 */
public class PaipuPayload {
	public record Request(long originPacked, int gameIdx) implements CustomPacketPayload {
		public static final Type<Request> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_request"));

		public static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC = StreamCodec.of(
				PaipuPayload::writeRequest, PaipuPayload::readRequest);

		@Override
		public Type<Request> type() {
			return TYPE;
		}

		public static void handleData(final Request message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer sp))
						return;
					var origin = net.minecraft.core.BlockPos.of(message.originPacked());
					String content = mcr.richi.game.MahjongGameLog.read(sp.serverLevel(), origin);
					net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
							new Data(message.originPacked(), content == null ? "" : content, message.gameIdx()));
				});
			}
		}
	}

	public record Data(long originPacked, String content, int gameIdx) implements CustomPacketPayload {
		public static final Type<Data> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_data"));

		public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.of(
				PaipuPayload::writeData, PaipuPayload::readData);

		@Override
		public Type<Data> type() {
			return TYPE;
		}

		public static void handleData(final Data message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> mcr.richi.client.PaipuReplay.open(message.originPacked(),
							message.content(), mcr.richi.client.PaipuReplay.consumePendingFromList(),
							message.gameIdx()));
			}
		}
	}

	private static void writeRequest(RegistryFriendlyByteBuf buf, Request msg) {
		buf.writeLong(msg.originPacked());
		buf.writeVarInt(msg.gameIdx());
	}

	private static Request readRequest(RegistryFriendlyByteBuf buf) {
		return new Request(buf.readLong(), buf.readVarInt());
	}

	private static void writeData(RegistryFriendlyByteBuf buf, Data msg) {
		buf.writeLong(msg.originPacked());
		buf.writeVarInt(msg.gameIdx());
		buf.writeUtf(msg.content() == null ? "" : msg.content(), 2_097_152); // 一桌多局牌谱可达数百 KB
	}

	private static Data readData(RegistryFriendlyByteBuf buf) {
		return new Data(buf.readLong(), buf.readUtf(2_097_152), buf.readVarInt());
	}

	public record ListRequest() implements CustomPacketPayload {
		public static final Type<ListRequest> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_list_request"));

		public static final StreamCodec<RegistryFriendlyByteBuf, ListRequest> STREAM_CODEC = StreamCodec.of(
				(buf, msg) -> {
				}, buf -> new ListRequest());

		@Override
		public Type<ListRequest> type() {
			return TYPE;
		}

		public static void handleData(final ListRequest message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer sp))
						return;
					var entries = mcr.richi.game.MahjongGameLog.list(sp.serverLevel());
					net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
							new ListData(entries.toArray(new String[0])));
				});
			}
		}
	}

	public record ListData(String[] entries) implements CustomPacketPayload {
		public static final Type<ListData> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_list_data"));

		public static final StreamCodec<RegistryFriendlyByteBuf, ListData> STREAM_CODEC = StreamCodec.of(
				PaipuPayload::writeListData, PaipuPayload::readListData);

		@Override
		public Type<ListData> type() {
			return TYPE;
		}

		public static void handleData(final ListData message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> mcr.richi.client.PaipuListScreen.apply(message.entries()));
			}
		}
	}

	private static void writeListData(RegistryFriendlyByteBuf buf, ListData msg) {
		buf.writeVarInt(msg.entries() == null ? 0 : msg.entries().length);
		if (msg.entries() != null)
			for (String e : msg.entries())
				buf.writeUtf(e, 256);
	}

	private static ListData readListData(RegistryFriendlyByteBuf buf) {
		int n = buf.readVarInt();
		String[] entries = new String[n];
		for (int i = 0; i < n; i++)
			entries[i] = buf.readUtf(256);
		return new ListData(entries);
	}

	public static void register() {
		MurmolMod.addNetworkMessage(ListRequest.TYPE, ListRequest.STREAM_CODEC, ListRequest::handleData);
		MurmolMod.addNetworkMessage(ListData.TYPE, ListData.STREAM_CODEC, ListData::handleData);
		MurmolMod.addNetworkMessage(Request.TYPE, Request.STREAM_CODEC, Request::handleData);
		MurmolMod.addNetworkMessage(Data.TYPE, Data.STREAM_CODEC, Data::handleData);
	}
}
