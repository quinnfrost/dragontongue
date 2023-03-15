package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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

    public MessageClientCommandDistance(FriendlyByteBuf buffer) {
        this.type = buffer.readEnum(DistanceType.class);
        this.distance = buffer.readDouble();
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        buffer.writeDouble(distance);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            switch (type) {

                case COMMAND:
                    if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                        LocalPlayer clientPlayerEntity = (LocalPlayer) util.getClientSidePlayer();
                        clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setCommandDistance(distance);
                        });
                    } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                        ServerPlayer serverPlayerEntity = contextSupplier.get().getSender();
                        serverPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setCommandDistance(distance);
                        });
                    }
                    break;
                case SELECT:
                    if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                        LocalPlayer clientPlayerEntity = (LocalPlayer) util.getClientSidePlayer();
                        clientPlayerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setSelectDistance(distance);
                        });
                    } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                        ServerPlayer serverPlayerEntity = contextSupplier.get().getSender();
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
