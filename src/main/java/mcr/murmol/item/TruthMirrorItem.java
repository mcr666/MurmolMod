package mcr.murmol.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FeralForms;

public class TruthMirrorItem extends Item {
	public TruthMirrorItem() {
		super(new Item.Properties().stacksTo(1).food((new FoodProperties.Builder()).nutrition(0).saturationModifier(0f).alwaysEdible().build()));
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		// 3 秒使用时间
		return 60;
	}

	/** 使用动画：拉弓姿势 */
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	/**
	 * 潜行右键：贴在被点击面放置照妖镜方块（类似物品展示框）。
	 * 不潜行 → 返回 PASS，走 use() 的 3 秒蓄力变形。
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.isShiftKeyDown())
			return InteractionResult.PASS;
		// 显形药效期间照妖镜不可用
		if (mcr.murmol.potion.RevealManager.isLocked(player)) {
			if (!context.getLevel().isClientSide())
				player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.murmol.reveal.blocked"), true);
			return InteractionResult.FAIL;
		}
		Level world = context.getLevel();
		BlockPos clickPos = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockPos placePos = clickPos.relative(face);
		// 面必须结实（类似展示框需要贴在完整方块面上），且目标位可替换
		if (!world.getBlockState(clickPos).isFaceSturdy(world, clickPos, face))
			return InteractionResult.FAIL;
		if (!world.getBlockState(placePos).canBeReplaced())
			return InteractionResult.FAIL;
		BlockState blockState = mcr.murmol.init.MurmolModBlocks.TRUTH_MIRROR.get().defaultBlockState()
				.setValue(mcr.murmol.block.TruthMirrorBlock.FACING, face);
		world.setBlock(placePos, blockState, 3);
		world.playSound(player, placePos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
		if (!player.getAbilities().instabuild)
			context.getItemInHand().shrink(1);
		return InteractionResult.sidedSuccess(world.isClientSide());
	}

	/**
	 * 右键：举起照妖镜 3 秒后变回人类（显式 startUsingItem，否则客户端可能不进入使用状态）。
	 * 人类形态照镜无意义（照出原形即人类），直接无效提示，不进入使用。
	 */
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		// 显形药效期间照妖镜不可用
		if (mcr.murmol.potion.RevealManager.isLocked(player)) {
			if (!world.isClientSide())
				player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.murmol.reveal.blocked"), true);
			return InteractionResultHolder.fail(stack);
		}
		if (FeralFormManager.getForm(player) == FeralForms.HUMAN)
			return InteractionResultHolder.fail(stack);
		player.startUsingItem(hand);
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		// 变回人类；HUMAN 不受两仪约束（禁用列表本就排除人类）
		FeralFormManager.transformFromItem(world, entity.getX(), entity.getY(), entity.getZ(), entity, FeralForms.HUMAN, itemstack);
		// 生存模式消耗物品
		if (entity instanceof Player player && !player.getAbilities().instabuild)
			itemstack.shrink(1);
		return itemstack;
	}
}
