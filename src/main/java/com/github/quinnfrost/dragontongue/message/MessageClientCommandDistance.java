package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.KeyBindRegistry;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClientCommandDistance {
    private double commandDistance = 128d;
    private KeyBindRegistry.EnumMouseScroll action = KeyBindRegistry.EnumMouseScroll.NONE;

    public MessageClientCommandDistance(double commandDistance) {
        this.commandDistance = commandDistance;
    }

    public MessageClientCommandDistance(PacketBuffer buffer) {
        this.commandDistance = buffer.readDouble();
    }

    public void encoder(PacketBuffer buffer) {
        buffer.writeDouble(commandDistance);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) util.getClientSidePlayer();
                clientPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                    iCapabilityInfoHolder.setCommandDistance(commandDistance);
                });
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayerEntity serverPlayerEntity = contextSupplier.get().getSender();
                serverPlayerEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                    iCapabilityInfoHolder.setCommandDistance(commandDistance);
                });
            }


        });
        return true;
    }


}
