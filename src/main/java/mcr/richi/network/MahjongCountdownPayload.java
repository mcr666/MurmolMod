package mcr.richi.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * 麻将回合倒计时同步（S -> C）：shortSec = 短考剩余秒（白色），longSec = 长考银行剩余秒（黄色）。
 * 客户端渲染格式："x+XX"（短考>0 且长考>0，两色同显）；短考耗尽后只显示黄色长考。
 * 服务端仅在任一数字变化时发包；停发后客户端按时效自动隐藏。
 * 客户端缓存为纯原始类型，不引用任何客户端类（该类服务端也会加载）。
 */
public class MahjongCountdownPayload {
	/** 客户端缓存：短考剩余秒（<=0 = 已进入长考/无短考） */
	public static volatile int shortSec = -1;
	/** 客户端缓存：长考银行剩余秒 */
	public static volatile int longSec = -1;
	/** 包接收计数：HUD 据此判断时效（每次收包 +1） */
	public static volatile int receiveCounter;

	public record CountdownMessage(int shortSec, int longSec) implements CustomPacketPayload {
		public static final Type<CountdownMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_countdown"));

		public static final StreamCodec<RegistryFriendlyByteBuf, CountdownMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, CountdownMessage msg) -> {
					buf.writeVarInt(msg.shortSec());
					buf.writeVarInt(msg.longSec());
				},
				(RegistryFriendlyByteBuf buf) -> new CountdownMessage(buf.readVarInt(), buf.readVarInt()));

		@Override
		public Type<CountdownMessage> type() {
			return TYPE;
		}

		public static void handleData(final CountdownMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					MahjongCountdownPayload.shortSec = message.shortSec();
					MahjongCountdownPayload.longSec = message.longSec();
					MahjongCountdownPayload.receiveCounter++;
				});
			}
		}
	}

	public static void register() {
		MurmolMod.addNetworkMessage(CountdownMessage.TYPE, CountdownMessage.STREAM_CODEC, CountdownMessage::handleData);
	}
}
