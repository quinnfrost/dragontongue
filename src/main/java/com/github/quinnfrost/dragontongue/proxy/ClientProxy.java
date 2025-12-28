package com.github.quinnfrost.dragontongue.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientProxy extends CommonProxy {
    public void init(){
        super.init();
    }

    public static void clientInit() {

    }

    public Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

}
