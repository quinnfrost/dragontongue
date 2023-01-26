package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImplementation;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageClientCommandDistance;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
public class KeyBindRegistry {
    public static EnumMouseScroll scroll_status = EnumMouseScroll.NONE;
    public static boolean scan_scroll = false;
    public static boolean mkey_left_down = false;
    public static boolean mkey_right_down = false;
    public static KeyBinding command_tamed = new KeyBinding("key.command_tamed", -1, "key.categories.gameplay");
    public static KeyBinding select_tamed = new KeyBinding("key.select_tamed", -1, "key.categories.gameplay");
    public static KeyBinding debug_tamed = new KeyBinding("key.debug_tamed", -1, "key.categories.gameplay");
    public static KeyBinding set_tamed = new KeyBinding("key.set_tamed", -1, "key.categories.gameplay");

    public static EnumMouseScroll getScrollStatus() {
        EnumMouseScroll status = scroll_status;
        if (scroll_status != EnumMouseScroll.NONE) {
            scroll_status = EnumMouseScroll.NONE;
        }
        return status;
    }

    public static void scanScrollAction(ClientPlayerEntity clientPlayerEntity) {
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            switch (getScrollStatus()) {
                case NONE:
                    break;
                case UP:
                    clientPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(iCapabilityInfoHolder.modifyCommandDistance(1))
                        );
                    });
                    break;
                case DOWN:
                    clientPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(iCapabilityInfoHolder.modifyCommandDistance(-1))
                        );
                    });
                    break;
            }
        }
        if (KeyBindRegistry.select_tamed.isKeyDown()) {
            switch (getScrollStatus()) {
                case NONE:
                    break;
                case UP:
                    break;
                case DOWN:
                    break;
            }
        }
    }

    public static void scanMouseClick(ClientPlayerEntity clientPlayerEntity) {

    }

    /**
     * Scan key press
     *
     * @param clientPlayerEntity
     */
    public static void scanKeyPress(ClientPlayerEntity clientPlayerEntity) {
        if (KeyBindRegistry.debug_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            if (entityRayTraceResult != null) {
                DragonTongue.debugTarget = (MobEntity) entityRayTraceResult.getEntity();
            }
        }
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(new CapabilityInfoHolderImplementation()).getCommandDistance();
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(clientPlayerEntity, (float) commandDistance, 1.0f);

            if (gameSettings.keyBindAttack.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.ATTACK, clientPlayerEntity.getUniqueID(), entityRayTraceResult, blockRayTraceResult
                ));
            }
            if (gameSettings.keyBindUseItem.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.REACH, clientPlayerEntity.getUniqueID(), blockRayTraceResult
                ));
            }
            if (gameSettings.keyBindPickBlock.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.HALT, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
            }

            // Scan mouse scroll
            if (!KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = true;
            }
            scanScrollAction(clientPlayerEntity);
        } else {
            if (KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = false;
            }
        }

        if (KeyBindRegistry.select_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(new CapabilityInfoHolderImplementation()).getCommandDistance();
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);

            RegistryMessages.sendToServer(new MessageCommandEntity(
                    EnumCommandEntity.DEBUG, clientPlayerEntity.getUniqueID(), entityRayTraceResult
            ));

            if (gameSettings.keyBindAttack.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.ADD, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
            }
            if (gameSettings.keyBindUseItem.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.REMOVE, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
            }
            if (gameSettings.keyBindPickBlock.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.SET, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
            }

            // Scan mouse scroll
            if (!KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = true;
            }
            scanScrollAction(clientPlayerEntity);
        } else {
            if (KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = false;
            }
        }

    }

    public enum EnumMouseScroll {
        NONE,
        UP,
        DOWN
    }

    public static void registerKeyBind() {
        for (Field f : KeyBindRegistry.class.getDeclaredFields()
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
