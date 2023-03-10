package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Pathfinding;
import com.github.quinnfrost.dragontongue.message.MessageClientCommandDistance;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageDebugEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class KeyBindRegistry {
    public static EnumMouseScroll scroll_status = EnumMouseScroll.NONE;
    public static boolean scan_scroll = false;
    public static KeyBinding command_tamed = new KeyBinding("key.command_tamed", 71, "key.categories.gameplay");
    public static KeyBinding select_tamed = new KeyBinding("key.select_tamed", 86, "key.categories.gameplay");
    public static KeyBinding set_tamed_status = new KeyBinding("key.set_tamed_status", 72, "key.categories.gameplay");
    public static KeyBinding debug = new KeyBinding("key.debug", -1, "key.categories.gameplay");
    public static long last_command_press = 0;

    public static EnumMouseScroll getScrollStatus() {
        EnumMouseScroll status = scroll_status;
        if (scroll_status != EnumMouseScroll.NONE) {
            scroll_status = EnumMouseScroll.NONE;
        }
        return status;
    }

    @OnlyIn(Dist.CLIENT)
    public static void scanScrollAction(ClientPlayerEntity clientPlayerEntity) {
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            ICapabilityInfoHolder cap = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity));
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(Minecraft.getInstance().player, (float) cap.getCommandDistance(), null);
            if (rayTraceResult.getType() == RayTraceResult.Type.MISS) {
                OverlayCrossHair.setCrossHairDisplay(null, 0, 2, OverlayCrossHair.IconType.WARN, true);
            }
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(Minecraft.getInstance().player, 128, 1.0f, RayTraceContext.BlockMode.COLLIDER);
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
        if (KeyBindRegistry.select_tamed.isKeyDown()) {
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
        if (KeyBindRegistry.debug.isPressed()) {
            RayTraceResult debugRayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);
            if (debugRayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                RegistryMessages.sendToServer(new MessageDebugEntity(((EntityRayTraceResult)debugRayTraceResult).getEntity().getEntityId()));
                ClientGlow.setGlowing(((EntityRayTraceResult)debugRayTraceResult).getEntity(), 20);
            }
        }


        // Scan scroll action
        if (
                KeyBindRegistry.command_tamed.isKeyDown()
                        || KeyBindRegistry.select_tamed.isKeyDown()
                        || KeyBindRegistry.set_tamed_status.isKeyDown()
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

        if (KeyBindRegistry.set_tamed_status.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity)).getCommandDistance();
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);

            // Select entity
            if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                // H + LB
                if (gameSettings.keyBindAttack.isKeyDown()) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.FOLLOW, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
                // H + RB
                if (gameSettings.keyBindUseItem.isKeyDown()) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SIT, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
                // H + MB
                if (gameSettings.keyBindPickBlock.isKeyDown()) {
                    // For some reason isPress() doesn't work in server mode
                    gameSettings.keyBindPickBlock.setPressed(false);
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.GUARD, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
//                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
//                    RegistryMessages.sendToServer(new MessageCommandEntity(
//                            EnumCommandType.WONDER, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
//                    ));
                }
            }

        }
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity)).getCommandDistance();
//            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
//                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(clientPlayerEntity, (float) commandDistance, 1.0f, RayTraceContext.BlockMode.COLLIDER);
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    (float) commandDistance, null);
            ICapabilityInfoHolder capTargetHolder = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity));

            // G + LB
            if (gameSettings.keyBindAttack.isKeyDown()) {
                // Entity selected
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.ATTACK, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                    // G + LB + RB
                } else if (gameSettings.keyBindUseItem.isKeyDown()) {
//                        if (capTargetHolder.getCommandEntities().isEmpty()) {
//                            ClientGlow.glowSurroundTamed(clientPlayerEntity, 2 * 50, capTargetHolder.getSelectDistance(), null);
//                        }
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.BREATH, clientPlayerEntity.getUniqueID(), null, blockRayTraceResult
                    ));
                }
                if (capTargetHolder.getCommandEntities().isEmpty()) {
                    ClientGlow.glowSurroundTamed(clientPlayerEntity, 2 * 50, capTargetHolder.getSelectDistance(), null);
                }
                // G + RB
            } else if (gameSettings.keyBindUseItem.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.REACH, clientPlayerEntity.getUniqueID(), blockRayTraceResult
                ));
            }
            // G + MB
            if (gameSettings.keyBindPickBlock.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.HALT, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                } else {
                    // Nothing selected
                    ClientGlow.glowSurroundTamed(clientPlayerEntity, 2 * 50, capTargetHolder.getSelectDistance(), null);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.HALT, clientPlayerEntity.getUniqueID(), null, blockRayTraceResult
                    ));
                }
            }

        }

        if (KeyBindRegistry.select_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(clientPlayerEntity)).getCommandDistance();
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);

            // V + LB
            if (gameSettings.keyBindAttack.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.ADD, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                    ));
                    // V + LB + RB
                } else if (gameSettings.keyBindUseItem.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SET, clientPlayerEntity.getUniqueID(), (UUID) null
                    ));
                }
            }
            // V + RB
            if (gameSettings.keyBindUseItem.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                }
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.REMOVE, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.WONDER, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
            }
            // V + MB
            if (gameSettings.keyBindPickBlock.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    ClientGlow.setGlowing(((EntityRayTraceResult) rayTraceResult).getEntity(), 2 * 50);
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SET, clientPlayerEntity.getUniqueID(), entityRayTraceResult
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
                if (obj instanceof KeyBinding) {
                    ClientRegistry.registerKeyBinding((KeyBinding) obj);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
