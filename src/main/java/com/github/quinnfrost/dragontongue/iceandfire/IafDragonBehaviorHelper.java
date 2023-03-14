package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.entity.ai.GuardGoal;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.ai.*;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.base.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class IafDragonBehaviorHelper {
    public static boolean registerDragonAI(Mob mobEntity) {
        if (IafHelperClass.isDragon(mobEntity)) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;

            return true;
        }
        return false;
    }

    public static boolean registerHippogryphAI(Mob mobEntity) {
        if (IafHelperClass.isHippogryph(mobEntity)) {
            EntityHippogryph hippogryph = (EntityHippogryph) mobEntity;

            hippogryph.goalSelector.addGoal(3, new HippogryphAIFollowCommandAndAttack(hippogryph, 1.0f,true));

            hippogryph.targetSelector.addGoal(3, new GuardGoal<>(hippogryph, LivingEntity.class, false, new java.util.function.Predicate<LivingEntity>() {
                @Override
                public boolean test(@Nullable LivingEntity entity) {
                    return (!(entity instanceof Player) || !((Player) entity).isCreative() || !entity.isSpectator())
                            && util.isHostile(entity);
                }
            }));
            return true;
        }
        return false;
    }

    public static boolean keepDragonHover(Mob dragonIn, BlockPos hoverPos) {
        // Flight logic should be done in flight manager update
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));

        if (cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE) != EnumCommandSettingType.MovementType.AIR
                && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.HOVER && !IafDragonBehaviorHelper.isOverAir(dragon)) {
            dragon.setHovering(false);
            dragon.setFlying(false);
            cap.setCommandStatus(EnumCommandSettingType.CommandStatus.STAY);
        }

        dragon.setDeltaMovement(0, 0, 0);

        // Todo: dragon head position glitch when hover
        // Look position is updated only if the dragon reaches her destination
        Player playerEntity = dragon.level.getPlayerByUUID(dragon.getOwnerUUID());
        // Backoff looking control if the dragon is breathing
        if (playerEntity != null
                && !cap.getBreathTarget().isPresent()) {
            setDragonLook(dragon, playerEntity.blockPosition());
//            dragon.setDragonPitch();
        }

        dragon.setHovering(true);
        dragon.setFlying(false);
        // Force set hover ticks to prevent dragon from flying triggered by dragon logic
        dragon.hoverTicks = 10;
        dragon.ticksStill = 10;
        dragon.flightManager.setFlightTarget(Vec3.atCenterOf(hoverPos));
        dragon.getNavigation().stop();

        return true;
    }

    public static boolean keepDragonStay(Mob dragonIn) {
        // Ground logic should be done in AI
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));

        if (cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE) != EnumCommandSettingType.MovementType.LAND
                && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.STAY && IafDragonBehaviorHelper.isOverAir(dragon)) {
            cap.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);
        }

        dragon.setHovering(false);
        dragon.setFlying(false);
        dragon.ticksStill = 10;
        dragon.getNavigation().stop();

        return true;
    }

    public static boolean keepDragonFlyTo(LivingEntity dragonIn, BlockPos targetPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn));

        cap.getDestination().ifPresent(blockPos -> {
            if (util.hasArrived(dragon, blockPos, null)) {
                dragon.getNavigation().stop();
                dragon.setDeltaMovement(0, 0, 0);
                if (!IafDragonBehaviorHelper.isDragonInAir(dragon) || !IafDragonBehaviorHelper.shouldHoverAt(dragon, blockPos)) {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.STAY);
                } else {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);
                }
            } else {
                if (dragon.getCommand() != 0) {
                    dragon.setCommand(0);
                }
                if (dragon.level.isEmptyBlock(targetPos)) {
                    IafDragonBehaviorHelper.setDragonFlightTarget(dragon, Vec3.atCenterOf(targetPos));
                } else if (dragon.level.isEmptyBlock(targetPos.offset(0, 1, 0))) {
                    IafDragonBehaviorHelper.setDragonFlightTarget(dragon, Vec3.atCenterOf(targetPos.offset(0, 1, 0)));
                } else {
                    DragonTongue.LOGGER.warn("Dragon flight target set to non air block");
                }
            }
        });

        return true;
    }

    public static boolean keepDragonWalkTo(LivingEntity dragonIn, BlockPos targetPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));

        cap.getDestination().ifPresent(blockPos -> {
            if (util.hasArrived(dragon, blockPos, null)) {
                dragon.getNavigation().stop();
                dragon.setDeltaMovement(0, 0, 0);
                if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);
                } else {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.STAY);
                }
            } else {
                dragon.setInSittingPose(false);
                IafDragonBehaviorHelper.setDragonWalkTarget(dragon, targetPos);
            }
        });
        return true;
    }

    public static boolean keepDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        EnumCommandSettingType.BreathType breathType = (EnumCommandSettingType.BreathType) cap.getObjectSetting(EnumCommandSettingType.BREATH_TYPE);

//        dragon.groundAttack = IafDragonAttacks.GroundAttackType.FIRE;
//        dragon.usingGroundAttack = true;

        if (breathPos != null) {
            dragon.burningTarget = breathPos;
            dragon.getLookControl().setLookAt(breathPos.getX() + 0.5D, breathPos.getY() + 0.5D, breathPos.getZ() + 0.5D, 180F, 180F);
            dragon.yRot = dragon.yBodyRot;
            switch (breathType) {
                case ANY:
                    if (dragon.getDragonStage() >= 4 && dragon.getDragonStage() / 2.0f - 1.5 > dragon.getRandom().nextFloat()) {
                        dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 5);
                    }
                case WITHOUT_BLAST:
                    if (dragon.getDragonStage() >= 2) {
                        dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 1);
                        dragon.setBreathingFire(true);
                    }
                    break;
                case NONE:
                    cap.setBreathTarget(null);
                    dragon.setBreathingFire(false);
                    break;
            }
        } else {
            dragon.setBreathingFire(false);
        }
        return true;
    }

    public static boolean setDragonTryReach(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        boolean canFly = cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE) != EnumCommandSettingType.MovementType.LAND;

        if (blockPos != null) {
            if (shouldFlyToTarget(dragon, blockPos)) {
                setDragonTakeOff(dragon);
            }
            if (!isDragonInAir(dragon) || !shouldHoverAt(dragon, blockPos)) {
                setDragonWalkTarget(dragon, blockPos);
            }
            if (isDragonInAir(dragon) || shouldHoverAt(dragon, blockPos)) {
                setDragonFlightTarget(dragon, Vec3.atCenterOf(blockPos));
            }
        }
        return true;
    }

    /**
     * The took off dragon will switch its navigator type and use flight manager for movement control
     *
     * @param dragonIn
     * @return
     */
    public static boolean setDragonTakeOff(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        if (cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE) == EnumCommandSettingType.MovementType.LAND) {
            return false;
        }

        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            dragon.setDeltaMovement(dragon.getDeltaMovement().add(0, 0.02, 0));
        }
        dragon.setHovering(true);
        dragon.setInSittingPose(false);
        dragon.setOrderedToSit(false);
        dragon.flyTicks = 0;

        return true;
    }

    public static boolean setDragonLand(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        if (cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE) == EnumCommandSettingType.MovementType.AIR) {
            return false;
        }
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            dragon.setDeltaMovement(dragon.getDeltaMovement().add(0.0, -0.25, 0.0));
        }

        dragon.setFlying(false);

        return true;
    }

    public static boolean setDragonHalt(Mob dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder capTargetHolder = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));

        capTargetHolder.setDestination(dragon.blockPosition());
        // If no command was issued, do as the vanilla way
        if (capTargetHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            if (isDragonInAir(dragon)) {
                capTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);
            } else {
                capTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.STAY);
            }
        }
        capTargetHolder.setBreathTarget(null);

        dragon.setTarget(null);
//        setDragonFlightTarget(dragon, null);
        dragon.getNavigation().stop();

        return true;
    }

    public static boolean setDragonReach(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn));

        if (dragon.getCommand() == 1) {
            dragon.setCommand(2);
        }
        if (IafDragonBehaviorHelper.shouldFlyToTarget(dragon, blockPos)) {
            IafDragonBehaviorHelper.setDragonTakeOff(dragon);
        }

        dragon.setTarget(null);
        setDragonTryReach(dragon, blockPos);
        cap.setDestination(blockPos);
        cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);

        return true;
    }

    public static boolean setDragonAttackTarget(LivingEntity dragonIn, @Nullable LivingEntity target) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));

        if (target != null) {
            // If dragon was command 0, after attack IafAdvancedDragonLogic#254 will set to HOVER/STAY
            if (dragon.getCommand() == 1) {
                dragon.setCommand(2);
            }
            cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
            // One target at a time
            setDragonBreathTarget(dragon, null);
        }
        dragon.setTarget(target);

        return true;

    }

    public static boolean setDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));

        if (breathPos != null) {
            dragon.getLookControl().setLookAt(Vec3.atCenterOf(breathPos));
            cap.setBreathTarget(breathPos);
            cap.setDestination(dragon.blockPosition());
            if (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE) {
                if (isDragonInAir(dragon)) {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);
                } else {
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.STAY);
                }
            }
            // One target at a time
            setDragonAttackTarget(dragon, null);
        } else {
            cap.setBreathTarget(null);
            dragon.burningTarget = null;
            dragon.setBreathingFire(false);
        }

        return true;
    }

    public static boolean setDragonFlightTarget(LivingEntity dragonIn, Vec3 targetVec) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        if (targetVec != null) {
            dragon.flightManager.setFlightTarget(targetVec);
        }

        return true;
    }

    public static boolean setDragonWalkTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        if (blockPos != null) {
            ((AdvancedPathNavigate) dragon.getNavigation()).tryMoveToBlockPos(blockPos, 1.0f);
            if (dragon.isInWater()) {
                setDragonFlightTarget(dragon, Vec3.atBottomCenterOf(blockPos));
            }
        } else {
            dragon.getNavigation().stop();
        }

        return true;
    }

    public static boolean setDragonLook(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.getLookControl().setLookAt(Vec3.atCenterOf(blockPos));
        return true;
    }

    public static boolean setDragonCommandState(LivingEntity dragonIn, EnumCommandSettingType.CommandStatus status) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn)).setCommandStatus(status);
        return true;
    }

    public static boolean setDragonHover(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn));

        IafDragonBehaviorHelper.setDragonTakeOff(dragon);
        cap.setDestination(dragon.blockPosition());
        cap.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);

        return true;
    }


    public static boolean resurrectDragon(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        int deathStage = dragon.getDeathStage();

        if (!dragon.isModelDead() || dragon.isSkeletal()) {
            return false;
        }
        if (deathStage == 0) {
            cap.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
            cap.setDestination(null);

            dragon.setHealth((float) Math.ceil(dragon.getMaxHealth() / 20.0f));
            dragon.removeAllEffects();
            dragon.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
//        dragon.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
            dragon.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            dragon.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            dragon.level.broadcastEntityEvent(dragon, (byte) 35);

            dragon.setDeathStage(0);
            dragon.setModelDead(false);
            dragon.setNoAi(false);
            return true;
        } else if (deathStage > 0 && deathStage <= 2) {
//            util.spawnParticleForce(dragon.world, ParticleTypes.HAPPY_VILLAGER, )
            dragon.level.broadcastEntityEvent(dragon, (byte) 35);
            dragon.setDeathStage(deathStage - 1);
            return true;
        }
        return false;
    }

    /**
     * Called in ServerEvents#onLivingUpdate
     *
     * @param dragonIn
     * @return
     */
    public static boolean applyPatchedDragonLogic(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        if (!(dragon.logic instanceof IafAdvancedDragonLogic)) {
            IafAdvancedDragonLogic.applyDragonLogic(dragon);
        }
        if (!(dragon.flightManager instanceof IafAdvancedDragonFlightManager)) {
            IafAdvancedDragonFlightManager.applyDragonFlightManager(dragon);
        }
        return true;
    }

    public static boolean shouldHoverAt(EntityDragonBase dragon, BlockPos blockPos) {
        EnumCommandSettingType.MovementType movementType = (EnumCommandSettingType.MovementType) dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon)).getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE);
        if (movementType == EnumCommandSettingType.MovementType.LAND) {
            return false;
        }
        if (movementType == EnumCommandSettingType.MovementType.AIR) {
            return true;
        }
//        if (IafDragonBehaviorHelper.isOverAir(dragon)) {
//            return true;
//        }
        for (int i = 0; i < dragon.getDragonStage() + 1; i++) {
            if (!dragon.level.isEmptyBlock(blockPos.offset(0, -i, 0))) {
                return false;
            }
        }
        return true;

    }

    public static boolean shouldFlyToTarget(EntityDragonBase dragon, BlockPos blockPos) {
        EnumCommandSettingType.MovementType movementType = (EnumCommandSettingType.MovementType) dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon)).getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE);
        if (movementType == EnumCommandSettingType.MovementType.LAND) {
            return false;
        }
        if (movementType == EnumCommandSettingType.MovementType.AIR) {
            return true;
        }
        return shouldHoverAt(dragon, blockPos);
//        return shouldHoverAt(dragon, blockPos) || dragon.getDistanceSq(Vector3d.copyCentered(blockPos)) > 48 * 48;
    }

    public static boolean isDragonInAir(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        return dragon.isHovering() || dragon.isFlying();
    }

    public static boolean isOverAir(EntityDragonBase dragon) {
        return dragon.level.isEmptyBlock(new BlockPos(dragon.getX(), dragon.getBoundingBox().minY - 1, dragon.getZ()));
    }

    public static boolean shouldDestroy(LivingEntity dragonIn, BlockPos destroyPosition) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        EnumCommandSettingType.DestroyType destroyType = (EnumCommandSettingType.DestroyType) cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE);

        if (destroyType == EnumCommandSettingType.DestroyType.ANY
            || destroyType == EnumCommandSettingType.DestroyType.DELIBERATE) {
            return true;
        }
        if (destroyType == EnumCommandSettingType.DestroyType.CAREFUL_AROUND_ROOST) {
            if (!cap.getHomePosition().isPresent()) {
                return true;
            } else if (util.getDistance(cap.getHomePosition().get(), destroyPosition) > 64) { // Todo: replace 64 with a settable value
                return true;
            }
        }
        return false;
    }
}
