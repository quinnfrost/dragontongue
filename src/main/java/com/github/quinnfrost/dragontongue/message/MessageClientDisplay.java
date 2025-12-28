package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageClientDisplay {

    private EnumClientDisplay messageType = EnumClientDisplay.PASS;
    private int displayTime = 20;
    private int size = 3;
    public enum EnumClientDisplay {
        PASS,
        DAMAGE,
        CRITICAL
    }

    private List<String> message = new ArrayList<>();
    public MessageClientDisplay(EnumClientDisplay messageType,int displayTime,List<String> message) {
        this.messageType = messageType;
        this.displayTime = displayTime;

        this.size = message.size();
        this.message = message;
    }

    public MessageClientDisplay(FriendlyByteBuf buffer) {
        this.messageType = buffer.readEnum(EnumClientDisplay.class);
        this.displayTime = buffer.readInt();

        this.size = buffer.readInt();
        if (this.size > 0) {
            for (int i = 0; i < this.size; i++) {
                this.message.add(buffer.readUtf());
            }
        }
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeEnum(messageType);
        buffer.writeInt(displayTime);

        buffer.writeInt(size);
        for (int i = 0; i < size; i++) {
            buffer.writeUtf(message.get(i));
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                switch (messageType) {
                    case PASS:
                        break;
                    case DAMAGE:
                        OverlayCrossHair.setCrossHairDisplay(message.get(0), 20, 20, OverlayCrossHair.IconType.HIT, true);
                        break;
                    case CRITICAL:
                        OverlayCrossHair.setCrossHairDisplay(message.get(0), 20, 20, OverlayCrossHair.IconType.CRITICAL, true);
                        break;
                }
            }

        });
        return true;
    }
}
