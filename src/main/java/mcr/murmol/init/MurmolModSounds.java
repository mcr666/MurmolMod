/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import mcr.murmol.MurmolMod;

public class MurmolModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, MurmolMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> CGAMEMODE = REGISTRY.register("cgamemode", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "cgamemode")));
	public static final DeferredHolder<SoundEvent, SoundEvent> FROZENING = REGISTRY.register("frozening", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "frozening")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ASTRAL_AFFECTION = REGISTRY.register("astral_affection", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "astral_affection")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DRAKE_0 = REGISTRY.register("drake_0", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "drake_0")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DRAKE_DEATH = REGISTRY.register("drake_death", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "drake_death")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ASTRAL_BURST = REGISTRY.register("astral_burst", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "astral_burst")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SNOWY_HILLS = REGISTRY.register("snowy_hills", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "snowy_hills")));
	public static final DeferredHolder<SoundEvent, SoundEvent> GRACEFUL = REGISTRY.register("graceful", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("murmol", "graceful")));
}