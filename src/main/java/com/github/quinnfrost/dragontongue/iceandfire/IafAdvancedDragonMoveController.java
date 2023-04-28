package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.alexthe666.iceandfire.entity.util.IFlyingMount;
import com.github.alexthe666.iceandfire.util.IAFMath;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;


public class IafAdvancedDragonMoveController {
    public static class GroundMoveHelper extends MoveControl {
        public GroundMoveHelper(Mob LivingEntityIn) {
            super(LivingEntityIn);
        }

        public float distance(float rotateAngleFrom, float rotateAngleTo) {
            return (float) IAFMath.atan2_accurate(Mth.sin(rotateAngleTo - rotateAngleFrom), Mth.cos(rotateAngleTo - rotateAngleFrom));
        }

        public void tick() {
            if (this.operation == Operation.STRAFE) {
                float f = (float) this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
                float f1 = (float) this.speedModifier * f;
                float f2 = this.strafeForwards;
                float f3 = this.strafeRight;
                float f4 = Mth.sqrt(f2 * f2 + f3 * f3);

                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }

                f4 = f1 / f4;
                f2 = f2 * f4;
                f3 = f3 * f4;
                float f5 = Mth.sin(this.mob.yRot * 0.017453292F);
                float f6 = Mth.cos(this.mob.yRot * 0.017453292F);
                float f7 = f2 * f6 - f3 * f5;
                float f8 = f3 * f6 + f2 * f5;
                PathNavigation pathnavigate = this.mob.getNavigation();
                if (pathnavigate != null) {
                    NodeEvaluator nodeprocessor = pathnavigate.getNodeEvaluator();
                    if (nodeprocessor != null && nodeprocessor.getBlockPathType(this.mob.level, Mth.floor(this.mob.getX() + (double) f7), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() + (double) f8)) != BlockPathTypes.WALKABLE) {
                        this.strafeForwards = 1.0F;
                        this.strafeRight = 0.0F;
                        f1 = f;
                    }
                }
                this.mob.setSpeed(f1);
                this.mob.setZza(this.strafeForwards);
                this.mob.setXxa(this.strafeRight);
                this.operation = Operation.WAIT;
            } else if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                EntityDragonBase dragonBase = (EntityDragonBase) mob;
                double d0 = this.getWantedX() - this.mob.getX();
                double d1 = this.getWantedZ() - this.mob.getZ();
                double d2 = this.getWantedY() - this.mob.getY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;

                if (d3 < 2.500000277905201E-7D) {
                    this.mob.setZza(0.0F);
                    return;
                }
                float targetDegree = (float) (Mth.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
                float changeRange = 70F;
                if (Math.ceil(dragonBase.getBbWidth()) > 2F) {
                    float ageMod = 1F - Math.min(dragonBase.getAgeInDays(), 125) / 125F;
                    changeRange = 5 + ageMod * 10;
                }
                this.mob.yRot = this.rotlerp(this.mob.yRot, targetDegree, changeRange);
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));
                if (d2 > (double) this.mob.maxUpStep && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getBbWidth() / 2)) {
                    this.mob.getJumpControl().jump();
                    this.operation = Operation.JUMPING;
                }
            } else if (this.operation == Operation.JUMPING) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));

                if (this.mob.isOnGround()) {
                    this.operation = Operation.WAIT;
                }
            } else {
                this.mob.setZza(0.0F);
            }
        }

    }

    public static class FlightMoveHelper extends MoveControl {

        private EntityDragonBase dragon;

        /*
        Detouring state
        0: no detour
        1: climbing
        2: flying over the terrain
         */
        private int detourState;
        private Vec3 detourTarget;

        public FlightMoveHelper(EntityDragonBase dragonBase) {
            super(dragonBase);
            this.dragon = dragonBase;

            detourState = 0;
            detourTarget = null;
        }

        public void tick() {
            if (dragon.flightManager.getFlightTarget() == null) {
                return;
            }

            ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl());
            EnumCommandSettingType.CommandStatus commandStatus = cap.getCommandStatus();
            IafAdvancedDragonFlightManager dragonFlightManager = (IafAdvancedDragonFlightManager) dragon.flightManager;
            Vec3 flightTarget = dragonFlightManager.getFlightTarget();

            if (dragon.horizontalCollision) {
                if (dragon.position().y < IafConfig.maxDragonFlight) {
                    dragon.setDeltaMovement(new Vec3(dragon.getDeltaMovement().scale(-0.5d).x, 0.2, dragon.getDeltaMovement().scale(-0.5d).y));
                    return;
                }
            }
            // Todo: 用AbstractPathJob#isPassableBB代替
            if (dragon.verticalCollision && !IafDragonFlightUtil.isAreaPassable(
                    dragon.level,
                    dragon.blockPosition().above((int) Math.ceil(dragon.getBoundingBox().getYsize())),
                    (int) dragon.getBoundingBox().getSize())) {
                dragon.setDeltaMovement(new Vec3(dragon.getDeltaMovement().scale(1d).x, -0.2, dragon.getDeltaMovement().scale(1d).y));
            }

            float distToX = (float) (flightTarget.x - dragon.getX());
            float distToY = (float) (flightTarget.y - dragon.getY());
            float distToZ = (float) (flightTarget.z - dragon.getZ());

            // Following logic makes dragon actually fly to the target, it's not touched except the name
            // The shortest possible distance to the target plane (parallel to y)
            double xzPlaneDist = Mth.sqrt(distToX * distToX + distToZ * distToZ);
            // f = 1 - |0.7 * Y| / sqrt(X^2+Y^2)
            double yDistMod = 1.0D - (double) Mth.abs(distToY * 0.7F) / xzPlaneDist;
            distToX = (float) ((double) distToX * yDistMod);
            distToZ = (float) ((double) distToZ * yDistMod);
            xzPlaneDist = Mth.sqrt(distToX * distToX + distToZ * distToZ);
            double distToTarget = Mth.sqrt(distToX * distToX + distToZ * distToZ + distToY * distToY);
            if (distToTarget > 1.0F) {
                float oldYaw = dragon.yRot;
                // Theta = atan2(y,x) - the angle of (x,y)
                float targetYaw = (float) Mth.atan2(distToZ, distToX);
                float currentYawTurn = Mth.wrapDegrees(dragon.yRot + 90);
                // Radian to degree
                float targetYawDegrees = Mth.wrapDegrees(targetYaw * 57.295776F);
                dragon.yRot = IafDragonFlightManager.approachDegrees(currentYawTurn, targetYawDegrees, dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getTarget() != null ? 10 : 4.0F) - 90.0F;
                dragon.yBodyRot = dragon.yRot;
                if (IafDragonFlightManager.degreesDifferenceAbs(oldYaw, dragon.yRot) < 3.0F) {
                    speedModifier = IafDragonFlightManager.approach((float) speedModifier, 1.8F, 0.005F * (1.8F / (float) speedModifier));
                } else {
                    speedModifier = IafDragonFlightManager.approach((float) speedModifier, 0.2F, 0.025F);
                    if (distToTarget < 100D && dragon.getTarget() != null) {
                        speedModifier = speedModifier * (distToTarget / 100D);
                    }
                }
                float finPitch = (float) (-(Mth.atan2(-distToY, xzPlaneDist) * 57.2957763671875D));
                dragon.xRot = finPitch;
                float yawTurnHead = dragon.yRot + 90.0F;
                speedModifier *= dragon.getFlightSpeedModifier();

                if (dragon.getCommand() == 2) {
                    speedModifier *= 1.5;
                }

                speedModifier *= dragonFlightManager.getFlightPhase() == IafAdvancedDragonFlightManager.FlightPhase.DIRECT
                        ? Math.min(1, distToTarget / 50 + 0.3)  //Make the dragon fly slower when close to target
                        : 1;    // Do not limit speed when detouring
                double lvt_16_1_ = speedModifier * Mth.cos(yawTurnHead * 0.017453292F) * Math.abs((double) distToX / distToTarget);
                double lvt_18_1_ = speedModifier * Mth.sin(yawTurnHead * 0.017453292F) * Math.abs((double) distToZ / distToTarget);
                double lvt_20_1_ = speedModifier * Mth.sin(finPitch * 0.017453292F) * Math.abs((double) distToY / distToTarget);
                double motionCap = 0.2D;
                dragon.setDeltaMovement(dragon.getDeltaMovement().add(Math.min(lvt_16_1_ * 0.2D, motionCap), Math.min(lvt_20_1_ * 0.2D, motionCap), Math.min(lvt_18_1_ * 0.2D, motionCap)));
            }

        }
    }

    public static class PlayerFlightMoveHelper<T extends Mob & IFlyingMount> extends MoveControl {

        private T dragon;

        public PlayerFlightMoveHelper(T dragon) {
            super(dragon);
            this.dragon = dragon;
        }

        @Override
        public void tick() {
            if (!dragon.isControlledByLocalInstance()) {
                double flySpeed = speedModifier * speedMod() * 3;
                Vec3 dragonVec = dragon.position();
                Vec3 moveVec = new Vec3(wantedX, wantedY, wantedZ);
                Vec3 normalized = moveVec.subtract(dragonVec).normalize();
                double dist = dragonVec.distanceTo(moveVec);
                dragon.setDeltaMovement(normalized.x * flySpeed, normalized.y * flySpeed, normalized.z * flySpeed);
                if (dist > 2.5E-7) {
                    float yaw = (float) Math.toDegrees(Math.PI * 2 - Math.atan2(normalized.x, normalized.y));
                    dragon.setYRot(rotlerp(dragon.getYRot(), yaw, 5));
                    dragon.setSpeed((float) (speedModifier));
                }
                dragon.move(MoverType.SELF, dragon.getDeltaMovement());
            }
        }

        public double speedMod() {
            return (dragon instanceof EntityAmphithere ? 0.75D : 0.5D) * IafConfig.dragonFlightSpeedMod;
        }
    }

    public static float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
        float f = Mth.wrapDegrees(pTargetAngle - pSourceAngle);
        if (f > pMaximumChange) {
            f = pMaximumChange;
        }

        if (f < -pMaximumChange) {
            f = -pMaximumChange;
        }

        float f1 = pSourceAngle + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }
}
