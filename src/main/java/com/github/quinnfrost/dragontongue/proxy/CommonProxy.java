package com.github.quinnfrost.dragontongue.proxy;

import com.github.quinnfrost.dragontongue.event.ServerEvents;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    public void init(){
        MinecraftForge.EVENT_BUS.register(ServerEvents.class);
    }

    public static void commonInit() {

    }
}
