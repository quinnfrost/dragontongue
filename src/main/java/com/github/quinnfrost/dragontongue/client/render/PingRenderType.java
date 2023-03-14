package com.github.quinnfrost.dragontongue.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

public class PingRenderType extends RenderStateShard {
    protected static final RenderStateShard.LayeringStateShard DISABLE_DEPTH = new RenderStateShard.LayeringStateShard("disable_depth", GlStateManager::_disableDepthTest, GlStateManager::_enableDepthTest);
    public PingRenderType(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getPingOverlay() {
        RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY).setTextureState(BLOCK_SHEET).setLayeringState(DISABLE_DEPTH).createCompositeState(true);
        return RenderType.create("ping_overlay", DefaultVertexFormat.POSITION_TEX_COLOR, 7, 262144, true, true, renderTypeState);
    }
}
