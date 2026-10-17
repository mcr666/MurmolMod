package mcr.murmol.compat.tlm;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;

/**
 * TouhouLittleMaid 扩展入口（{@link LittleMaidExtension} + {@link ILittleMaid}，无参构造）。
 * <p>
 * TLM 启动时经 {@code AnnotatedInstanceUtil.getModExtensions()} 扫描所有 mod 的
 * {@code @LittleMaidExtension} 注解并实例化（写入 {@code TouhouLittleMaid.EXTENSIONS}）；
 * 未安装 TLM 时本类不会被加载，因此无需任何桥接。
 * <p>
 * 在此注册"雀士"任务：玩家在 TLM 女仆管理界面把女仆的任务（工作状态）设为"雀士"、
 * 且女仆处于工作日程后，自动走向附近风盘并入座（见 {@link TaskMahjongPlayer} 与
 * {@link MaidPlayMahjongTask}）。
 */
@LittleMaidExtension
public class TlmMaidExtension implements ILittleMaid {
	public TlmMaidExtension() {
	}

	@Override
	public void addMaidTask(TaskManager manager) {
		manager.add(new TaskMahjongPlayer());
	}

	@Override
	public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
		// "雀士"行为注入 CORE 活动（恒激活，不受工作时段门控）
		manager.addExtraMaidBrain(new TlmMaidBrain());
	}
}
