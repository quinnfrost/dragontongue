package com.github.quinnfrost.dragontongue.message;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageClientSetReferenceDragon {
    private int entityID;

    public MessageClientSetReferenceDragon(int entityID) {
        this.entityID = entityID;
    }

    public static MessageClientSetReferenceDragon decoder(PacketBuffer buffer) {
        return new MessageClientSetReferenceDragon(buffer.readInt());
    }

    public void encoder(PacketBuffer buffer) {
        buffer.writeInt(entityID);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Entity entity = Minecraft.getInstance().world.getEntityByID(entityID);
                if (entity instanceof EntityDragonBase) {
                    ScreenDragon.referencedDragon = (EntityDragonBase) entity;
                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER){
            }
        });
        return true;
    }

}
