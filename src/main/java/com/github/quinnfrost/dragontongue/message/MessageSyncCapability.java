package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageSyncCapability {
    private int entityID;
    private ICapabilityInfoHolder cap = new CapabilityInfoHolderImpl();

    public MessageSyncCapability(Entity entity) {
        this.entityID = entity.getId();
        this.cap = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
    }

    public MessageSyncCapability(FriendlyByteBuf buffer) {
        this.entityID = buffer.readInt();
        int listSize = buffer.readInt();
        ListTag listNBT = new ListTag();
        for (int i = 0; i < listSize; i++) {
            listNBT.add(buffer.readNbt());
        }
        CapabilityInfoHolder.TARGET_HOLDER.readNBT(cap, null, listNBT);
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);

        ListTag listNBT = (ListTag) CapabilityInfoHolder.TARGET_HOLDER.writeNBT(cap, null);
        buffer.writeInt(listNBT.size());
        CompoundTag compoundNBT;
        for (int i = 0; i < listNBT.size(); i++) {
            compoundNBT = listNBT.getCompound(i);
            buffer.writeNbt(compoundNBT);
        }

    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Entity entity = Minecraft.getInstance().player.level.getEntity(entityID);
//                DragonTongue.LOGGER.debug("Getting cap sync for " + entity);
                if (entity != null) {
                    entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                        iCapTargetHolder.copy(cap);
                    });
                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER){
                Entity entity = contextSupplier.get().getSender().level.getEntity(entityID);
//                DragonTongue.LOGGER.debug("Getting cap sync for " + entity);
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
        if (entity instanceof Player) {
            RegistryMessages.sendToClient(new MessageSyncCapability(entity), (ServerPlayer) entity);
        }
    }

}
