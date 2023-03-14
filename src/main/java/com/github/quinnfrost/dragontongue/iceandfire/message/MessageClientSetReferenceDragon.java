package com.github.quinnfrost.dragontongue.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClientSetReferenceDragon {
    private int entityID;

    public MessageClientSetReferenceDragon(int entityID) {
        this.entityID = entityID;
    }

    public static MessageClientSetReferenceDragon decoder(FriendlyByteBuf buffer) {
        return new MessageClientSetReferenceDragon(buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityID);
                if (entity instanceof EntityDragonBase) {
                    ScreenDragon.referencedDragon = (EntityDragonBase) entity;
                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER){
            }
        });
        return true;
    }

}
