package com.github.quinnfrost.dragontongue.client.gui;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUIEvent extends IngameGui {
    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
    public static boolean loadTexture = true;

    private static Minecraft minecraft;
    public static List<String> buffer = new ArrayList<>(3);
    public static String bufferCrosshair = "";

    public GUIEvent(Minecraft minecraft) {
        super(minecraft);
        GUIEvent.minecraft = minecraft;
    }

//    @SubscribeEvent
//    public void loadResource(GuiScreenEvent.InitGuiEvent event) {
//        if (loadTexture) {
//            Minecraft m = Minecraft.getInstance();
//            m.textureManager.bindTexture(markTexture);
//            loadTexture = false;
//        }
//    }

    @SubscribeEvent(
            priority = EventPriority.NORMAL
    )
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        int s = 16;
        PlayerEntity player = minecraft.player;
        int width = event.getWindow().getScaledWidth();
        int height = event.getWindow().getScaledHeight();

        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.TEXT) {
            FontRenderer fontRender = minecraft.fontRenderer;
            MainWindow scaled = minecraft.getMainWindow();
            GL11.glPushMatrix();

            MatrixStack ms = new MatrixStack();
            Color colour = new Color(255, 255, 255, 255);
            final int maxLength = 50;

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
                bufferCrosshair = String.valueOf(iCapabilityInfoHolder.getCommandDistance());
                fontRender.drawString(ms,
                        bufferCrosshair,
                        width / 2.0f - 2, height / 2.0f - 3 * s + 8, colour.getRGB());
            });
            GL11.glPopMatrix();
        }

//        draw(minecraft, true, width, height);
        if (type == RenderGameOverlayEvent.ElementType.ALL) {
            MatrixStack ms = new MatrixStack();
            Minecraft.getInstance().getTextureManager().bindTexture(markTexture);
//            AbstractGui.blit(event.getMatrixStack(), width / 2 - s / 2, height / 2 - s / 2, 0, 0,s,s,s,s);
            GuiUtils.drawTexturedModalRect(ms, width / 2 - s / 2, height / 2 - s / 2, 0, 0, s, s,0);
        }

    }

    public void draw(Minecraft m, boolean isAlive, int width, int height) {
        int s = 16;
        MatrixStack ms = new MatrixStack();
        GL11.glEnable(3042);
        GL11.glEnable(3008);
        m.textureManager.bindTexture(markTexture);

        GuiUtils.drawTexturedModalRect(ms, width / 2 - s / 2, height / 2 - s / 2, 0, 0, s, s,0);
        GL11.glDisable(3042);
    }

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

