package com.github.quinnfrost.dragontongue.iceandfire.container;

import com.github.quinnfrost.dragontongue.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;

public class RegistryContainers {
    public static final RegistryObject<MenuType<ContainerDragon>> CONTAINER_DRAGON = Registration.CONTAINERS.register(
            "container_dragon",
            () -> new MenuType<>(ContainerDragon::new)
            );

    public static void registerContainers(IEventBus eventBus) {
        Registration.CONTAINERS.register(eventBus);
    }
}
