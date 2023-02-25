package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.iceandfire.message.MessageSyncPath;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageClientSetReferenceDragon;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageSyncPathReached;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class RegistryMessages {
    public static SimpleChannel CHANNEL;
    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(References.MOD_ID, "dragontongue"),
                () -> "1.0",
                s -> true,
                s -> true
                );

        // Register all message types
        CHANNEL.messageBuilder(MessageCrowWand.class, nextID())
                .encoder(MessageCrowWand::toBytes)
                .decoder(MessageCrowWand::new)
                .consumer(MessageCrowWand::handle)
                .add();

        CHANNEL.messageBuilder(MessageCommandEntity.class, nextID())
                .encoder(MessageCommandEntity::toBytes)
                .decoder(MessageCommandEntity::new)
                .consumer(MessageCommandEntity::handle)
                .add();

        CHANNEL.messageBuilder(MessageClientDisplay.class, nextID())
                .encoder(MessageClientDisplay::encoder)
                .decoder(MessageClientDisplay::new)
                .consumer(MessageClientDisplay::handler)
                .add();

        CHANNEL.messageBuilder(MessageClientCommandDistance.class, nextID())
                .encoder(MessageClientCommandDistance::encoder)
                .decoder(MessageClientCommandDistance::new)
                .consumer(MessageClientCommandDistance::handler)
                .add();

        CHANNEL.messageBuilder(MessageCommandSettings.class, nextID())
                .encoder(MessageCommandSettings::encoder)
                .decoder(MessageCommandSettings::new)
                .consumer(MessageCommandSettings::handler)
                .add();

        CHANNEL.messageBuilder(MessageSyncCapability.class, nextID())
                .encoder(MessageSyncCapability::encoder)
                .decoder(MessageSyncCapability::new)
                .consumer(MessageSyncCapability::handler)
                .add();

        CHANNEL.messageBuilder(MessageClientDraw.class, nextID())
                .encoder(MessageClientDraw::encoder)
                .decoder(MessageClientDraw::decoder)
                .consumer(MessageClientDraw::handler)
                .add();

        if (DragonTongue.isIafPresent) {
            CHANNEL.messageBuilder(MessageClientSetReferenceDragon.class, nextID())
                    .encoder(MessageClientSetReferenceDragon::encoder)
                    .decoder(MessageClientSetReferenceDragon::decoder)
                    .consumer(MessageClientSetReferenceDragon::handler)
                    .add();

            CHANNEL.messageBuilder(MessageSyncPath.class, nextID())
                    .encoder(MessageSyncPath::write)
                    .decoder(MessageSyncPath::read)
                    .consumer(MessageSyncPath.Handler::handle)
                    .add();
            CHANNEL.messageBuilder(MessageSyncPathReached.class, nextID())
                    .encoder(MessageSyncPathReached::write)
                    .decoder(MessageSyncPathReached::read)
                    .consumer(MessageSyncPathReached.Handler::handle)
                    .add();
        }
    }

    /**
     * Send pre-defined message packets
     * @param packet
     * @param player
     */
    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        CHANNEL.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object packet) {
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            CHANNEL.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

}
