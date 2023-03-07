package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayInfoPanel;
import com.github.quinnfrost.dragontongue.client.render.RenderNode;
import com.github.quinnfrost.dragontongue.entity.ai.EntityBehaviorDebugger;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageSyncPathReached;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Pathfinding;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class MessageDebugEntity {
    private final boolean isActive;
    private int entityId;
    private List<Vector3d> associatedTarget;
    private List<String> serverEntityInfo;

    public MessageDebugEntity(int entityIdIn, List<Vector3d> associatedTargetIn, List<String> serverEntityInfoIn) {
        this.isActive = true;
        this.entityId = entityIdIn;
        this.associatedTarget = associatedTargetIn;
        this.serverEntityInfo = serverEntityInfoIn;
    }

    public MessageDebugEntity() {
        this.isActive = false;
    }

    public MessageDebugEntity(int entityIdIn) {
        this(entityIdIn, new ArrayList<>(), new ArrayList<>());
    }

    public void encoder(PacketBuffer buffer) {
        buffer.writeBoolean(isActive);
        if (isActive) {
            buffer.writeInt(entityId);

            buffer.writeInt(associatedTarget.size());
            for (Vector3d target :
                    associatedTarget) {
                buffer.writeDouble(target.x);
                buffer.writeDouble(target.y);
                buffer.writeDouble(target.z);
            }

            buffer.writeInt(serverEntityInfo.size());
            for (String infoItem :
                    serverEntityInfo) {
                buffer.writeString(infoItem);
            }
        }
    }

    public static MessageDebugEntity decoder(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            int entityId = buffer.readInt();
            int length = buffer.readInt();
            List<Vector3d> associatedTarget = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                associatedTarget.add(new Vector3d(
                        buffer.readDouble(),
                        buffer.readDouble(),
                        buffer.readDouble()
                ));
            }

            length = buffer.readInt();
            List<String> serverEntityInfo = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                serverEntityInfo.add(buffer.readString());
            }

            return new MessageDebugEntity(entityId, associatedTarget, serverEntityInfo);
        } else {
            return new MessageDebugEntity();
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                if (isActive) {
                    MobEntity targetEntity = (MobEntity) Minecraft.getInstance().world.getEntityByID(entityId);

                    for (Vector3d target:
                         associatedTarget) {
                        RenderNode.drawCube(2, target, false, null);
                        RenderNode.drawLine(2, targetEntity.getPositionVec(), target, null);
                    }

                    OverlayInfoPanel.bufferInfoLeft = serverEntityInfo;
                    OverlayInfoPanel.bufferInfoRight = EntityBehaviorDebugger.getTargetInfoString(targetEntity);
                } else {
                    OverlayInfoPanel.bufferInfoLeft = new ArrayList<>();
                    OverlayInfoPanel.bufferInfoRight = new ArrayList<>();
                }

            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                if (EntityBehaviorDebugger.targetEntity == null) {
                    EntityBehaviorDebugger.startDebugFor(contextSupplier.get().getSender(), (MobEntity) contextSupplier.get().getSender().getServerWorld().getEntityByID(entityId));
                } else {
                    EntityBehaviorDebugger.stopDebug();
                }
            }
        });
        return true;
    }


}
