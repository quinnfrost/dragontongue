package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.client.render.RenderNode;
import com.github.quinnfrost.dragontongue.enums.EnumClientDraw;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClientDraw {
    private EnumClientDraw type;
    private int time = 2;
    private Vector3d pos;
    private Vector3d start;
    private Integer index;

    public MessageClientDraw(Integer index, Vector3d pos, Vector3d start) {
        this.index = index;
        this.pos = pos;
        this.start = start;
    }

    public static MessageClientDraw decoder(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return new MessageClientDraw(buffer.readInt(), new Vector3d(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ), new Vector3d(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ));
        } else {
            return new MessageClientDraw(null, new Vector3d(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ), new Vector3d(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ));
        }
    }

    public void encoder(PacketBuffer buffer) {
        if (index != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(index);
        } else {
            buffer.writeBoolean(false);
            buffer.writeInt(0);
        }

        buffer.writeDouble(pos.x);
        buffer.writeDouble(pos.y);
        buffer.writeDouble(pos.z);

        buffer.writeDouble(start.x);
        buffer.writeDouble(start.y);
        buffer.writeDouble(start.z);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                RenderNode.setRenderPos(2 * 20,pos,start, index);
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {

            }
        });
        return true;
    }

}
