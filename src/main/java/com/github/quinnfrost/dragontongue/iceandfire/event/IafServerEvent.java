package com.github.quinnfrost.dragontongue.iceandfire.event;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.api.event.DragonFireDamageWorldEvent;
import com.github.alexthe666.iceandfire.api.event.DragonFireEvent;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.props.FrozenProperties;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDragonforgeInput;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.misc.IafDamageRegistry;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageClientSetReferenceDragon;
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
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IafServerEvent {

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
        ICapabilityInfoHolder cap = destroyerDragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(destroyerDragon));
        if (!IafDragonBehaviorHelper.shouldDestroy(destroyerDragon, destroyCenter) || destroyerDragon instanceof EntityIceDragon && ((EntityIceDragon) destroyerDragon).isInMaterialWater()) {
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
            ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
            BlockPos destroyCenter = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());

            if (!IafDragonBehaviorHelper.shouldDestroy(dragon, destroyCenter)) {
                event.setResult(Event.Result.DENY);
                event.setCanceled(true);
            }
        }
    }

    // This event is called in server event, could register though
    public static boolean onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity targetEntity = event.getTarget();
        if (IafHelperClass.isDragon(IafHelperClass.getDragon(targetEntity)) && event.getEntityLiving() instanceof PlayerEntity) {
            EntityDragonBase dragon = IafHelperClass.getDragon(targetEntity);
            PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
            if (!util.isOwner(dragon, playerEntity)) {
                return false;
            }

            Hand hand = event.getPlayer().getActiveHand();
            ItemStack itemStack = playerEntity.getHeldItem(event.getHand());
            ItemStack itemStackMainhand = playerEntity.getHeldItemMainhand();
            ItemStack itemStackOffhand = playerEntity.getHeldItemOffhand();

            if (playerEntity.isSneaking()) {
                if (itemStackMainhand.getItem() == IafItemRegistry.DRAGON_STAFF || itemStackOffhand.getItem() == IafItemRegistry.DRAGON_STAFF) {
                    // Hijack the dragon staff right click dragon in sneaking
                    if (event.getHand() == event.getPlayer().getActiveHand() && playerEntity.getDistance(dragon) < 6) {
                        if (dragon.hasHomePosition) {
                            dragon.hasHomePosition = false;
                            playerEntity.sendStatusMessage(new TranslationTextComponent("dragon.command.remove_home"), true);
                            dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
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
//                    if (event.getHand() == event.getPlayer().getActiveHand() && playerEntity.getDistance(dragon) < 6) {
//                        RegistryMessages.sendToClient(new MessageClientSetReferenceDragon(
//                                dragon.getEntityId()
//                        ), (ServerPlayerEntity) playerEntity);
//                        ContainerDragon.openGui(playerEntity, dragon);
//                    }
//                    event.setCancellationResult(ActionResultType.SUCCESS);
//                    event.setCanceled(true);
                }
            }

        }

        // Hijack the original dragon staff function in EntityDragonBase#1269

        return true;
    }

    public static boolean onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
            Hand hand = event.getHand();
            ItemStack itemStack = playerEntity.getHeldItem(hand);

            // Hijack the original dragon staff function in EntityDragonBase#1269
            if (itemStack.getItem() == IafItemRegistry.DRAGON_STAFF) {
                EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(playerEntity, Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f,
                        entity -> entity instanceof EntityDragonPart || entity instanceof LivingEntity);
                if (entityRayTraceResult == null || !IafHelperClass.isDragon(IafHelperClass.getDragon(entityRayTraceResult.getEntity()))) {
                    return false;
                }
                EntityDragonBase dragon = IafHelperClass.getDragon(entityRayTraceResult.getEntity());

//                playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty("Dragon staff used"), Util.DUMMY_UUID);

                if (!playerEntity.isSneaking()) {
                    if (util.isOwner(dragon, playerEntity)) {
                        RegistryMessages.sendToClient(new MessageClientSetReferenceDragon(
                                dragon.getEntityId()
                        ), (ServerPlayerEntity) playerEntity);
                        ContainerDragon.openGui(playerEntity, dragon);
                        event.setCancellationResult(ActionResultType.SUCCESS);
                        event.setCanceled(true);
                    }
                }
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

    public static boolean onLivingHurt(LivingHurtEvent event) {
        String damageType = event.getSource().getDamageType();
        if (event.getSource().isProjectile()) {
//            float multi = 1;
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof ItemTrollArmor) {
//                multi -= 0.1;
//            }
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ItemTrollArmor) {
//                multi -= 0.3;
//            }
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof ItemTrollArmor) {
//                multi -= 0.2;
//            }
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof ItemTrollArmor) {
//                multi -= 0.1;
//            }
//            event.setAmount(event.getAmount() * multi);
        }
//        if (IafDamageRegistry.DRAGON_FIRE_TYPE.equals(damageType) || IafDamageRegistry.DRAGON_ICE_TYPE.equals(damageType) ||
//                IafDamageRegistry.DRAGON_LIGHTNING_TYPE.equals(damageType)) {
//            float multi = 1;
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof ItemScaleArmor ||
//                    event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof ItemDragonsteelArmor) {
//                multi -= 0.1;
//            }
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ItemScaleArmor ||
//                    event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ItemDragonsteelArmor) {
//                multi -= 0.3;
//            }
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof ItemScaleArmor ||
//                    event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof ItemDragonsteelArmor) {
//                multi -= 0.2;
//            }
//            if (event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof ItemScaleArmor ||
//                    event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof ItemDragonsteelArmor) {
//                multi -= 0.1;
//            }
//            event.setAmount(event.getAmount() * multi);
//        }

        return true;
    }

    public static boolean onLivingKnockBack(LivingKnockBackEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) entity;
            if (dragon.getDragonStage() >= 4) {
                event.setCanceled(true);
            }
        }

        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entity;

            String setName = IafHelperClass.isFullSetOf(playerEntity);
            if (setName != null) {
                BlockPos blockPos = playerEntity.getPosition();
                boolean isOnIceSpike = entity.world.getBlockState(blockPos).getBlock() == IafBlockRegistry.DRAGON_ICE_SPIKES || entity.world.getBlockState(blockPos.down()).getBlock() == IafBlockRegistry.DRAGON_ICE_SPIKES;

                switch (setName) {
                    case "ice":
                    case "dragonsteel_ice":
                        if (isOnIceSpike) {
                            event.setCanceled(true);
                        }
                        break;
                    case "fire":
                    case "dragonsteel_fire":
                        break;
                    case "dragonsteel_lightning":
                    case "lightning":
                        break;
                }
            }

        }

        return true;
    }

    public static void onEntityDamage(LivingDamageEvent event) {

    }

    public static boolean onEntityAttacked(LivingAttackEvent event) {
        Entity entity = event.getEntity();
        DamageSource damageSource = event.getSource();
        BlockPos blockPos = new BlockPos(entity.getPosition());
        boolean isOnIceSpike = entity.world.getBlockState(blockPos).getBlock() == IafBlockRegistry.DRAGON_ICE_SPIKES || entity.world.getBlockState(blockPos.down()).getBlock() == IafBlockRegistry.DRAGON_ICE_SPIKES;

        boolean minorDamages =
                (damageSource == DamageSource.CACTUS && !isOnIceSpike)
                        || damageSource == DamageSource.SWEET_BERRY_BUSH;
        boolean medianDamages =
                damageSource == DamageSource.ANVIL
                        || damageSource == DamageSource.HOT_FLOOR;
        boolean greaterDamages =
                damageSource == DamageSource.IN_FIRE
                        || damageSource == DamageSource.ON_FIRE
                        || damageSource == DamageSource.LAVA
                        || damageSource == DamageSource.CACTUS;

        if (entity instanceof EntityDragonBase) {

            if (entity instanceof EntityIceDragon) {
                EntityIceDragon iceDragon = (EntityIceDragon) entity;
                if (iceDragon.getDragonStage() >= 4 && event.getAmount() < 2f) {
                    event.setCanceled(true);
                }

                if (iceDragon.getDragonStage() >= 4 && greaterDamages) {
                    iceDragon.forceFireTicks(0);
                    event.setCanceled(true);
                } else if (iceDragon.getDragonStage() >= 3 && medianDamages) {
                    event.setCanceled(true);
                } else if (iceDragon.getDragonStage() >= 2 && minorDamages) {
                    event.setCanceled(true);
                }
            }

            if (entity instanceof EntityFireDragon) {
                EntityFireDragon fireDragon = (EntityFireDragon) entity;
                if (fireDragon.getDragonStage() >= 4 && event.getAmount() < 2f) {
                    event.setCanceled(true);
                }
                // Ice spike is also count as cactus damage
                if (fireDragon.getDragonStage() >= 4 && greaterDamages) {
                    event.setCanceled(true);
                } else if (fireDragon.getDragonStage() >= 3 && medianDamages) {
                    event.setCanceled(true);
                } else if (fireDragon.getDragonStage() >= 2 && minorDamages) {
                    event.setCanceled(true);
                }

            }

            if (entity instanceof EntityLightningDragon) {
                EntityLightningDragon lightningDragon = (EntityLightningDragon) entity;
                if (lightningDragon.getDragonStage() >= 4 && event.getAmount() < 2f) {
                    event.setCanceled(true);
                }
                // Ice spike is also count as cactus damage
                if (lightningDragon.getDragonStage() >= 4 && greaterDamages) {
                    lightningDragon.forceFireTicks(0);
                    event.setCanceled(true);
                } else if (lightningDragon.getDragonStage() >= 3 && medianDamages) {
                    event.setCanceled(true);
                } else if (lightningDragon.getDragonStage() >= 2 && minorDamages) {
                    event.setCanceled(true);
                }
            }
        }

        boolean iceDamage = (damageSource == DamageSource.CACTUS && isOnIceSpike);
        boolean fireDamage = damageSource == DamageSource.HOT_FLOOR
                || damageSource == DamageSource.IN_FIRE
                || damageSource == DamageSource.ON_FIRE
                || damageSource == DamageSource.LAVA;
        boolean lightningDamage = damageSource == DamageSource.LIGHTNING_BOLT;


        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            String setName = IafHelperClass.isFullSetOf(playerEntity);
            if (setName != null) {
                switch (setName) {
                    case "ice":
                        if (iceDamage) {
                            event.setCanceled(true);
                        }
                        break;
                    case "fire":
                        if (fireDamage) {
                            playerEntity.forceFireTicks(0);
                            event.setCanceled(true);
                        }
                        break;
                    case "lightning":
                        if (lightningDamage) {
                            event.setCanceled(true);
                        }
                        break;
                    case "dragonsteel_lightning":
                    case "dragonsteel_ice":
                    case "dragonsteel_fire":
                        if (iceDamage || fireDamage || lightningDamage ) {
                            playerEntity.forceFireTicks(0);
                            event.setCanceled(true);
                        }
                        break;
                }
            }

        }


        return true;
    }
}
