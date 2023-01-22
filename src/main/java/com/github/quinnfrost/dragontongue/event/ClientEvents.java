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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void detectScroll(InputEvent.MouseScrollEvent event) {
        if (KeyBindRegistry.scan_scroll) {
            final double inaccuracy = 0.0001;
            double scrollDelta = event.getScrollDelta();
            if (scrollDelta > inaccuracy) {
                KeyBindRegistry.scroll_status = KeyBindRegistry.EnumMouseScroll.UP;
            } else if (scrollDelta < -inaccuracy) {
                KeyBindRegistry.scroll_status = KeyBindRegistry.EnumMouseScroll.DOWN;
            }
            event.setCanceled(true);
        }
    }

    // TODO:组合快捷键
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        if (!event.player.world.isRemote) {
            return;
        }
        ClientPlayerEntity clientPlayer = (ClientPlayerEntity) event.player;
        KeyBindRegistry.scanKeyPress(clientPlayer);
        KeyBindRegistry.scanScrollAction(clientPlayer);
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



}
