package mcr.murmol.compat.tlm;

import java.util.List;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;

/**
 * 女仆额外大脑：把"雀士"行为注入 CORE 活动。
 * <p>
 * 背景：任务行为若经 {@code createBrainTasks} 注入 WORK 活动，会被日程门控
 * （默认 DAY 日程仅 06:00~18:00 激活 WORK），导致"把工作状态设为雀士后"非工作时段不执行。
 * 改走 {@link IExtraMaidBrain#getCoreBehaviors()}（CORE 任意日程/时段都运行），
 * 行为内部自行判断女仆当前任务是否为"雀士"（{@link TaskMahjongPlayer#UID}），
 * 从而"设为雀士即自动走向牌桌"，不依赖工作时段。
 */
public class TlmMaidBrain implements IExtraMaidBrain {
	@Override
	public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getCoreBehaviors() {
		// 优先级 8 > followOwner(3)：在跟随行为之后重设寻路目标，走向牌桌优先
		return List.of(Pair.of(8, new MaidPlayMahjongTask(0.6f)));
	}
}
