package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.client.render.RenderNode;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.message.MessageClientCommandDistance;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;
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
            ICapTargetHolder cap = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity));
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(Minecraft.getInstance().player, (float) cap.getCommandDistance(), null);
            if (rayTraceResult.getType() == RayTraceResult.Type.MISS) {
                OverlayCrossHair.setCrossHairDisplay(null, 0, 2, OverlayCrossHair.IconType.WARN, true);
            }
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(Minecraft.getInstance().player, 128, 1.0f);
//            RenderNode.setRenderPos(4, rayTraceResult.getHitVec(), clientPlayerEntity.getPositionVec(), 0);

            switch (getScrollStatus()) {
                case NONE:
                    break;
                case UP:
                    clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(MessageClientCommandDistance.DistanceType.COMMAND, iCapTargetHolder.modifyCommandDistance(1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getCommandDistance()), 60, 0, OverlayCrossHair.IconType.TARGET, false);
                    });
                    break;
                case DOWN:
                    clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
                    clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        RegistryMessages.sendToServer(
                                new MessageClientCommandDistance(MessageClientCommandDistance.DistanceType.SELECT, iCapTargetHolder.modifySelectDistance(1))
                        );
                        OverlayCrossHair.setCrossHairDisplay(String.valueOf(iCapTargetHolder.getSelectDistance()), 60, 0, OverlayCrossHair.IconType.TARGET, false);
                    });
                    break;
                case DOWN:
                    clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
        if (KeyBindRegistry.debug.isKeyDown()) {
            RayTraceResult debugRayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);
            if (debugRayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.DEBUG, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) debugRayTraceResult
                ));
            }
        }
        if (KeyBindRegistry.debug.isPressed()) {
            clientPlayerEntity.setGlowing(!clientPlayerEntity.isGlowing());
        }

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
            double commandDistance = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity)).getCommandDistance();
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);
            if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                if (gameSettings.keyBindAttack.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.FOLLOW, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
                if (gameSettings.keyBindUseItem.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SIT, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
                if (gameSettings.keyBindPickBlock.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.WONDER, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                }
            }

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
                            EnumCommandType.ATTACK, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                } else if (gameSettings.keyBindUseItem.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.BREATH, clientPlayerEntity.getUniqueID(), null, blockRayTraceResult
                    ));
                }
            } else if (gameSettings.keyBindUseItem.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.REACH, clientPlayerEntity.getUniqueID(), blockRayTraceResult
                ));
            }
            if (gameSettings.keyBindPickBlock.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.HALT, clientPlayerEntity.getUniqueID(), (EntityRayTraceResult) rayTraceResult
                    ));
                } else {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.HALT, clientPlayerEntity.getUniqueID(), null, blockRayTraceResult
                    ));
                }
            }

        }
        if (KeyBindRegistry.select_tamed.isKeyDown()) {
            GameSettings gameSettings = Minecraft.getInstance().gameSettings;
            double commandDistance = clientPlayerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(clientPlayerEntity)).getCommandDistance();
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), null);

            if (gameSettings.keyBindAttack.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.ADD, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                    ));
                } else if (gameSettings.keyBindUseItem.isKeyDown()) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(
                            EnumCommandType.SET, clientPlayerEntity.getUniqueID(), (UUID) null
                    ));
                }
            }
            if (gameSettings.keyBindUseItem.isKeyDown()) {
                RegistryMessages.sendToServer(new MessageCommandEntity(
                        EnumCommandType.REMOVE, clientPlayerEntity.getUniqueID(), entityRayTraceResult
                ));
            }
            if (gameSettings.keyBindPickBlock.isKeyDown()) {
                if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
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
