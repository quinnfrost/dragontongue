package com.github.quinnfrost.dragontongue.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class RenderNode {
    public static delayedTimer timer = new delayedTimer();
    public static List<List<Integer>> renderTimers = new ArrayList<>();
    public static Map<Integer, List<Vector3d>> renderList = new HashMap<>();
    public static final Object lock = new Object();
    public static Random random = new Random();

    public static class delayedTimer extends Thread {
        public void run() {
            while (!renderTimers.isEmpty()) {
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    for (int i = 0; i < renderTimers.size(); i++) {
                        int index = renderTimers.get(i).get(0);
                        int time = renderTimers.get(i).get(1);
                        List<Integer> item = new ArrayList<>();
                        if (time > 0) {
                            item.add(index);
                            item.add(time - 1);
                            renderTimers.set(i, item);
                        } else {
                            renderTimers.remove(i);
                            renderList.remove(i);
                        }
                    }
                }

            }
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
                List<Integer> timerItem = new ArrayList<>();
                timerItem.add(i);
                timerItem.add(time);
                renderTimers.add(timerItem);
                List<Vector3d> renderItem = new ArrayList<>();
                renderItem.add(node);
                if (lineStart != null) {
                    renderItem.add(lineStart);
                }
                renderList.put(i, renderItem);
            }
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }

    }

    public static void drawAllNodes(MatrixStack matrixStack) {
        if (renderList.isEmpty()) {
            return;
        }
        synchronized (lock) {
            for (List<Integer> timerItem :
                    renderTimers) {
                int index = timerItem.get(0);
                List<Vector3d> renderItem = renderList.get(index);
                if (renderItem != null) {
                    Vector3d renderPosition = renderItem.get(0);
                    if (renderItem.size() == 2) {
                        Vector3d renderLineStart = renderItem.get(1);
                        debugDrawNode(matrixStack, renderPosition, renderLineStart);
                    } else {
                        debugDrawNode(matrixStack, renderPosition, null);
                    }
                }
            }
        }
    }

    // From RenderPath#debugDrawNode
    public static void debugDrawNode(MatrixStack matrixStack, Vector3d nodePos, Vector3d lineStartPos) {
        if (nodePos == null) {
            return;
        }

        float r = 1;
        float g = 0;
        float b = 0;

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

        matrixStack.push();
        matrixStack.translate((double) nodePos.getX() + 0.375, (double) nodePos.getY() + 0.375, (double) nodePos.getZ() + 0.375);

//        final Entity entity = Minecraft.getInstance().getRenderViewEntity();
//        final double dx = blockPos.getX() - entity.getPosX();
//        final double dy = blockPos.getY() - entity.getPosY();
//        final double dz = blockPos.getZ() - entity.getPosZ();

//        if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 5D) {
//            renderDebugText(blockPos, matrixStack);
//        }

        matrixStack.scale(0.25F, 0.25F, 0.25F);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuffer();

        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        RenderSystem.color3f(r, g, b);

        //  X+
        vertexBuffer.pos(matrix4f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 0.0f, 1.0f).endVertex();

        //  X-
        vertexBuffer.pos(matrix4f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 0.0f, 0.0f).endVertex();

        //  Z-
        vertexBuffer.pos(matrix4f, 0.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 0.0f, 0.0f).endVertex();

        //  Z+
        vertexBuffer.pos(matrix4f, 1.0f, 0.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 0.0f, 1.0f).endVertex();

        //  Y+
        vertexBuffer.pos(matrix4f, 1.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 1.0f, 1.0f).endVertex();

        //  Y-
        vertexBuffer.pos(matrix4f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexBuffer.pos(matrix4f, 0.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.pos(matrix4f, 1.0f, 0.0f, 1.0f).endVertex();

        tessellator.draw();

        if (lineStartPos != null) {
            final float pdx = (float) (lineStartPos.getX() - nodePos.getX() + 0.125f);
            final float pdy = (float) (lineStartPos.getY() - nodePos.getY() + 0.125f);
            final float pdz = (float) (lineStartPos.getZ() - nodePos.getZ() + 0.125f);
            vertexBuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            vertexBuffer.pos(matrix4f, 0.5f, 0.5f, 0.5f).color(0.75F, 0.75F, 0.75F, 1.0F).endVertex();
            vertexBuffer.pos(matrix4f, pdx / 0.25f, pdy / 0.25f, pdz / 0.25f).color(0.75F, 0.75F, 0.75F, 1.0F).endVertex();
            tessellator.draw();
        }

        matrixStack.pop();

        RenderSystem.disableDepthTest();
        RenderSystem.popAttributes();
        matrixStack.pop();
    }

//    private static void renderDebugText(final Node n, final MatrixStack matrixStack) {
//        final String s1 = String.format("F: %.3f [%d]", n.getCost(), n.getCounterAdded());
//        final String s2 = String.format("G: %.3f [%d]", n.getScore(), n.getCounterVisited());
//        final FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
//
//        matrixStack.push();
//        matrixStack.translate(0.0F, 0.75F, 0.0F);
//        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
//
//        final EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
//        matrixStack.rotate(renderManager.getCameraOrientation());
//        matrixStack.scale(-0.014F, -0.014F, 0.014F);
//        matrixStack.translate(0.0F, 18F, 0.0F);
//
//        RenderSystem.depthMask(false);
//
//        RenderSystem.enableBlend();
//        RenderSystem.blendFuncSeparate(
//                GlStateManager.SourceFactor.SRC_ALPHA,
//                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
//                GlStateManager.SourceFactor.ONE,
//                GlStateManager.DestFactor.ZERO);
//        RenderSystem.disableTexture();
//
//        final int i = Math.max(fontrenderer.getStringWidth(s1), fontrenderer.getStringWidth(s2)) / 2;
//
//        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();
//        final Tessellator tessellator = Tessellator.getInstance();
//        final BufferBuilder vertexBuffer = tessellator.getBuffer();
//        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
//        vertexBuffer.pos(matrix4f, (-i - 1), -5.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
//        vertexBuffer.pos(matrix4f, (-i - 1), 12.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
//        vertexBuffer.pos(matrix4f, (i + 1), 12.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
//        vertexBuffer.pos(matrix4f, (i + 1), -5.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
//        tessellator.draw();
//
//        RenderSystem.enableTexture();
//
//        final IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
//        matrixStack.translate(0.0F, -5F, 0.0F);
//        fontrenderer.renderString(s1, -fontrenderer.getStringWidth(s1) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
//        matrixStack.translate(0.0F, 8F, 0.0F);
//        fontrenderer.renderString(s2, -fontrenderer.getStringWidth(s2) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
//
//        RenderSystem.depthMask(true);
//        matrixStack.translate(0.0F, -8F, 0.0F);
//        fontrenderer.renderString(s1, -fontrenderer.getStringWidth(s1) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
//        matrixStack.translate(0.0F, 8F, 0.0F);
//        fontrenderer.renderString(s2, -fontrenderer.getStringWidth(s2) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
//        buffer.finish();
//
//        matrixStack.pop();
//    }


}
