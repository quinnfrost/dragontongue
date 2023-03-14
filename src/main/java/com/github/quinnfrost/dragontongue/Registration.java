package com.github.quinnfrost.dragontongue;

import com.github.quinnfrost.dragontongue.iceandfire.container.RegistryContainers;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.RegistryBrains;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, References.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, References.MOD_ID);
    public static final DeferredRegister<Schedule> SCHEDULES = DeferredRegister.create(ForgeRegistries.SCHEDULES, References.MOD_ID);
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, References.MOD_ID);
    public static final DeferredRegister<SensorType<?>> SENSOR = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, References.MOD_ID);
    public static final DeferredRegister<Activity> ACTIVITY = DeferredRegister.create(ForgeRegistries.ACTIVITIES, References.MOD_ID);
    // Add new creative mod tab
//    public static final ItemGroup TAB_DRAGONTONGUE = new ItemGroup("dragontongue") {
//        @Override
//        public ItemStack createIcon() {
//            return new ItemStack(RegistryItems.CROW_WAND.get());
//        }
//    };

    public static void registerModContent(IEventBus eventBus) {
        RegistryMessages.registerMessages();
        RegistryBrains.register(eventBus);
        RegistryItems.registerItems(eventBus);
        if (DragonTongue.isIafPresent) {
            RegistryContainers.registerContainers(eventBus);
        }
    }

}
