package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.entity.ai.RegistryAI;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.MessageClientCommandDistance;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.MessageCrowWand;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

public class ServerEvents {
    /**
     * Add function
     *      Make trident hit a signal for tamed to attack
     *      Sneaking when trident lands teleport the player
     * @param event
     */
    @SubscribeEvent
    public static void onTridentImpact(ProjectileImpactEvent.Arrow event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

        ProjectileEntity projectile = event.getArrow();
        Entity shooter = projectile.getShooter();
        ServerWorld serverWorld = (ServerWorld) shooter.getEntityWorld();

        if (projectile instanceof TridentEntity && shooter instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) shooter;
            if (shooter.isSneaking() && event.getRayTraceResult().getType() != RayTraceResult.Type.MISS) {
                Vector3d targetBlock = event.getRayTraceResult().getHitVec();
                shooter.teleportKeepLoaded(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
                // shooter.getCapability(CapTargetHolder.ENTITY_TEST_CAPABILITY).ifPresent(iCapabilityInfoHolder
                // -> {
                // iCapabilityInfoHolder.setFallbackTimer(80);
                // });
                serverWorld.spawnParticle(ParticleTypes.PORTAL, targetBlock.getX(), targetBlock.getY(),
                        targetBlock.getZ(),
                        800, 2, 1, 2, 0.1);
                player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 20, 0, true, false));
            } else if (event.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityRayTraceResult = (EntityRayTraceResult) event.getRayTraceResult();
                try {
                    player.setLastAttackedEntity(entityRayTraceResult.getEntity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Process crow wand fallback and its timer
     * @param event
     */
    @SubscribeEvent
    public static void updateFallbackTimer(TickEvent.PlayerTickEvent event) {
        if (event.player.world.isRemote) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        ServerWorld serverWorld = player.getServerWorld();
        // Player can fall back before fallback timer tick to 0
        player.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
            iCapTargetHolder.tickFallbackTimer();
            // Sneak to fallback, set timer to 0 if not holding wand anymore
            if (iCapTargetHolder.getFallbackTimer() != 0 && player.isSneaking()) {
                MessageCrowWand.teleportPlayer(EnumCrowWand.FALLBACK, player, serverWorld);
            } else if (
                    iCapTargetHolder.getFallbackTimer() != 0
                    && !(player.getHeldItemMainhand().getItem().equals(RegistryItems.CROW_WAND.get())
                                    || player.getHeldItemOffhand().getItem().equals(RegistryItems.CROW_WAND.get()))
            ) {
                iCapTargetHolder.setFallbackTimer(0);
//                player.getCapability(CapTargetHolder.TARGET_HOLDER, null).ifPresent(iCapTargetHolder1 -> {
//                    iCapTargetHolder.setFallbackTimer(0);
//                });
            }
        });

    }

    /**
     * Set entity destination if valid
     *
     * @param event
     */
    @SubscribeEvent
    public static void updateLivingDestination(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

//        if (event.getEntity() instanceof AnimalEntity){
//            AnimalEntity entity = (AnimalEntity)event.getEntityLiving();
//            entity.getCapability(CapTargetHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
//                BlockPos blockPos = iCapabilityInfoHolder.getPos();
//                double targetX = iCapabilityInfoHolder.getPos().getX();
//                double targetY = iCapabilityInfoHolder.getPos().getY();
//                double targetZ = iCapabilityInfoHolder.getPos().getZ();
//                double entityX = entity.getPositionVec().getX();
//                double entityY = entity.getPositionVec().getY();
//                double entityZ = entity.getPositionVec().getZ();
//                // formula from wiki to transform attribute speed to block per second
//                double speed = 43.178 * entity.getBaseAttributeValue(Attributes.MOVEMENT_SPEED) - 0.02141;
//                if (iCapabilityInfoHolder.getDestinationSet()) {
//                    boolean result = entity.getNavigator().tryMoveToXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.2d);
//                    AxisAlignedBB axisAlignedBB = new AxisAlignedBB(targetX,targetY,targetZ,targetX,targetY,targetZ).grow(1);
//                    if (axisAlignedBB.intersects(entity.getBoundingBox())) {
//                        iCapabilityInfoHolder.setDestinationSet(false);
//                    }
//                }
//            });
//        }

    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity) event.getEntity();
            RegistryAI.registerAI(mobEntity);

        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            playerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                RegistryMessages.sendToClient(new MessageClientCommandDistance(iCapTargetHolder.getCommandDistance()), (ServerPlayerEntity) playerEntity);
            });
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getTrueSource();
        LivingEntity hurtEntity = event.getEntityLiving();
        if (source instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) source;
            float damageAmount = event.getAmount();
            if (hurtEntity.isAlive()) {
                RegistryMessages.sendToClient(new MessageClientDisplay(
                        EnumClientDisplay.DAMAGE, 1,Collections.singletonList(String.valueOf(damageAmount))),
                        playerEntity
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (source instanceof ServerPlayerEntity) {
            ServerPlayerEntity attacker = (ServerPlayerEntity) source;
            RegistryMessages.sendToClient(new MessageClientDisplay(
                            EnumClientDisplay.CRITICAL, 1,Collections.singletonList("")),
                    attacker
            );
        }
    }

    }
