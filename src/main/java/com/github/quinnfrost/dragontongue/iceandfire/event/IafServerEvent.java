package com.github.quinnfrost.dragontongue.iceandfire.event;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.api.event.DragonFireDamageWorldEvent;
import com.github.alexthe666.iceandfire.api.event.DragonFireEvent;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.props.FrozenProperties;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDragonforgeInput;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.misc.IafDamageRegistry;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import com.github.quinnfrost.dragontongue.message.MessageClientSetReferenceDragon;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IafServerEvent {
    @Deprecated
    public static final int TEMP_ROOST_PROTECTION_RANGE = 64;

    public static void register(IEventBus eventBus) {
        eventBus.register(IafServerEvent.class);
    }

    @SubscribeEvent
    public static void onDragonFireDamage(DragonFireDamageWorldEvent event) {
        // On IafDragonDestructionManager#91, the dragon stop damage everything if the event is canceled
        // We need to cancel the event and handle the damaging ourselves
        EntityDragonBase destroyerDragon = event.getDragon();
        BlockPos destroyCenter = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        World world = destroyerDragon.world;
        ICapTargetHolder cap = destroyerDragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(destroyerDragon));
        if (!IafDragonBehaviorHelper.shouldDestroy(destroyerDragon, destroyCenter)) {
            // From IafDragonDestructionManager#destroyAreaFire
            DamageSource source = destroyerDragon.getRidingPlayer() != null ?
                    IafDamageRegistry.causeIndirectDragonIceDamage(destroyerDragon, destroyerDragon.getRidingPlayer()) :
                    IafDamageRegistry.causeDragonIceDamage(destroyerDragon);
            int stage = destroyerDragon.getDragonStage();
            double damageRadius = 3.5D;
            float dmgScale = (float) IafConfig.dragonAttackDamageIce;

            if (stage <= 3) {
                BlockPos.getAllInBox(destroyCenter.add(-1, -1, -1), destroyCenter.add(1, 1, 1)).forEach(pos -> {
                    if (world.getTileEntity(pos) instanceof TileEntityDragonforgeInput) {
                        ((TileEntityDragonforgeInput) world.getTileEntity(pos)).onHitWithFlame();
                        return;
                    }
//                    if (IafConfig.dragonGriefing != 2 && world.rand.nextBoolean()) {
//                        IafDragonDestructionManager.iceAttackBlock(world, pos);
//                    }
                });
            } else {
                final int radius = stage == 4 ? 2 : 3;
                final int j = radius + world.rand.nextInt(1);
                final int k = radius + world.rand.nextInt(1);
                final int l = radius + world.rand.nextInt(1);
                final float f = (float) (j + k + l) * 0.333F + 0.5F;
                final float ff = f * f;

                damageRadius = 2.5F + f * 1.2F;
                BlockPos.getAllInBox(destroyCenter.add(-j, -k, -l), destroyCenter.add(j, k, l)).forEach(pos -> {
                    if (world.getTileEntity(pos) instanceof TileEntityDragonforgeInput) {
                        ((TileEntityDragonforgeInput) world.getTileEntity(pos)).onHitWithFlame();
                        return;
                    }
//                    if (destroyCenter.distanceSq(pos) <= ff) {
//                        if (IafConfig.dragonGriefing != 2 && world.rand.nextFloat() > (float) destroyCenter.distanceSq(pos) / ff) {
//                            iceAttackBlock(world, pos);
//                        }
//                    }
                });
            }

            final float stageDmg = stage * dmgScale;
            final int statusDuration = 50 * stage;
            world.getEntitiesWithinAABB(
                    LivingEntity.class,
                    new AxisAlignedBB(
                            (double) destroyCenter.getX() - damageRadius,
                            (double) destroyCenter.getY() - damageRadius,
                            (double) destroyCenter.getZ() - damageRadius,
                            (double) destroyCenter.getX() + damageRadius,
                            (double) destroyCenter.getY() + damageRadius,
                            (double) destroyCenter.getZ() + damageRadius
                    )
            ).stream().forEach(livingEntity -> {
                if (!DragonUtils.onSameTeam(destroyerDragon, livingEntity) && !destroyerDragon.isEntityEqual(livingEntity) && destroyerDragon.canEntityBeSeen(livingEntity)) {
                    livingEntity.attackEntityFrom(source, stageDmg);
                    FrozenProperties.setFrozenFor(livingEntity, statusDuration);
                }
            });

            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDragonBreathe(DragonFireEvent event) {
        // This event only fires after the leading breath animation is over

    }

    @SubscribeEvent
    public static void onEntityGrief(GenericGriefEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (livingEntity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) livingEntity;
            ICapTargetHolder cap = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
            BlockPos destroyCenter = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());

            if (!IafDragonBehaviorHelper.shouldDestroy(dragon, destroyCenter)) {
                event.setResult(Event.Result.DENY);
                event.setCanceled(true);
            }
        }
    }

    // This event is called in server event, could register though
    public static boolean onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isRemote) {
            return false;
        }
        Entity targetEntity = event.getTarget();
        if (IafHelperClass.isDragon(IafHelperClass.getDragon(targetEntity)) && event.getEntityLiving() instanceof PlayerEntity) {
            EntityDragonBase dragon = IafHelperClass.getDragon(targetEntity);
            PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();

            Hand hand = event.getPlayer().getActiveHand();
            ItemStack itemStack = playerEntity.getHeldItem(event.getHand());
            ItemStack itemStackMainhand = playerEntity.getHeldItemMainhand();
            ItemStack itemStackOffhand = playerEntity.getHeldItemOffhand();

            if (playerEntity.isSneaking()) {
                if (itemStackMainhand.getItem() == IafItemRegistry.DRAGON_STAFF || itemStackOffhand.getItem() == IafItemRegistry.DRAGON_STAFF) {
                    // Hijack the dragon staff right click dragon in sneaking
                    if (event.getHand() == event.getPlayer().getActiveHand() && playerEntity.getDistance(dragon) < 5) {
                        playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty("Dragon staff used"), Util.DUMMY_UUID);
                        if (dragon.hasHomePosition) {
                            dragon.hasHomePosition = false;
                            playerEntity.sendStatusMessage(new TranslationTextComponent("dragon.command.remove_home"), true);
                            dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                                iCapTargetHolder.setHomePosition(null);
                                iCapTargetHolder.setHomeDimension(null);
                            });
                        } else {
                            BlockPos pos = dragon.getPosition();
                            dragon.homePos = new HomePosition(pos, dragon.world);
                            dragon.hasHomePosition = true;
                            playerEntity.sendStatusMessage(new TranslationTextComponent("dragon.command.new_home", pos.getX(), pos.getY(), pos.getZ(), dragon.homePos.getDimension()), true);
                        }
                    }
                    MessageSyncCapability.syncCapabilityToClients(dragon);
                    event.setCancellationResult(ActionResultType.SUCCESS);
                    event.setCanceled(true);
                } else if (itemStackMainhand.isEmpty() || itemStackOffhand.isEmpty()) {
                    // Hijack empty hand right click dragon in sneaking
                    if (event.getHand() == event.getPlayer().getActiveHand() && playerEntity.getDistance(dragon) < 5) {
                        RegistryMessages.sendToClient(new MessageClientSetReferenceDragon(
                                dragon.getEntityId()
                        ), (ServerPlayerEntity) playerEntity);
                        ScreenDragon.openGui(playerEntity, dragon);
                    }
                    event.setCancellationResult(ActionResultType.SUCCESS);
                    event.setCanceled(true);
                }
            }

        }

        // Hijack the original dragon staff function in EntityDragonBase#1269

        return true;
    }

    public static boolean onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            return false;
        }
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
            Hand hand = event.getHand();
            ItemStack itemStack = playerEntity.getHeldItem(hand);

            // Hijack the original dragon staff function in EntityDragonBase#1269
            if (itemStack.getItem() == IafItemRegistry.DRAGON_STAFF && !playerEntity.isSneaking()) {
                EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(playerEntity, Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f,
                        entity -> entity instanceof EntityDragonPart || entity instanceof LivingEntity);
                if (entityRayTraceResult == null || !IafHelperClass.isDragon(IafHelperClass.getDragon(entityRayTraceResult.getEntity()))) {
                    return false;
                }
                EntityDragonBase dragon = IafHelperClass.getDragon(entityRayTraceResult.getEntity());

                playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty("Dragon staff used"), Util.DUMMY_UUID);

                RegistryMessages.sendToClient(new MessageClientSetReferenceDragon(
                        dragon.getEntityId()
                ), (ServerPlayerEntity) playerEntity);
                ScreenDragon.openGui(playerEntity, dragon);
                event.setCanceled(true);
            }
        }
        return true;
    }

    public static boolean onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityIceDragon) {
            EntityIceDragon iceDragon = (EntityIceDragon) entity;
            if (iceDragon.getDragonStage() >= 2) {

            }
        }

        return true;
    }

    public static boolean onLivingKnockBack(LivingKnockBackEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityIceDragon) {
            EntityIceDragon iceDragon = (EntityIceDragon) entity;
            if (iceDragon.getDragonStage() >= 2) {
//                event.setStrength(0f);
                event.setCanceled(true);
            }
        }
        return true;
    }

    public static void onEntityDamage(LivingDamageEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }

    }

    public static boolean onEntityAttacked(LivingAttackEvent event) {
        if (event.getEntity().world.isRemote) {
            return false;
        }
        Entity entity = event.getEntity();
        DamageSource damageSource = event.getSource();

        if (entity instanceof EntityIceDragon) {
            EntityIceDragon iceDragon = (EntityIceDragon) entity;
            if (iceDragon.getDragonStage() >= 2 && event.getAmount() < 2f){
                event.setCanceled(true);
            }
            if (iceDragon.getDragonStage() >= 2 &&
                    (damageSource == DamageSource.CACTUS
                            || damageSource == DamageSource.ANVIL
                            || damageSource == DamageSource.IN_FIRE
                            || damageSource == DamageSource.ON_FIRE
                            || damageSource == DamageSource.LAVA
                            || damageSource == DamageSource.SWEET_BERRY_BUSH)
            ) {
                iceDragon.forceFireTicks(0);
                event.setCanceled(true);
            }
        }
        return true;
    }
}
