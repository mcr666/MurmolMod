package mcr.richi.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * 点数变化界面推送（S→C）：和牌结算界面结束后展示本局四家点数变化（局前 → 局后）。
 * mode=0 展示 / mode=1 关闭。客户端 5s 自动确认（回 settle_confirm 推进）。
 */
public class MahjongPointDeltaPayload {
	public static final int MODE_SHOW = 0, MODE_CLOSE = 1;

	public record PointDeltaMessage(int mode, long originPacked, java.util.List<String> names,
			java.util.List<Integer> before, java.util.List<Integer> after) implements CustomPacketPayload {
		public static final Type<PointDeltaMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_point_delta"));

		public static final StreamCodec<RegistryFriendlyByteBuf, PointDeltaMessage> STREAM_CODEC = StreamCodec.of(
				MahjongPointDeltaPayload::write, MahjongPointDeltaPayload::read);

		@Override
		public Type<PointDeltaMessage> type() {
			return TYPE;
		}

		public static void handleData(final PointDeltaMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					if (message.mode() == MODE_CLOSE) {
						mcr.richi.client.MahjongPointDeltaScreen.closeIfOpen();
						return;
					}
					mcr.richi.client.MahjongPointDeltaScreen.open(message);
				});
			}
		}
	}

	private static void write(RegistryFriendlyByteBuf buf, PointDeltaMessage msg) {
		buf.writeVarInt(msg.mode());
		buf.writeLong(msg.originPacked());
		for (int i = 0; i < 4; i++)
			buf.writeUtf(msg.names().get(i) == null ? "" : msg.names().get(i), 64);
		for (int i = 0; i < 4; i++)
			buf.writeVarInt(msg.before().get(i));
		for (int i = 0; i < 4; i++)
			buf.writeVarInt(msg.after().get(i));
	}

	private static PointDeltaMessage read(RegistryFriendlyByteBuf buf) {
		int mode = buf.readVarInt();
		long origin = buf.readLong();
		java.util.List<String> names = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			names.add(buf.readUtf(64));
		java.util.List<Integer> before = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			before.add(buf.readVarInt());
		java.util.List<Integer> after = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			after.add(buf.readVarInt());
		return new PointDeltaMessage(mode, origin, names, before, after);
	}

	/** 服务端便捷方法：向单个玩家推送点数变化界面 */
	public static void sendTo(net.minecraft.server.level.ServerPlayer player, long originPacked, String[] names,
			int[] before, int[] after) {
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
				new PointDeltaMessage(MODE_SHOW, originPacked,
						java.util.Arrays.asList(names),
						java.util.List.of(before[0], before[1], before[2], before[3]),
						java.util.List.of(after[0], after[1], after[2], after[3])));
	}

	/** 服务端便捷方法：关闭某玩家已打开的点数变化界面 */
	public static void sendClose(net.minecraft.server.level.ServerPlayer player) {
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
				new PointDeltaMessage(MODE_CLOSE, 0,
						java.util.List.of("", "", "", ""),
						java.util.List.of(0, 0, 0, 0), java.util.List.of(0, 0, 0, 0)));
	}

	public static void register() {
		MurmolMod.addNetworkMessage(PointDeltaMessage.TYPE, PointDeltaMessage.STREAM_CODEC,
				PointDeltaMessage::handleData);
	}
}
