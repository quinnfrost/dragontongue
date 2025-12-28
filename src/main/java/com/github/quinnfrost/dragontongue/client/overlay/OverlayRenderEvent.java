package com.github.quinnfrost.dragontongue.client.overlay;

import com.github.quinnfrost.dragontongue.client.preview.RenderTrajectory;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class OverlayRenderEvent extends Gui {
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

    public void drawTexturedModalRect(PoseStack ms, int x, int y, int textureX, int textureY, int width, int height) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        Matrix4f matrix4f = ms.last().pose();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, x + 0, y + height, 0).uv((float) (textureX + 0) * 0.00390625F, (float) (textureY + height) * 0.00390625F).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y + height, 0).uv((float) (textureX + width) * 0.00390625F, (float) (textureY + height) * 0.00390625F).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y + 0, 0).uv((float) (textureX + width) * 0.00390625F, (float) (textureY + 0) * 0.00390625F).endVertex();
        bufferbuilder.vertex(matrix4f, x + 0, y + 0, 0).uv((float) (textureX + 0) * 0.00390625F, (float) (textureY + 0) * 0.00390625F).endVertex();
        tessellator.end();
    }
}

