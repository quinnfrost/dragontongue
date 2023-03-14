package com.github.quinnfrost.dragontongue.event;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.quinnfrost.dragontongue.client.ClientGlow;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.utils.Vector2f;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static Options gameSettings = Minecraft.getInstance().options;
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
                KeyBindRegistry.command_tamed.isDown()
                || KeyBindRegistry.select_tamed.isDown()
                || KeyBindRegistry.set_tamed_status.isDown()
        ) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void detectKeys(InputEvent.KeyInputEvent event) {
        LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        int maxDistance = Minecraft.getInstance().options.renderDistance * 16;
        LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;

        if (clientPlayerEntity != null && clientPlayerEntity.isShiftKeyDown() && (clientPlayerEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == IafItemRegistry.DRAGON_BOW
        || clientPlayerEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.BOW)) {
            OverlayCrossHair.renderScope = true;
            HitResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity, maxDistance, null);
            if (rayTraceResult.getType() != HitResult.Type.MISS) {
                double distance = clientPlayerEntity.getLightProbePosition(1.0f).distanceTo(rayTraceResult.getLocation());
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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.ClientTickEvent event) {

    }
    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        keySneakPressed = false;
        keySprintPressed = false;
    }

    @SubscribeEvent
    public static void onGuiKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getKeyCode() == gameSettings.keyShift.getKey().getValue()) {
            keySneakPressed = true;
        }
        if (event.getKeyCode() == gameSettings.keySprint.getKey().getValue()) {
            keySprintPressed = true;
        }
    }
    @SubscribeEvent
    public static void onGuiKeyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent event) {
        if (event.getKeyCode() == gameSettings.keyShift.getKey().getValue()) {
            keySneakPressed = false;
        }
        if (event.getKeyCode() == gameSettings.keySprint.getKey().getValue()) {
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
        LocalPlayer player = Minecraft.getInstance().player;
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
        LocalPlayer player = Minecraft.getInstance().player;
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
