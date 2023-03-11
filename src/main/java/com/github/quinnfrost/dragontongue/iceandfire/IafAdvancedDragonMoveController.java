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
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class IafAdvancedDragonMoveController {
    public static class GroundMoveHelper extends MovementController {
        public GroundMoveHelper(MobEntity LivingEntityIn) {
            super(LivingEntityIn);
        }

        public float distance(float rotateAngleFrom, float rotateAngleTo) {
            return (float) IAFMath.atan2_accurate(MathHelper.sin(rotateAngleTo - rotateAngleFrom), MathHelper.cos(rotateAngleTo - rotateAngleFrom));
        }

        public void tick() {
            if (this.action == Action.STRAFE) {
                float f = (float) this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
                float f1 = (float) this.speed * f;
                float f2 = this.moveForward;
                float f3 = this.moveStrafe;
                float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);

                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }

                f4 = f1 / f4;
                f2 = f2 * f4;
                f3 = f3 * f4;
                float f5 = MathHelper.sin(this.mob.rotationYaw * 0.017453292F);
                float f6 = MathHelper.cos(this.mob.rotationYaw * 0.017453292F);
                float f7 = f2 * f6 - f3 * f5;
                float f8 = f3 * f6 + f2 * f5;
                PathNavigator pathnavigate = this.mob.getNavigator();
                if (pathnavigate != null) {
                    NodeProcessor nodeprocessor = pathnavigate.getNodeProcessor();
                    if (nodeprocessor != null && nodeprocessor.getFloorNodeType(this.mob.world, MathHelper.floor(this.mob.getPosX() + (double) f7), MathHelper.floor(this.mob.getPosY()), MathHelper.floor(this.mob.getPosZ() + (double) f8)) != PathNodeType.WALKABLE) {
                        this.moveForward = 1.0F;
                        this.moveStrafe = 0.0F;
                        f1 = f;
                    }
                }
                this.mob.setAIMoveSpeed(f1);
                this.mob.setMoveForward(this.moveForward);
                this.mob.setMoveStrafing(this.moveStrafe);
                this.action = Action.WAIT;
            } else if (this.action == Action.MOVE_TO) {
                this.action = Action.WAIT;
                EntityDragonBase dragonBase = (EntityDragonBase) mob;
                double d0 = this.getX() - this.mob.getPosX();
                double d1 = this.getZ() - this.mob.getPosZ();
                double d2 = this.getY() - this.mob.getPosY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;

                if (d3 < 2.500000277905201E-7D) {
                    this.mob.setMoveForward(0.0F);
                    return;
                }
                float targetDegree = (float) (MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
                float changeRange = 70F;
                if (Math.ceil(dragonBase.getWidth()) > 2F) {
                    float ageMod = 1F - Math.min(dragonBase.getAgeInDays(), 125) / 125F;
                    changeRange = 5 + ageMod * 10;
                }
                this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, targetDegree, changeRange);
                this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));
                if (d2 > (double) this.mob.stepHeight && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getWidth() / 2)) {
                    this.mob.getJumpController().setJumping();
                    this.action = Action.JUMPING;
                }
            } else if (this.action == Action.JUMPING) {
                this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));

                if (this.mob.isOnGround()) {
                    this.action = Action.WAIT;
                }
            } else {
                this.mob.setMoveForward(0.0F);
            }
        }

    }

    public static class FlightMoveHelper extends MovementController {

        private EntityDragonBase dragon;

        /*
        Detouring state
        0: no detour
        1: climbing
        2: flying over the terrain
         */
        private int detourState;
        private Vector3d detourTarget;

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
            Vector3d flightTarget = dragonFlightManager.getFlightTarget();

            if (dragon.collidedHorizontally) {
                if (dragon.getPositionVec().y < IafConfig.maxDragonFlight) {
                    dragon.setMotion(new Vector3d(dragon.getMotion().scale(-0.5d).x, 0.2, dragon.getMotion().scale(-0.5d).y));
                    return;
                }
            }

            float distToX = (float) (flightTarget.x - dragon.getPosX());
            float distToY = (float) (flightTarget.y - dragon.getPosY());
            float distToZ = (float) (flightTarget.z - dragon.getPosZ());

            // Following logic makes dragon actually fly to the target, it's not touched except the name
            // The shortest possible distance to the target plane (parallel to y)
            double xzPlaneDist = MathHelper.sqrt(distToX * distToX + distToZ * distToZ);
            // f = 1 - |0.7 * Y| / sqrt(X^2+Y^2)
            double yDistMod = 1.0D - (double) MathHelper.abs(distToY * 0.7F) / xzPlaneDist;
            distToX = (float) ((double) distToX * yDistMod);
            distToZ = (float) ((double) distToZ * yDistMod);
            xzPlaneDist = MathHelper.sqrt(distToX * distToX + distToZ * distToZ);
            double distToTarget = MathHelper.sqrt(distToX * distToX + distToZ * distToZ + distToY * distToY);
            if (distToTarget > 1.0F) {
                float oldYaw = dragon.rotationYaw;
                // Theta = atan2(y,x) - the angle of (x,y)
                float targetYaw = (float) MathHelper.atan2(distToZ, distToX);
                float currentYawTurn = MathHelper.wrapDegrees(dragon.rotationYaw + 90);
                // Radian to degree
                float targetYawDegrees = MathHelper.wrapDegrees(targetYaw * 57.295776F);
                dragon.rotationYaw = IafDragonFlightManager.approachDegrees(currentYawTurn, targetYawDegrees, dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getAttackTarget() != null ? 10 : 4.0F) - 90.0F;
                dragon.renderYawOffset = dragon.rotationYaw;
                if (IafDragonFlightManager.degreesDifferenceAbs(oldYaw, dragon.rotationYaw) < 3.0F) {
                    speed = IafDragonFlightManager.approach((float) speed, 1.8F, 0.005F * (1.8F / (float) speed));
                } else {
                    speed = IafDragonFlightManager.approach((float) speed, 0.2F, 0.025F);
                    if (distToTarget < 100D && dragon.getAttackTarget() != null) {
                        speed = speed * (distToTarget / 100D);
                    }
                }
                float finPitch = (float) (-(MathHelper.atan2(-distToY, xzPlaneDist) * 57.2957763671875D));
                dragon.rotationPitch = finPitch;
                float yawTurnHead = dragon.rotationYaw + 90.0F;
                speed *= dragon.getFlightSpeedModifier();
                speed *= dragonFlightManager.getFlightPhase() == IafAdvancedDragonFlightManager.FlightPhase.DIRECT
                        ? Math.min(1, distToTarget / 50 + 0.3)  //Make the dragon fly slower when close to target
                        : 1;    // Do not limit speed when detouring
                double lvt_16_1_ = speed * MathHelper.cos(yawTurnHead * 0.017453292F) * Math.abs((double) distToX / distToTarget);
                double lvt_18_1_ = speed * MathHelper.sin(yawTurnHead * 0.017453292F) * Math.abs((double) distToZ / distToTarget);
                double lvt_20_1_ = speed * MathHelper.sin(finPitch * 0.017453292F) * Math.abs((double) distToY / distToTarget);
                double motionCap = 0.2D;
                dragon.setMotion(dragon.getMotion().add(Math.min(lvt_16_1_ * 0.2D, motionCap), Math.min(lvt_20_1_ * 0.2D, motionCap), Math.min(lvt_18_1_ * 0.2D, motionCap)));
            }

        }
    }

    public static class PlayerFlightMoveHelper<T extends MobEntity & IFlyingMount> extends MovementController {

        private T dragon;

        public PlayerFlightMoveHelper(T dragon) {
            super(dragon);
            this.dragon = dragon;
        }

        @Override
        public void tick() {
            double flySpeed = speed * speedMod();
            Vector3d dragonVec = dragon.getPositionVec();
            Vector3d moveVec = new Vector3d(posX, posY, posZ);
            Vector3d normalized = moveVec.subtract(dragonVec).normalize();
            double dist = dragonVec.distanceTo(moveVec);
            dragon.setMotion(normalized.x * flySpeed, normalized.y * flySpeed, normalized.z * flySpeed);
            if (dist > 2.5E-7) {
                float yaw = (float) Math.toDegrees(Math.PI * 2 - Math.atan2(normalized.x, normalized.y));
                dragon.rotationYaw = limitAngle(dragon.rotationYaw, yaw, 5);
                dragon.setAIMoveSpeed((float) (speed));
            }
            dragon.move(MoverType.SELF, dragon.getMotion());
        }

        public double speedMod() {
            return (dragon instanceof EntityAmphithere ? 0.75D : 0.5D) * IafConfig.dragonFlightSpeedMod;
        }
    }
}
