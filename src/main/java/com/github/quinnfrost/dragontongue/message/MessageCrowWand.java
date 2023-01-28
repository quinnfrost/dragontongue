package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
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
import net.minecraft.util.math.vector.Vector3d;
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

            crowWandAction(this.action, contextSupplier.get().getSender(),
                    contextSupplier.get().getSender().getServerWorld());
        });
        return true;
    }

    public static void crowWandAction(EnumCrowWand action, ServerPlayerEntity player, ServerWorld serverWorld) {
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
                        player.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
                            iCapTargetHolder.setFallbackPosition(player.getPosition());
                            player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 10, 0, true, false));
                            player.teleportKeepLoaded(targetX, targetY, targetZ);
                            util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, targetX, targetY, targetZ, 800, 2, 1, 2,
                                    0.1);
                            iCapTargetHolder.setFallbackTimer(200);
                        });

                        break;
                    case FALLBACK:
                        player.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            if (iCapTargetHolder.getFallbackTimer() != 0) {
                                Vector3d playerPos = player.getPositionVec();
                                player.teleportKeepLoaded(iCapTargetHolder.getFallbackPosition().getX(),
                                        iCapTargetHolder.getFallbackPosition().getY(), iCapTargetHolder.getFallbackPosition().getZ());
                                player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 10, 0, true, false));
                                util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, iCapTargetHolder.getFallbackPosition().getX(),
                                        iCapTargetHolder.getFallbackPosition().getY(), iCapTargetHolder.getFallbackPosition().getZ(), 800, 2, 1, 2, 0.1);
                                util.spawnParticleForce(serverWorld, ParticleTypes.WITCH, playerPos.getX(), playerPos.getY(), playerPos.getZ(), 800, 2, 1, 2, 0.1);

                                iCapTargetHolder.setFallbackTimer(0);
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
