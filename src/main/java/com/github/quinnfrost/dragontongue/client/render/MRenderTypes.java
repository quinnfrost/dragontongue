package com.github.quinnfrost.dragontongue.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

/**
 * Holding all kind of render types of minecolonies
 */
public final class MRenderTypes extends RenderType {
    public static final VertexFormat format = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).put("UV2", ELEMENT_UV2).build());

    public MRenderTypes(final String nameIn,
                        final VertexFormat formatIn,
                        final VertexFormat.Mode drawModeIn,
                        final int bufferSizeIn,
                        final boolean useDelegateIn,
                        final boolean needsSortingIn,
                        final Runnable setupTaskIn,
                        final Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        throw new IllegalStateException();
    }

    /**
     * Custom texture renderer type.
     *
     * @param resourceLocation the location fo the texture.
     * @return the renderType which is created.
     */
    public static RenderType customTexRenderer(final ResourceLocation resourceLocation) {
        final CompositeState state = CompositeState.builder()
            .setTextureState(new TextureStateShard(resourceLocation, false, false))//Texture state
            .setShaderState(ShaderStateShard.POSITION_TEX_SHADER)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .createCompositeState(true);


        return create("custommctexrenderer", format, VertexFormat.Mode.QUADS, 256, true, false, state);
    }

    /**
     * Custom line renderer type.
     *
     * @return the renderType which is created.
     */
    public static RenderType customLineRenderer() {
        return create("minecolonieslines", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256, false, false,
            CompositeState.builder()
                .setShaderState(ShaderStateShard.RENDERTYPE_LINES_SHADER)
                .setLineState(new LineStateShard(OptionalDouble.empty()))
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setCullState(NO_CULL).createCompositeState(false));
    }

    /**
     * Custom line renderer type.
     *
     * @return the renderType which is created.
     */
    public static RenderType customPathRenderer() {
        return create("minecoloniespath", POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            CompositeState.builder()
                .setShaderState(ShaderStateShard.POSITION_COLOR_SHADER)
                .setLineState(new LineStateShard(OptionalDouble.empty()))
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .createCompositeState(false));
    }

    /**
     * Custom line renderer type.
     *
     * @return the renderType which is created.
     */
    public static RenderType customPathTextRenderer() {
        return create("minecoloniespathtext", POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 256, false, false,
            CompositeState.builder()
                .setShaderState(ShaderStateShard.POSITION_COLOR_SHADER)
                .createCompositeState(false));
    }
}
