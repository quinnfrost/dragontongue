package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IafTestClass {
    public static boolean isDragon(@Nullable Entity dragonIn) {
        return DragonTongue.isIafPresent && dragonIn instanceof EntityDragonBase;
    }

    /**
     * Try to get an entity's target position
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

            return new BlockPos(-1, -1, -1);
        } else if (entity.getNavigator().getTargetPos() != null) {
            return entity.getNavigator().getTargetPos();
        }
        return new BlockPos(-1, -1, -1);
    }

    public static boolean setDragonTakeOff(@Nullable LivingEntity dragonIn) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setQueuedToSit(false);
        dragon.setSitting(false);
        dragon.flyTicks = 0;

        return true;
    }

    public static boolean setDragonHover(MobEntity dragonIn) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder iCapabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null);

        dragon.setHovering(true);
        dragon.setFlying(false);
        dragon.hoverTicks = 10;
        dragon.ticksStill = 10;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(iCapabilityInfoHolder.getDestination()));
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonStay(MobEntity dragonIn) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder iCapabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null);

        dragon.setHovering(false);
        dragon.setFlying(false);
//        dragon.ticksStill = 10;
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonAttackTarget(LivingEntity dragonIn, @Nullable LivingEntity target) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.setAttackTarget(target);
        return true;
    }

    public static boolean setDragonFlightTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        return true;
    }

    public static void setPetReach(MobEntity entity, @Nullable BlockPos blockPos) {
        BlockPos pos = (blockPos != null ? blockPos : entity.getPosition());
        entity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
            iCapabilityInfoHolder.setDestination(pos);
            iCapabilityInfoHolder.setCommandStatus(EnumCommandStatus.REACH);
        });
        if (isDragon(entity)) {
            EntityDragonBase dragon = (EntityDragonBase) entity;
            dragon.flightManager.setFlightTarget(Vector3d.copyCentered(pos));
            dragon.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
        } else {
            entity.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
        }
    }

}
