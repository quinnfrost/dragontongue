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
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {
    @SubscribeEvent
    public static void onPlayerTickClient(TickEvent.PlayerTickEvent event){
        if (!event.player.world.isRemote) {
            return;
        }
        ClientPlayerEntity player = (ClientPlayerEntity) event.player;
        if (KeyBindRegistry.command_tamed.isKeyDown()) {
            player.getCapability(CapabilityInfoHolder.ENTITY_TEST_CAPABILITY, null).ifPresent(iTestCapability -> {
//                    EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(player,
//                            Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f, null);
//                    MyNetworking.sendToServer(
//                            new PacketCommandEntity(EnumCommandType.ATTACK, player, entityRayTraceResult));
                BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(player, Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f);
                if (blockRayTraceResult.getType() != RayTraceResult.Type.MISS) {
                    RegistryMessages.sendToServer(new MessageCommandEntity(EnumCommandEntity.REACH, player.getUniqueID(), iTestCapability.getUUID(), blockRayTraceResult));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemColor(ColorHandlerEvent.Item event) {
//        event.getItemColors().register((stack, i) -> 0xff0000, ItemRegistry.FIRSTENTITYSPAWNEGG.get());
    }

}
