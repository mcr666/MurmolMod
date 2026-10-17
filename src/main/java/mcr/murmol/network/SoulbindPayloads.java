package mcr.murmol.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;
import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FeralForms;
import mcr.murmol.init.MurmolModItems;

/**
 * 塑魂之约确认对话网络包：
 * <ul>
 *   <li>OpenMessage（S→C）：手持塑魂之约右键且满足前置条件时推送，客户端据此打开确认界面。</li>
 *   <li>ConfirmMessage（C→S）：玩家点击「确认绑定」后发送，服务端二次校验后永久绑定当前形态并消耗物品。</li>
 * </ul>
 */
public class SoulbindPayloads {
	/** S→C：打开塑魂之约确认界面（formId 为将要绑定的形态） */
	public record OpenMessage(String formId) implements CustomPacketPayload {
		public static final Type<OpenMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "soulbind_open"));

		public static final StreamCodec<FriendlyByteBuf, OpenMessage> STREAM_CODEC = StreamCodec.of(
				(FriendlyByteBuf buf, OpenMessage msg) -> buf.writeUtf(msg.formId(), 64),
				(FriendlyByteBuf buf) -> new OpenMessage(buf.readUtf(64)));

		@Override
		public Type<OpenMessage> type() {
			return TYPE;
		}

		public static void handleData(final OpenMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					// 客户端线程打开确认界面（专用服务器不会进入此分支，引用不到客户端类）
					net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
					mc.setScreen(new mcr.murmol.client.screen.SoulbindConfirmScreen(message.formId()));
				});
			}
		}
	}

	/** C→S：确认缔结塑魂之约（formId 须与服务端当前形态一致，防过期/篡改） */
	public record ConfirmMessage(String formId) implements CustomPacketPayload {
		public static final Type<ConfirmMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "soulbind_confirm"));

		public static final StreamCodec<FriendlyByteBuf, ConfirmMessage> STREAM_CODEC = StreamCodec.of(
				(FriendlyByteBuf buf, ConfirmMessage msg) -> buf.writeUtf(msg.formId(), 64),
				(FriendlyByteBuf buf) -> new ConfirmMessage(buf.readUtf(64)));

		@Override
		public Type<ConfirmMessage> type() {
			return TYPE;
		}

		public static void handleData(final ConfirmMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer sp) {
				context.enqueueWork(() -> {
					FeralForm form = FeralForms.byId(message.formId());
					// 二次校验：形态有效、为野性形态、尚未绑定、当前形态与确认时一致
					if (form == null || !form.isFeral() || FeralFormManager.isSoulbound(sp))
						return;
					if (FeralFormManager.getForm(sp) != form)
						return;
					if (!FeralFormManager.bindSoul(sp, form))
						return;
					consumeVow(sp);
					sp.displayClientMessage(Component.translatable("message.murmol.soulbind.bound", form.getDisplayName()), false);
					if (sp.level() instanceof ServerLevel lv) {
						lv.playSound(null, sp.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
					}
				});
			}
		}

		/** 消耗一个塑魂之约（主手 → 副手 → 背包兜底；创造模式不消耗）。未找到时仍已完成绑定（不因此卡死）。 */
		private static void consumeVow(ServerPlayer sp) {
			boolean consume = !sp.getAbilities().instabuild;
			ItemStack main = sp.getMainHandItem();
			if (main.is(MurmolModItems.RESHAPE_VOW.get())) {
				if (consume)
					main.shrink(1);
				return;
			}
			ItemStack off = sp.getOffhandItem();
			if (off.is(MurmolModItems.RESHAPE_VOW.get())) {
				if (consume)
					off.shrink(1);
				return;
			}
			var inv = sp.getInventory();
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack st = inv.getItem(i);
				if (st.is(MurmolModItems.RESHAPE_VOW.get())) {
					if (consume)
						st.shrink(1);
					return;
				}
			}
		}
	}

	/** 客户端确认按钮：发送确认包 */
	public static void sendConfirm(String formId) {
		PacketDistributor.sendToServer(new ConfirmMessage(formId));
	}

	public static void register() {
		MurmolMod.addNetworkMessage(OpenMessage.TYPE, OpenMessage.STREAM_CODEC, OpenMessage::handleData);
		MurmolMod.addNetworkMessage(ConfirmMessage.TYPE, ConfirmMessage.STREAM_CODEC, ConfirmMessage::handleData);
	}
}
