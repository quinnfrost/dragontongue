package com.github.quinnfrost.dragontongue.client.overlay;

import com.github.quinnfrost.dragontongue.client.render.CustomRenderTypes;
import com.github.quinnfrost.dragontongue.client.render.MRenderTypes;
import com.github.quinnfrost.dragontongue.utils.Vector2f;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OverlayCrossHair extends GuiComponent {
    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
    public static ResourceLocation scopeTexture = new ResourceLocation("dragontongue", "textures/misc/scope.png");
    public static boolean renderScope = false;
    public static float scopeSuggestion = 0;
    public static delayedTimer timer = new delayedTimer();

    private static Map<Vector2f, Pair<Integer, String>> bufferStringMap = new HashMap<Vector2f, Pair<Integer, String>>() {
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }
    };
    private static Map<Vector2f, Pair<Integer, IconType>> bufferIconMap = new HashMap<>();
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
                    !bufferIconMap.isEmpty()
                            || !bufferStringMap.isEmpty()
            ) {
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    bufferStringMap.replaceAll((vector3i, integerStringPair) -> {
                        if (integerStringPair.getFirst() >= 0) {
                            return Pair.of(integerStringPair.getFirst() - 1, integerStringPair.getSecond());
                        }
                        return integerStringPair;
                    });
                    bufferStringMap.entrySet().removeIf(vector3iPairEntry -> vector3iPairEntry.getValue().getFirst() <= 0);

                    bufferIconMap.replaceAll((vector2f, integerIconTypePair) -> {
                        if (integerIconTypePair.getFirst() >= 0) {
                            return Pair.of(integerIconTypePair.getFirst() - 1, integerIconTypePair.getSecond());
                        }
                        return integerIconTypePair;
                    });
                    bufferIconMap.entrySet().removeIf(vector2fPairEntry -> vector2fPairEntry.getValue().getFirst() <= 0);
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
        setCrossHairIcon(Vector2f.of(0, 0), iconTime, type);
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

    public static void setCrossHairIcon(Vector2f position, int iconTime, @Nullable IconType type) {
        if (iconTime != 0 || type == null) {
//            crIconTime = iconTime;
//            crIconType = type;
            bufferIconMap.put(position, Pair.of(iconTime, type));
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void renderStringCrossHair(PoseStack ms) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Font fontRender = minecraft.font;
        Color colour = new Color(255, 255, 255, 255);

//        int width = event.getWindow().getScaledWidth();
//        int height = event.getWindow().getScaledHeight();
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        synchronized (lock) {
            if (!bufferStringMap.isEmpty()) {
                bufferStringMap.forEach((vector2f, integerStringPair) -> {
                    fontRender.draw(ms, integerStringPair.getSecond(), scaledWidth / 2.0f + vector2f.x, scaledHeight / 2.0f + vector2f.y, colour.getRGB());
                });
            }
        }
    }

    public static void renderScope(PoseStack ms) {
        if (!renderScope) {
            return;
        }
        int scopeTextureLength = 256;
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        RenderSystem.setShaderTexture(0, scopeTexture);

        GuiComponent.blit(ms, scaledWidth / 2 - scopeTextureLength / 2, scaledHeight / 2, 0, 0, 0, scopeTextureLength, scopeTextureLength, 256, 256);
//        GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - scopeTextureLength / 2, scaledHeight / 2, 0, 0, scopeTextureLength, scopeTextureLength, 1);
    }

    public static void renderScopeSuggestionQuad(PoseStack poseStack) {
        if (!renderScope) {
            return;
        }

        final int lineWidth = 1;
        final float suggestionWidth = 40;
        float suggestPos = (float) (0.4058604333 * Math.pow(scopeSuggestion, 1.395441973));


        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();


//        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//        VertexConsumer bufferbuilder = buffer.getBuffer(MRenderTypes.customLineRenderer());
//        poseStack.pushPose();
//        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Matrix4f pMatrix = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(pMatrix, 0.5f + scaledWidth / 2f + suggestionWidth / 2, 0.5f + scaledHeight / 2f + suggestPos, 0).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(pMatrix, 0.5f + scaledWidth / 2f - suggestionWidth / 2, 0.5f + scaledHeight / 2f + suggestPos, 0).color(0, 0, 0, 255).endVertex();

        bufferbuilder.vertex(pMatrix, 0.5f + scaledWidth / 2f - suggestionWidth / 2, 0.5f + scaledHeight / 2f + suggestPos + lineWidth, 0).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(pMatrix, 0.5f + scaledWidth / 2f + suggestionWidth / 2, 0.5f + scaledHeight / 2f + suggestPos + lineWidth, 0).color(0, 0, 0, 255).endVertex();

        bufferbuilder.vertex(pMatrix, scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(pMatrix, scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(255, 255, 255, 255).endVertex();

        bufferbuilder.vertex(pMatrix, scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos + lineWidth, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(pMatrix, scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos + lineWidth, 0).color(255, 255, 255, 255).endVertex();

        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);

        RenderSystem.enableCull();

    }

    public static void renderScopeSuggestion(PoseStack ms) {
        if (!renderScope) {
            return;
        }
        final float suggestionWidth = 40;

        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        float suggestPos = (float) (0.4058604333 * Math.pow(scopeSuggestion, 1.395441973));
        Color color = Color.WHITE;

        ms.pushPose();

        RenderBuffers renderBuffers = new RenderBuffers();
        MultiBufferSource.BufferSource pBufferSource = renderBuffers.bufferSource();
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.lines());

        PoseStack.Pose pose = ms.last();
        vertexconsumer.vertex(scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .normal(1.0f,0.0f,0.0f)
                .endVertex();

        vertexconsumer.vertex(scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .normal(1.0f,0.0f,0.0f)
                .endVertex();

        pBufferSource.endBatch(RenderType.lines());

//        RenderSystem.enableDepthTest();
//        RenderSystem.disableTexture();
//        RenderSystem.disableBlend();
//
//        final Tesselator tessellator = Tesselator.getInstance();
//        final BufferBuilder vertexBuffer = tessellator.getBuilder();
//
//        RenderSystem.lineWidth(2.0F);
////        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
//        vertexBuffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
////        vertexBuffer.vertex(0.5 + scaledWidth / 2f - suggestionWidth / 2, 0.5 + scaledHeight / 2f + suggestPos, 0).color(0f, 0f, 0f, 1.0f).endVertex();
////        vertexBuffer.vertex(0.5 + scaledWidth / 2f + suggestionWidth / 2, 0.5 + scaledHeight / 2f + suggestPos, 0).color(0f, 0f, 0f, 1.0f).endVertex();
////        tessellator.end();
//
////        vertexBuffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
//        vertexBuffer.vertex(scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
//        vertexBuffer.vertex(scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
//        tessellator.end();
//
//        RenderSystem.lineWidth(1.0F);
//        RenderSystem.disableDepthTest();
        ms.popPose();
    }

    public static void renderIconCrossHair(PoseStack ms) {
        synchronized (lock) {
            if (!bufferIconMap.isEmpty()) {
                bufferIconMap.forEach((vector2f, integerIconTypePair) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    int markTextureLength = 16;
                    int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
                    int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
                    int xOffset = 0;
                    int yOffset = 0;

                    int xPosition = (int) (scaledWidth / 2 - markTextureLength / 2 + vector2f.x);
                    int yPosition = (int) (scaledHeight / 2 - markTextureLength / 2 + vector2f.y);
                    minecraft.getTextureManager().bindForSetup(markTexture);
                    switch (integerIconTypePair.getSecond()) {

                        case HIT:
                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, 0, 0, markTextureLength, markTextureLength, 1);
                            break;
                        case CRITICAL:
                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, markTextureLength, 0, markTextureLength, markTextureLength, 1);
                            break;
                        case WARN:
                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, markTextureLength * 2, 0, markTextureLength, markTextureLength, 1);
                            break;
                        case TARGET:
                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, markTextureLength * 3, 0, markTextureLength, markTextureLength, 1);
                            break;
                    }
                });
            }
        }

//        if (crIconTime > 0) {

//
//        }
    }

}
