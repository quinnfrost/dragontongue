package com.github.quinnfrost.dragontongue.client.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OverlayCrossHair {
    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
    public static List<String> buffer = new ArrayList<>(3);
    private static String bufferCrossHair = "";
    public static int crStringTime = 0;
    public static int crIconTime = 0;
    public static boolean critical = false;
    public static delayedTimer timer = new delayedTimer();

    public static class delayedTimer extends Thread {
        public delayedTimer() {

        }

        public void run() {
            while (
                    crStringTime > 0
                            || crIconTime > 0
            ) {
                try {
                    sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                --crStringTime;
                --crIconTime;
            }
        }
    }

    public static void setCrossHairString(String string, int stringTime, int iconTime) {
        crStringTime = stringTime;
        crIconTime = iconTime;
        if (!string.isEmpty()) {
            bufferCrossHair = string;
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
        for (int i = 0; i < buffer.size(); i++) {
            String currentString = buffer.get(i);
            if (buffer.get(i).length() < maxLength) {
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

//        player.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
//            if (bufferCrossHair.isEmpty()) {
//                bufferCrossHair = String.valueOf(iCapTargetHolder.getCommandDistance());
//            }
//            if (iCapTargetHolder.getCommandDistance() != Double.valueOf(bufferCrossHair)) {
//                setCrossHairString(String.valueOf(iCapTargetHolder.getCommandDistance()));
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
            if (!critical) {
//                AbstractGui.blit(event.getMatrixStack(), screenWidth / 2 - textureLength / 2, screenHeight / 2 - textureLength / 2, 0, 0,textureLength,textureLength,textureLength,textureLength);
                GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - textureLength / 2, scaledHeight / 2 - textureLength / 2, 0, 0, textureLength, textureLength, 1);
            } else {
                GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - textureLength / 2, scaledHeight / 2 - textureLength / 2, textureLength, 0, textureLength, textureLength, 1);
            }
        }
    }

}
