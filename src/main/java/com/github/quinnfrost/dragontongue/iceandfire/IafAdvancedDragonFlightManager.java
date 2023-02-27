package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Optional;

public class IafAdvancedDragonFlightManager extends IafDragonFlightManager {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    public Vector3d currentFlightTarget;
    public Vector3d finalFlightTarget;
    public FlightPhase flightPhase = FlightPhase.DIRECT;
    public Optional<Double> preferredFlightLevel = Optional.empty();
    public Vector3d flightLevel;
    private IafDragonAttacks.Air prevAirAttack;
    private Vector3d startAttackVec;
    private Vector3d startPreyVec;
    private boolean hasStartedToScorch = false;
    private LivingEntity prevAttackTarget = null;

    public enum FlightPhase {
        CLIMB,
        CRUISE,
        DIRECT
    }

    public IafAdvancedDragonFlightManager(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
    }

    public static boolean applyDragonFlightManager(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.flightManager = new IafAdvancedDragonFlightManager(dragon);
        return true;
    }

    @Override
    public void update() {
        // Periodic check if the target is in sight
        if (finalFlightTarget != null) {
            if (dragon.getBoundingBox().grow(dragon.getRenderSize()).contains(finalFlightTarget) || dragon.isInWater()) {
                flightPhase = FlightPhase.DIRECT;
            } else {
                flightLevel = Vector3d.copyCenteredHorizontally(
                        IafDragonFlightUtil.getHighestPosOnPath(dragon, finalFlightTarget).add(0, 0, 0)
                );
                if (!dragon.isTargetBlocked(finalFlightTarget)) {
                    if (util.getDistanceXZ(finalFlightTarget, dragon.getPositionVec()) < 30 || flightLevel.y <= finalFlightTarget.y) {
                        flightPhase = FlightPhase.DIRECT;
                    } else {
                        flightPhase = FlightPhase.CRUISE;
                    }
                } else {
                    flightPhase = FlightPhase.CLIMB;
                }
            }
            switch (flightPhase) {
                case DIRECT:
                    if (preferredFlightLevel.isPresent()) {
                        preferredFlightLevel = Optional.empty();
                    }
                    currentFlightTarget = finalFlightTarget;
                    break;
                case CLIMB:
                    if (!preferredFlightLevel.isPresent()) {
                        preferredFlightLevel = Optional.of(flightLevel.getY() + 2 * dragon.getYNavSize());
                    }
                    currentFlightTarget = new Vector3d(flightLevel.x, preferredFlightLevel.orElse(flightLevel.y + 3 * dragon.getYNavSize()), flightLevel.z);
                    if (dragon.getPositionVec().y > flightLevel.y + 2 * dragon.getYNavSize()) {
                        flightPhase = FlightPhase.CRUISE;
                    }
                    break;
                case CRUISE:
                    if (!preferredFlightLevel.isPresent()) {
                        preferredFlightLevel = Optional.of(flightLevel.getY() + 2 * dragon.getYNavSize());
                    }
                    currentFlightTarget = new Vector3d(finalFlightTarget.x, flightLevel.y + 2 * dragon.getYNavSize(), finalFlightTarget.z);
                    break;
            }
        }
        if (dragon.collidedHorizontally) {
            if (flightLevel != null && flightLevel.y < IafConfig.maxDragonFlight) {
                flightLevel.add(0, dragon.getYNavSize(), 0);
            }
        }

        // Attack related
        if (dragon.getAttackTarget() != null && dragon.getAttackTarget().isAlive()) {
            flightToAttackTarget();
//
        } else if (finalFlightTarget == null || (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE && dragon.getDistanceSq(currentFlightTarget.x, currentFlightTarget.y, currentFlightTarget.z) < 4) || !(dragon.world.isAirBlock(new BlockPos(finalFlightTarget)) || dragon.world.getBlockState(new BlockPos(finalFlightTarget).up()).getMaterial().isLiquid()) && (dragon.isHovering() || dragon.isFlying()) || dragon.getCommand() == 2 && dragon.shouldTPtoOwner()) {
            flightToNewPosition();
        }

        // Ceil to max height
        if (currentFlightTarget != null) {
            if (currentFlightTarget.y > IafConfig.maxDragonFlight) {
                currentFlightTarget = new Vector3d(currentFlightTarget.x, IafConfig.maxDragonFlight, currentFlightTarget.z);
            }
            if (currentFlightTarget.y >= dragon.getPosY() && !dragon.isModelDead() && !dragon.isInWater()) {
                dragon.setMotion(dragon.getMotion().add(0, 0.1D, 0));

            }
        }

        this.prevAirAttack = dragon.airAttack;


        // In DragonUtils#getBlockInView:65, dragon's flight height is a random value between 0~8, which is too little for elder dragons

        // In DragonUtils#getBlockInView, a random position is returned if dragon is airborne and nowhere to go
//        if (flightTarget != null && IafDragonBehaviorHelper.isDragonInAir(dragon)) {
//            switch (cap.getCommandStatus()) {
//                case REACH:
//                case STAY:
//                case HOVER:
//                    dragon.flightManager.setFlightTarget(flightTarget);
//                    break;
//            }
//        }
        // In IafDragonFlightManger#91, if flight target is on ground, a random position is used instead
        // This is solved by never set flight target to none air block

    }

    private void flightToNewPosition() {
        BlockPos viewBlock = null;

        if (dragon instanceof EntityIceDragon && dragon.isInWater()) {
            viewBlock = IafDragonFlightUtil.getWaterBlockInView(dragon);
        }
        if (dragon.getCommand() == 2 && dragon.isFlying()) {
            viewBlock = IafDragonFlightUtil.getBlockInViewEscort(dragon);
        } else if (dragon.lookingForRoostAIFlag) {
            double xDist = Math.abs(dragon.getPosX() - dragon.getHomePosition().getX() - 0.5F);
            double zDist = Math.abs(dragon.getPosZ() - dragon.getHomePosition().getZ() - 0.5F);
            double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);
            BlockPos upPos = dragon.getHomePosition();
            if (dragon.getDistanceSquared(Vector3d.copyCentered(dragon.getHomePosition())) > 200) {
                upPos = upPos.up(30);
            }
            viewBlock = upPos;

        } else if (viewBlock == null) {
            viewBlock = IafDragonFlightUtil.getBlockInView(dragon);
        }
        if (viewBlock != null) {
            setFlightTarget(new Vector3d(viewBlock.getX() + 0.5, viewBlock.getY() + 0.5, viewBlock.getZ() + 0.5));
        }
    }

    private void flightToAttackTarget() {
        // Ice dragon in water attack
        if (dragon instanceof EntityIceDragon && dragon.isInWater()) {
            if (dragon.getAttackTarget() == null) {
                dragon.airAttack = IafDragonAttacks.Air.SCORCH_STREAM;
            } else {
                dragon.airAttack = IafDragonAttacks.Air.TACKLE;
            }
        }

        LivingEntity entity = dragon.getAttackTarget();
        if (dragon.airAttack == IafDragonAttacks.Air.TACKLE) {
            setFlightTarget(new Vector3d(entity.getPosX(), entity.getPosY() + entity.getHeight(), entity.getPosZ()));
        }
        if (dragon.airAttack == IafDragonAttacks.Air.HOVER_BLAST) {
            float distY = 5 + dragon.getDragonStage() * 2;
            int randomDist = 20;
            if (dragon.getDistanceSq(entity.getPosX(), dragon.getPosY(), entity.getPosZ()) < 16 || dragon.getDistanceSq(entity.getPosX(), dragon.getPosY(), entity.getPosZ()) > 900) {
                setFlightTarget(new Vector3d(entity.getPosX() + dragon.getRNG().nextInt(randomDist) - randomDist / 2, entity.getPosY() + distY, entity.getPosZ() + dragon.getRNG().nextInt(randomDist) - randomDist / 2));
            }
            dragon.stimulateFire(entity.getPosX(), entity.getPosY(), entity.getPosZ(), 3);
        }
        if (dragon.airAttack == IafDragonAttacks.Air.SCORCH_STREAM && startPreyVec != null && startAttackVec != null) {
            float distX = (float) (startPreyVec.x - startAttackVec.x);
            float distY = 5 + dragon.getDragonStage() * 2;
            float distZ = (float) (startPreyVec.z - startAttackVec.z);
            setFlightTarget(new Vector3d(entity.getPosX() + distX, entity.getPosY() + distY, entity.getPosZ() + distZ));
            dragon.tryScorchTarget();
            hasStartedToScorch = true;
            if (getFinalFlightTarget() != null && dragon.getDistanceSq(getFinalFlightTarget().x, getFinalFlightTarget().y, getFinalFlightTarget().z) < 100) {
                setFlightTarget(new Vector3d(entity.getPosX() - distX, entity.getPosY() + distY, entity.getPosZ() - distZ));
            }
        }
    }

    @Nullable
    @Override
    public Vector3d getFlightTarget() {
        return currentFlightTarget;
    }

    public Vector3d getFinalFlightTarget() {
        return finalFlightTarget;
    }

    @Override
    public void setFlightTarget(@Nullable Vector3d target) {
        this.finalFlightTarget = target;
    }

    @Override
    public void onSetAttackTarget(@Nullable LivingEntity LivingEntityIn) {
        if (prevAttackTarget != LivingEntityIn) {
            if (LivingEntityIn != null) {
                startPreyVec = new Vector3d(LivingEntityIn.getPosX(), LivingEntityIn.getPosY(), LivingEntityIn.getPosZ());
            } else {
                startPreyVec = new Vector3d(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ());
            }
            startAttackVec = new Vector3d(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ());
        }
        prevAttackTarget = LivingEntityIn;
    }

    public FlightPhase getFlightPhase() {
        return this.flightPhase;
    }

    public boolean isFlightTargetBlocked() {
        if (finalFlightTarget != null) {
            final BlockRayTraceResult rayTrace = this.dragon.world.rayTraceBlocks(new RayTraceContext(this.dragon.getPositionVec().add(0, this.dragon.getEyeHeight(), 0), finalFlightTarget, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.dragon));
            final BlockPos sidePos = rayTrace.getPos();
            if (!this.dragon.world.isAirBlock(sidePos)) {
                return true;
            } else if (!this.dragon.world.isAirBlock(new BlockPos(rayTrace.getHitVec()))) {
                return true;
            }
            return rayTrace.getType() == RayTraceResult.Type.BLOCK;
        }
        return false;
    }

}
