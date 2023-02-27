package com.github.quinnfrost.dragontongue.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class PingRenderType extends RenderState {
    protected static final RenderState.LayerState DISABLE_DEPTH = new RenderState.LayerState("disable_depth", GlStateManager::disableDepthTest, GlStateManager::enableDepthTest);
    public PingRenderType(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getPingOverlay() {
        RenderType.State renderTypeState = RenderType.State.getBuilder().transparency(TRANSLUCENT_TRANSPARENCY).texture(BLOCK_SHEET).layer(DISABLE_DEPTH).build(true);
        return RenderType.makeType("ping_overlay", DefaultVertexFormats.POSITION_TEX_COLOR, 7, 262144, true, true, renderTypeState);
    }
}
