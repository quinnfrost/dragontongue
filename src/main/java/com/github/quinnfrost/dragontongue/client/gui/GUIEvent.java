package com.github.quinnfrost.dragontongue.client.gui;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GUIEvent extends IngameGui {
    private static Minecraft mc;
    public static List<String> buffer = new ArrayList<>(3);

    public GUIEvent(Minecraft mc) {
        super(mc);
        GUIEvent.mc = mc;
    }

    @SubscribeEvent(
            priority = EventPriority.NORMAL
    )
    public void renderOverlay(RenderGameOverlayEvent.Pre e) {
        RenderGameOverlayEvent.ElementType type = e.getType();
        if (type == RenderGameOverlayEvent.ElementType.TEXT) {
            FontRenderer fontRender = mc.fontRenderer;
            MainWindow scaled = mc.getMainWindow();
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

            GL11.glPopMatrix();
        }
    }
}

