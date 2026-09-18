package net.mcr.murmol.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import java.util.List;

/**
 * 古代知识残页。NBT（CUSTOM_DATA 组件）中存有一个形态 id（键 FormId），
 * 使用后解锁手册中该形态的背景描述、特性描述与变形配方（不消耗物品）。
 * 未记录形态的残页使用无效。
 */
public class AncientKnowledgeScrapItem extends Item {
	public static final String TAG_FORM_ID = "FormId";

	public AncientKnowledgeScrapItem() {
		super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
	}

	/** 创建记录了指定形态的残页堆栈 */
	public static ItemStack forForm(String formId) {
		ItemStack stack = new ItemStack(net.mcr.murmol.init.MurmolModItems.ANCIENT_KNOWLEDGE_SCRAP.get());
		CompoundTag tag = new CompoundTag();
		tag.putString(TAG_FORM_ID, formId);
		stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
			String formId = data == null ? "" : data.getUnsafe().getString(TAG_FORM_ID);
			if (formId.isEmpty()) {
				player.displayClientMessage(Component.literal("\u00A77\u00A7o残页上是褪色的文字，无法辨认。"), true);
				return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
			}
			// 通过授予隐藏进度解锁手册中对应形态的条目
			AdvancementHolder advancement = serverPlayer.server.getAdvancements()
					.get(ResourceLocation.fromNamespaceAndPath(net.mcr.murmol.MurmolMod.MODID, "unlock/" + formId));
			if (advancement == null) {
				player.displayClientMessage(Component.literal("\u00A77\u00A7o残页上的知识晦涩难懂。"), true);
				return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
			}
			AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
			if (!progress.isDone()) {
				for (String criterion : progress.getRemainingCriteria())
					serverPlayer.getAdvancements().award(advancement, criterion);
			}
			// 提示：xx已记录于《幻星秘典》（形态名取自 FeralForm.getDisplayName，新形态自动生效）
			net.mcr.murmol.feral.FeralForm form = net.mcr.murmol.feral.FeralForms.byId(formId);
			player.sendSystemMessage(Component.translatable("message.murmol.scrap.recorded",
					form.getDisplayName(),
					Component.translatable("item.murmol.the_astral_tome")));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, list, flag);
		list.add(Component.translatable("item.murmol.ancient_knowledge_scrap.description_0"));
		list.add(Component.translatable("item.murmol.ancient_knowledge_scrap.description_1"));
		// 已记录形态的残页标注出处：《峰洋经》——形态名（取自 FeralForm.getDisplayName）
		CustomData data = itemstack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
		String formId = data == null ? "" : data.getUnsafe().getString(TAG_FORM_ID);
		if (!formId.isEmpty()) {
			list.add(Component.translatable("item.murmol.ancient_knowledge_scrap.origin",
					net.mcr.murmol.feral.FeralForms.byId(formId).getDisplayName()));
		}
	}
}
