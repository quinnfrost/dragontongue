package com.github.quinnfrost.dragontongue.proxy;

import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.github.quinnfrost.dragontongue.event.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
    public void init(){
        super.init();
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    public static void clientInit() {
        KeyBindRegistry.registerKeyBind();
    }

    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

}
