package net.mcr.astralcruse.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AstralCruseModSounds {
	public static final String MODID = "astral_cruse";
    public static final DeferredRegister<SoundEvent> REGISTRY =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ASTRAL_BOOK_GUI = REGISTRY.register(
        "astralbookgui",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(MODID, "astralbookgui")
        )
    );

    private AstralCruseModSounds() {
    }
}
