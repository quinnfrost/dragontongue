package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class IafAdvancedDragonFlightManager extends IafDragonFlightManager {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    public Vec3 currentFlightTarget;
    public Vec3 finalFlightTarget;
    public FlightPhase flightPhase = FlightPhase.DIRECT;
    public Optional<Double> preferredFlightLevel = Optional.empty();
    public Vec3 flightLevel;
    private IafDragonAttacks.Air prevAirAttack;
    private Vec3 startAttackVec;
    private Vec3 startPreyVec;
    private boolean hasStartedToScorch = false;
    private LivingEntity prevAttackTarget = null;

    public enum FlightPhase {
        DETOUR,
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

    public static Vec3 getCurrentFlightTargetFor(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return null;
        }
        return ((IafAdvancedDragonFlightManager)((EntityDragonBase)dragonIn).flightManager).getFlightTarget();
    }

    public void updateFlightSensors() {

    }

    @Override
    public void update() {
        // Periodic check if the target is in sight
        if (finalFlightTarget != null) {
            if (dragon.getBoundingBox().inflate(dragon.getRenderSize()).contains(finalFlightTarget) || dragon.isInWater()) {
                flightPhase = FlightPhase.DIRECT;
            } else {
                flightLevel = Vec3.atBottomCenterOf(
                        IafDragonFlightUtil.getHighestPosOnPath(dragon, finalFlightTarget).offset(0, 0, 0)
                );
                if (!dragon.isTargetBlocked(finalFlightTarget)) {
                    if (util.getDistanceXZ(finalFlightTarget, dragon.position()) < 30 || flightLevel.y <= finalFlightTarget.y) {
                        flightPhase = FlightPhase.DIRECT;
                    } else {
                        flightPhase = FlightPhase.CRUISE;
                    }
                } else {
                    if (IafDragonFlightUtil.canAreaSeeSky(dragon.level, dragon.blockPosition(), dragon.getYNavSize())) {
                        flightPhase = FlightPhase.CLIMB;
                    } else {
                        flightPhase = FlightPhase.DETOUR;
                    }
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
                        preferredFlightLevel = Optional.of(flightLevel.y() + 2 * dragon.getYNavSize());
                    }
                    currentFlightTarget = new Vec3(flightLevel.x, preferredFlightLevel.orElse(flightLevel.y + 3 * dragon.getYNavSize()), flightLevel.z);
                    if (dragon.position().y > flightLevel.y + 2 * dragon.getYNavSize()) {
                        flightPhase = FlightPhase.CRUISE;
                    }
                    break;
                case CRUISE:
                    if (!preferredFlightLevel.isPresent()) {
                        preferredFlightLevel = Optional.of(flightLevel.y() + 2 * dragon.getYNavSize());
                    }
                    currentFlightTarget = new Vec3(finalFlightTarget.x, flightLevel.y + 2 * dragon.getYNavSize(), finalFlightTarget.z);
                    break;
                case DETOUR:
                    BlockPos skyPos = IafDragonFlightUtil.getSkyPosOnPath(dragon.level, dragon.position(), dragon.position().subtract(finalFlightTarget), 128f, dragon.getYNavSize());
                    if (skyPos != null) {
                        currentFlightTarget = Vec3.atBottomCenterOf(skyPos);
                        flightPhase = FlightPhase.DETOUR;
                        if (dragon.getY() > currentFlightTarget.y() || IafDragonFlightUtil.canAreaSeeSky(dragon.level, dragon.blockPosition(), dragon.getYNavSize())) {
                            flightPhase = FlightPhase.CLIMB;
                        }
                    } else {
                        // We couldn't handle this situation, maybe we're inside a cave or something
                        flightPhase = FlightPhase.DIRECT;
                    }
                    break;
            }
        }

        if (dragon.horizontalCollision) {
            if (flightLevel != null && flightLevel.y < IafConfig.maxDragonFlight) {
                flightLevel.add(0, dragon.getYNavSize(), 0);
            }
        }
        if (dragon.verticalCollision) {

        }

        // Attack related
        if (dragon.getTarget() != null && dragon.getTarget().isAlive()) {
            flightToAttackTarget();
//
        } else if (finalFlightTarget == null ||
                (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE
                        && dragon.distanceToSqr(currentFlightTarget.x, currentFlightTarget.y, currentFlightTarget.z) < 4)
                || !(dragon.level.isEmptyBlock(new BlockPos(finalFlightTarget))
                        || dragon.level.getBlockState(new BlockPos(finalFlightTarget).above()).getMaterial().isLiquid())
                && (dragon.isHovering() || dragon.isFlying())
                || dragon.getCommand() == 2 && dragon.shouldTPtoOwner()
        ) {
            flightToNewPosition();
        }

        // Ceil to max height
        if (currentFlightTarget != null) {
            if (currentFlightTarget.y > IafConfig.maxDragonFlight) {
                currentFlightTarget = new Vec3(currentFlightTarget.x, IafConfig.maxDragonFlight, currentFlightTarget.z);
            }
            if (currentFlightTarget.y >= dragon.getY() && !dragon.isModelDead() && !dragon.isInWater()) {
                dragon.setDeltaMovement(dragon.getDeltaMovement().add(0, 0.1D, 0));

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
        if (dragon.getCommand() == 2 && dragon.useFlyingPathFinder()) {
            if (dragon instanceof EntityIceDragon && dragon.isInWater()) {
                viewBlock = IafDragonFlightUtil.getWaterBlockInViewEscort(dragon);
            } else {
                viewBlock = IafDragonFlightUtil.getBlockInViewEscort(dragon);
            }
        } else if (dragon.lookingForRoostAIFlag) {
            double xDist = Math.abs(dragon.getX() - dragon.getRestrictCenter().getX() - 0.5F);
            double zDist = Math.abs(dragon.getZ() - dragon.getRestrictCenter().getZ() - 0.5F);
            double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);
            BlockPos upPos = dragon.getRestrictCenter();
            if (dragon.getDistanceSquared(Vec3.atCenterOf(dragon.getRestrictCenter())) > 200) {
                upPos = upPos.above(30);
            }
            viewBlock = upPos;

        } else if (viewBlock == null && dragon.getCommand() == 0 && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE) {
            viewBlock = IafDragonFlightUtil.getBlockInView(dragon);
        }
        if (viewBlock != null) {
            setFlightTarget(new Vec3(viewBlock.getX() + 0.5, viewBlock.getY() + 0.5, viewBlock.getZ() + 0.5));
        }
    }

    private void flightToAttackTarget() {
        // Ice dragon in water attack
        if (dragon instanceof EntityIceDragon && dragon.isInWater()) {
            if (dragon.getTarget() == null) {
                dragon.airAttack = IafDragonAttacks.Air.SCORCH_STREAM;
            } else {
                dragon.airAttack = IafDragonAttacks.Air.TACKLE;
            }
        }

        LivingEntity entity = dragon.getTarget();
        if (dragon.airAttack == IafDragonAttacks.Air.TACKLE) {
            setFlightTarget(new Vec3(entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ()));
        }
        if (dragon.airAttack == IafDragonAttacks.Air.HOVER_BLAST) {
            float distY = 5 + dragon.getDragonStage() * 2;
            int randomDist = 20;
            if (dragon.distanceToSqr(entity.getX(), dragon.getY(), entity.getZ()) < 16 || dragon.distanceToSqr(entity.getX(), dragon.getY(), entity.getZ()) > 900) {
                setFlightTarget(new Vec3(entity.getX() + dragon.getRandom().nextInt(randomDist) - randomDist / 2, entity.getY() + distY, entity.getZ() + dragon.getRandom().nextInt(randomDist) - randomDist / 2));
            }
            dragon.stimulateFire(entity.getX(), entity.getY(), entity.getZ(), 3);
        }
        if (dragon.airAttack == IafDragonAttacks.Air.SCORCH_STREAM && startPreyVec != null && startAttackVec != null) {
            float distX = (float) (startPreyVec.x - startAttackVec.x);
            float distY = 5 + dragon.getDragonStage() * 2;
            float distZ = (float) (startPreyVec.z - startAttackVec.z);
            setFlightTarget(new Vec3(entity.getX() + distX, entity.getY() + distY, entity.getZ() + distZ));
            dragon.tryScorchTarget();
            hasStartedToScorch = true;
            if (getFinalFlightTarget() != null && dragon.distanceToSqr(getFinalFlightTarget().x, getFinalFlightTarget().y, getFinalFlightTarget().z) < 100) {
                setFlightTarget(new Vec3(entity.getX() - distX, entity.getY() + distY, entity.getZ() - distZ));
            }
        }
    }

    @Nullable
    @Override
    public Vec3 getFlightTarget() {
        return currentFlightTarget;
    }

    public Vec3 getFinalFlightTarget() {
        return finalFlightTarget;
    }

    @Override
    public void setFlightTarget(@Nullable Vec3 target) {
        this.finalFlightTarget = target;
    }

    @Override
    public void onSetAttackTarget(@Nullable LivingEntity LivingEntityIn) {
        if (prevAttackTarget != LivingEntityIn) {
            if (LivingEntityIn != null) {
                startPreyVec = new Vec3(LivingEntityIn.getX(), LivingEntityIn.getY(), LivingEntityIn.getZ());
            } else {
                startPreyVec = new Vec3(dragon.getX(), dragon.getY(), dragon.getZ());
            }
            startAttackVec = new Vec3(dragon.getX(), dragon.getY(), dragon.getZ());
        }
        prevAttackTarget = LivingEntityIn;
    }

    public FlightPhase getFlightPhase() {
        return this.flightPhase;
    }

    public boolean isFlightTargetBlocked() {
        if (finalFlightTarget != null) {
            final BlockHitResult rayTrace = this.dragon.level.clip(new ClipContext(this.dragon.position().add(0, this.dragon.getEyeHeight(), 0), finalFlightTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.dragon));
            final BlockPos sidePos = rayTrace.getBlockPos();
            if (!this.dragon.level.isEmptyBlock(sidePos)) {
                return true;
            } else if (!this.dragon.level.isEmptyBlock(new BlockPos(rayTrace.getLocation()))) {
                return true;
            }
            return rayTrace.getType() == HitResult.Type.BLOCK;
        }
        return false;
    }

}
