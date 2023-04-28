package com.github.quinnfrost.dragontongue.client.overlay;

import com.github.quinnfrost.dragontongue.client.preview.RenderTrajectory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
        if (event.getType() == RenderGameOverlayEvent.ElementType.LAYER) {
            RenderTrajectory.renderTrajectory(event.getMatrixStack());
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            if (!minecraft.options.renderDebug) {
                OverlayInfoPanel.renderPanel(event.getMatrixStack());
            }
            OverlayCrossHair.renderStringCrossHair(event.getMatrixStack());
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            OverlayCrossHair.renderScopeSuggestionQuad(event.getMatrixStack());
            OverlayCrossHair.renderScope(event.getMatrixStack());
//            OverlayCrossHair.renderIconCrossHair(event.getMatrixStack());
        }
    }

}

