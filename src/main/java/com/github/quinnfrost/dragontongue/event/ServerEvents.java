package com.github.quinnfrost.dragontongue.event;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.entity.ai.RegistryAI;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.enums.EnumClientDraw;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.iceandfire.IafAdvancedDragonFlightManager;
import com.github.quinnfrost.dragontongue.iceandfire.IafAdvancedDragonLogic;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.*;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerEvents {
    /**
     * Add function
     * Make trident hit a signal for tamed to attack
     * Sneaking when trident lands teleport the player
     *
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

        if (projectile instanceof TridentEntity && shooter instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) shooter;
            if (shooter.isSneaking() && event.getRayTraceResult().getType() != RayTraceResult.Type.MISS) {
                Vector3d targetBlock = event.getRayTraceResult().getHitVec();
                shooter.teleportKeepLoaded(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
                util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, targetBlock.getX(), targetBlock.getY(),
                        targetBlock.getZ(),
                        800, 2, 1, 2, 0.1);
                player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 10, 0, true, false));
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
     *
     * @param event
     */
    @SubscribeEvent
    public static void updateFallbackTimer(TickEvent.PlayerTickEvent event) {
        if (event.player.world.isRemote) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        ServerWorld serverWorld = player.getServerWorld();
        Item mainhandItem = player.getHeldItemMainhand().getItem();
        Item offhandItem = player.getHeldItemOffhand().getItem();
        // Player can fall back before fallback timer tick to 0
        player.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
            iCapTargetHolder.tickFallbackTimer();
            List<String> msg = new ArrayList<>();
            msg.add(String.valueOf(iCapTargetHolder.getFallbackTimer()));
            // Sneak to fallback, set timer to 0 if not holding wand anymore
            if (iCapTargetHolder.getFallbackTimer() != 0 && player.isSneaking()) {
                MessageCrowWand.crowWandAction(EnumCrowWand.FALLBACK, player, serverWorld);
            } else if (iCapTargetHolder.getFallbackTimer() != 0 && !(
                    mainhandItem.equals(RegistryItems.CROW_WAND.get())
                            || offhandItem.equals(RegistryItems.CROW_WAND.get())
            )) {
                iCapTargetHolder.setFallbackTimer(0);
            }
        });

    }

    /**
     * @param event
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onLivingUpdate(event);

            if (IafHelperClass.isDragon(event.getEntityLiving())) {
                IafDragonBehaviorHelper.updateDragonCommand(event.getEntityLiving());
            }
            if (event.getEntityLiving() instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
                ICapTargetHolder cap = playerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(playerEntity));
                for (UUID entityUUID :
                        cap.getCommandEntities()) {
                    MobEntity mobEntity = (MobEntity) ((ServerWorld) playerEntity.world).getEntityByUuid(entityUUID);
                    BlockPos pos = IafHelperClass.getReachTarget(mobEntity);
                    if (pos != null && !IafDragonBehaviorHelper.isDragonInAir(mobEntity)) {
                        RegistryMessages.sendToAll(new MessageClientDraw(
                                mobEntity.getEntityId(), Vector3d.copyCentered(pos),
                                mobEntity.getPositionVec()
                        ));
                    } else if (IafHelperClass.isDragon(mobEntity)) {
                        RegistryMessages.sendToAll(new MessageClientDraw(
                                mobEntity.getEntityId(), ((EntityDragonBase) mobEntity).flightManager.getFlightTarget(),
                                mobEntity.getPositionVec()
                        ));
                    }
//                    RegistryMessages.sendToAll(new MessageClientDraw(
//                            mobEntity.getEntityId(),
//                            new Vector3d(
//                                    (mobEntity.getPositionVec().x + mobEntity.getMotion().x * 10),
//                                    (mobEntity.getPositionVec().y + mobEntity.getMotion().y * 10),
//                                    (mobEntity.getPositionVec().z + mobEntity.getMotion().z * 10)
//                                    ),
//                            mobEntity.getPositionVec()
//                    ));
                }
            }
        }
        // Ask all client to display entity debug string
        if (event.getEntity() == DragonTongue.debugTarget) {
            MobEntity mobEntity = (MobEntity) event.getEntity();
            CompoundNBT compoundNBT = new CompoundNBT();
            DragonTongue.debugTarget.writeAdditional(compoundNBT);

            ICapTargetHolder capabilityInfoHolder = mobEntity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(mobEntity));
            BlockPos targetPos = mobEntity.getNavigator().getTargetPos();
            String targetPosString = (targetPos == null ? "" :
                    targetPos.getCoordinatesAsString() + "(" + String.valueOf(util.getDistance(mobEntity.getPosition(), targetPos))) + ")";
            Entity targetEntity = mobEntity.getAttackTarget();
            String targetString = targetEntity == null ? "" :
                    targetEntity.getEntityString() + " " + mobEntity.getAttackTarget().getPosition().getCoordinatesAsString();
            String destinationString = capabilityInfoHolder.getDestination().isPresent() ?
                    capabilityInfoHolder.getDestination().get().getCoordinatesAsString() + "(" + util.getDistance(capabilityInfoHolder.getDestination().get(), mobEntity.getPosition()) + ")" : "";

            List<String> debugMsg = Arrays.asList(
                    mobEntity.getEntityString() + "[" + mobEntity.getCustomName() + "]",
                    "Pos:" + mobEntity.getPosition().getCoordinatesAsString(),
                    "Motion:" + mobEntity.getMotion(),
                    "Goals:",
                    mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                    mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                    "Targets:" + targetString,
                    "Current dest:" + targetPosString,
                    "Command status:" + capabilityInfoHolder.getCommandStatus().toString(),
                    "Command dest:" + destinationString,
                    "HurtTick:" + mobEntity.hurtTime,
                    "ResistanceTick:" + mobEntity.hurtResistantTime
            );
            if (DragonTongue.isIafPresent) {
                List<String> additional = IafHelperClass.getAdditionalDragonDebugStrings(mobEntity);
                debugMsg = Stream.concat(debugMsg.stream(), additional.stream())
                        .collect(Collectors.toList());
            }
            RegistryMessages.sendToAll(new MessageClientDisplay(
                    EnumClientDisplay.ENTITY_DEBUG,
                    1,
                    debugMsg
            ));

        }

    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        // Register ai
        Entity entity = event.getEntity();
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity) event.getEntity();
            RegistryAI.registerAI(mobEntity);
            if (DragonTongue.isIafPresent && IafHelperClass.isDragon(mobEntity)) {
                IafAdvancedDragonLogic.applyDragonLogic(mobEntity);
                IafAdvancedDragonFlightManager.applyDragonFlightManager(mobEntity);
            }
        }
        // Initial capability update for the first time player logs in
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            playerEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                RegistryMessages.sendToClient(new MessageSyncCapability(playerEntity), (ServerPlayerEntity) playerEntity);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) event.getPlayer();
        if (event.getTarget() instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity) event.getTarget();
            // Initial capability update for the player client loads the entity for the first time
            RegistryMessages.sendToClient(new MessageSyncCapability(mobEntity), serverPlayerEntity);
        }
    }


    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

    }

    @SubscribeEvent
    public static void onEntityUseItem(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

        if (DragonTongue.isIafPresent) {
            PlayerEntity playerEntity = event.getPlayer();
            Hand hand = event.getHand();
            ItemStack itemStack = playerEntity.getHeldItem(hand);
            Entity target = event.getTarget();
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING && IafHelperClass.isDragon(target)) {
                LivingEntity dragon = (LivingEntity) target;
                if (IafDragonBehaviorHelper.resurrectDragon(dragon)) {
                    itemStack.shrink(1);
                }
                event.setCancellationResult(ActionResultType.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof CreeperEntity) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        if (event.getWorld().isRemote) {
            return;
        }

    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getWorld().isRemote) {
            return;
        }
        Explosion explosion = event.getExplosion();
        Entity exploder = event.getExplosion().getExploder();
        Entity placer = event.getExplosion().getExplosivePlacedBy();
        if ((exploder instanceof TNTEntity)
        || (exploder instanceof TNTMinecartEntity)) {
            explosion.clearAffectedBlockPositions();
        }
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent event) {

    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onLivingKnockBack(event);
        }
    }

}
