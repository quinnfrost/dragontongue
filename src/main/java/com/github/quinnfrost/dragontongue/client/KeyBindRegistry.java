package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
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
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
public class KeyBindRegistry {
    public static EnumMouseScroll scroll_status = EnumMouseScroll.NONE;
    public static boolean scan_scroll = false;
    public static KeyBinding command_tamed = new KeyBinding("key.command_tamed", 86, "key.categories.gameplay");
    public static KeyBinding select_tamed = new KeyBinding("key.select_tamed", 71, "key.categories.gameplay");
    public static KeyBinding set_tamed_status = new KeyBinding("key.set_tamed_status", 72, "key.categories.gameplay");

    public static EnumMouseScroll getScrollStatus() {
        EnumMouseScroll status = scroll_status;
        if (scroll_status != EnumMouseScroll.NONE) {
            scroll_status = EnumMouseScroll.NONE;
        }
        return status;
    }

    public static void scanScrollAction(ClientPlayerEntity clientPlayerEntity) {
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            ICapTargetHolder cap = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity));
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(Minecraft.getInstance().player, (float) cap.getCommandDistance(), null);
            if (rayTraceResult.getType() != RayTraceResult.Type.MISS) {
                OverlayCrossHair.setCrossHairDisplay(null, 0, 10, true);
            }
            switch (getScrollStatus()) {
                case NONE:
                    break;
                case UP:
                    clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(iCapTargetHolder.modifyCommandDistance(1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getCommandDistance()),60,0, false);
                    });
                    break;
                case DOWN:
                    clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(iCapTargetHolder.modifyCommandDistance(-1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getCommandDistance()),60,0, false);
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
        if (KeyBindRegistry.set_tamed_status.isKeyDown()) {
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

        if (
                KeyBindRegistry.command_tamed.isKeyDown()
                || KeyBindRegistry.select_tamed.isKeyDown()
                || KeyBindRegistry.set_tamed_status.isKeyDown()
        ) {
            // Scan mouse scroll
            if (!KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = true;
            }
        } else {
            if (KeyBindRegistry.scan_scroll) {
                KeyBindRegistry.scan_scroll = false;
            }
        }
        if (KeyBindRegistry.set_tamed_status.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity)).getCommandDistance();
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);
            if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                if (gameSettings.keyBindAttack.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandEntity.FOLLOW, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
                if (gameSettings.keyBindUseItem.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandEntity.SIT, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
                if (gameSettings.keyBindPickBlock.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandEntity.WONDER, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
            }

            scanScrollAction(clientPlayerEntity);
        }
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity)).getCommandDistance();
//            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
//                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(clientPlayerEntity, (float) commandDistance, 1.0f);
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    (float) commandDistance, null);

            if (gameSettings.keyBindAttack.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandEntity.ATTACK, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult, blockRayTraceResult
                    ));
                } else {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandEntity.ATTACK, clientPlayerEntity.getUniqueID(), null, blockRayTraceResult
                    ));
                }
            }
            if (gameSettings.keyBindUseItem.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.REACH, clientPlayerEntity.getUniqueID(), blockRayTraceResult
                ));
            }
            if (gameSettings.keyBindPickBlock.isKeyDown() && rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandEntity.HALT, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                ));
            }

            scanScrollAction(clientPlayerEntity);
        }
        if (KeyBindRegistry.select_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity)).getCommandDistance();
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);

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

            scanScrollAction(clientPlayerEntity);
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
