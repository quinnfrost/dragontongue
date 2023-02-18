package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.item.ItemCrowWand;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.ServerPropertiesProvider;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
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
                    ItemCrowWand.CROW_WAND_MAX_DISTANCE, 1.0f, RayTraceContext.BlockMode.COLLIDER);

            if (blockRayTraceResult.getType() != RayTraceResult.Type.MISS) {
                double targetX = blockRayTraceResult.getHitVec().getX();
                double targetY = blockRayTraceResult.getHitVec().getY();
                double targetZ = blockRayTraceResult.getHitVec().getZ();
                switch (action) {
                    case PASS:
                        break;
                    case TELEPORT:
                        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setFallbackPosition(player.getPosition());
                            player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 10, 0, true, false));
                            player.teleportKeepLoaded(targetX, targetY, targetZ);
                            util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, targetX, targetY, targetZ, 800, 2, 1, 2,
                                    0.1);
                            iCapTargetHolder.setFallbackTimer(200);
                        });
//                        DragonTongue.LOGGER.debug("Target biome:" + player.world.getBiome(blockRayTraceResult.getPos()).getRegistryName().toString());
                        break;
                    case FALLBACK:
                        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
//                        LightningBoltEntity lightningBolt = new LightningBoltEntity(EntityType.LIGHTNING_BOLT,
//                                player.world);
                        LightningBoltEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(serverWorld);
                        lightningBolt.setCaster(player);
//                        lightningBolt.moveForced(targetX,targetY,targetZ);
                        lightningBolt.setPosition(targetX, targetY, targetZ);

                        List<Entity> list = lightningBolt.world.getEntitiesInAABBexcluding(lightningBolt, new AxisAlignedBB(lightningBolt.getPosX() - 3.0D, lightningBolt.getPosY() - 3.0D, lightningBolt.getPosZ() - 3.0D, lightningBolt.getPosX() + 3.0D, lightningBolt.getPosY() + 6.0D + 3.0D, lightningBolt.getPosZ() + 3.0D), Entity::isAlive);
                        for(Entity entity : list) {
                            if (entity instanceof LivingEntity) {
                                if (!MinecraftForge.EVENT_BUS.post(new LivingAttackEvent((LivingEntity) entity, DamageSource.LIGHTNING_BOLT, lightningBolt.getDamage()))) {
                                    entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(lightningBolt, player), lightningBolt.getDamage());
                                }
                            } else {
                                entity.attackEntityFrom(DamageSource.LIGHTNING_BOLT,lightningBolt.getDamage());
                            }
                        }

                        lightningBolt.setDamage(0f);
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
