package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageClientDisplay {

    private EnumClientDisplay messageType = EnumClientDisplay.PASS;
    private int displayTime = 3;
    private int size = 3;
    private List<String> message = new ArrayList<>();

    public MessageClientDisplay(EnumClientDisplay messageType,int displayTime,List<String> message) {
        this.messageType = messageType;
        this.displayTime = displayTime;

        this.size = message.size();
        this.message = message;
    }
    public MessageClientDisplay(PacketBuffer buffer) {
        this.messageType = buffer.readEnumValue(EnumClientDisplay.class);
        this.displayTime = buffer.readInt();

        this.size = buffer.readInt();
        for (int i = 0; i < this.size; i++) {
            this.message.add(buffer.readString());
        }
    }
    public void encoder(PacketBuffer buffer) {
        buffer.writeEnumValue(messageType);
        buffer.writeInt(displayTime);

        buffer.writeInt(size);
        for (int i = 0; i < size; i++) {
            buffer.writeString(message.get(i));
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            switch (messageType) {
                case PASS:
                    break;
                case DAMAGE:
                    OverlayCrossHair.setCrossHairString(message.get(0), displayTime,1);
                    OverlayCrossHair.critical = false;
                    break;
                case CRITICAL:
                    OverlayCrossHair.setCrossHairString(message.get(0),displayTime,1);
                    OverlayCrossHair.critical = true;
                    break;
                case ENTITY_DEBUG:
                    OverlayCrossHair.buffer = message;
                    break;
            }

        });
        return true;
    }
}
