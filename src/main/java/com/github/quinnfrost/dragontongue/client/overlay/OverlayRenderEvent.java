package com.github.quinnfrost.dragontongue.client.overlay;

import com.github.quinnfrost.dragontongue.client.preview.RenderTrajectory;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class OverlayRenderEvent extends IngameGui {
    public OverlayRenderEvent(Minecraft minecraft) {
        super(minecraft);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            RenderTrajectory.renderTrajectory(event.getMatrixStack());
            OverlayCrossHair.renderScope(event.getMatrixStack());
            OverlayCrossHair.renderScopeSuggestion(event.getMatrixStack());
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            OverlayInfoPanel.renderPanel(event.getMatrixStack());
            OverlayCrossHair.renderStringCrossHair(event.getMatrixStack());
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            OverlayCrossHair.renderIconCrossHair(event.getMatrixStack());
        }
    }

//    public void draw(Minecraft m, boolean isAlive, int width, int height) {
//        int s = 16;
//        MatrixStack ms = new MatrixStack();
//        GL11.glEnable(3042);
//        GL11.glEnable(3008);
//        m.textureManager.bindTexture(markTexture);
//
//        GuiUtils.drawTexturedModalRect(ms, width / 2 - s / 2, height / 2 - s / 2, 0, 0, s, s,0);
//        GL11.glDisable(3042);
//    }

    public void drawTexturedModalRect(MatrixStack ms, int x, int y, int textureX, int textureY, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        Matrix4f matrix4f = ms.getLast().getMatrix();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix4f, x + 0, y + height, 0).tex((float) (textureX + 0) * 0.00390625F, (float) (textureY + height) * 0.00390625F).endVertex();
        bufferbuilder.pos(matrix4f, x + width, y + height, 0).tex((float) (textureX + width) * 0.00390625F, (float) (textureY + height) * 0.00390625F).endVertex();
        bufferbuilder.pos(matrix4f, x + width, y + 0, 0).tex((float) (textureX + width) * 0.00390625F, (float) (textureY + 0) * 0.00390625F).endVertex();
        bufferbuilder.pos(matrix4f, x + 0, y + 0, 0).tex((float) (textureX + 0) * 0.00390625F, (float) (textureY + 0) * 0.00390625F).endVertex();
        tessellator.draw();
    }
}

