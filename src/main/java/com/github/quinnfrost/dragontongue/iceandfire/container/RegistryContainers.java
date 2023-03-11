package com.github.quinnfrost.dragontongue.iceandfire.container;

import com.github.quinnfrost.dragontongue.Registration;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;

public class RegistryContainers {
    public static final RegistryObject<ContainerType<ContainerDragon>> CONTAINER_DRAGON = Registration.CONTAINERS.register(
            "container_dragon",
            () -> new ContainerType<>(ContainerDragon::new)
            );

    public static void registerContainers(IEventBus eventBus) {
        Registration.CONTAINERS.register(eventBus);
    }
}
