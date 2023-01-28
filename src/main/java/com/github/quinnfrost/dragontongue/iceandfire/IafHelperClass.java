package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIAsYouWish;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAICalmLook;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IafHelperClass {
    public static boolean isDragon(Entity dragonIn) {
        return DragonTongue.isIafPresent && dragonIn instanceof EntityDragonBase;
    }

    public static boolean registerDragonAI(MobEntity mobEntity) {
        if (isDragon(mobEntity)) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;

            dragon.goalSelector.addGoal(0, new DragonAIAsYouWish(dragon));
            dragon.goalSelector.addGoal(0, new DragonAICalmLook(dragon));
            return true;
        }
        return false;
    }

    /**
     * Try to get an entity's target position
     *
     * @param entity
     * @return
     */
    public static BlockPos getReachTarget(MobEntity entity) {
        if (isDragon(entity)) {
            EntityDragonBase dragon = (EntityDragonBase) entity;
            AdvancedPathNavigate navigate = (AdvancedPathNavigate) dragon.getNavigator();
            try {
                if (navigate.getTargetPos() != null) {
                    return navigate.getTargetPos();
                } else if (navigate.getDestination() != null) {
                    return navigate.getDestination();
                } else if (navigate.getDesiredPos() != null) {
                    return navigate.getDesiredPos();
                } else {
                    return new BlockPos(dragon.flightManager.getFlightTarget());
                }
            } catch (Exception ignored) {

            }
        } else if (entity.getNavigator().getTargetPos() != null) {
            return entity.getNavigator().getTargetPos();
        }
        return null;
    }

    public static boolean setDragonTakeOff(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setQueuedToSit(false);
        dragon.setSitting(false);
        dragon.flyTicks = 0;

        return true;
    }

    public static boolean setDragonHover(MobEntity dragonIn, BlockPos hoverPos) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setFlying(false);
        // Force set hover ticks to prevent dragon from flying triggered by dragon logic
        dragon.hoverTicks = 10;
        dragon.ticksStill = 10;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(hoverPos));
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonStay(MobEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder iCapTargetHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(null);

        dragon.setHovering(false);
        dragon.setFlying(false);
//        dragon.ticksStill = 10;
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonAttackTarget(LivingEntity dragonIn, @Nullable LivingEntity target, @Nullable BlockPos breathPos) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        if (target == null && breathPos != null) {
            dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setDestination(breathPos);
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.ATTACK);
            });
        } else {
            dragon.setAttackTarget(target);
        }
        return true;

    }

    public static boolean setDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

//        dragon.groundAttack = IafDragonAttacks.Ground.FIRE;
//        dragon.usingGroundAttack = true;

        if (breathPos != null) {
            dragon.burningTarget = breathPos;
            dragon.getLookController().setLookPosition(breathPos.getX() + 0.5D, breathPos.getY() + 0.5D, breathPos.getZ() + 0.5D, 180F, 180F);
            dragon.rotationYaw = dragon.renderYawOffset;
            // SyncType=1 : no charge, SyncType=5 : with charge
            dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 5);
            dragon.setBreathingFire(true);
        } else {
            dragon.setBreathingFire(false);
        }
        return true;
    }

    public static boolean setDragonFlightTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(blockPos));

        return true;
    }

    public static List<String> getAdditionalDragonDebugStrings(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return new ArrayList<>();
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        CompoundNBT compoundNBT = new CompoundNBT();
        DragonTongue.debugTarget.writeAdditional(compoundNBT);

        ICapTargetHolder capabilityInfoHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(null);
        BlockPos targetPos = dragon.getNavigator().getTargetPos();

        return Arrays.asList(
                "Navigator target:" + getReachTarget(dragon),
                "FlightMgr:" + dragon.flightManager.getFlightTarget().toString() + "(" + util.getDistance(dragon.flightManager.getFlightTarget(), dragon.getPositionVec()) + ")",
                "NavType:" + String.valueOf(dragon.navigatorType),
                "Flying:" + compoundNBT.getByte("Flying"),
                "Hovering:" + dragon.isHovering(),
                "HoverTicks:" + dragon.hoverTicks,
                "TacklingTicks:" + dragon.tacklingTicks,
                "TicksStill:" + dragon.ticksStill
        );


    }

}
