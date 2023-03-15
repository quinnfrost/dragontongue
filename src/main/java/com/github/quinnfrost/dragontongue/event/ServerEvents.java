package com.github.quinnfrost.dragontongue.event;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.entity.ai.EntityBehaviorDebugger;
import com.github.quinnfrost.dragontongue.entity.ai.RegistryAI;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.iceandfire.IafAdvancedDragonFlightManager;
import com.github.quinnfrost.dragontongue.iceandfire.IafAdvancedDragonLogic;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.iceandfire.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageClientSetReferenceDragon;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.*;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class ServerEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // Resets the debug option, or the getChunk() might cause infinite wait
        EntityBehaviorDebugger.stopDebug();
    }

    /**
     * Add function
     * Make trident hit a signal for tamed to attack
     * Sneaking when trident lands teleport the player
     *
     * @param event
     */
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }

        Projectile projectile = event.getProjectile();
        Entity shooter = projectile.getOwner();

        // Trident teleports
        if (projectile instanceof ThrownTrident && shooter instanceof ServerPlayer) {
            ServerLevel serverWorld = (ServerLevel) shooter.getCommandSenderWorld();
            ServerPlayer player = (ServerPlayer) shooter;
            if (Config.TRIDENT_TELEPORT.get() && shooter.isShiftKeyDown() && event.getRayTraceResult().getType() != HitResult.Type.MISS) {
                Vec3 targetBlock = event.getRayTraceResult().getLocation();
                shooter.teleportToWithTicket(targetBlock.x(), targetBlock.y(), targetBlock.z());
                util.spawnParticleForce(serverWorld, ParticleTypes.PORTAL, targetBlock.x(), targetBlock.y(),
                        targetBlock.z(),
                        800, 2, 1, 2, 0.1);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10, 0, true, false));
            } else if (event.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityRayTraceResult = (EntityHitResult) event.getRayTraceResult();
                try {
                    player.setLastHurtMob(entityRayTraceResult.getEntity());
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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level.isClientSide) {
            return;
        }
        if (event.player == EntityBehaviorDebugger.requestedPlayer) {
            EntityBehaviorDebugger.updateDebugMessage();
        }

        ServerPlayer player = (ServerPlayer) event.player;
        ServerLevel serverWorld = player.getLevel();
        Item mainhandItem = player.getMainHandItem().getItem();
        Item offhandItem = player.getOffhandItem().getItem();
        // Player can fall back before fallback timer tick to 0
        player.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
            iCapTargetHolder.tickFallbackTimer();
            List<String> msg = new ArrayList<>();
            msg.add(String.valueOf(iCapTargetHolder.getFallbackTimer()));
            // Sneak to fallback, set timer to 0 if not holding wand anymore
            if (iCapTargetHolder.getFallbackTimer() != 0 && player.isShiftKeyDown()) {
                MessageCrowWand.crowWandAction(EnumCrowWand.FALLBACK, player, serverWorld);
            } else if (iCapTargetHolder.getFallbackTimer() != 0 && !(
                    mainhandItem.equals(RegistryItems.CROW_WAND)
                            || offhandItem.equals(RegistryItems.CROW_WAND)
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
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (DragonTongue.isIafPresent) {
            IafServerEvent.onLivingUpdate(event);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        // Register ai
        Entity entity = event.getEntity();
        if (entity instanceof Mob) {
            Mob mobEntity = (Mob) event.getEntity();
            RegistryAI.registerAI(mobEntity);
            if (DragonTongue.isIafPresent && IafHelperClass.isDragon(mobEntity)) {
                IafAdvancedDragonLogic.applyDragonLogic(mobEntity);
                IafAdvancedDragonFlightManager.applyDragonFlightManager(mobEntity);
            }
        }
        // Initial capability update for the first time player logs in
        if (entity instanceof Player) {
            Player playerEntity = (Player) entity;
            playerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                RegistryMessages.sendToClient(new MessageSyncCapability(playerEntity), (ServerPlayer) playerEntity);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        ServerPlayer serverPlayerEntity = (ServerPlayer) event.getPlayer();
        if (event.getTarget() instanceof Mob) {
            Mob mobEntity = (Mob) event.getTarget();
            // Initial capability update for the player client loads the entity for the first time
            RegistryMessages.sendToClient(new MessageSyncCapability(mobEntity), serverPlayerEntity);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (DragonTongue.isIafPresent) {
            Player playerEntity = event.getPlayer();
            InteractionHand hand = event.getHand();
            ItemStack itemStack = playerEntity.getItemInHand(hand);
            Entity target = event.getTarget();
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING && IafHelperClass.isDragon(target)) {
                LivingEntity dragon = (LivingEntity) target;
                if (IafDragonBehaviorHelper.resurrectDragon(dragon)) {
                    itemStack.shrink(1);
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        if (event.getEntity() == null || event.getEntity().level.isClientSide) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Creeper) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (event.getWorld().isClientSide) {
            return;
        }

    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getWorld().isClientSide) {
            return;
        }
        Explosion explosion = event.getExplosion();
        Entity exploder = event.getExplosion().getExploder();
        Entity placer = event.getExplosion().getSourceMob();
        if ((exploder instanceof PrimedTnt)
                || (exploder instanceof MinecartTNT)) {
            explosion.clearToBlow();
        }
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent event) {

    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isClientSide) {
            return;
        }
        // Using dragon staff right-click on a dragon will open gui
        Entity targetEntity = event.getTarget();
        Player playerEntity = event.getPlayer();
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityInteract(event);
        }
    }

    @SubscribeEvent
    public static void onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isClientSide) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityUseItem(event);
        }
        if (event.getEntityLiving() instanceof Player) {
            Player playerEntity = (Player) event.getEntityLiving();
            InteractionHand hand = event.getHand();
            ItemStack itemStack = playerEntity.getItemInHand(hand);

            if (itemStack.getItem() == RegistryItems.DRAGON_STAFF_ICE.get()) {
//                EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(playerEntity, Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f,
//                        entity -> entity instanceof EntityDragonPart || entity instanceof LivingEntity);
//                if (entityRayTraceResult == null || !IafHelperClass.isDragon(IafHelperClass.getDragon(entityRayTraceResult.getEntity()))) {
//                    return;
//                }
//                EntityDragonBase dragon = IafHelperClass.getDragon(entityRayTraceResult.getEntity());

                HitResult rayTraceResult = util.getTargetBlockOrEntity(playerEntity, (float) ICapabilityInfoHolder.getCapability(playerEntity).getCommandDistance(), null);

                if (EntityBehaviorDebugger.targetEntity != null) {
                    Mob targetEntity = EntityBehaviorDebugger.targetEntity;
                    Vec3 target = rayTraceResult.getLocation();
                    double xTarget = target.x;
                    double yTarget = target.y;
                    double zTarget = target.z;
                    targetEntity.getNavigation().moveTo(xTarget,yTarget,zTarget,1.0f);
                }

            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        // Display critical hit mark
        Entity source = event.getSource().getEntity();
        if (source instanceof ServerPlayer) {
            ServerPlayer attacker = (ServerPlayer) source;
            RegistryMessages.sendToClient(new MessageClientDisplay(
                            MessageClientDisplay.EnumClientDisplay.CRITICAL, 1, Collections.singletonList("")),
                    attacker
            );
        }
    }

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityDamage(event);
        }
        // Display hit mark
        Entity source = event.getSource().getEntity();
        LivingEntity hurtEntity = event.getEntityLiving();
        if (source instanceof ServerPlayer) {
            ServerPlayer playerEntity = (ServerPlayer) source;
            float damageAmount = event.getAmount();
            if (hurtEntity.isAlive()) {

                RegistryMessages.sendToClient(new MessageClientDisplay(
                                MessageClientDisplay.EnumClientDisplay.DAMAGE, 1, Collections.singletonList(String.format("%.1f", damageAmount))),
                        playerEntity
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(LivingAttackEvent event) {
        if (event.getEntity().level.isClientSide) {
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
