package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.entity.ai.RegistryAI;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
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
    public static void onProjectileImpact(ProjectileImpactEvent.Arrow event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

        ProjectileEntity projectile = event.getArrow();
        Entity shooter = projectile.getShooter();

        // Trident teleports
        if (projectile instanceof TridentEntity && shooter instanceof ServerPlayerEntity) {
            ServerWorld serverWorld = (ServerWorld) shooter.getEntityWorld();
            ServerPlayerEntity player = (ServerPlayerEntity) shooter;
            if (Config.TRIDENT_TELEPORT.get() && shooter.isSneaking() && event.getRayTraceResult().getType() != RayTraceResult.Type.MISS) {
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
        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
                IafDragonBehaviorHelper.applyPatchedDragonLogic(event.getEntityLiving());
            }

            // Ask all clients to draw entity destination
            if (event.getEntityLiving() instanceof PlayerEntity && DragonTongue.debugTarget != null) {
                PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
                ICapabilityInfoHolder cap = playerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(playerEntity));
                for (UUID entityUUID :
                        cap.getCommandEntities()) {
                    MobEntity mobEntity = (MobEntity) ((ServerWorld) playerEntity.world).getEntityByUuid(entityUUID);
                    BlockPos pos = IafHelperClass.getReachTarget(mobEntity);
                    if (pos != null) {
                        RegistryMessages.sendToAll(new MessageClientDraw(
                                mobEntity.getEntityId(), Vector3d.copyCentered(pos),
                                mobEntity.getPositionVec()
                        ));
                    } else if (IafHelperClass.isDragon(mobEntity)) {
                        IafHelperClass.drawDragonFlightDestination(mobEntity);
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

            ICapabilityInfoHolder capabilityInfoHolder = mobEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(mobEntity));
            BlockPos targetPos = DragonTongue.isIafPresent ? IafHelperClass.getReachTarget(mobEntity) : mobEntity.getNavigator().getTargetPos();
            String targetPosString = (targetPos == null ? "" :
                    targetPos + "(" + String.valueOf(util.getDistance(mobEntity.getPosition(), targetPos)) + ")");
            Entity targetEntity = mobEntity.getAttackTarget();
            String targetString = targetEntity == null ? "" :
                    targetEntity.getEntityString() + " " + mobEntity.getAttackTarget().getPosition();
            String destinationString = capabilityInfoHolder.getDestination().isPresent() ?
                    capabilityInfoHolder.getDestination().get() + "(" + util.getDistance(capabilityInfoHolder.getDestination().get(), mobEntity.getPosition()) + ")" : "";

            List<String> debugMsg = Arrays.asList(
                    mobEntity.getEntityString() + "[" + mobEntity.getCustomName() + "]",
                    "Pos:" + mobEntity.getPosition(),
                    "Motion:" + mobEntity.getMotion(),
                    "Goals:",
                    mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                    mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                    "Targets:" + targetString,
                    "Current dest:" + targetPosString,
                    "Command status:" + capabilityInfoHolder.getCommandStatus().toString(),
                    "Command dest:" + destinationString,
                    "AttackDecision:" + capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE),
                    "StepHeight:" + mobEntity.stepHeight
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
            playerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
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

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isRemote) {
            return;
        }
        // Using dragon staff right-click on a dragon will open gui
        Entity targetEntity = event.getTarget();
        PlayerEntity playerEntity = event.getPlayer();
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityInteract(event);
        }
    }

    @SubscribeEvent
    public static void onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityUseItem(event);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        // Display critical hit mark
        Entity source = event.getSource().getTrueSource();
        if (source instanceof ServerPlayerEntity) {
            ServerPlayerEntity attacker = (ServerPlayerEntity) source;
            RegistryMessages.sendToClient(new MessageClientDisplay(
                            EnumClientDisplay.CRITICAL, 1, Collections.singletonList("")),
                    attacker
            );
        }
    }

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityDamage(event);
        }
        // Display hit mark
        Entity source = event.getSource().getTrueSource();
        LivingEntity hurtEntity = event.getEntityLiving();
        if (source instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) source;
            float damageAmount = event.getAmount();
            if (hurtEntity.isAlive()) {

                RegistryMessages.sendToClient(new MessageClientDisplay(
                                EnumClientDisplay.DAMAGE, 1, Collections.singletonList(String.format("%.1f", damageAmount))),
                        playerEntity
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(LivingAttackEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityAttacked(event);
        }
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        // This seems to be a client only event
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onLivingKnockBack(event);
        }
    }
}
