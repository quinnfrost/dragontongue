package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.item.ItemCrowWand;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.network.NetworkEvent;


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
    public MessageCrowWand(FriendlyByteBuf buffer) {
        this.action = EnumCrowWand.valueOf(buffer.readUtf());
    }

    // Encode to bytes to send over network
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(action.name());
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            crowWandAction(this.action, contextSupplier.get().getSender(),
                    contextSupplier.get().getSender().getLevel());
        });
        return true;
    }

    public static void crowWandAction(EnumCrowWand action, ServerPlayer player, ServerLevel serverWorld) {
        try {
            BlockHitResult blockRayTraceResult = util.getTargetBlock(player,
                    ItemCrowWand.CROW_WAND_MAX_DISTANCE, 1.0f, ClipContext.Block.COLLIDER);

            if (blockRayTraceResult.getType() != HitResult.Type.MISS) {
                double targetX = blockRayTraceResult.getLocation().x();
                double targetY = blockRayTraceResult.getLocation().y();
                double targetZ = blockRayTraceResult.getLocation().z();
                switch (action) {
                    case PASS:
                        break;
                    case TELEPORT:
                        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            iCapTargetHolder.setFallbackPosition(player.blockPosition());
                            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10, 0, true, false));
                            player.teleportToWithTicket(targetX, targetY, targetZ);
                            util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, targetX, targetY, targetZ, 800, 2, 1, 2,
                                    0.1);
                            iCapTargetHolder.setFallbackTimer(200);
                        });
//                        DragonTongue.LOGGER.debug("Target biome:" + player.world.getBiome(blockRayTraceResult.getPos()).getRegistryName().toString());
                        break;
                    case FALLBACK:
                        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                            if (iCapTargetHolder.getFallbackTimer() != 0) {
                                Vec3 playerPos = player.position();
                                player.teleportToWithTicket(iCapTargetHolder.getFallbackPosition().getX(),
                                        iCapTargetHolder.getFallbackPosition().getY(), iCapTargetHolder.getFallbackPosition().getZ());
                                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10, 0, true, false));
                                util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, iCapTargetHolder.getFallbackPosition().getX(),
                                        iCapTargetHolder.getFallbackPosition().getY(), iCapTargetHolder.getFallbackPosition().getZ(), 800, 2, 1, 2, 0.1);
                                util.spawnParticleForce(serverWorld, ParticleTypes.WITCH, playerPos.x(), playerPos.y(), playerPos.z(), 800, 2, 1, 2, 0.1);

                                iCapTargetHolder.setFallbackTimer(0);
                            }
                        });
                        break;
                    case LIGHTNING:
//                        LightningBoltEntity lightningBolt = new LightningBoltEntity(EntityType.LIGHTNING_BOLT,
//                                player.world);
                        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverWorld);
                        lightningBolt.setCause(player);
//                        lightningBolt.moveForced(targetX,targetY,targetZ);
                        lightningBolt.setPos(targetX, targetY, targetZ);

                        List<Entity> list = lightningBolt.level.getEntities(lightningBolt, new AABB(lightningBolt.getX() - 3.0D, lightningBolt.getY() - 3.0D, lightningBolt.getZ() - 3.0D, lightningBolt.getX() + 3.0D, lightningBolt.getY() + 6.0D + 3.0D, lightningBolt.getZ() + 3.0D), Entity::isAlive);
                        for(Entity entity : list) {
                            if (entity instanceof LivingEntity) {
                                if (!MinecraftForge.EVENT_BUS.post(new LivingAttackEvent((LivingEntity) entity, DamageSource.LIGHTNING_BOLT, lightningBolt.getDamage()))) {
                                    entity.hurt(DamageSource.indirectMagic(lightningBolt, player), lightningBolt.getDamage());
                                }
                            } else {
                                entity.hurt(DamageSource.LIGHTNING_BOLT,lightningBolt.getDamage());
                            }
                        }

                        lightningBolt.setDamage(0f);
                        player.level.addFreshEntity(lightningBolt);
                        break;
                    case FANG:
                        EvokerFangs evokerFangs = new EvokerFangs(EntityType.EVOKER_FANGS, player.level);
                        evokerFangs.setOwner(player);
                        evokerFangs.setPos(targetX, targetY, targetZ);
                        player.level.addFreshEntity(evokerFangs);
                        break;

                }

            }
        } catch (Exception e) {
            DragonTongue.LOGGER.warn("Could not handle crow wand package");
        }

    }
}
