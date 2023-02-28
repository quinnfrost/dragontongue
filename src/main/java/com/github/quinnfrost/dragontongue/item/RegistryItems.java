package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.Registration;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;


public class RegistryItems {
    public static final RegistryObject<ItemDragonStaff> DRAGON_STAFF = Registration.ITEMS.register("dragon_staff", ItemDragonStaff::new);
    // Items in mod
    public static final Item CROW_WAND = new ItemCrowWand();




    // Register item
    public static void registerItems(IEventBus eventBus) {
        Registration.ITEMS.register("crow_wand", () -> CROW_WAND);

        Registration.ITEMS.register(eventBus);
    }

}
