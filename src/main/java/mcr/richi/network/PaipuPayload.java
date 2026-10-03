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
		MurmolMod.addNetworkMessage(DownloadRequest.TYPE, DownloadRequest.STREAM_CODEC, DownloadRequest::handleData);
		MurmolMod.addNetworkMessage(DownloadData.TYPE, DownloadData.STREAM_CODEC, DownloadData::handleData);
		MurmolMod.addNetworkMessage(UploadData.TYPE, UploadData.STREAM_CODEC, UploadData::handleData);
		MurmolMod.addNetworkMessage(UploadResult.TYPE, UploadResult.STREAM_CODEC, UploadResult::handleData);
	}

	// ==================================================================
	// 牌谱上传/下载（客户端本地 richi 目录 ↔ 服务端 data/mahjong）
	// ==================================================================

	/** 下载：C→S 请求整桌 .log 全文（服务端 list 条目按局拆分，下载按桌一文件） */
	public record DownloadRequest(long originPacked) implements CustomPacketPayload {
		public static final Type<DownloadRequest> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_download_request"));

		public static final StreamCodec<RegistryFriendlyByteBuf, DownloadRequest> STREAM_CODEC = StreamCodec.of(
				PaipuPayload::writeDownloadRequest, PaipuPayload::readDownloadRequest);

		@Override
		public Type<DownloadRequest> type() {
			return TYPE;
		}

		public static void handleData(final DownloadRequest message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer sp))
						return;
					var origin = net.minecraft.core.BlockPos.of(message.originPacked());
					String content = mcr.richi.game.MahjongGameLog.read(sp.serverLevel(), origin);
					net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
							new DownloadData(message.originPacked(), content == null ? "" : content));
				});
			}
		}
	}

	/** 下载：S→C 整桌牌谱全文（客户端保存到 游戏根目录/richi/&lt;x&gt;_&lt;y&gt;_&lt;z&gt;.log） */
	public record DownloadData(long originPacked, String content) implements CustomPacketPayload {
		public static final Type<DownloadData> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_download_data"));

		public static final StreamCodec<RegistryFriendlyByteBuf, DownloadData> STREAM_CODEC = StreamCodec.of(
				PaipuPayload::writeDownloadData, PaipuPayload::readDownloadData);

		@Override
		public Type<DownloadData> type() {
			return TYPE;
		}

		public static void handleData(final DownloadData message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> mcr.richi.client.PaipuLocal.save(message.originPacked(), message.content()));
			}
		}
	}

	/** 上传：C→S 客户端牌谱全文（服务端覆写 data/mahjong/&lt;x&gt;_&lt;y&gt;_&lt;z&gt;.log） */
	public record UploadData(long originPacked, String content) implements CustomPacketPayload {
		public static final Type<UploadData> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_upload_data"));

		public static final StreamCodec<RegistryFriendlyByteBuf, UploadData> STREAM_CODEC = StreamCodec.of(
				PaipuPayload::writeUploadData, PaipuPayload::readUploadData);

		@Override
		public Type<UploadData> type() {
			return TYPE;
		}

		public static void handleData(final UploadData message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer sp))
						return;
					boolean ok = message.content() != null && message.content().length() <= 2_097_151
							&& mcr.richi.game.MahjongGameLog.writeFile(sp.serverLevel(),
									net.minecraft.core.BlockPos.of(message.originPacked()), message.content());
					net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
							new UploadResult(ok));
				});
			}
		}
	}

	/** 上传结果：S→C（客户端聊天框提示） */
	public record UploadResult(boolean ok) implements CustomPacketPayload {
		public static final Type<UploadResult> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "paipu_upload_result"));

		public static final StreamCodec<RegistryFriendlyByteBuf, UploadResult> STREAM_CODEC = StreamCodec.of(
				(buf, msg) -> buf.writeBoolean(msg.ok()), buf -> new UploadResult(buf.readBoolean()));

		@Override
		public Type<UploadResult> type() {
			return TYPE;
		}

		public static void handleData(final UploadResult message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> mcr.richi.client.PaipuLocal.uploadResult(message.ok()));
			}
		}
	}

	private static void writeDownloadRequest(RegistryFriendlyByteBuf buf, DownloadRequest msg) {
		buf.writeLong(msg.originPacked());
	}

	private static DownloadRequest readDownloadRequest(RegistryFriendlyByteBuf buf) {
		return new DownloadRequest(buf.readLong());
	}

	private static void writeDownloadData(RegistryFriendlyByteBuf buf, DownloadData msg) {
		buf.writeLong(msg.originPacked());
		buf.writeUtf(msg.content() == null ? "" : msg.content(), 2_097_152);
	}

	private static DownloadData readDownloadData(RegistryFriendlyByteBuf buf) {
		return new DownloadData(buf.readLong(), buf.readUtf(2_097_152));
	}

	private static void writeUploadData(RegistryFriendlyByteBuf buf, UploadData msg) {
		buf.writeLong(msg.originPacked());
		buf.writeUtf(msg.content() == null ? "" : msg.content(), 2_097_152);
	}

	private static UploadData readUploadData(RegistryFriendlyByteBuf buf) {
		return new UploadData(buf.readLong(), buf.readUtf(2_097_152));
	}
}
