package net.mcr.murmol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.items.ItemStackHandler;

import net.mcr.murmol.MurmolMod;
import net.mcr.murmol.block.entity.SpiritTableBlockEntity;
import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.murmol.init.MurmolModItems;

public class SpiritTableBlock extends BaseEntityBlock {
	public static final com.mojang.serialization.MapCodec<SpiritTableBlock> CODEC = simpleCodec(SpiritTableBlock::new);

	public SpiritTableBlock() {
		this(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(25.0f, 1200.0f).sound(SoundType.STONE)
				.requiresCorrectToolForDrops().lightLevel(s -> 5).noOcclusion());
	}

	public SpiritTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 非完整方块碰撞箱：底座 + 顶部台面，避免相邻方块面被剔除 */
	@Override
	protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
			BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
		net.minecraft.world.phys.shapes.VoxelShape base = net.minecraft.world.level.block.Block.box(2, 0, 2, 14, 4, 14);
		net.minecraft.world.phys.shapes.VoxelShape top = net.minecraft.world.level.block.Block.box(0, 8, 0, 16, 12, 16);
		return net.minecraft.world.phys.shapes.Shapes.or(base, top);
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SpiritTableBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof SpiritTableBlockEntity blockEntity) {
			ItemStackHandler inventory = blockEntity.getInventory();
			// 潜行右键或空手：直接取下物品（空手时原版也会调用本方法，须在此处理，
			// 否则返回 SUCCESS 会拦截后续的 useWithoutItem 导致空手无法取出）
			if (player.isShiftKeyDown() || stack.isEmpty()) {
				takeTopItem(level, pos, state, player, inventory);
				return ItemInteractionResult.SUCCESS;
			}
			if (stack.is(MurmolModItems.MURMOL_SOUL.get())) {
				if (!level.isClientSide()) {
					java.util.List<ItemStack> materials = new java.util.ArrayList<>();
					boolean allFilled = true;
					for (int i = 0; i < inventory.getSlots(); i++) {
						ItemStack slotStack = inventory.getStackInSlot(i);
						materials.add(slotStack);
						if (slotStack.isEmpty())
							allFilled = false;
					}
					if (!allFilled) {
						player.displayClientMessage(Component.literal("\u00A74The ritual materials are incomplete."), true);
					} else {
						FeralForm form = FeralFormManager.findFormByMaterials(materials);
						if (form != null) {
							for (int i = 0; i < inventory.getSlots(); i++)
								inventory.setStackInSlot(i, ItemStack.EMPTY);
							blockEntity.setChanged();
							level.sendBlockUpdated(pos, state, state, 3);
							FeralFormManager.transformFromItem(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, player, form, ItemStack.EMPTY);
						} else {
							// 配方错误：小型爆炸（不破坏方块），内容物在爆炸结束后掉落，不会被炸毁
							java.util.List<ItemStack> drops = new java.util.ArrayList<>();
							for (int i = 0; i < inventory.getSlots(); i++) {
								ItemStack slotStack = inventory.getStackInSlot(i);
								if (!slotStack.isEmpty()) {
									drops.add(slotStack);
									inventory.setStackInSlot(i, ItemStack.EMPTY);
								}
							}
							blockEntity.setChanged();
							level.sendBlockUpdated(pos, state, state, 3);
							level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2f, Level.ExplosionInteraction.NONE);
							MurmolMod.queueServerWork(10, () -> spawnDrops(level, pos, drops));
							player.displayClientMessage(Component.literal("\u00A74Error Recipes."), true);
						}
					}
				}
				return ItemInteractionResult.SUCCESS;
			}
			// 魂之瓶：收取对应形态的灵魂物品（不变形）
			if (stack.is(MurmolModItems.SOUL_BOTTLE.get())) {
				if (!level.isClientSide()) {
					java.util.List<ItemStack> materials = new java.util.ArrayList<>();
					boolean allFilled = true;
					for (int i = 0; i < inventory.getSlots(); i++) {
						ItemStack slotStack = inventory.getStackInSlot(i);
						materials.add(slotStack);
						if (slotStack.isEmpty())
							allFilled = false;
					}
					if (!allFilled) {
						player.displayClientMessage(Component.literal("\u00A74The ritual materials are incomplete."), true);
					} else {
						FeralForm form = FeralFormManager.findFormByMaterials(materials);
						if (form != null) {
							for (int i = 0; i < inventory.getSlots(); i++)
								inventory.setStackInSlot(i, ItemStack.EMPTY);
							blockEntity.setChanged();
							level.sendBlockUpdated(pos, state, state, 3);
							// 消耗瓶子（创造模式不消耗），给予对应形态灵魂物品（人类形态退回摩摩尔之魂）
							if (!player.getAbilities().instabuild)
								stack.shrink(1);
							ItemStack soul = form.getSoulItem();
							if (soul.isEmpty())
								soul = new ItemStack(MurmolModItems.MURMOL_SOUL.get());
							if (!player.getInventory().add(soul))
								player.drop(soul, false);
							level.playSound(null, pos, SoundType.STONE.getBreakSound(), SoundSource.BLOCKS, 0.8f, 1.4f);
						} else {
							// 配方错误：小型爆炸（不破坏方块），内容物在爆炸结束后掉落
							java.util.List<ItemStack> drops = new java.util.ArrayList<>();
							for (int i = 0; i < inventory.getSlots(); i++) {
								ItemStack slotStack = inventory.getStackInSlot(i);
								if (!slotStack.isEmpty()) {
									drops.add(slotStack);
									inventory.setStackInSlot(i, ItemStack.EMPTY);
								}
							}
							blockEntity.setChanged();
							level.sendBlockUpdated(pos, state, state, 3);
							level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2f, Level.ExplosionInteraction.NONE);
							MurmolMod.queueServerWork(10, () -> spawnDrops(level, pos, drops));
							player.displayClientMessage(Component.literal("\u00A74Error Recipes."), true);
						}
					}
				}
				return ItemInteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				int slot = -1;
				for (int i = 0; i < inventory.getSlots(); i++) {
					if (inventory.getStackInSlot(i).isEmpty()) {
						slot = i;
						break;
					}
				}
				if (slot < 0) {
					// 槽位满时不提示，静默忽略
					return ItemInteractionResult.CONSUME;
				}
				inventory.setStackInSlot(slot, stack.copyWithCount(1));
				stack.shrink(1);
				blockEntity.setChanged();
				level.sendBlockUpdated(pos, state, state, 3);
				level.playSound(null, pos, SoundType.STONE.getPlaceSound(), SoundSource.BLOCKS, 0.8f, 0.9f);
			}
			return ItemInteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof SpiritTableBlockEntity blockEntity) {
			takeTopItem(level, pos, state, player, blockEntity.getInventory());
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	/** 唤灵台被破坏时掉落全部内容物 */
	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SpiritTableBlockEntity blockEntity) {
			dropContents(level, pos, blockEntity.getInventory());
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	/** 将库存内容物全部掉落在台面附近 */
	private static void dropContents(Level level, BlockPos pos, net.neoforged.neoforge.items.ItemStackHandler inventory) {
		if (level.isClientSide())
			return;
		java.util.List<ItemStack> drops = new java.util.ArrayList<>();
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				inventory.setStackInSlot(i, ItemStack.EMPTY);
				drops.add(stack);
			}
		}
		spawnDrops(level, pos, drops);
	}

	/** 生成掉落物实体（带随机小速度） */
	private static void spawnDrops(Level level, BlockPos pos, java.util.List<ItemStack> drops) {
		for (ItemStack stack : drops) {
			if (stack.isEmpty())
				continue;
			net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(level,
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
			drop.setDeltaMovement((level.random.nextDouble() - 0.5) * 0.3, 0.25, (level.random.nextDouble() - 0.5) * 0.3);
			level.addFreshEntity(drop);
		}
	}

	/** 取下第一个非空槽位的物品（槽位 0→3 顺序），给到玩家手上/掉落 */
	private static void takeTopItem(Level level, BlockPos pos, BlockState state, Player player, ItemStackHandler inventory) {
		if (!level.isClientSide()) {
			for (int i = 0; i < inventory.getSlots(); i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (!stack.isEmpty()) {
					inventory.setStackInSlot(i, ItemStack.EMPTY);
					if (!player.getInventory().add(stack))
						player.drop(stack, false);
					level.sendBlockUpdated(pos, state, state, 3);
					level.playSound(null, pos, SoundType.STONE.getBreakSound(), SoundSource.BLOCKS, 0.6f, 1.0f);
					break;
				}
			}
		}
	}
}
