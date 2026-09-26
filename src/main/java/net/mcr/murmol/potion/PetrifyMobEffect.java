package net.mcr.murmol.potion;

import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.EffectCure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * 石化：持续时间内的效果等同狛犬的石像状态（抗性提升 I、2 格以外怪物无法选为目标），
 * 但更彻底——无法移动、无法使用/攻击。
 * 移动与交互锁定见 {@link PetrifyLockHandlers}，仇恨豁免见 TargetingConditionsMixin。
 */
public class PetrifyMobEffect extends MobEffect {

	public PetrifyMobEffect() {
		super(MobEffectCategory.HARMFUL, -8355712);
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
		cures.add(EffectCures.MILK);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		// 生物直接关 AI：寻路/转向/动画全部停止，从源头消除抽搐
		if (entity instanceof net.minecraft.world.entity.Mob mob)
			mob.setNoAi(true);
		// 同步石化标记给追踪的客户端（原版不向观察者同步生物身上的药水效果，渲染端贴图判定依赖此附件）
		if (!entity.level().isClientSide())
			entity.setData(net.mcr.murmol.network.MurmolModVariables.PETRIFIED_STATE, true);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		// 彻底锁定移动：清空速度（含击退）
		entity.setDeltaMovement(Vec3.ZERO);
		// 冻结朝向：渲染取 yBodyRot/yBodyRotO 的插值，两者相等即身体不再随视角或 AI 转动
		entity.yBodyRot = entity.yBodyRotO;
		entity.yHeadRot = entity.yHeadRotO;
		if (!entity.level().isClientSide()) {
			// 石像状态等同效果：抗性提升 I（短时长每 tick 续期，隐藏粒子图标）
			entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
			// 客户端速度也同步清零（玩家移动为客户端权威，见 PetrifyLockHandlers 输入锁定）
			if (entity instanceof Player player)
				player.hurtMarked = true;
		}
		return super.applyEffectTick(entity, amplifier);
	}

	/** 实体当前是否被石化：本地效果（自己身上，原版会同步）或同步附件标记（观察其他实体用） */
	public static boolean isPetrified(LivingEntity entity) {
		return entity.hasEffect(net.mcr.murmol.init.MurmolModMobEffects.PETRIFY)
				|| entity.getData(net.mcr.murmol.network.MurmolModVariables.PETRIFIED_STATE);
	}
}
