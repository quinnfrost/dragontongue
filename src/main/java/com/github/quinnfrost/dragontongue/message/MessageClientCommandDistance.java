package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClientCommandDistance {
    private double distance = 128d;
    private DistanceType type;
    public enum DistanceType {
        COMMAND,
        SELECT
    }

    public MessageClientCommandDistance(DistanceType type, double distance) {
        this.type = type;
        this.distance = distance;
    }

    public MessageClientCommandDistance(PacketBuffer buffer) {
        this.type = buffer.readEnumValue(DistanceType.class);
        this.distance = buffer.readDouble();
    }

    public void encoder(PacketBuffer buffer) {
        buffer.writeEnumValue(type);
        buffer.writeDouble(distance);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            switch (type) {

                case COMMAND:
                    if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                        ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) util.getClientSidePlayer();
                        clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setCommandDistance(distance);
                        });
                    } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                        ServerPlayerEntity serverPlayerEntity = contextSupplier.get().getSender();
                        serverPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setCommandDistance(distance);
                        });
                    }
                    break;
                case SELECT:
                    if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                        ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) util.getClientSidePlayer();
                        clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setSelectDistance(distance);
                        });
                    } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                        ServerPlayerEntity serverPlayerEntity = contextSupplier.get().getSender();
                        serverPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setSelectDistance(distance);
                        });
                    }
                    break;
            }



        });
        return true;
    }


}
