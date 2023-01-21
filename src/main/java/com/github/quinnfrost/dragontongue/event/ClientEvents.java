package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {
    // TODO:组合快捷键
    @SubscribeEvent
    public static void onKeyPress(TickEvent.PlayerTickEvent event){
        if (!event.player.world.isRemote) {
            return;
        }
        ClientPlayerEntity player = (ClientPlayerEntity) event.player;

        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            player.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE, null).ifPresent(iCapabilityInfoHolder -> {
//                    EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(player,
//                            Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
//                    MyNetworking.sendToServer(
//                            new PacketCommandEntity(EnumCommandType.ATTACK, player, entityRayTraceResult));
                BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(player, Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f);

                //                if (blockRayTraceResult.getType() != RayTraceResult.Type.MISS) {
//                        RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REACH, player.getUniqueID(), blockRayTraceResult));
//                }
                RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REACH, player.getUniqueID(), blockRayTraceResult));
            });
        }
        if (KeyBindRegistry.add_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(player,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.ADD, player.getUniqueID(), entityRayTraceResult));
        }
        if (KeyBindRegistry.remove_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(player,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REMOVE, player.getUniqueID(), entityRayTraceResult));
        }
        if (KeyBindRegistry.set_tamed.isKeyDown()) {
            EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(player,
                    Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
            RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.SET, player.getUniqueID(), entityRayTraceResult));
        }
    }

    @SubscribeEvent
    public static void onItemColor(ColorHandlerEvent.Item event) {
//        event.getItemColors().register((stack, i) -> 0xff0000, ItemRegistry.FIRSTENTITYSPAWNEGG.get());
    }

//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.ClientTickEvent event) {
//        if (DragonTongue.aiDebugger.target != null) {
//            DragonTongue.aiDebugger.currentGoal = DragonTongue.aiDebugger.target.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString();
//            DragonTongue.aiDebugger.currentTarget = DragonTongue.aiDebugger.target.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString();
//        }
//    }

    @SubscribeEvent
    public static void detectScroll(InputEvent.MouseScrollEvent event) {
        final double inaccuracy = 0.0001;
        double scrollDelta = event.getScrollDelta();
        if (scrollDelta > inaccuracy) {
            KeyBindRegistry.scroll_status = KeyBindRegistry.EnumMouseScroll.UP;
        } else if (scrollDelta < -inaccuracy) {
            KeyBindRegistry.scroll_status = KeyBindRegistry.EnumMouseScroll.DOWN;
        } else {
            KeyBindRegistry.scroll_status = KeyBindRegistry.EnumMouseScroll.NONE;
        }
    }

}
