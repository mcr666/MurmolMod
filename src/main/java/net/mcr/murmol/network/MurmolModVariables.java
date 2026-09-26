package net.mcr.murmol.network;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;

import net.mcr.murmol.MurmolMod;
import net.mcr.murmol.feral.FeralForm;

import java.util.function.Supplier;

@EventBusSubscriber
public class MurmolModVariables {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MurmolMod.MODID);
	public static final Supplier<AttachmentType<PlayerVariables>> PLAYER_VARIABLES = ATTACHMENT_TYPES.register("player_variables", () -> AttachmentType.serializable(() -> new PlayerVariables()).build());
	/** 石化标记：同步给追踪该实体的客户端（原版不向观察者同步生物身上的药水效果，石化贴图判定需要这个标记） */
	public static final Supplier<AttachmentType<Boolean>> PETRIFIED_STATE = ATTACHMENT_TYPES.register("petrified_state",
			() -> AttachmentType.builder(() -> Boolean.FALSE).serialize(com.mojang.serialization.Codec.BOOL).sync(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());
	/** 禁锢标记（囚笼）：同步给追踪该实体的客户端，客户端据此锁定移动输入 */
	public static final Supplier<AttachmentType<Boolean>> CAGED_STATE = ATTACHMENT_TYPES.register("caged_state",
			() -> AttachmentType.builder(() -> Boolean.FALSE).serialize(com.mojang.serialization.Codec.BOOL).sync(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		MurmolMod.addNetworkMessage(PlayerVariablesSyncMessage.TYPE, PlayerVariablesSyncMessage.STREAM_CODEC, PlayerVariablesSyncMessage::handleData);
	}

	@SubscribeEvent
	public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			for (Entity entityiterator : player.level().players())
				if (entityiterator != player && entityiterator instanceof ServerPlayer playeriterator)
					PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(playeriterator.getData(PLAYER_VARIABLES), playeriterator.getId()));
			PacketDistributor.sendToPlayersInDimension((ServerLevel) player.level(), new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES), player.getId()));
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			for (Entity entityiterator : player.level().players())
				if (entityiterator != player && entityiterator instanceof ServerPlayer playeriterator)
					PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(playeriterator.getData(PLAYER_VARIABLES), playeriterator.getId()));
			PacketDistributor.sendToPlayersInDimension((ServerLevel) player.level(), new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES), player.getId()));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			for (Entity entityiterator : player.level().players())
				if (entityiterator != player && entityiterator instanceof ServerPlayer playeriterator)
					PacketDistributor.sendToPlayer(player, new PlayerVariablesSyncMessage(playeriterator.getData(PLAYER_VARIABLES), playeriterator.getId()));
			PacketDistributor.sendToPlayersInDimension((ServerLevel) player.level(), new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES), player.getId()));
		}
	}

	@SubscribeEvent
	public static void onPlayerTickUpdateSyncPlayerVariables(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player && player.getData(PLAYER_VARIABLES)._syncDirty) {
			PacketDistributor.sendToPlayersInDimension((ServerLevel) player.level(), new PlayerVariablesSyncMessage(player.getData(PLAYER_VARIABLES), player.getId()));
			player.getData(PLAYER_VARIABLES)._syncDirty = false;
		}
	}

	@SubscribeEvent
	public static void clonePlayer(PlayerEvent.Clone event) {
		PlayerVariables original = event.getOriginal().getData(PLAYER_VARIABLES);
		PlayerVariables clone = new PlayerVariables();
		if (!event.isWasDeath()) {
			clone.alfarspatt = original.alfarspatt;
		}
		clone.feralFormId = original.feralFormId;
		event.getEntity().setData(PLAYER_VARIABLES, clone);
	}

	public static class PlayerVariables implements INBTSerializable<CompoundTag> {
		boolean _syncDirty = false;
		public boolean alfarspatt = true;
		/** 形态标识（字符串）。旧存档中的数字 id 会在反序列化时自动迁移。 */
		public String feralFormId = FeralForm.HUMAN_ID;
		/** 禁锢标记（玩家自身同步用：附件 sync 不会发给玩家本人，客户端输入锁定依赖此字段） */
		public boolean caged = false;

		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider lookupProvider) {
			CompoundTag nbt = new CompoundTag();
			nbt.putBoolean("alfarspatt", alfarspatt);
		nbt.putBoolean("caged", caged);
		nbt.putString("feralFormId", feralFormId);
			return nbt;
		}

		@Override
		public void deserializeNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
			alfarspatt = nbt.getBoolean("alfarspatt");
			caged = nbt.getBoolean("caged");
			// 读取字符串 id；旧存档为数字 id 时自动迁移
			if (nbt.contains("feralFormId", net.minecraft.nbt.Tag.TAG_STRING)) {
				feralFormId = nbt.getString("feralFormId");
			} else if (nbt.contains("feralFormId", net.minecraft.nbt.Tag.TAG_INT)) {
				feralFormId = switch (nbt.getInt("feralFormId")) {
					case 1 -> "luohong";
					case 2 -> "chen_huang";
					case 3 -> "moss_beast";
					default -> FeralForm.HUMAN_ID;
				};
			} else {
				feralFormId = FeralForm.HUMAN_ID;
			}
		}

		public void markSyncDirty() {
			_syncDirty = true;
		}
	}

	public record PlayerVariablesSyncMessage(PlayerVariables data, int player) implements CustomPacketPayload {
		public static final Type<PlayerVariablesSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "player_variables_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PlayerVariablesSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PlayerVariablesSyncMessage message) -> {
			buffer.writeInt(message.player());
			buffer.writeNbt(message.data().serializeNBT(buffer.registryAccess()));
		}, (RegistryFriendlyByteBuf buffer) -> {
			PlayerVariablesSyncMessage message = new PlayerVariablesSyncMessage(new PlayerVariables(), buffer.readInt());
			message.data.deserializeNBT(buffer.registryAccess(), buffer.readNbt());
			return message;
		});

		@Override
		public Type<PlayerVariablesSyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final PlayerVariablesSyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND && message.data != null) {
				context.enqueueWork(() -> {
					Entity player = context.player().level().getEntity(message.player);
					if (player == null)
						return;
					player.getData(PLAYER_VARIABLES).deserializeNBT(context.player().registryAccess(), message.data.serializeNBT(context.player().registryAccess()));
					// 附件同步后立即刷新玩家尺寸（野性形态尺寸变化）
					if (player instanceof net.minecraft.world.entity.player.Player pl) {
						net.mcr.astralcruse.events.FeralPlayerDimensions.refreshAfterClientAttributeSync(pl);
					}
				}).exceptionally(e -> {
					context.connection().disconnect(Component.literal(e.getMessage()));
					return null;
				});
			}
		}
	}
}