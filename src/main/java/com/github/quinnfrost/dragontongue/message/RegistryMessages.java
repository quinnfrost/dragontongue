package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.References;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
    }

    /**
     * Send pre-defined message packets
     * @param packet
     * @param player
     */
    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        CHANNEL.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

}
