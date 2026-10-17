package mcr.murmol.planetarium;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 星象仪：手持右键打开 wiki 界面。
 */
public class PlanetariumItem extends Item {
	public PlanetariumItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide) {
			openScreen();
		} else {
			// 播放附魔台使用音效
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
					net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
		}
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

	@OnlyIn(Dist.CLIENT)
	private static void openScreen() {
		mcr.murmol.client.screen.PlanetariumScreen.open();
	}
}
