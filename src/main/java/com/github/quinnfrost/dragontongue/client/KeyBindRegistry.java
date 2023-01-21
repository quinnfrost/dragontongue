package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.gui.GUIEvent;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;

public class KeyBindRegistry {
    public static EnumMouseScroll scroll_status = EnumMouseScroll.NONE;
    public static boolean scan_scroll = false;
    public static KeyBinding command_tamed = new KeyBinding("key.command_tamed",-1,"key.categories.gameplay");
    public static KeyBinding add_tamed = new KeyBinding("key.add_tamed", -1, "key.categories.gameplay");
    public static KeyBinding remove_tamed = new KeyBinding("key.remove_tamed", -1, "key.categories.gameplay");
    public static KeyBinding set_tamed = new KeyBinding("key.set_tamed", -1, "key.categories.gameplay");

    public static EnumMouseScroll getScrollStatus() {
        EnumMouseScroll status = scroll_status;
        if (scroll_status != EnumMouseScroll.NONE) {
            scroll_status = EnumMouseScroll.NONE;
        }
        return status;
    }

    public static void scanKeyPress(ClientPlayerEntity clientPlayerEntity) {
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            clientPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE, null).ifPresent(iCapabilityInfoHolder -> {
//                    EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(player,
//                            Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
//                    MyNetworking.sendToServer(
//                            new PacketCommandEntity(EnumCommandType.ATTACK, player, entityRayTraceResult));
                BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(clientPlayerEntity, Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f);

                //                if (blockRayTraceResult.getType() != RayTraceResult.Type.MISS) {
//                        RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REACH, player.getUniqueID(), blockRayTraceResult));
//                }
                RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REACH, clientPlayerEntity.getUniqueID(), blockRayTraceResult));
            });

            if (!KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = true;
            }
        } else {
            if (KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = false;
            }
        }
        if (KeyBindRegistry.add_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.ADD, clientPlayerEntity.getUniqueID(), entityRayTraceResult));
        }
        if (KeyBindRegistry.remove_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REMOVE, clientPlayerEntity.getUniqueID(), entityRayTraceResult));
        }
        if (KeyBindRegistry.set_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.SET, clientPlayerEntity.getUniqueID(), entityRayTraceResult));
        }

    }

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

}
