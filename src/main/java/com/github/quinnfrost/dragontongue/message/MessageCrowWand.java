package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageCrowWand {
    private UUID playerUUID;
    private EnumCrowWand action;

    public MessageCrowWand() {
        this.action = EnumCrowWand.PASS;
    }
    public MessageCrowWand(EnumCrowWand action) {
        this.action = action;
    }
    // Decode bytes from network
    public MessageCrowWand(PacketBuffer buffer) {
        this.action = EnumCrowWand.valueOf(buffer.readString());
    }
    // Encode to bytes to send over network
    public void toBytes(PacketBuffer buffer) {
        buffer.writeString(action.name());
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            teleportPlayer(this.action, contextSupplier.get().getSender(),
                    contextSupplier.get().getSender().getServerWorld());
        });
        return true;
    }

    public static void teleportPlayer(EnumCrowWand action, ServerPlayerEntity player, ServerWorld serverWorld) {
        try {
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(player,
                    Config.CROW_WAND_RANGE_MAX.get().floatValue(), 1.0f);

            if (blockRayTraceResult.getType() != RayTraceResult.Type.MISS) {
                double targetX = blockRayTraceResult.getHitVec().getX();
                double targetY = blockRayTraceResult.getHitVec().getY();
                double targetZ = blockRayTraceResult.getHitVec().getZ();
                switch (action) {
                    case PASS:
                        break;
                    case TELEPORT:
                        player.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                            iCapabilityInfoHolder.setFallbackPosition(player.getPosition());

                            serverWorld.spawnParticle(ParticleTypes.PORTAL, targetX, targetY, targetZ, 800, 2, 1, 2,
                                    0.1);
                            player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 20, 0, true, false));

                            switch (blockRayTraceResult.getFace()) {
                                case DOWN:
                                    break;
                                case UP:
                                    break;
                                case NORTH:
                                    break;
                                case SOUTH:
                                    break;
                                case WEST:
                                    break;
                                case EAST:
                                    break;
                            }
                            player.teleportKeepLoaded(targetX, targetY, targetZ);
                            iCapabilityInfoHolder.setFallbackTimer(80);
                        });

                        break;
                    case FALLBACK:
                        player.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                            if (iCapabilityInfoHolder.getFallbackTimer() != 0) {
                                player.teleportKeepLoaded(iCapabilityInfoHolder.getFallbackPosition().getX(),
                                        iCapabilityInfoHolder.getFallbackPosition().getY(), iCapabilityInfoHolder.getFallbackPosition().getZ());
                                iCapabilityInfoHolder.setFallbackTimer(0);
                            }
                        });
                        break;
                    case LIGHTNING:
                        LightningBoltEntity lightningBolt = new LightningBoltEntity(EntityType.LIGHTNING_BOLT,
                                player.world);
                        lightningBolt.setCaster(player);
                        lightningBolt.setPosition(targetX, targetY, targetZ);
                        player.world.addEntity(lightningBolt);
                        break;
                    case FANG:
                        EvokerFangsEntity evokerFangs = new EvokerFangsEntity(EntityType.EVOKER_FANGS, player.world);
                        evokerFangs.setCaster(player);
                        evokerFangs.setPosition(targetX, targetY, targetZ);
                        player.world.addEntity(evokerFangs);
                        break;

                }

            }
        } catch (Exception e) {
            DragonTongue.LOGGER.warn("Could not handle crow wand package");
        }

    }
}
