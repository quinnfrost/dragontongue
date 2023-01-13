package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.Registration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;


public class RegistryItems {
    // Items in mod
    public static final RegistryObject<ItemCrowWand> CROW_WAND = Registration.ITEMS.register("crow_wand", ItemCrowWand::new);

    // Register item
    public static void registerItems(IEventBus eventBus) {
        Registration.ITEMS.register(eventBus);
    }

}
