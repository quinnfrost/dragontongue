package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.IDeadMob;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.message.MessageClientDraw;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IafHelperClass {
    public static boolean isDragon(Entity dragonIn) {
        return DragonTongue.isIafPresent && dragonIn instanceof EntityDragonBase;
    }

    public static EntityDragonBase getDragon(Entity dragonIn) {
        if (DragonTongue.isIafPresent) {
            if (dragonIn instanceof EntityDragonBase) {
                return (EntityDragonBase) dragonIn;
            }
            if (dragonIn instanceof EntityDragonPart && ((EntityDragonPart) dragonIn).getParent() != null) {
                return (EntityDragonBase) ((EntityDragonPart) dragonIn).getParent();
            }
        }
        return null;
    }

    public static double getFlightHeight(Entity dragonIn) {
        if (!isDragon(dragonIn)) {
            return 0;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dragon.getPosition());
        return dragon.getPosY() - ground.getY();
    }

    /**
     * Try to get an entity's target position
     *
     * @param entity
     * @return
     */
    public static BlockPos getReachTarget(MobEntity entity) {
        try {
            if (isDragon(entity)) {
                EntityDragonBase dragon = (EntityDragonBase) entity;
                AdvancedPathNavigate navigate = (AdvancedPathNavigate) dragon.getNavigator();

                if (navigate.getTargetPos() != null) {
                    return navigate.getTargetPos();
                } else if (navigate.getDestination() != null) {
                    return navigate.getDestination();
                } else if (navigate.getDesiredPos() != null) {
                    return navigate.getDesiredPos();
                } else {
                    return null;
                }

            } else if (entity.getNavigator().getTargetPos() != null) {
                return entity.getNavigator().getTargetPos();
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public static List<String> getAdditionalDragonDebugStrings(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return new ArrayList<>();
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        AdvancedPathNavigate navigator = (AdvancedPathNavigate) dragon.getNavigator();

        CompoundNBT compoundNBT = new CompoundNBT();
        DragonTongue.debugTarget.writeAdditional(compoundNBT);

        ICapTargetHolder capabilityInfoHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
        BlockPos targetPos = getReachTarget(dragon);

        float distX = (float) (dragon.flightManager.getFlightTarget().x - dragon.getPosX());
        float distY = (float) (dragon.flightManager.getFlightTarget().y - dragon.getPosY());
        float distZ = (float) (dragon.flightManager.getFlightTarget().z - dragon.getPosZ());

        return Arrays.asList(
                "Navigator target:" + (targetPos != null ? targetPos.getCoordinatesAsString() : ""),
                "FlightMgr:" + dragon.flightManager.getFlightTarget().toString() + "(" + util.getDistance(dragon.flightManager.getFlightTarget(), dragon.getPositionVec()) + ")",
                "NavType:" + dragon.navigatorType,
//                "Flying:" + compoundNBT.getByte("Flying"),
//                "HoverTicks:" + dragon.hoverTicks,
//                "TicksStill:" + dragon.ticksStill,
//                "LookVec:" + dragon.getLookVec(),
                "NoPath? " + dragon.getNavigator().noPath(),
                "Hovering:" + dragon.isHovering(),
                "Pitch: " + dragon.getDragonPitch() + "|" + dragon.rotationPitch,
                "Yaw: " + dragon.rotationYaw,
                "PlaneDist:" + (double) MathHelper.sqrt(distX * distX + distZ * distZ),
                "Distance:" + (double) MathHelper.sqrt(distX * distX + distZ * distZ + distY * distY),
                "AirAttack:" + dragon.airAttack,
                "GroundAttack:" + dragon.groundAttack,
                "UseGroundAttack? " + dragon.usingGroundAttack
        );


    }

    public static boolean drawDragonFlightDestination(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        RegistryMessages.sendToAll(new MessageClientDraw(
                dragon.getEntityId(), dragon.flightManager.getFlightTarget(),
                dragon.getPositionVec()
        ));
        return true;
    }

    public static boolean isIafHostile(LivingEntity livingEntity) {
        if (livingEntity instanceof IDeadMob || !DragonUtils.isAlive(livingEntity)) {
            return false;
        }
        if (livingEntity instanceof EntityDragonBase && ((EntityDragonBase)livingEntity).isModelDead()) {
            return false;
        }
        // Todo: what hostiles does iaf have?
        return livingEntity instanceof EntityDragonBase
                ;
    }

}
