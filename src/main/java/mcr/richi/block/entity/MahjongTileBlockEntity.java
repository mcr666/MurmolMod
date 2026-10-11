package mcr.richi.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcr.richi.MahjongBlockEntities;

/**
 * 麻将牌方块实体：持有牌面代码（0..36，-1=未知牌）。
 * 放置时由方块从麻将牌物品的 TILE_CODE 组件写入；随方块更新包同步给客户端 BER。
 */
public class MahjongTileBlockEntity extends BlockEntity {
	private int code = -1;

	public MahjongTileBlockEntity(BlockPos pos, BlockState state) {
		super(MahjongBlockEntities.MAHJONG_TILE.get(), pos, state);
	}

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
		setChanged();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt("Code", this.code);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.code = tag.getInt("Code");
	}

	@Override
	public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
