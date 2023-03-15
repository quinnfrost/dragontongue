package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.Registration;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;


public class RegistryItems {
    public static final RegistryObject<ItemDragonStaff> DRAGON_STAFF_ICE = Registration.ITEMS.register("dragonstaff_ice", ItemDragonStaff::new);
    // Items in mod
    public static final RegistryObject<ItemCrowWand> CROW_WAND = Registration.ITEMS.register("crow_wand", ItemCrowWand::new);


    // Register item
    public static void registerItems(IEventBus eventBus) {
        Registration.ITEMS.register(eventBus);
    }

}
