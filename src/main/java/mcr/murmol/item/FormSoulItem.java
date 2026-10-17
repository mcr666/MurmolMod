package mcr.murmol.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FeralForms;

/**
 * 形态灵魂。单一物品 id（murmol:form_soul），以 CUSTOM_DATA 组件中的 FormId 键区分目标形态，
 * 显示名与图标（custom_model_data）随组件变化。结构对应原 8 个独立灵魂物品类的合并。
 */
public class FormSoulItem extends Item {
	public static final String TAG_FORM_ID = "FormId";

	public FormSoulItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
				.food((new FoodProperties.Builder()).nutrition(0).saturationModifier(0f).alwaysEdible().build()));
	}

	/** 创建记录了指定形态的灵魂堆栈（含数据组件与图标标识） */
	public static ItemStack forForm(String formId) {
		ItemStack stack = new ItemStack(mcr.murmol.init.MurmolModItems.FORM_SOUL.get());
		CompoundTag tag = new CompoundTag();
		tag.putString(TAG_FORM_ID, formId);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelDataFor(formId)));
		return stack;
	}

	/** 读取堆栈记录的形态 id；无组件返回空串 */
	public static String formIdOf(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? "" : data.getUnsafe().getString(TAG_FORM_ID);
	}

	/** 形态 id → 图标标识（与 models/item/form_soul.json 的 custom_model_data override 对应） */
	private static int customModelDataFor(String formId) {
		return switch (formId) {
			case "spring_flower" -> 1;
			case "chen_huang" -> 2;
			case "moss_beast" -> 3;
			case "silkmoth" -> 4;
			case "komainu" -> 5;
			case "wen_yao" -> 6;
			case "ferocious" -> 7;
			case "dark_fury" -> 8;
			default -> 0;
		};
	}

	@Override
	public Component getName(ItemStack stack) {
		String formId = formIdOf(stack);
		if (!formId.isEmpty())
			return Component.translatable("item.murmol.form_soul." + formId);
		return super.getName(stack);
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 80;
	}

	/** 使用动画：拉弓姿势 */
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	/** 右键：先检查显形锁与解锁限制，通过后才进入蓄力 */
	@Override
	public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level world, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		String formId = formIdOf(stack);
		if (formId.isEmpty())
			return net.minecraft.world.InteractionResultHolder.fail(stack);
		if (FeralFormManager.checkSoulItemUse(world, player, stack, FeralForms.byId(formId)) != null)
			return net.minecraft.world.InteractionResultHolder.fail(stack);
		player.startUsingItem(hand);
		return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		// 显形药效期间禁止变形
		if (mcr.murmol.potion.RevealManager.isLocked(entity)) {
			if (entity instanceof net.minecraft.world.entity.player.Player p)
				p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.murmol.reveal.blocked"), true);
			return itemstack;
		}
		// 形态被两仪禁用时：失败提示（在 transformFromItem 中），不消耗物品
		String formId = formIdOf(itemstack);
		if (formId.isEmpty())
			return itemstack;
		if (!FeralFormManager.transformFromItem(world, entity.getX(), entity.getY(), entity.getZ(), entity, FeralForms.byId(formId), itemstack))
			return itemstack;
		return super.finishUsingItem(itemstack, world, entity);
	}
}
