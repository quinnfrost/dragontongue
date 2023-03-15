package com.github.quinnfrost.dragontongue.client.render;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class RenderEvent {
    @SubscribeEvent
    public static void renderWorldLastEvent(RenderLevelLastEvent event) {
        RenderNode.render(event.getPoseStack());
        if (DragonTongue.isIafPresent) {
            IafHelperClass.renderWorldLastEvent(event);
        }
    }

}
