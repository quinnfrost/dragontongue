package com.github.quinnfrost.dragontongue.mixin;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinIngameGui {

    @Inject(
            method = "renderCrosshair",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $renderCrosshair(PoseStack matrixStack, CallbackInfo ci) {
        if (OverlayCrossHair.renderScope) {
            ci.cancel();
        }

//        ci.cancel();
//        GlStateManager.disableTexture();
//        GlStateManager.depthMask(false);
//        Tessellator tessellator = RenderSystem.renderThreadTesselator();
//        BufferBuilder bufferbuilder = tessellator.getBuffer();
//        GL11.glLineWidth(4.0F);
//        bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
//
//        bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//        bufferbuilder.pos((double) 10, 0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//
//        bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//        bufferbuilder.pos(0.0D, (double) 10, 0.0D).color(0, 0, 0, 255).endVertex();
//
//        bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//        bufferbuilder.pos(0.0D, 0.0D, (double) 10).color(0, 0, 0, 255).endVertex();
//
//        tessellator.draw();
//        GL11.glLineWidth(2.0F);
//        bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
//
//
//        tessellator.draw();
//        GL11.glLineWidth(1.0F);
//        GlStateManager.depthMask(true);
//        GlStateManager.enableTexture();
    }
}
