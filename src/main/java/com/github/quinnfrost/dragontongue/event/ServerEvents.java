package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.MessageCrowWand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEvents {
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent.Arrow event) {
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
                // shooter.getCapability(CapabilityInfoHolder.ENTITY_TEST_CAPABILITY).ifPresent(iCapabilityInfoHolder
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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.world.isRemote) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        ServerWorld serverWorld = player.getServerWorld();
        player.getCapability(CapabilityInfoHolder.ENTITY_TEST_CAPABILITY).ifPresent(iCapabilityInfoHolder -> {
            iCapabilityInfoHolder.fallbackTimerTick();
            if (iCapabilityInfoHolder.getFallbackTimer() != 0 && player.isSneaking()) {
                MessageCrowWand.teleportPlayer(EnumCrowWand.FALLBACK, player, serverWorld);
            } else if (iCapabilityInfoHolder.getFallbackTimer() != 0
                    && !(player.getHeldItemMainhand().getItem().equals(RegistryItems.CROW_WAND.get())
                    || player.getHeldItemOffhand().getItem().equals(RegistryItems.CROW_WAND.get()))) {
                player.getCapability(CapabilityInfoHolder.ENTITY_TEST_CAPABILITY, null).ifPresent(iTestCapability1 -> {
                    iCapabilityInfoHolder.setFallbackTimer(0);
                });
            }
        });

    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

        if (event.getEntity() instanceof AnimalEntity){
            AnimalEntity entity = (AnimalEntity)event.getEntityLiving();
            entity.getCapability(CapabilityInfoHolder.ENTITY_TEST_CAPABILITY).ifPresent(iCapabilityInfoHolder -> {
                BlockPos blockPos = iCapabilityInfoHolder.getPos();
                double targetX = iCapabilityInfoHolder.getPos().getX();
                double targetY = iCapabilityInfoHolder.getPos().getY();
                double targetZ = iCapabilityInfoHolder.getPos().getZ();
                double entityX = entity.getPositionVec().getX();
                double entityY = entity.getPositionVec().getY();
                double entityZ = entity.getPositionVec().getZ();
                // formula from wiki to transform attribute speed to block per second
                double speed = 43.178 * entity.getBaseAttributeValue(Attributes.MOVEMENT_SPEED) - 0.02141;
                if (iCapabilityInfoHolder.getDestinationSet()) {
                    boolean result = entity.getNavigator().tryMoveToXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.2d);
                    AxisAlignedBB axisAlignedBB = new AxisAlignedBB(targetX,targetY,targetZ,targetX,targetY,targetZ).grow(1);
                    if (axisAlignedBB.intersects(entity.getBoundingBox())) {
                        iCapabilityInfoHolder.setDestinationSet(false);
                    }
                }
            });
        }

    }

}
