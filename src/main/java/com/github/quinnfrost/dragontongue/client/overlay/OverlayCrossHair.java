package com.github.quinnfrost.dragontongue.client.overlay;

import com.github.quinnfrost.dragontongue.utils.Vector2f;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OverlayCrossHair extends AbstractGui {
    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
    public static ResourceLocation scopeTexture = new ResourceLocation("dragontongue", "textures/misc/scope.png");
    public static boolean renderScope = false;
    public static float scopeSuggestion = 0;
    public static int crIconTime = 0;
    public static IconType crIconType = IconType.HIT;
    public static delayedTimer timer = new delayedTimer();

    private static Map<Vector2f, Pair<Integer, String>> bufferStringMap = new HashMap<Vector2f, Pair<Integer, String>>() {
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }
    };
    private static final Object lock = new Object();

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
                    crIconTime > 0
                            || !bufferStringMap.isEmpty()
            ) {
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                --crIconTime;
                synchronized (lock) {
                    bufferStringMap.replaceAll((vector3i, integerStringPair) -> {
                        if (integerStringPair.getFirst() >= 0) {
                            return Pair.of(integerStringPair.getFirst() - 1, integerStringPair.getSecond());
                        }
                        return integerStringPair;
                    });
                    bufferStringMap.entrySet().removeIf(vector3iPairEntry -> vector3iPairEntry.getValue().getFirst() < 0);
                }
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
    public static void setCrossHairDisplay(@Nullable String string, int stringTime, int iconTime, @Nullable IconType type, boolean force) {
        setCrossHairIcon(type, iconTime);
        setCrossHairString(Vector2f.CR_DAMAGE, string, stringTime, force);
    }

    public static void setCrossHairString(Vector2f offsetFromCrosshair, @Nullable String string, int stringTime, boolean force) {
        synchronized (lock) {
            if (string != null) {

                if (!force && string.equals(bufferStringMap.getOrDefault(offsetFromCrosshair, Pair.of(0, null)).getSecond())) {
                    return;
                }
                if (!string.isEmpty() && stringTime != 0) {
                    bufferStringMap.put(offsetFromCrosshair, Pair.of(stringTime, string));
                }
            } else {
//                bufferStringMap.put(offsetFromCrosshair, Pair.of(0, ""));
//                return;
            }
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void setScopeString(String string) {

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

    public static void renderStringCrossHair(MatrixStack ms) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        FontRenderer fontRender = minecraft.fontRenderer;
        Color colour = new Color(255, 255, 255, 255);

//        int width = event.getWindow().getScaledWidth();
//        int height = event.getWindow().getScaledHeight();
        int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
        int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();

        synchronized (lock) {
            if (!bufferStringMap.isEmpty()) {
                bufferStringMap.forEach((vector2f, integerStringPair) -> {
                    fontRender.drawString(ms, integerStringPair.getSecond(), scaledWidth / 2.0f + vector2f.x, scaledHeight / 2.0f + vector2f.y, colour.getRGB());
                });
            }
        }
    }

    public static void renderScope(MatrixStack ms) {
        if (!renderScope) {
            return;
        }
        int scopeTextureLength = 256;
        int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
        int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(scopeTexture);
        GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - scopeTextureLength / 2, scaledHeight / 2, 0, 0, scopeTextureLength, scopeTextureLength, 1);
    }

    public static void renderScopeSuggestion(MatrixStack ms) {
        if (!renderScope) {
            return;
        }
        final float suggestionWidth = 40;

        int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
        int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();

        float suggestPos = (float) (0.4058604333 * Math.pow(scopeSuggestion, 1.395441973));

        RenderSystem.pushTextureAttributes();
        ms.push();
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        RenderSystem.disableLighting();

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();

        GL11.glLineWidth(2.0F);
        vertexBuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(0.5 + scaledWidth / 2f - suggestionWidth / 2, 0.5 + scaledHeight / 2f + suggestPos, 0).color(0,0,0,255).endVertex();
        vertexBuffer.pos(0.5 + scaledWidth / 2f + suggestionWidth / 2, 0.5 + scaledHeight / 2f + suggestPos, 0).color(0,0,0,255).endVertex();

        vertexBuffer.pos(scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(255,255,255,255).endVertex();
        vertexBuffer.pos(scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(255,255,255,255).endVertex();
        tessellator.draw();

        GL11.glLineWidth(1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.popAttributes();
        ms.pop();
    }

    public static void renderIconCrossHair(MatrixStack ms) {
        if (crIconTime > 0) {
            int markTextureLength = 16;
            int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
            int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
//            int screenWidth = event.getWindow().getWidth();
//            int screenHeight = event.getWindow().getHeight();
//            double scale = event.getWindow().getGuiScaleFactor();

            Minecraft.getInstance().getTextureManager().bindTexture(markTexture);
            switch (crIconType) {

                case HIT:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - markTextureLength / 2, scaledHeight / 2 - markTextureLength / 2, 0, 0, markTextureLength, markTextureLength, 1);
                    break;
                case CRITICAL:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - markTextureLength / 2, scaledHeight / 2 - markTextureLength / 2, markTextureLength, 0, markTextureLength, markTextureLength, 1);
                    break;
                case WARN:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - markTextureLength / 2, scaledHeight / 2 - markTextureLength / 2, markTextureLength * 2, 0, markTextureLength, markTextureLength, 1);
                    break;
                case TARGET:
                    GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - markTextureLength / 2, scaledHeight / 2 - markTextureLength / 2, markTextureLength * 3, 0, markTextureLength, markTextureLength, 1);
                    break;
            }

        }
    }

}
