package mcr.murmol.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.network.chat.Component;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.network.SoulbindPayloads;

/**
 * 塑魂之约：手持右键弹出确认对话，确认后玩家永久绑定到当前形态。
 * 绑定后无法以非作弊手段（灵魂物品/唤灵台/照妖镜/返归人类/显形/NeoOrigins 起源联动）变形为其他形态或人类，
 * 仅 `/murmol tf` 等管理指令可变形，`/murmol soulbind false` 可解除绑定。
 * 确认界面由服务端推送 {@link SoulbindPayloads.OpenMessage} 打开，绑定由 {@link SoulbindPayloads.ConfirmMessage} 服务端完成。
 */
public class ReshapeVowItem extends Item {
	public ReshapeVowItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		// 客户端不做事，确认界面由服务端推送的 OpenMessage 打开
		if (world.isClientSide())
			return InteractionResultHolder.sidedSuccess(stack, true);
		if (!(player instanceof ServerPlayer sp))
			return InteractionResultHolder.fail(stack);
		FeralForm form = FeralFormManager.getForm(player);
		// 已绑定：拒绝并提示
		if (FeralFormManager.isSoulbound(player)) {
			FeralForm bound = FeralFormManager.getBoundForm(player);
			player.displayClientMessage(Component.translatable("message.murmol.soulbind.already_bound",
					bound != null ? bound.getDisplayName() : Component.literal("?")), true);
			return InteractionResultHolder.fail(stack);
		}
		// 显形药效中：其形态为临时状态，禁止缔结
		if (mcr.murmol.potion.RevealManager.isLocked(player)) {
			player.displayClientMessage(Component.translatable("message.murmol.reveal.blocked"), true);
			return InteractionResultHolder.fail(stack);
		}
		// 需处于野性形态（绑定到人类无意义）
		if (!form.isFeral()) {
			player.displayClientMessage(Component.translatable("message.murmol.soulbind.need_form"), true);
			return InteractionResultHolder.fail(stack);
		}
		// 推送确认对话（服务端权威；确认后由 ConfirmMessage 完成绑定与消耗）
		PacketDistributor.sendToPlayer(sp, new SoulbindPayloads.OpenMessage(form.getId()));
		return InteractionResultHolder.sidedSuccess(stack, true);
	}
}
