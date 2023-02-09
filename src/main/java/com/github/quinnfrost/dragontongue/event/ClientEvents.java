package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.client.ClientGlow;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static int lastMouseKeyCode = 0;
    public static boolean keySneakPressed = false;
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
    @SubscribeEvent
    public static void detectClicks(InputEvent.ClickInputEvent event) {
        if (
                KeyBindRegistry.command_tamed.isKeyDown()
                || KeyBindRegistry.select_tamed.isKeyDown()
                || KeyBindRegistry.set_tamed_status.isKeyDown()
        ) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void detectKeys(InputEvent.KeyInputEvent event) {
        ClientPlayerEntity clientPlayerEntity = Minecraft.getInstance().player;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        ClientPlayerEntity clientPlayerEntity = Minecraft.getInstance().player;
        ClientGlow.tickGlowing();
        KeyBindRegistry.scanKeyPress(clientPlayerEntity);
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
    public static void onGuiKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getKeyCode() == Minecraft.getInstance().gameSettings.keyBindSneak.getKey().getKeyCode()) {
            keySneakPressed = true;
        }
    }
    @SubscribeEvent
    public static void onGuiKeyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent event) {
        if (event.getKeyCode() == Minecraft.getInstance().gameSettings.keyBindSneak.getKey().getKeyCode()) {
            keySneakPressed = false;
        }
    }

    @SubscribeEvent
    public static void onGuiMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {

    }

}
