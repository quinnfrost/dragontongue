package com.github.quinnfrost.dragontongue.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;

public class KeyBindRegistry {
    public static KeyBinding command_tamed = new KeyBinding("key.command_tamed",-1,"key.categories.gameplay");

    public static void registerKeyBind(){

        for (Field f:KeyBindRegistry.class.getDeclaredFields()
        ) {
            try {
                Object obj = f.get(null);
                if (obj instanceof KeyBinding) {
                    ClientRegistry.registerKeyBinding((KeyBinding) obj);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
