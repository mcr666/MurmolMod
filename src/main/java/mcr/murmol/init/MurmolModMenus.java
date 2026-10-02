package mcr.murmol.init;

import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import mcr.murmol.MurmolMod;

public class MurmolModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, MurmolMod.MODID);
}
