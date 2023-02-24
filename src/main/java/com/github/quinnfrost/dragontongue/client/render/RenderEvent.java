package com.github.quinnfrost.dragontongue.client.render;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class RenderEvent {
    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        RenderNode.render(event.getMatrixStack());
        if (DragonTongue.isIafPresent) {
            IafHelperClass.renderWorldLastEvent(event);
        }
    }

}
