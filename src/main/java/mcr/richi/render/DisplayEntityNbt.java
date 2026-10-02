package mcr.richi.render;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import mcr.richi.MahjongItems;

/**
 * 显示实体 NBT 构造（服务端/客户端共用）：item_display 与 text_display 的 CompoundTag 路径
 * （照原 FengPanBlock.spawnDisplayEntity/spawnTextDisplay），变换走 legacy 16-float 列主序矩阵
 * （分解格式 compound 的 scale/rotation 实测不生效）。实体带 richi_display 标签且服务端不持久化
 * （牌局状态在内存，重启后旧实体会与状态脱节）；客户端 ClientLevel 生成的实体天然仅本端存在。
 */
public final class DisplayEntityNbt {
	private DisplayEntityNbt() {
	}

	/** 立直千点棒物品堆 */
	public static ItemStack riichiStickStack() {
		return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation
				.fromNamespaceAndPath(MahjongItems.NAMESPACE, "mahjong_stick_1000")));
	}

	/** 立牌矩阵：纯缩放 */
	public static float[] standingMatrix(float s) {
		return new float[] { s, 0, 0, 0, 0, s, 0, 0, 0, 0, s, 0, 0, 0, 0, 1 };
	}

	/** 平躺牌矩阵：绕 X ±90° × 缩放（faceUp=true 牌面转 +Y 朝上） */
	public static float[] flatMatrix(float s, boolean faceUp) {
		return faceUp
				? new float[] { s, 0, 0, 0, 0, 0, s, 0, 0, -s, 0, 0, 0, 0, 0, 1 }
				: new float[] { s, 0, 0, 0, 0, 0, -s, 0, 0, s, 0, 0, 0, 0, 0, 1 };
	}

	/** 千点棒矩阵：长轴转到 local +Z 复用平躺牌 yaw 公式（厚 0.5px 朝上、宽 2.5px 横向） */
	public static float[] stickMatrix(float s) {
		return new float[] { 0, 0, s, 0, 0, s, 0, 0, -s, 0, 0, 0, 0, 0, 0, 1 };
	}

	/**
	 * item_display NBT：richi_display 标签 + 可选 extraTag（手牌定位）+ 可选 customName
	 * （不显示名牌，仅随实体数据同步）+ 牌面 item + transformation 矩阵。
	 */
	public static CompoundTag itemDisplay(ItemStack stack, float[] matrix, String extraTag, String customName,
			HolderLookup.Provider access) {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "minecraft:item_display");
		ListTag tags = new ListTag();
		tags.add(StringTag.valueOf("richi_display"));
		if (extraTag != null)
			tags.add(StringTag.valueOf(extraTag));
		tag.put("Tags", tags);
		tag.put("item", stack.save(access));
		ListTag matrixTag = new ListTag();
		for (float v : matrix)
			matrixTag.add(FloatTag.valueOf(v));
		tag.put("transformation", matrixTag);
		// pos-rot 插值时长（tick）：客户端 lerpTo 移动/转向时由 display 实体自带机制平滑过渡
		tag.putInt("teleport_duration", 2);
		if (customName != null)
			tag.putString("CustomName", net.minecraft.network.chat.Component.Serializer.toJson(
					net.minecraft.network.chat.Component.literal(customName), access));
		return tag;
	}

	/** text_display NBT（billboard 居中 + 默认背景；座位标记/队列文本/结算横幅/中心信息共用） */
	public static CompoundTag textDisplay(HolderLookup.Provider access, String tag, String text) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", "minecraft:text_display");
		ListTag tags = new ListTag();
		tags.add(StringTag.valueOf("richi_display"));
		tags.add(StringTag.valueOf(tag));
		nbt.put("Tags", tags);
		// text_display 的 text 必须是 JSON 文本组件（纯字符串会显示为空）
		nbt.putString("text", net.minecraft.network.chat.Component.Serializer.toJson(
				net.minecraft.network.chat.Component.literal(text), access));
		nbt.putString("billboard", "center");
		nbt.putBoolean("default_background", true);
		return nbt;
	}

	/**
	 * 通过 NBT 在指定位置生成实体：服务端 = 真实实体；客户端传 ClientLevel 即 client-only 实体
	 * （不会被服务端同步/抓包）。返回生成的实体（失败为 null）。
	 */
	public static Entity spawn(Level level, CompoundTag nbt, Vec3 pos, float yRot) {
		return EntityType.loadEntityRecursive(nbt, level, entity -> {
			entity.moveTo(pos.x, pos.y, pos.z, yRot, 0);
			level.addFreshEntity(entity);
			return entity;
		});
	}
}
