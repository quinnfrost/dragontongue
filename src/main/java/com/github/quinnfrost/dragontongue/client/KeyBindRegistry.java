package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import com.github.quinnfrost.dragontongue.message.MessageClientCommandDistance;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageDebugEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;

import java.lang.reflect.Field;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class KeyBindRegistry {
    public static EnumMouseScroll scroll_status = EnumMouseScroll.NONE;
    public static boolean scan_scroll = false;
    public static KeyMapping command_tamed = new KeyMapping("key.command_tamed", 71, "key.categories.gameplay");
    public static KeyMapping select_tamed = new KeyMapping("key.select_tamed", 86, "key.categories.gameplay");
    public static KeyMapping set_tamed_status = new KeyMapping("key.set_tamed_status", 72, "key.categories.gameplay");
    public static KeyMapping debug = new KeyMapping("key.debug", -1, "key.categories.gameplay");
    public static long last_command_press = 0;

    public static EnumMouseScroll getScrollStatus() {
        EnumMouseScroll status = scroll_status;
        if (scroll_status != EnumMouseScroll.NONE) {
            scroll_status = EnumMouseScroll.NONE;
        }
        return status;
    }

    @OnlyIn(Dist.CLIENT)
    public static void scanScrollAction(LocalPlayer clientPlayerEntity) {
        if (KeyBindRegistry.command_tamed.isDown()) {
            ICapabilityInfoHolder cap = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity));
            HitResult rayTraceResult = util.getTargetBlockOrEntity(Minecraft.getInstance().player, (float) cap.getCommandDistance(), null);
            if (rayTraceResult.getType() == HitResult.Type.MISS) {
                OverlayCrossHair.setCrossHairDisplay(null, 0, 2, OverlayCrossHair.IconType.WARN, true);
            }
            BlockHitResult blockRayTraceResult = util.getTargetBlock(Minecraft.getInstance().player, 128, 1.0f, ClipContext.Block.COLLIDER);
//            RenderNode.setRenderPos(4, rayTraceResult.getHitVec(), clientPlayerEntity.getPositionVec(), 0);

            switch (getScrollStatus()) {
                case NONE:
                    break;
                case UP:
                    clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(MessageClientCommandDistance.DistanceType.COMMAND, iCapTargetHolder.modifyCommandDistance(1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getCommandDistance()), 60, 0, OverlayCrossHair.IconType.TARGET, false);
                    });
                    break;
                case DOWN:
                    clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(MessageClientCommandDistance.DistanceType.COMMAND, iCapTargetHolder.modifyCommandDistance(-1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getCommandDistance()), 60, 0, OverlayCrossHair.IconType.TARGET, false);
                    });
                    break;
            }
        }
        if (KeyBindRegistry.select_tamed.isDown()) {
            switch (getScrollStatus()) {
                case NONE:
                    break;
                case UP:
                    clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(MessageClientCommandDistance.DistanceType.SELECT, iCapTargetHolder.modifySelectDistance(1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getSelectDistance()), 60, 0, OverlayCrossHair.IconType.TARGET, false);
                    });
                    break;
                case DOWN:
                    clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(MessageClientCommandDistance.DistanceType.SELECT, iCapTargetHolder.modifySelectDistance(-1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getSelectDistance()), 60, 0, OverlayCrossHair.IconType.TARGET, false);
                    });
                    break;
            }
        }
        if (KeyBindRegistry.set_tamed_status.isDown()) {
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

    public static void scanMouseClick(LocalPlayer clientPlayerEntity) {

    }

    /**
     * Scan key press
     *
     * @param clientPlayerEntity
     */
    public static void scanKeyPress(LocalPlayer clientPlayerEntity) {
        if (KeyBindRegistry.debug.consumeClick()) {
            HitResult debugRayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);
            if (debugRayTraceResult.getType() == HitResult.Type.ENTITY) {
                RegistryMessages.sendToServer(new MessageDebugEntity(((EntityHitResult)debugRayTraceResult).getEntity().getId()));
                ClientGlow.setGlowing(((EntityHitResult)debugRayTraceResult).getEntity(), 20);
            }
        }


        // Scan scroll action
        if (
                KeyBindRegistry.command_tamed.isDown()
                        || KeyBindRegistry.select_tamed.isDown()
                        || KeyBindRegistry.set_tamed_status.isDown()
        ) {
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

        if (KeyBindRegistry.set_tamed_status.isDown()) {
            Options gameSettings = Minecraft.getInstance().options;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity)).getCommandDistance();
            HitResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);

            // Select entity
            if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
                // H + LB
                if (gameSettings.keyAttack.isDown()) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.FOLLOW, clientPlayerEntity.getUUID(), (EntityHitResult) rayTraceResult
                    ));
                }
                // H + RB
                if (gameSettings.keyUse.isDown()) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SIT, clientPlayerEntity.getUUID(), (EntityHitResult) rayTraceResult
                    ));
                }
                // H + MB
                if (gameSettings.keyPickItem.isDown()) {
                    // For some reason isPress() doesn't work in server mode
                    gameSettings.keyPickItem.setDown(false);
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.GUARD, clientPlayerEntity.getUUID(), (EntityHitResult) rayTraceResult
                    ));
//                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
//                    RegistryMessages.sendToServer(new MessageCommandEntity(
//                            EnumCommandType.WONDER, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
//                    ));
                }
            }

        }
        if (KeyBindRegistry.command_tamed.isDown()) {
            Options gameSettings = Minecraft.getInstance().options;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity)).getCommandDistance();
//            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
//                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            BlockHitResult blockRayTraceResult = util.getTargetBlock(clientPlayerEntity, (float) commandDistance, 1.0f, ClipContext.Block.COLLIDER);
            HitResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    (float) commandDistance, null);
            ICapabilityInfoHolder capTargetHolder = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity));

            // G + LB
            if (gameSettings.keyAttack.isDown()) {
                // Entity selected
                if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.ATTACK, clientPlayerEntity.getUUID(), (EntityHitResult) rayTraceResult
                    ));
                    // G + LB + RB
                } else if (gameSettings.keyUse.isDown()) {
//                        if (capTargetHolder.getCommandEntities().isEmpty()) {
//                            ClientGlow.glowSurroundTamed(clientPlayerEntity, 2 * 50, capTargetHolder.getSelectDistance(), null);
//                        }
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.BREATH, clientPlayerEntity.getUUID(), null, blockRayTraceResult
                    ));
                }
                if (capTargetHolder.getCommandEntities().isEmpty()) {
                    ClientGlow.glowSurroundTamed(clientPlayerEntity, 2 * 50, capTargetHolder.getSelectDistance(), null);
                }
                // G + RB
            } else if (gameSettings.keyUse.isDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.REACH, clientPlayerEntity.getUUID(), blockRayTraceResult
                ));
            }
            // G + MB
            if (gameSettings.keyPickItem.isDown()) {
                if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.HALT, clientPlayerEntity.getUUID(), (EntityHitResult) rayTraceResult
                    ));
                } else {
                    // Nothing selected
                    ClientGlow.glowSurroundTamed(clientPlayerEntity, 2 * 50, capTargetHolder.getSelectDistance(), null);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.HALT, clientPlayerEntity.getUUID(), null, blockRayTraceResult
                    ));
                }
            }

        }

        if (KeyBindRegistry.select_tamed.isDown()) {
            Options gameSettings = Minecraft.getInstance().options;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity)).getCommandDistance();
            EntityHitResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            HitResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);

            // V + LB
            if (gameSettings.keyAttack.isDown()) {
                if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.ADD, clientPlayerEntity.getUUID(), entityRayTraceResult
                    ));
                    // V + LB + RB
                } else if (gameSettings.keyUse.isDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SET, clientPlayerEntity.getUUID(), (UUID) null
                    ));
                }
            }
            // V + RB
            if (gameSettings.keyUse.isDown()) {
                if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                }
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.REMOVE, clientPlayerEntity.getUUID(), entityRayTraceResult
                ));
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.WONDER, clientPlayerEntity.getUUID(), entityRayTraceResult
                ));
            }
            // V + MB
            if (gameSettings.keyPickItem.isDown()) {
                if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityHitResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SET, clientPlayerEntity.getUUID(), entityRayTraceResult
                    ));
                }
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
                if (obj instanceof KeyMapping) {
                    ClientRegistry.registerKeyBinding((KeyMapping) obj);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
