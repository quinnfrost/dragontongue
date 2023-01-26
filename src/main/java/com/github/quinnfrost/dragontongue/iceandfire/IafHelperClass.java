package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIAsYouWish;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAICalmLook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IafHelperClass {
    public static boolean isDragon(Entity dragonIn) {
        return DragonTongue.isIafPresent && dragonIn instanceof EntityDragonBase;
    }

    public static boolean registerDragonAI(MobEntity mobEntity) {
        if (DragonTongue.isIafPresent && mobEntity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;

            dragon.goalSelector.addGoal(0, new DragonAIAsYouWish(dragon));
            dragon.goalSelector.addGoal(0, new DragonAICalmLook(dragon));
            return true;
        }
        return false;
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
        } else if (entity.getNavigator().getTargetPos() != null) {
            return entity.getNavigator().getTargetPos();
        }
        return null;
    }

    public static boolean setDragonTakeOff(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setQueuedToSit(false);
        dragon.setSitting(false);
        dragon.flyTicks = 0;

        return true;
    }

    public static boolean setDragonHover(MobEntity dragonIn, BlockPos hoverPos) {
        if (!isDragon(dragonIn)){return false;}
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
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapabilityInfoHolder iCapabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null);

        dragon.setHovering(false);
        dragon.setFlying(false);
//        dragon.ticksStill = 10;
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonAttackTarget(LivingEntity dragonIn, @Nullable LivingEntity target,@Nullable BlockPos breathPos) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        if (target == null && breathPos != null) {
            dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
                iCapabilityInfoHolder.setDestination(breathPos);
                iCapabilityInfoHolder.setCommandStatus(EnumCommandStatus.ATTACK);
            });
        } else {
            dragon.setAttackTarget(target);
        }
        return true;
    }

    public static boolean setDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        if (breathPos == null) {
            dragon.burningTarget = null;
            return true;
        }
//        dragon.groundAttack = IafDragonAttacks.Ground.FIRE;
//        dragon.usingGroundAttack = true;
        dragon.burningTarget = breathPos;
        dragon.getLookController().setLookPosition(breathPos.getX() + 0.5D, breathPos.getY() + 0.5D, breathPos.getZ() + 0.5D, 180F, 180F);
        dragon.rotationYaw = dragon.renderYawOffset;
        dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 1);
        dragon.setBreathingFire(true);
        return true;
    }

    public static boolean setDragonFlightTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!isDragon(dragonIn)){return false;}
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        double x = blockPos.getX();
        double y = blockPos.getY();
        double z = blockPos.getZ();

        dragon.flightManager.setFlightTarget(new Vector3d(x,y,z));
        dragon.getNavigator().tryMoveToXYZ(x,y,z, 1.1D);

        return true;
    }

    public static void setPetHalt(MobEntity mobEntity) {
        if (isDragon(mobEntity)) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;
            setDragonBreathTarget(dragon,null);
        }
        mobEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
            iCapabilityInfoHolder.setDestination(mobEntity.getPosition());
        });
        mobEntity.getNavigator().clearPath();
        mobEntity.setAttackTarget(null);
    }

    public static void setPetReach(MobEntity mobEntity, @Nullable BlockPos blockPos) {
        BlockPos pos = (blockPos != null ? blockPos : mobEntity.getPosition());
        mobEntity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).ifPresent(iCapabilityInfoHolder -> {
            iCapabilityInfoHolder.setDestination(pos);
            iCapabilityInfoHolder.setCommandStatus(EnumCommandStatus.REACH);
        });
        if (isDragon(mobEntity)) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;
            dragon.flightManager.setFlightTarget(Vector3d.copyCentered(pos));
            dragon.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
        } else {
            mobEntity.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
        }
    }

}
