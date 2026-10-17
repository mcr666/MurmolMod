package mcr.murmol.compat.tlm;

import java.util.List;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;

import mcr.murmol.MurmolMod;
import mcr.richi.MahjongItems;

/**
 * "雀士"任务（{@link IMaidTask}）——女仆的一种工作状态/职业。
 * <p>
 * 玩家在 TLM 女仆管理界面把任务切换为本任务（显示名由翻译键
 * {@code task.murmol.mahjong_player} 提供，即"雀士"）并处于工作日程后，
 * {@code registerWorkGoals} 会把 {@link #createBrainTasks(EntityMaid)} 返回的行为注入
 * Work 活动，女仆自动走向附近风盘并入座（见 {@link MaidPlayMahjongTask}）。
 * <p>
 * 切换任务会触发 {@code EntityMaid.refreshBrain}，故运行中改状态立即生效。
 */
public class TaskMahjongPlayer implements IMaidTask {
	public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "mahjong_player");

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public ItemStack getIcon() {
		return MahjongItems.STICK_1000.get().getDefaultInstance();
	}

	@Override
	public SoundEvent getAmbientSound(EntityMaid maid) {
		return null;
	}

	@Override
	public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
		// 行为改由 TlmMaidBrain.getCoreBehaviors() 注入 CORE 活动（恒激活，不受工作时段门控），此处留空
		return List.of();
	}

	@Override
	public boolean enableLookAndRandomWalk(EntityMaid maid) {
		// 直奔牌桌：禁用四处张望与随机走动
		return false;
	}

	@Override
	public boolean enablePanic(EntityMaid maid) {
		// 打牌期间不因受击乱跑（入座后由每 tick 钉位保持原位）
		return false;
	}

	@Override
	public String getMaidActionSummary() {
		return "Walk to a nearby mahjong table and sit down as a player";
	}
}
