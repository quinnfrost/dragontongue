package com.github.quinnfrost.dragontongue.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class CustomRenderTypes extends RenderType {
    public CustomRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0));

    public static final RenderType OVERLAY_LINES = create("overlay_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            true,
            true,
            CompositeState.builder()
                    .setLineState(THICK_LINES)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .createCompositeState(false)
    );
}
