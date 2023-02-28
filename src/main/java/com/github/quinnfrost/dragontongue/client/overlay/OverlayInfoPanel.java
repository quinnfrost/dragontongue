package com.github.quinnfrost.dragontongue.client.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OverlayInfoPanel extends AbstractGui {
    public static List<String> bufferInfoLeft = new ArrayList<>(3);
    public static List<String> bufferInfoRight = new ArrayList<>(3);

    public static void renderPanel(MatrixStack ms) {
        if (bufferInfoLeft == null) {
            return;
        }

        final int maxLineLength = 50;

        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        FontRenderer fontRender = minecraft.fontRenderer;
        Color colour = new Color(255, 255, 255, 255);

        renderString(ms, bufferInfoLeft, colour, 5, 5, maxLineLength, false);

    }

    public static void renderString(MatrixStack ms, List<String> content, Color color, int xOffset, int yOffset, int maxLineLength, boolean rightAlign) {
        if (content == null) {
            return;
        }
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int lineHeight = 10;
        int sublineHeight = 8;

        int sublineCount = 0;
        if (!rightAlign) {
            for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
                String currentString = content.get(lineIndex);
                if (currentString.length() < maxLineLength) {
                    fontRenderer.drawString(ms, currentString, xOffset, yOffset + lineHeight * lineIndex + sublineHeight * sublineCount, color.getRGB());
                } else {
                    while (currentString.length() >= maxLineLength) {
                        fontRenderer.drawString(ms, currentString.substring(0, maxLineLength), xOffset, yOffset + lineHeight * lineIndex + sublineHeight * sublineCount, color.getRGB());
                        currentString = currentString.substring(maxLineLength);
                        sublineCount += 1;
                    }
                    fontRenderer.drawString(ms, currentString, xOffset, yOffset + lineHeight * lineIndex + sublineHeight * sublineCount, color.getRGB());
                }
            }
        } else {
            for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
                String currentString = content.get(lineIndex);
                fontRenderer.getStringWidth(currentString);
                if (currentString.length() < maxLineLength) {
                    fontRenderer.drawString(ms, currentString, xOffset - fontRenderer.getStringWidth(currentString), yOffset + lineHeight * lineIndex + sublineHeight * sublineCount, color.getRGB());
                } else {
                    while (currentString.length() >= maxLineLength) {
                        String currentRenderString = currentString.substring(0, maxLineLength - 1);
                        fontRenderer.drawString(ms, currentRenderString, xOffset - fontRenderer.getStringWidth(currentRenderString), yOffset + lineHeight * lineIndex + sublineHeight * sublineCount, color.getRGB());
                        currentString = currentString.substring(maxLineLength);
                        sublineCount += 1;
                    }
                    fontRenderer.drawString(ms, currentString, xOffset - fontRenderer.getStringWidth(currentString), yOffset + lineHeight * lineIndex + sublineHeight * sublineCount, color.getRGB());
                }
            }
        }

    }

    public static void renderDifference(MatrixStack ms) {
        if (bufferInfoLeft == null) {
            return;
        }
        final int xOffsetLeft = 5;
        final int yOffsetLeft = 5;
        final int xOffsetRight = Minecraft.getInstance().getMainWindow().getScaledWidth();
        final int yOffsetRight = 5;
        final int maxLineLength = 60;

        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int lineHeight = 10;
        int sublineHeight = 8;

        int sublineCountLeft = 0;
        int sublineCountRight = 0;
        if (bufferInfoLeft.size() == bufferInfoRight.size()) {
            for (int lineIndex = 0; lineIndex < bufferInfoLeft.size(); lineIndex++) {
                Color color = Color.WHITE;

                String currentStringLeft = bufferInfoLeft.get(lineIndex);
                if (currentStringLeft.length() < maxLineLength) {
                    fontRenderer.drawString(ms, currentStringLeft, xOffsetLeft, yOffsetLeft + lineHeight * lineIndex + sublineHeight * sublineCountLeft, color.getRGB());
                } else {
                    while (currentStringLeft.length() >= maxLineLength) {
                        fontRenderer.drawString(ms, currentStringLeft.substring(0, maxLineLength - 1), xOffsetLeft, yOffsetLeft + lineHeight * lineIndex + sublineHeight * sublineCountLeft, color.getRGB());
                        currentStringLeft = currentStringLeft.substring(maxLineLength);
                        sublineCountLeft += 1;
                    }
                    fontRenderer.drawString(ms, currentStringLeft, xOffsetLeft, yOffsetLeft + lineHeight * lineIndex + sublineHeight * sublineCountLeft, color.getRGB());
                }

                String currentStringRight = bufferInfoRight.get(lineIndex);
                if (!bufferInfoLeft.get(lineIndex).equals(bufferInfoRight.get(lineIndex))) {
                    color = Color.RED;
                }
                if (currentStringRight.length() < maxLineLength) {
                    fontRenderer.drawString(ms, currentStringRight, xOffsetRight - fontRenderer.getStringWidth(currentStringRight), yOffsetRight + lineHeight * lineIndex + sublineHeight * sublineCountRight, color.getRGB());
                } else {
                    while (currentStringRight.length() >= maxLineLength) {
                        String currentRenderString = currentStringRight.substring(0, maxLineLength - 1);
                        fontRenderer.drawString(ms, currentRenderString, xOffsetRight - fontRenderer.getStringWidth(currentRenderString), yOffsetRight + lineHeight * lineIndex + sublineHeight * sublineCountRight, color.getRGB());
                        currentStringRight = currentStringRight.substring(maxLineLength);
                        sublineCountRight += 1;
                    }
                    fontRenderer.drawString(ms, currentStringRight, xOffsetRight - fontRenderer.getStringWidth(currentStringRight), yOffsetRight + lineHeight * lineIndex + sublineHeight * sublineCountRight, color.getRGB());
                }

            }
        }


    }

}
