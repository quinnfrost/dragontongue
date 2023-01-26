package com.github.quinnfrost.dragontongue.client.gui;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//TODO: 完成击中提示
@OnlyIn(Dist.CLIENT)
public class GUICrossHair {
    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
    public static List<String> buffer = new ArrayList<>(3);
    private static String bufferCrosshair = "";
    public static int displayTime = 0;
    public static Delay delayS = new Delay();

    public static class Delay extends Thread {
        public Delay() {

        }

        public void run() {
            while (displayTime > 0) {
                try {
                    sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                --displayTime;
            }
        }
    }

    public static void setCrossHairString(String string) {
        displayTime = 5;
        bufferCrosshair = string;
        if (!delayS.isAlive()) {
            delayS = new Delay();
            delayS.start();
        }
    }

    public static void renderString(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            int s = 16;
            int width = event.getWindow().getScaledWidth();
            int height = event.getWindow().getScaledHeight();
            final int maxLength = 50;

            Minecraft minecraft = Minecraft.getInstance();
            PlayerEntity player = minecraft.player;
            FontRenderer fontRender = minecraft.fontRenderer;
            MatrixStack ms = new MatrixStack();
            Color colour = new Color(255, 255, 255, 255);

            GL11.glPushMatrix();

            int heightoffset = 0;
            for (int i = 0; i < buffer.size(); i++) {
                String currentString = buffer.get(i);
                if (buffer.get(i).length() < maxLength) {
                    fontRender.drawString(ms, currentString, 5, 5 + 10 * i + heightoffset, colour.getRGB());
                } else {
                    while (currentString.length() >= maxLength) {
                        fontRender.drawString(ms, currentString.substring(0,maxLength - 1), 5, 5 + 10 * i + heightoffset, colour.getRGB());
                        currentString = currentString.substring(maxLength);
                        heightoffset += 8;
                    }
                    fontRender.drawString(ms, currentString, 5, 5 + 10 * i + heightoffset, colour.getRGB());
                }
            }

            player.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                if (bufferCrosshair.isEmpty()){
                    bufferCrosshair = String.valueOf(iCapabilityInfoHolder.getCommandDistance());
                }
                if (iCapabilityInfoHolder.getCommandDistance() != Double.valueOf(bufferCrosshair)) {
                    setCrossHairString(String.valueOf(iCapabilityInfoHolder.getCommandDistance()));
                }
            });
            if (displayTime > 0) {
                fontRender.drawString(ms, bufferCrosshair, width / 2.0f - 2, height / 2.0f - 3 * s + 8, colour.getRGB());
            }

            GL11.glPopMatrix();
        }
    }

    public static void renderCrossHairIcon(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int s = 16;
            int width = event.getWindow().getScaledWidth();
            int height = event.getWindow().getScaledHeight();
            MatrixStack ms = new MatrixStack();
            Minecraft.getInstance().getTextureManager().bindTexture(markTexture);
//            AbstractGui.blit(event.getMatrixStack(), width / 2 - s / 2, height / 2 - s / 2, 0, 0,s,s,s,s);
            GuiUtils.drawTexturedModalRect(ms, width / 2 - s / 2, height / 2 - s / 2, 0, 0, s, s,0);
        }
    }

}
