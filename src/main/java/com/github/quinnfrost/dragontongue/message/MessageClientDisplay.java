package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.client.gui.GUICrossHair;
import com.github.quinnfrost.dragontongue.client.gui.GUIEvent;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageClientDisplay {
    private int size = 3;
    private EnumClientDisplay messageType = EnumClientDisplay.PASS;
    private List<String> message = new ArrayList<>(3);

    public MessageClientDisplay(EnumClientDisplay messageType, List<String> message) {
        this.size = message.size();
        this.messageType = messageType;
        this.message = message;
    }
    public MessageClientDisplay(PacketBuffer buffer) {
        this.size = buffer.readInt();
        this.messageType = buffer.readEnumValue(EnumClientDisplay.class);
        for (int i = 0; i < this.size; i++) {
            this.message.add(buffer.readString());
        }
    }
    public void encoder(PacketBuffer buffer) {
        buffer.writeInt(size);
        buffer.writeEnumValue(messageType);
        for (int i = 0; i < size; i++) {
            buffer.writeString(message.get(i));
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            switch (messageType) {
                case ENTITY_DEBUG:
                    GUICrossHair.buffer = message;
                    break;
            }

        });
        return true;
    }
}
