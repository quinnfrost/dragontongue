package com.github.quinnfrost.dragontongue.client.render;

import com.github.alexthe666.iceandfire.client.render.pathfinding.RenderPath;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.Pathfinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class RenderEvent {
    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        RenderNode.drawAllNodes(event.getMatrixStack());
    }

}
