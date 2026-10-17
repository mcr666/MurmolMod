package mcr.murmol.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 陌生的来信：手持右键打开信件阅读界面。
 */
public class StrangeLetterItem extends Item {
	public StrangeLetterItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide) {
			openScreen();
		}
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

	@OnlyIn(Dist.CLIENT)
	private static void openScreen() {
		mcr.murmol.client.screen.StrangeLetterScreen.open();
	}
}
