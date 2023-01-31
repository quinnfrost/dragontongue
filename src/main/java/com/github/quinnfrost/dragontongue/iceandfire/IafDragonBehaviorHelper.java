package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIAsYouWish;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAICalmLook;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IafDragonBehaviorHelper {
    public static boolean registerDragonAI(MobEntity mobEntity) {
        if (IafHelperClass.isDragon(mobEntity)) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;

            dragon.goalSelector.addGoal(0, new DragonAIAsYouWish(dragon));
            dragon.goalSelector.addGoal(0, new DragonAICalmLook(dragon));
            return true;
        }
        return false;
    }

    public static boolean isDragonInAir(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        return dragon.isHovering() || dragon.isFlying();
    }

    public static boolean setDragonTakeOff(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setQueuedToSit(false);
        dragon.setSitting(false);
        dragon.flyTicks = 0;

        return true;
    }

    public static boolean setDragonLand(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;



        return true;
    }

    // fixme: halt cause dragon to takeoff
    public static boolean setDragonHalt(MobEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder capTargetHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));

        capTargetHolder.setDestination(dragon.getPosition());
        // If no command was issued, do as the vanilla way
        if (capTargetHolder.getCommandStatus() != EnumCommandStatus.NONE) {
            if (isDragonInAir(dragon)) {
                capTargetHolder.setCommandStatus(EnumCommandStatus.HOVER);
            } else {
                capTargetHolder.setCommandStatus(EnumCommandStatus.STAY);
            }
        }
        capTargetHolder.setBreathTarget(null);

        dragon.setAttackTarget(null);
//        dragon.flightManager.setFlightTarget(dragon.getPositionVec());
        // fixme: doesn't really clears the flying navigator's path
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean keepDragonHover(MobEntity dragonIn, BlockPos hoverPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder cap = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));

        dragon.setMotion(0, 0, 0);

        // fixme: setLookPosition took couple of seconds to take effect
        PlayerEntity playerEntity = dragon.world.getPlayerByUuid(dragon.getOwnerId());
        // Backoff looking control if the dragon is breathing
        if (playerEntity != null
                && !cap.getBreathTarget().isPresent()) {
            setDragonLook(dragon, playerEntity.getPosition());
//            dragon.setDragonPitch();
        }

        dragon.setHovering(true);
        dragon.setFlying(false);
        // Force set hover ticks to prevent dragon from flying triggered by dragon logic
        dragon.hoverTicks = 10;
        dragon.ticksStill = 10;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(hoverPos));
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean keepDragonStay(MobEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder iCapTargetHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));

        dragon.setHovering(false);
        dragon.setFlying(false);
        dragon.ticksStill = 10;
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonAttackTarget(LivingEntity dragonIn, @Nullable LivingEntity target) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
            if (target != null) {
                dragon.setCommand(2);
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.ATTACK);
                // One target at a time
                iCapTargetHolder.setBreathTarget(null);
            }
            dragon.setAttackTarget(target);
        });

        return true;

    }

    public static boolean setDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder cap = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));

        if (breathPos != null) {
            cap.setBreathTarget(breathPos);
            cap.setDestination(dragon.getPosition());
            if (cap.getCommandStatus() == EnumCommandStatus.NONE) {
                if (isDragonInAir(dragon)) {
                    cap.setCommandStatus(EnumCommandStatus.HOVER);
                } else {
                    cap.setCommandStatus(EnumCommandStatus.STAY);
                }
            }
            // One target at a time
            IafDragonBehaviorHelper.setDragonAttackTarget(dragon, null);
        }

        return true;
    }

    public static boolean keepDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

//        dragon.groundAttack = IafDragonAttacks.GroundAttackType.FIRE;
//        dragon.usingGroundAttack = true;

        if (breathPos != null) {
            dragon.burningTarget = breathPos;
            dragon.getLookController().setLookPosition(breathPos.getX() + 0.5D, breathPos.getY() + 0.5D, breathPos.getZ() + 0.5D, 180F, 180F);
            dragon.rotationYaw = dragon.renderYawOffset;
            if (dragon.getDragonStage() >= 4) {
                // SyncType=1 : no charge, SyncType=5 : with charge
                dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 5);
            } else if (dragon.getDragonStage() >= 2){
                dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 1);
            }
            dragon.setBreathingFire(true);
        } else {
            dragon.setBreathingFire(false);
        }
        return true;
    }

    // fixme: always takeoff
    public static boolean keepDragonReach(LivingEntity dragonIn, BlockPos targetPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder cap = dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn));

        if (util.hasArrived(dragon, cap.getDestination())) {
            dragon.getNavigator().clearPath();
            dragon.setMotion(0, 0, 0);
            // Determine if destination has support block
            if (IafDragonBehaviorHelper.shouldHover(dragon)) {
                cap.setCommandStatus(EnumCommandStatus.HOVER);
            } else {
                cap.setCommandStatus(EnumCommandStatus.STAY);
            }
        } else if (dragon.isFlying() || dragon.isHovering()) {
            IafDragonBehaviorHelper.setDragonFlightTarget(dragon, targetPos);
        } else {
            IafDragonBehaviorHelper.setDragonWalkTarget(dragon, targetPos);
        }
        return true;
    }

    public static boolean setDragonFlightTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(blockPos));

        return true;
    }

    public static boolean setDragonWalkTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        if (dragon.getCommand() != 0) {
            dragon.setCommand(0);
        }
        dragon.setQueuedToSit(false); // In case dragon is sleeping
        dragon.getNavigator().tryMoveToXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0f);

        return true;
    }

    public static boolean setDragonLook(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.getLookController().setLookPosition(Vector3d.copyCentered(blockPos));
        return true;
    }

    public static boolean setDragonCommandState(LivingEntity dragonIn, EnumCommandStatus status) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn)).setCommandStatus(status);
        return true;
    }

    public static boolean setDragonReach(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder cap = dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn));

        // If destination is too far, fly there
        if (blockPos.distanceSq(dragon.getPosition()) > 30 * 30) {
            IafDragonBehaviorHelper.setDragonTakeOff(dragon);
        }
        IafDragonBehaviorHelper.setDragonFlightTarget(dragon, blockPos);
        IafDragonBehaviorHelper.setDragonWalkTarget(dragon, blockPos);

        cap.setDestination(blockPos);
        cap.setCommandStatus(EnumCommandStatus.REACH);

        return true;
    }

    public static boolean setDragonHover(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder cap = dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn));

        IafDragonBehaviorHelper.setDragonTakeOff(dragon);
        cap.setDestination(dragon.getPosition());
        cap.setCommandStatus(EnumCommandStatus.HOVER);

        return true;
    }


    public static boolean resurrectDragon(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        int deathStage = dragon.getDeathStage();

        if (!dragon.isModelDead() || dragon.isSkeletal()) {
            return false;
        }
        if (deathStage == 0) {
            dragon.setHealth((float) Math.ceil(dragon.getMaxHealth() / 20.0f));
            dragon.clearActivePotions();
            dragon.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
//        dragon.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
            dragon.addPotionEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
            dragon.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 800, 0));
            dragon.world.setEntityState(dragon, (byte) 35);

            dragon.setDeathStage(0);
            dragon.setModelDead(false);
            dragon.setNoAI(false);
        } else if (deathStage > 0 && deathStage <= 2) {
//            util.spawnParticleForce(dragon.world, ParticleTypes.HAPPY_VILLAGER, )
            dragon.world.setEntityState(dragon, (byte) 35);
            dragon.setDeathStage(deathStage - 1);
        }
        return true;
    }

    /**
     * Called in ServerEvents#onLivingUpdate
     *
     * @param dragonIn
     * @return
     */
    public static boolean updateDragonCommand(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }

        return true;
    }

    public static boolean shouldHover(EntityDragonBase dragon) {
        ICapTargetHolder dragonCaps = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
        BlockPos targetPos = dragonCaps.getDestination();

        return (dragon.world.getBlockState(targetPos).isAir()
                && dragon.world.getBlockState(targetPos.add(0, -1, 0)).isAir());
    }
}
