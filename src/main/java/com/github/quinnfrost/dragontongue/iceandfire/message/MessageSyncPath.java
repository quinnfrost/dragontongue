package com.github.quinnfrost.dragontongue.iceandfire.message;

import com.github.quinnfrost.dragontongue.client.render.RenderNode;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Node;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Pathfinding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Message to sync some path over to the client.
 */
public class MessageSyncPath {
    /**
     * Set of visited nodes.
     */
    public Set<Node> lastDebugNodesVisited = new HashSet<>();

    /**
     * Set of not visited nodes.
     */
    public Set<Node> lastDebugNodesNotVisited = new HashSet<>();

    /**
     * Set of chosen nodes for the path.
     */
    public Set<Node> lastDebugNodesPath = new HashSet<>();

    /**
     * Default constructor.
     */
    public MessageSyncPath() {
        super();
    }

    /**
     * Create a new path message with the filled pathpoints.
     */
    public MessageSyncPath(final Set<Node> lastDebugNodesVisited, final Set<Node> lastDebugNodesNotVisited, final Set<Node> lastDebugNodesPath) {
        super();
        this.lastDebugNodesVisited = lastDebugNodesVisited;
        this.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        this.lastDebugNodesPath = lastDebugNodesPath;
    }

    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(lastDebugNodesVisited.size());
        for (final Node node : lastDebugNodesVisited) {
            node.serializeToBuf(buf);
        }

        buf.writeInt(lastDebugNodesNotVisited.size());
        for (final Node node : lastDebugNodesNotVisited) {
            node.serializeToBuf(buf);
        }

        buf.writeInt(lastDebugNodesPath.size());
        for (final Node node : lastDebugNodesPath) {
            node.serializeToBuf(buf);
        }
    }

    public static MessageSyncPath read(final FriendlyByteBuf buf) {
        int size = buf.readInt();

        Set<Node> lastDebugNodesVisited = new HashSet<>();
        for (int i = 0; i < size; i++) {
            lastDebugNodesVisited.add(new Node(buf));
        }

        size = buf.readInt();
        Set<Node> lastDebugNodesNotVisited = new HashSet<>();
        for (int i = 0; i < size; i++) {
            lastDebugNodesNotVisited.add(new Node(buf));
        }

        size = buf.readInt();
        Set<Node> lastDebugNodesPath = new HashSet<>();
        for (int i = 0; i < size; i++) {
            lastDebugNodesPath.add(new Node(buf));
        }
        return new MessageSyncPath(lastDebugNodesVisited, lastDebugNodesNotVisited, lastDebugNodesPath);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Pathfinding.lastDebugNodesVisited = lastDebugNodesVisited;
                Pathfinding.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
                Pathfinding.lastDebugNodesPath = lastDebugNodesPath;
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {

            }
        });
        return true;
    }

    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

}
