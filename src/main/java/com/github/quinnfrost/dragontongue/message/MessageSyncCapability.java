package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageSyncCapability {
    private int entityID;
    private ICapabilityInfoHolder cap = new CapabilityInfoHolderImpl();

    public MessageSyncCapability(Entity entity) {
        this.entityID = entity.getEntityId();
        this.cap = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
    }

    public MessageSyncCapability(PacketBuffer buffer) {
        this.entityID = buffer.readInt();
        int listSize = buffer.readInt();
        ListNBT listNBT = new ListNBT();
        for (int i = 0; i < listSize; i++) {
            listNBT.add(buffer.readCompoundTag());
        }
        CapabilityInfoHolder.TARGET_HOLDER.readNBT(cap, null, listNBT);
    }

    public void encoder(PacketBuffer buffer) {
        buffer.writeInt(entityID);

        ListNBT listNBT = (ListNBT) CapabilityInfoHolder.TARGET_HOLDER.writeNBT(cap, null);
        buffer.writeInt(listNBT.size());
        CompoundNBT compoundNBT;
        for (int i = 0; i < listNBT.size(); i++) {
            compoundNBT = listNBT.getCompound(i);
            buffer.writeCompoundTag(compoundNBT);
        }

    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Entity entity = Minecraft.getInstance().player.world.getEntityByID(entityID);
                if (entity != null) {
                    entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.copy(cap);
                    });
                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER){
                Entity entity = contextSupplier.get().getSender().world.getEntityByID(entityID);
                if (entity != null) {
                    entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.copy(cap);
                    });
                }
                syncCapabilityToClients(entity);
            }
        });
        return true;
    }

    public static void syncCapabilityToClients(Entity entity) {
        if (entity != null) {
            RegistryMessages.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                    new MessageSyncCapability(entity));
        }
    }

}
