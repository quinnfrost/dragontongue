package com.github.quinnfrost.dragontongue.client.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.AABB;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderNode {
    public static delayedTimer timer = new delayedTimer();
    // Todo: 改成存voxelshape
    public static Map<Integer, Pair<Integer, Pair<Vec3, Boolean>>> renderCubeList = new HashMap<>();
    public static Map<Integer, Pair<Integer, Pair<Vec3, Vec3>>> renderLineList = new HashMap<>();
    public static Map<Integer, Pair<Integer, Pair<Vec3, String>>> renderStringList = new HashMap<>();

    public static final Object lock = new Object();
    public static Random random = new Random();

    public static class delayedTimer extends Thread {
        public void run() {
            while (!renderCubeList.isEmpty() || !renderLineList.isEmpty() || !renderStringList.isEmpty()) {
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    renderCubeList.replaceAll((integer, integerPairPair) -> {
                        if (integerPairPair.getFirst() >= 0) {
                            return Pair.of(integerPairPair.getFirst() - 1, integerPairPair.getSecond());
                        }
                        return integerPairPair;
                    });
                    renderCubeList.entrySet().removeIf(integerPairEntry -> integerPairEntry.getValue().getFirst() <= 0);

                    renderLineList.replaceAll((integer, integerPairPair) -> {
                        if (integerPairPair.getFirst() >= 0) {
                            return Pair.of(integerPairPair.getFirst() - 1, integerPairPair.getSecond());
                        }
                        return integerPairPair;
                    });
                    renderLineList.entrySet().removeIf(integerPairEntry -> integerPairEntry.getValue().getFirst() <= 0);

                    renderStringList.replaceAll((integer, integerPairPair) -> {
                        if (integerPairPair.getFirst() >= 0) {
                            return Pair.of(integerPairPair.getFirst() - 1, integerPairPair.getSecond());
                        }
                        return integerPairPair;
                    });
                    renderStringList.entrySet().removeIf(integerPairEntry -> integerPairEntry.getValue().getFirst() <= 0);

                }

            }
        }
    }

    public static void drawCube(Integer time, Vec3 pos, boolean hollow, @Nullable Integer index) {
        synchronized (lock) {
            int idx;
            if (index == null) {
                idx = random.nextInt();
            } else {
                idx = index;
            }
            renderCubeList.put(idx, Pair.of(time, Pair.of(pos, hollow)));
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void drawLine(Integer time, Vec3 startPos, Vec3 endPos, @Nullable Integer index) {
        synchronized (lock) {
            int idx;
            if (index == null) {
                idx = random.nextInt();
            } else {
                idx = index;
            }
            renderLineList.put(idx, Pair.of(time, Pair.of(startPos, endPos)));
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void drawString(Integer time, Vec3 pos, String content, @Nullable Integer index) {
        synchronized (lock) {
            int idx;
            if (index == null) {
                idx = random.nextInt();
            } else {
                idx = index;
            }
            renderStringList.put(idx, Pair.of(time, Pair.of(pos, content)));
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }
    public static void drawBoundingBox(Integer time, AABB axisAlignedBB, @Nullable Integer index) {
        Shapes.create(axisAlignedBB).forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            drawLine(time, new Vec3(x0, y0, z0), new Vec3(x1, y1, z1), null);
        });

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void setRenderPos(Integer time, Vec3 node, Vec3 lineStart, Integer index) {
        if (time > 0) {
            synchronized (lock) {
                Integer i;
                if (index != null) {
                    i = index;
                } else {
                    i = random.nextInt();
                }
                drawCube(time, node, false, i);
                drawLine(time, lineStart, node, i);
//                drawString(time, node, node.toString(), i);
//                drawBoundingBox(2, new AxisAlignedBB(new BlockPos(lineStart)), i);
            }
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }

    }

    public static void render(PoseStack matrixStack) {
        if (renderLineList.isEmpty() && renderCubeList.isEmpty() && renderStringList.isEmpty()) {
            return;
        }
        synchronized (lock) {
            Vec3 vec = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
            double dx = vec.x();
            double dy = vec.y();
            double dz = vec.z();


            matrixStack.pushPose();
            matrixStack.translate(-dx, -dy, -dz);

            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableBlend();

            renderCubeList.forEach((integer, integerPairPair) -> {
                Pair<Vec3, Boolean> renderInfo = integerPairPair.getSecond();
                if (renderInfo.getSecond()) {
                    renderCubeOutline(matrixStack, renderInfo.getFirst(), 1f);
                } else {
                    renderCube(matrixStack, renderInfo.getFirst(), 0.1f);
                }
            });

            renderLineList.forEach((integer, integerPairPair) -> {
                Pair<Vec3, Vec3> renderInfo = integerPairPair.getSecond();
                renderLine(matrixStack, renderInfo.getFirst(), renderInfo.getSecond());
            });

            renderStringList.forEach((integer, integerPairPair) -> {
                Pair<Vec3, String> renderInfo = integerPairPair.getSecond();
                renderString(matrixStack, renderInfo.getFirst(), renderInfo.getSecond());
            });

            RenderSystem.disableDepthTest();
            matrixStack.popPose();
        }
    }

    public static void renderCubeOutline(PoseStack matrixStack, Vec3 nodePos, float edgeLength) {
        VoxelShape voxelShape = Shapes.box(
                nodePos.x - edgeLength / 2f,
                nodePos.y - edgeLength / 2f,
                nodePos.z - edgeLength / 2f,
                nodePos.x + edgeLength / 2f,
                nodePos.y + edgeLength / 2f,
                nodePos.z + edgeLength / 2f
        );
        renderVoxelShapeOutline(matrixStack, voxelShape);
    }

    private static void renderVoxelShapeOutline(PoseStack matrixStack,
                                                VoxelShape voxelShape) {
        voxelShape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            renderLine(matrixStack, new Vec3(x0, y0, z0), new Vec3(x1, y1, z1));
        });
    }

    public static void renderCube(PoseStack matrixStack, Vec3 nodePos, float edgeLength) {
        if (nodePos == null) {
            return;
        }
        Color color = Color.RED;

        matrixStack.pushPose();
        matrixStack.translate(nodePos.x(), nodePos.y(), nodePos.z());

        matrixStack.scale(edgeLength, edgeLength, edgeLength);

        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();

        final Matrix4f matrix4f = matrixStack.last().pose();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        //  X+
        vertexBuffer.vertex(matrix4f, 0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  X-
        vertexBuffer.vertex(matrix4f, -0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Z-
        vertexBuffer.vertex(matrix4f, -0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Z+
        vertexBuffer.vertex(matrix4f, 0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Y+
        vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Y-
        vertexBuffer.vertex(matrix4f, -0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, -0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, 0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        tessellator.end();

        matrixStack.popPose();
    }

    public static void renderLine(PoseStack matrixStack, Vec3 lineStartPos, Vec3 lineEndPos) {
        if (lineEndPos == null) {
            return;
        }
        Color color = Color.WHITE;

        matrixStack.pushPose();
        matrixStack.translate(lineEndPos.x(), lineEndPos.y(), lineEndPos.z());

//        matrixStack.scale(0.25F, 0.25F, 0.25F);

        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();

        final Matrix4f matrix4f = matrixStack.last().pose();

        if (lineStartPos != null) {
            final float pdx = (float) (lineStartPos.x() - lineEndPos.x());
            final float pdy = (float) (lineStartPos.y() - lineEndPos.y());
            final float pdz = (float) (lineStartPos.z() - lineEndPos.z());
            vertexBuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            vertexBuffer.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            vertexBuffer.vertex(matrix4f, pdx, pdy, pdz).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            tessellator.end();
        }

        matrixStack.popPose();
    }

    // From RenderPath#debugDrawNode

    public static void renderString(final PoseStack matrixStack, Vec3 pos, String content) {
        if (pos == null) {
            return;
        }
        Color colorText = Color.WHITE;
        Color colorBackground = new Color(0,0,0,0.7f);

        final String s1 = content;
        final Font fontrenderer = Minecraft.getInstance().font;
        final int lineHeight = fontrenderer.lineHeight;

        matrixStack.pushPose();
        matrixStack.translate(pos.x, pos.y, pos.z);

        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        matrixStack.mulPose(renderManager.cameraOrientation());
        matrixStack.scale(-0.014F, -0.014F, 0.014F);
        matrixStack.translate(0.0F, -lineHeight / 2F, 0.0F);

        RenderSystem.depthMask(false);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.disableTexture();

        final int i = fontrenderer.width(s1) / 2;

        final Matrix4f matrix4f = matrixStack.last().pose();
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertexBuffer.vertex(matrix4f, (-i - 1), 0f, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, (-i - 1), lineHeight, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, (i + 1), lineHeight, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        vertexBuffer.vertex(matrix4f, (i + 1), 0f, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();

        final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//        matrixStack.translate(0.0F, -5F, 0.0F);
//        fontrenderer.renderString(s1, -fontrenderer.getStringWidth(s1) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);

        RenderSystem.depthMask(true);
        matrixStack.translate(0.0F, 1F, 0.0F);
        fontrenderer.drawInBatch(s1, -fontrenderer.width(s1) / 2.0f, 0, colorText.getRGB(), false, matrix4f, buffer, false, 0, 15728880);

        buffer.endBatch();

        matrixStack.popPose();
    }


}
