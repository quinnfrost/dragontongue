package com.github.quinnfrost.dragontongue;

import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, References.MOD_ID);

    // Add new creative mod tab
    public static final ItemGroup TAB_DRAGONTONGUE = new ItemGroup("dragontongue") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(RegistryItems.CROW_WAND.get());
        }
    };

    public static void registerModContent(IEventBus eventBus) {
        RegistryItems.registerItems(eventBus);

        RegistryMessages.registerMessages();
    }

}
