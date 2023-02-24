package com.github.quinnfrost.dragontongue.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

public class RenderNode {
    public static delayedTimer timer = new delayedTimer();
    // Todo: refactor to VoxelShapes to render AABBs
    public static Map<Integer, Pair<Integer, Pair<Vector3d, Boolean>>> renderCubeList = new HashMap<>();
    public static Map<Integer, Pair<Integer, Pair<Vector3d, Vector3d>>> renderLineList = new HashMap<>();
    public static Map<Integer, Pair<Integer, Pair<Vector3d, String>>> renderStringList = new HashMap<>();

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

    public static void drawCube(Integer time, Vector3d pos, boolean hollow, @Nullable Integer index) {
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

    public static void drawLine(Integer time, Vector3d startPos, Vector3d endPos, @Nullable Integer index) {
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

    public static void drawString(Integer time, Vector3d pos, String content, @Nullable Integer index) {
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
    public static void drawBoundingBox(Integer time, AxisAlignedBB axisAlignedBB, @Nullable Integer index) {
        VoxelShapes.create(axisAlignedBB).forEachEdge((x0, y0, z0, x1, y1, z1) -> {
            drawLine(time, new Vector3d(x0, y0, z0), new Vector3d(x1, y1, z1), null);
        });

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void setRenderPos(Integer time, Vector3d node, Vector3d lineStart, Integer index) {
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

    public static void render(MatrixStack matrixStack) {
        if (renderLineList.isEmpty() && renderCubeList.isEmpty() && renderStringList.isEmpty()) {
            return;
        }
        synchronized (lock) {
            Vector3d vec = Minecraft.getInstance().getRenderManager().info.getProjectedView();
            double dx = vec.getX();
            double dy = vec.getY();
            double dz = vec.getZ();

            RenderSystem.pushTextureAttributes();

            matrixStack.push();
            matrixStack.translate(-dx, -dy, -dz);

            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableBlend();
            RenderSystem.disableLighting();

            renderCubeList.forEach((integer, integerPairPair) -> {
                Pair<Vector3d, Boolean> renderInfo = integerPairPair.getSecond();
                if (renderInfo.getSecond()) {
                    renderCubeOutline(matrixStack, renderInfo.getFirst(), 1f);
                } else {
                    renderCube(matrixStack, renderInfo.getFirst(), 0.1f);
                }
            });

            renderLineList.forEach((integer, integerPairPair) -> {
                Pair<Vector3d, Vector3d> renderInfo = integerPairPair.getSecond();
                renderLine(matrixStack, renderInfo.getFirst(), renderInfo.getSecond());
            });

            renderStringList.forEach((integer, integerPairPair) -> {
                Pair<Vector3d, String> renderInfo = integerPairPair.getSecond();
                renderString(matrixStack, renderInfo.getFirst(), renderInfo.getSecond());
            });

            RenderSystem.disableDepthTest();
            RenderSystem.popAttributes();
            matrixStack.pop();
        }
    }

    public static void renderCubeOutline(MatrixStack matrixStack, Vector3d nodePos, float edgeLength) {
        VoxelShape voxelShape = VoxelShapes.create(
                nodePos.x - edgeLength / 2f,
                nodePos.y - edgeLength / 2f,
                nodePos.z - edgeLength / 2f,
                nodePos.x + edgeLength / 2f,
                nodePos.y + edgeLength / 2f,
                nodePos.z + edgeLength / 2f
        );
        renderShapeOutline(matrixStack, voxelShape);
    }

    private static void renderShapeOutline(MatrixStack matrixStack,
                                           VoxelShape voxelShape) {
        voxelShape.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
            renderLine(matrixStack, new Vector3d(x0, y0, z0), new Vector3d(x1, y1, z1));
        });
    }

    public static void renderCube(MatrixStack matrixStack, Vector3d nodePos, float edgeLength) {
        if (nodePos == null) {
            return;
        }
        Color color = Color.RED;

        matrixStack.push();
        matrixStack.translate(nodePos.getX(), nodePos.getY(), nodePos.getZ());

        matrixStack.scale(edgeLength, edgeLength, edgeLength);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();

        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        //  X+
        vertexBuffer.pos(matrix4f, 0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  X-
        vertexBuffer.pos(matrix4f, -0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Z-
        vertexBuffer.pos(matrix4f, -0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Z+
        vertexBuffer.pos(matrix4f, 0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Y+
        vertexBuffer.pos(matrix4f, 0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, 0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, 0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        //  Y-
        vertexBuffer.pos(matrix4f, -0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, -0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, -0.5f, -0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, 0.5f, -0.5f, 0.5f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        tessellator.draw();

        matrixStack.pop();
    }

    public static void renderLine(MatrixStack matrixStack, Vector3d lineStartPos, Vector3d lineEndPos) {
        if (lineEndPos == null) {
            return;
        }
        Color color = Color.WHITE;

        matrixStack.push();
        matrixStack.translate(lineEndPos.getX(), lineEndPos.getY(), lineEndPos.getZ());

//        matrixStack.scale(0.25F, 0.25F, 0.25F);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();

        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();

        if (lineStartPos != null) {
            final float pdx = (float) (lineStartPos.getX() - lineEndPos.getX());
            final float pdy = (float) (lineStartPos.getY() - lineEndPos.getY());
            final float pdz = (float) (lineStartPos.getZ() - lineEndPos.getZ());
            vertexBuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            vertexBuffer.pos(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            vertexBuffer.pos(matrix4f, pdx, pdy, pdz).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            tessellator.draw();
        }

        matrixStack.pop();
    }

    // From RenderPath#debugDrawNode

    public static void renderString(final MatrixStack matrixStack, Vector3d pos, String content) {
        if (pos == null) {
            return;
        }
        Color colorText = Color.WHITE;
        Color colorBackground = new Color(0,0,0,0.7f);

        final String s1 = content;
        final FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
        final int lineHeight = fontrenderer.FONT_HEIGHT;

        matrixStack.push();
        matrixStack.translate(pos.x, pos.y, pos.z);
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);

        final EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        matrixStack.rotate(renderManager.getCameraOrientation());
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

        final int i = fontrenderer.getStringWidth(s1) / 2;

        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(matrix4f, (-i - 1), 0f, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, (-i - 1), lineHeight, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, (i + 1), lineHeight, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        vertexBuffer.pos(matrix4f, (i + 1), 0f, 0.0f).color(colorBackground.getRed(), colorBackground.getGreen(), colorBackground.getBlue(), colorBackground.getAlpha()).endVertex();
        tessellator.draw();

        RenderSystem.enableTexture();

        final IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
//        matrixStack.translate(0.0F, -5F, 0.0F);
//        fontrenderer.renderString(s1, -fontrenderer.getStringWidth(s1) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);

        RenderSystem.depthMask(true);
        matrixStack.translate(0.0F, 1F, 0.0F);
        fontrenderer.renderString(s1, -fontrenderer.getStringWidth(s1) / 2.0f, 0, colorText.getRGB(), false, matrix4f, buffer, false, 0, 15728880);

        buffer.finish();

        matrixStack.pop();
    }


}
