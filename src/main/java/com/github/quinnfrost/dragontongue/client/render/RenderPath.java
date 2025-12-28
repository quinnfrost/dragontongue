package com.github.quinnfrost.dragontongue.client.render;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Node;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Pathfinding;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.Entity;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ConcurrentModificationException;

public class RenderPath {
    /**
     * Render debugging information for the pathfinding system.
     *
     * @param frame       entity movement weight.
     * @param matrixStack the matrix stack to apply to.
     */
    public static void debugDraw(final double frame, final PoseStack matrixStack) {
        if (Pathfinding.lastDebugNodesNotVisited.isEmpty() || Pathfinding.lastDebugNodesPath.isEmpty() || Pathfinding.lastDebugNodesVisited.isEmpty()) {
            return;
        }

        final Vec3 vec = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        final double dx = vec.x();
        final double dy = vec.y();
        final double dz = vec.z();

        RenderSystem.pushTextureAttributes();

        matrixStack.pushPose();
        matrixStack.translate(-dx, -dy, -dz);

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        RenderSystem.disableLighting();


        try {
            for (final Node n : Pathfinding.lastDebugNodesNotVisited) {
                debugDrawNode(n, 1.0F, 0F, 0F, matrixStack);
            }

            for (final Node n : Pathfinding.lastDebugNodesVisited) {
                debugDrawNode(n, 0F, 0F, 1.0F, matrixStack);
            }

            for (final Node n : Pathfinding.lastDebugNodesPath) {
                if (n.isReachedByWorker()) {
                    debugDrawNode(n, 1F, 0.4F, 0F, matrixStack);
                } else {
                    debugDrawNode(n, 0F, 1.0F, 0F, matrixStack);
                }
            }
        } catch (final ConcurrentModificationException exc) {
            IceAndFire.LOGGER.catching(exc);
        }

        RenderSystem.disableDepthTest();
        RenderSystem.popAttributes();
        matrixStack.popPose();
    }

    private static void debugDrawNode(final Node n, final float r, final float g, final float b, final PoseStack matrixStack) {
        matrixStack.pushPose();
        matrixStack.translate((double) n.pos.getX() + 0.375, (double) n.pos.getY() + 0.375, (double) n.pos.getZ() + 0.375);

        final Entity entity = Minecraft.getInstance().getCameraEntity();
        final double dx = n.pos.getX() - entity.getX();
        final double dy = n.pos.getY() - entity.getY();
        final double dz = n.pos.getZ() - entity.getZ();
        if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 5D) {
            renderDebugText(n, matrixStack);
        }

        matrixStack.scale(0.25F, 0.25F, 0.25F);

        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();

        final Matrix4f matrix4f = matrixStack.last().pose();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
        RenderSystem.color3f(r, g, b);

        //  X+
        vertexBuffer.vertex(matrix4f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 0.0f, 1.0f).endVertex();

        //  X-
        vertexBuffer.vertex(matrix4f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).endVertex();

        //  Z-
        vertexBuffer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 0.0f, 0.0f).endVertex();

        //  Z+
        vertexBuffer.vertex(matrix4f, 1.0f, 0.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 0.0f, 1.0f).endVertex();

        //  Y+
        vertexBuffer.vertex(matrix4f, 1.0f, 1.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 1.0f, 1.0f).endVertex();

        //  Y-
        vertexBuffer.vertex(matrix4f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexBuffer.vertex(matrix4f, 1.0f, 0.0f, 1.0f).endVertex();

        tessellator.end();

        if (n.parent != null) {
            final float pdx = n.parent.pos.getX() - n.pos.getX() + 0.125f;
            final float pdy = n.parent.pos.getY() - n.pos.getY() + 0.125f;
            final float pdz = n.parent.pos.getZ() - n.pos.getZ() + 0.125f;
            vertexBuffer.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
            vertexBuffer.vertex(matrix4f, 0.5f, 0.5f, 0.5f).color(0.75F, 0.75F, 0.75F, 1.0F).endVertex();
            vertexBuffer.vertex(matrix4f, pdx / 0.25f, pdy / 0.25f, pdz / 0.25f).color(0.75F, 0.75F, 0.75F, 1.0F).endVertex();
            tessellator.end();
        }

        matrixStack.popPose();
    }

    private static void renderDebugText(final Node n, final PoseStack matrixStack) {
        final String s1 = String.format("F: %.3f [%d]", n.getCost(), n.getCounterAdded());
        final String s2 = String.format("G: %.3f [%d]", n.getScore(), n.getCounterVisited());
        final Font fontrenderer = Minecraft.getInstance().font;

        matrixStack.pushPose();
        matrixStack.translate(0.0F, 0.75F, 0.0F);
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);

        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        matrixStack.mulPose(renderManager.cameraOrientation());
        matrixStack.scale(-0.014F, -0.014F, 0.014F);
        matrixStack.translate(0.0F, 18F, 0.0F);

        RenderSystem.depthMask(false);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        RenderSystem.disableTexture();

        final int i = Math.max(fontrenderer.width(s1), fontrenderer.width(s2)) / 2;

        final Matrix4f matrix4f = matrixStack.last().pose();
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertexBuffer.vertex(matrix4f, (-i - 1), -5.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
        vertexBuffer.vertex(matrix4f, (-i - 1), 12.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
        vertexBuffer.vertex(matrix4f, (i + 1), 12.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
        vertexBuffer.vertex(matrix4f, (i + 1), -5.0f, 0.0f).color(0.0F, 0.0F, 0.0F, 0.7F).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();

        final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        matrixStack.translate(0.0F, -5F, 0.0F);
        fontrenderer.drawInBatch(s1, -fontrenderer.width(s1) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
        matrixStack.translate(0.0F, 8F, 0.0F);
        fontrenderer.drawInBatch(s2, -fontrenderer.width(s2) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);

        RenderSystem.depthMask(true);
        matrixStack.translate(0.0F, -8F, 0.0F);
        fontrenderer.drawInBatch(s1, -fontrenderer.width(s1) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
        matrixStack.translate(0.0F, 8F, 0.0F);
        fontrenderer.drawInBatch(s2, -fontrenderer.width(s2) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, false, 0, 15728880);
        buffer.endBatch();

        matrixStack.popPose();
    }

}
