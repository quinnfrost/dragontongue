package com.github.quinnfrost.dragontongue.event;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.quinnfrost.dragontongue.client.ClientGlow;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.utils.Vector2f;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static GameSettings gameSettings = Minecraft.getInstance().gameSettings;
    public static int lastMouseKeyCode = 0;
    public static boolean keySneakPressed = false;
    public static boolean keySprintPressed = false;
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
        int maxDistance = Minecraft.getInstance().gameSettings.renderDistanceChunks * 16;
        ClientPlayerEntity clientPlayerEntity = Minecraft.getInstance().player;

        if (clientPlayerEntity != null && clientPlayerEntity.isSneaking() && (clientPlayerEntity.getHeldItem(Hand.MAIN_HAND).getItem() == IafItemRegistry.DRAGON_BOW
        || clientPlayerEntity.getHeldItem(Hand.MAIN_HAND).getItem() == Items.BOW)) {
            OverlayCrossHair.renderScope = true;
            RayTraceResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity, maxDistance, null);
            if (rayTraceResult.getType() != RayTraceResult.Type.MISS) {
                double distance = clientPlayerEntity.getClientEyePosition(1.0f).distanceTo(rayTraceResult.getHitVec());
                OverlayCrossHair.scopeSuggestion = (float) distance;
                OverlayCrossHair.setCrossHairString(
                        Vector2f.CR_DISTANCE,
                        String.format("%.1f", distance),
                        2,
                        true
                );
            } else {
                OverlayCrossHair.setCrossHairString(
                        Vector2f.CR_DISTANCE,
                        "--",
                        2,
                        true
                );
            }
        } else {
            if (OverlayCrossHair.renderScope) {
                // Clear text is not implemented
//                OverlayCrossHair.setCrossHairString(
//                        Vector2f.CR_DISTANCE,
//                        null,
//                        1,
//                        true
//                );
                OverlayCrossHair.renderScope = false;
            }
        }

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
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        keySneakPressed = false;
        keySprintPressed = false;
    }

    @SubscribeEvent
    public static void onGuiKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getKeyCode() == gameSettings.keyBindSneak.getKey().getKeyCode()) {
            keySneakPressed = true;
        }
        if (event.getKeyCode() == gameSettings.keyBindSprint.getKey().getKeyCode()) {
            keySprintPressed = true;
        }
    }
    @SubscribeEvent
    public static void onGuiKeyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent event) {
        if (event.getKeyCode() == gameSettings.keyBindSneak.getKey().getKeyCode()) {
            keySneakPressed = false;
        }
        if (event.getKeyCode() == gameSettings.keyBindSprint.getKey().getKeyCode()) {
            keySprintPressed = false;
        }
    }

    @SubscribeEvent
    public static void onGuiMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {

    }

    @SubscribeEvent
    public static void onGuiMouseRelease(GuiScreenEvent.MouseReleasedEvent.Pre event) {

    }

    @SubscribeEvent
    public static void onSetFogDensity(EntityViewRenderEvent.FogDensity event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            event.setDensity(0f);
            event.setCanceled(true);
        } else if (player.isInLava() && util.canSwimInLava(player)) {
            event.setDensity(0.03f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderBlockOverlayEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.FIRE) {
            return;
        }
        if (event.getPlayer().isCreative()) {
            event.setCanceled(true);
        } else if (player.isInLava() && util.canSwimInLava(event.getPlayer())) {
            event.getMatrixStack().translate(0, -0.25, 0);
        }
    }

}
