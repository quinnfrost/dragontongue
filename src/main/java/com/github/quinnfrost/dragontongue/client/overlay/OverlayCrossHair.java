package com.github.quinnfrost.dragontongue.client.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OverlayCrossHair extends AbstractGui {
    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
    public static List<String> bufferInfoLeft = new ArrayList<>(3);
    private static String bufferCrossHair = "";
    public static int crStringTime = 0;
    public static int crIconTime = 0;
    public static IconType crIconType = IconType.HIT;
    public static boolean critical = false;
    public static delayedTimer timer = new delayedTimer();

    public enum IconType {
        HIT,
        CRITICAL,
        WARN,
        TARGET
    }

    public static class delayedTimer extends Thread {
        public delayedTimer() {

        }

        public void run() {
            while (
                    crStringTime > 0
                            || crIconTime > 0
            ) {
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                --crStringTime;
                --crIconTime;
            }
        }
    }

    /**
     * Set cross-hair display
     *
     * @param string     Content to display
     * @param stringTime Time before the content disappears in ticks
     * @param iconTime   Time before the cross-hair disappears in ticks, 0 for not showing at all
     * @param type
     * @param force      Whether to refresh the display time even if content is the same
     */
    public static void setCrossHairDisplay(@Nullable String string, int stringTime, int iconTime,@Nullable IconType type, boolean force) {
        setCrossHairIcon(type, iconTime);
        setCrossHairString(string, stringTime, force);
    }

    public static void setCrossHairString(@Nullable String string, int stringTime, boolean force) {
        if (!force && bufferCrossHair.equals(string)) {
            return;
        }
        if (string != null && !string.isEmpty() && stringTime != 0) {
            crStringTime = stringTime;
            bufferCrossHair = string;
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void setCrossHairIcon(@Nullable IconType type, int iconTime) {
        if (iconTime != 0 || type == null) {
            crIconTime = iconTime;
            crIconType = type;
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void renderStringLeftPanel(RenderGameOverlayEvent.Post event) {
        int width = event.getWindow().getScaledWidth();
        int height = event.getWindow().getScaledHeight();
        final int maxLength = 50;

        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        FontRenderer fontRender = minecraft.fontRenderer;
        MatrixStack ms = new MatrixStack();
        Color colour = new Color(255, 255, 255, 255);

        int heightoffset = 0;
        for (int i = 0; i < bufferInfoLeft.size(); i++) {
            String currentString = bufferInfoLeft.get(i);
            if (bufferInfoLeft.get(i).length() < maxLength) {
                fontRender.drawString(ms, currentString, 5, 5 + 10 * i + heightoffset, colour.getRGB());
            } else {
                while (currentString.length() >= maxLength) {
                    fontRender.drawString(ms, currentString.substring(0, maxLength - 1), 5, 5 + 10 * i + heightoffset, colour.getRGB());
                    currentString = currentString.substring(maxLength);
                    heightoffset += 8;
                }
                fontRender.drawString(ms, currentString, 5, 5 + 10 * i + heightoffset, colour.getRGB());
            }
        }

    }

    public static void renderStringCrossHair(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        FontRenderer fontRender = minecraft.fontRenderer;
        MatrixStack ms = new MatrixStack();
        Color colour = new Color(255, 255, 255, 255);

        int xOffset = -2;
        int yOffset = -40;
        int width = event.getWindow().getScaledWidth();
        int height = event.getWindow().getScaledHeight();

//        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
//            if (bufferCrossHair.isEmpty()) {
//                bufferCrossHair = String.valueOf(iCapTargetHolder.getCommandDistance());
//            }
//            if (iCapTargetHolder.getCommandDistance() != Double.valueOf(bufferCrossHair)) {
//                setCrossHairDisplay(String.valueOf(iCapTargetHolder.getCommandDistance()));
//            }
//        });
        if (crStringTime > 0) {
//            fontRender.drawString(ms, bufferCrossHair, width / 2.0f - 2, height / 2.0f - 3 * yOffset + 8, colour.getRGB());
            fontRender.drawString(ms, bufferCrossHair, width / 2.0f + xOffset, height / 2.0f + yOffset, colour.getRGB());
        }
    }

    public static void renderIconCrossHair(RenderGameOverlayEvent.Post event) {
        if (crIconTime > 0) {
            int textureLength = 16;
            int scaledWidth = event.getWindow().getScaledWidth();
            int scaledHeight = event.getWindow().getScaledHeight();
            int screenWidth = event.getWindow().getWidth();
            int screenHeight = event.getWindow().getHeight();
            MatrixStack ms = new MatrixStack();
            Minecraft.getInstance().getTextureManager().bindTexture(markTexture);
            switch (crIconType) {

                case HIT:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - textureLength / 2, scaledHeight / 2 - textureLength / 2, 0, 0, textureLength, textureLength, 1);
                    break;
                case CRITICAL:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - textureLength / 2, scaledHeight / 2 - textureLength / 2, textureLength, 0, textureLength, textureLength, 1);
                    break;
                case WARN:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - textureLength / 2, scaledHeight / 2 - textureLength / 2, textureLength * 2, 0, textureLength, textureLength, 1);
                    break;
                case TARGET:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - textureLength / 2, scaledHeight / 2 - textureLength / 2, textureLength * 3, 0, textureLength, textureLength, 1);
                    break;
            }

        }
    }

}
