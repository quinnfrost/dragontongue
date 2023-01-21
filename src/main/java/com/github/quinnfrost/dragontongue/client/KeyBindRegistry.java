package com.github.quinnfrost.dragontongue.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;

public class KeyBindRegistry {
    public static EnumMouseScroll scroll_status = EnumMouseScroll.NONE;
    public static KeyBinding command_tamed = new KeyBinding("key.command_tamed",-1,"key.categories.gameplay");
    public static KeyBinding add_tamed = new KeyBinding("key.add_tamed", -1, "key.categories.gameplay");
    public static KeyBinding remove_tamed = new KeyBinding("key.remove_tamed", -1, "key.categories.gameplay");
    public static KeyBinding set_tamed = new KeyBinding("key.set_tamed", -1, "key.categories.gameplay");

    public enum EnumMouseScroll {
        NONE,
        UP,
        DOWN
    }
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

    public static EnumMouseScroll getScrollStatus() {
        EnumMouseScroll status = scroll_status;
        scroll_status = EnumMouseScroll.NONE;
        return status;
    }

}
