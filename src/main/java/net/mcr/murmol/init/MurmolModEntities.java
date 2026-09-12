/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.Registries;

import net.mcr.murmol.entity.*;
import net.mcr.murmol.MurmolMod;

@EventBusSubscriber
public class MurmolModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, MurmolMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<XiaoHuiProjectileEntity>> XIAO_HUI_PROJECTILE = register("xiao_hui_projectile",
			EntityType.Builder.<XiaoHuiProjectileEntity>of(XiaoHuiProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.3f, 0.3f));
	public static final DeferredHolder<EntityType<?>, EntityType<IceBoltEntity>> ICE_BOLT = register("ice_bolt",
			EntityType.Builder.<IceBoltEntity>of(IceBoltEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.3f, 0.3f));
	public static final DeferredHolder<EntityType<?>, EntityType<FeralCatEntity>> FERAL_CAT = register("feral_cat",
			EntityType.Builder.<FeralCatEntity>of(FeralCatEntity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 0.9f));
	public static final DeferredHolder<EntityType<?>, EntityType<PepperCHEntity>> PEPPER_CH = register("pepper_ch",
			EntityType.Builder.<PepperCHEntity>of(PepperCHEntity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.8f, 0.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<PetalProjectileEntity>> PETAL_PROJECTILE = register("petal_projectile",
			EntityType.Builder.<PetalProjectileEntity>of(PetalProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.2f, 0.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<AlfarEntity>> ALFAR = register("alfar",
			EntityType.Builder.<AlfarEntity>of(AlfarEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3).fireImmune()

					.sized(2f, 2f));
	public static final DeferredHolder<EntityType<?>, EntityType<AlfaSpitProjEntity>> ALFA_SPIT_PROJ = register("alfa_spit_proj",
			EntityType.Builder.<AlfaSpitProjEntity>of(AlfaSpitProjEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<AstralDrakeEntity>> ASTRAL_DRAKE = register("astral_drake",
			EntityType.Builder.<AstralDrakeEntity>of(AstralDrakeEntity::new, MobCategory.AMBIENT).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 1.8f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(RegisterSpawnPlacementsEvent event) {
		FeralCatEntity.init(event);
		PepperCHEntity.init(event);
		AlfarEntity.init(event);
		AstralDrakeEntity.init(event);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(FERAL_CAT.get(), FeralCatEntity.createAttributes().build());
		event.put(PEPPER_CH.get(), PepperCHEntity.createAttributes().build());
		event.put(ALFAR.get(), AlfarEntity.createAttributes().build());
		event.put(ASTRAL_DRAKE.get(), AstralDrakeEntity.createAttributes().build());
	}
}