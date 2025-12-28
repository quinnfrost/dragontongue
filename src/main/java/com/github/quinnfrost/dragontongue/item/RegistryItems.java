package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.Registration;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;


public class RegistryItems {
    public static final Item DRAGON_STAFF_ICE = new ItemDragonStaff();
    // Items in mod
    public static final Item CROW_WAND = new ItemCrowWand();


    // Register item
    public static void registerItems(IEventBus eventBus) {
        Registration.ITEMS.register("crow_wand", () -> CROW_WAND);
        Registration.ITEMS.register("dragonstaff_ice", () -> DRAGON_STAFF_ICE);

        Registration.ITEMS.register(eventBus);
    }

}
