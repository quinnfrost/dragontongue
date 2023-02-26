package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.props.MiscProperties;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.PathResult;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.quinnfrost.dragontongue.mixin.iceandfire.accessor.IEntityDragonAccess;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIGuard;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class IafAdvancedDragonLogic extends IafDragonLogic {
    private EntityDragonBase dragon;
    private IEntityDragonAccess iEntityDragon;

    public IafAdvancedDragonLogic(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
        this.iEntityDragon = (IEntityDragonAccess) dragon;
    }

    public static boolean applyDragonLogic(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.logic = new IafAdvancedDragonLogic(dragon);
        return true;
    }

    @Override
    public void updateDragonServer() {
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        LivingEntity attackTarget = dragon.getAttackTarget();
        boolean isFlying = IafDragonBehaviorHelper.isDragonInAir(dragon);
        EnumCommandSettingType.CommandStatus commandStatus = cap.getCommandStatus();
        EnumCommandSettingType.MovementType movementType = (EnumCommandSettingType.MovementType) cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE);
        EnumCommandSettingType.AttackDecisionType attackDecision = (EnumCommandSettingType.AttackDecisionType) cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE);
        EnumCommandSettingType.GroundAttackType groundAttackType = (EnumCommandSettingType.GroundAttackType) cap.getObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE);
        EnumCommandSettingType.AirAttackType airAttackType = (EnumCommandSettingType.AirAttackType) cap.getObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE);

        // Attack type changing
        switch (groundAttackType) {
            case ANY:
                break;
            case BITE:
                dragon.groundAttack = IafDragonAttacks.Ground.BITE;
                break;
            case SHAKE_PREY:
                dragon.groundAttack = IafDragonAttacks.Ground.SHAKE_PREY;
                break;
            case TAIL_WHIP:
                dragon.groundAttack = IafDragonAttacks.Ground.TAIL_WHIP;
                break;
            case WING_BLAST:
                dragon.groundAttack = IafDragonAttacks.Ground.WING_BLAST;
                break;
            case FIRE:
                dragon.groundAttack = IafDragonAttacks.Ground.FIRE;
                break;
            case NONE:
                // You need at least one attack type
                if (airAttackType != EnumCommandSettingType.AirAttackType.NONE) {
                    dragon.usingGroundAttack = false;
                }
                break;
        }
        switch (airAttackType) {
            case ANY:
                break;
            case SCORCH_STREAM:
                dragon.airAttack = IafDragonAttacks.Air.SCORCH_STREAM;
                break;
            case HOVER_BLAST:
                dragon.airAttack = IafDragonAttacks.Air.HOVER_BLAST;
                break;
            case TACKLE:
                dragon.airAttack = IafDragonAttacks.Air.TACKLE;
                break;
            case NONE:
                dragon.usingGroundAttack = true;
                break;
        }

        $updateDragonServer();

        // At IafDragonLogic#320, dragon takes random chance to flight if she is idle on ground
        if (movementType != EnumCommandSettingType.MovementType.AIR && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.STAY) {
            // Prevent flying
            dragon.setHovering(false);
            dragon.setFlying(false);
        }
        // At IafDragonLogic#227, dragon's target is reset if she can't move, cause issue when commanding sit dragons to attack
        if (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.ATTACK && dragon.getAttackTarget() != null && dragon.getAttackTarget() != attackTarget) {
            dragon.setAttackTarget(attackTarget);
        }

        // At IafDragonLogic#230, dragon's path is reset if she can't move
//        if (cap.getCommandStatus() == CommandStatus.REACH && !dragon.canMove()) {
//            cap.getDestination().ifPresent(blockPos -> {
//                IafDragonBehaviorHelper.setDragonWalkTarget(dragon, blockPos);
//            });
//        }

        // At IafDragonFlightManager$FlightMoveHelper#237, dragons will instantly turn around if she hit the wall
        // However this doesn't really help to solve the issue that she is behind a montain, so try to make her fly a little higher
        if (dragon.collidedHorizontally) {
            // This doesn't help
//            dragon.setMotion(dragon.getMotion().add(0,0.25,0));
        }


        // Do not sleep logic
        if (!cap.getShouldSleep()) {
            dragon.setQueuedToSit(false);
        }

        // Return to roost logic
        // Update home position if valid
        if (cap.getReturnHome()) {
            if (dragon.hasHomePosition) {
                // Vanilla behavior: return to roost
                // In LivingUpdateEvent, original Iaf dragon staff use event is hijacked to do the same plus invalidate
                // the home position in capability
                cap.setHomePosition(dragon.getHomePosition());
            } else {
                // Recover home pos
                // The home position optional must be invalidated manually when removing home position
                cap.getHomePosition().ifPresent(blockPos -> {
                    cap.getHomeDimension().ifPresent(homeDimensionName -> {
                        if (dragon.world.getDimensionKey().getLocation().toString().equals(homeDimensionName)) {
                            dragon.homePos = new HomePosition(blockPos, dragon.world);
                            dragon.hasHomePosition = true;
                            // Get up so she can return to roost
                            dragon.setQueuedToSit(false);
                        }
                    });
                });
            }
        } else {
            // Don't return to roost
            if (dragon.hasHomePosition) {
                cap.setHomePosition(dragon.homePos.getPosition());
                cap.setHomeDimension(dragon.homePos.getDimension());
                MessageSyncCapability.syncCapabilityToClients(dragon);
                // If dragon should not return to roost, invalidate roost pos
                dragon.hasHomePosition = false;
            }
        }

        // Do not breathe logic
        if (cap.getObjectSetting(EnumCommandSettingType.BREATH_TYPE) == EnumCommandSettingType.BreathType.NONE) {
            dragon.burnProgress = 0;
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_FIRECHARGE) {
                dragon.setAnimation(EntityDragonBase.NO_ANIMATION);
            }
            dragon.setBreathingFire(false);
            IafDragonBehaviorHelper.setDragonBreathTarget(dragon, null);
        } else if (cap.getObjectSetting(EnumCommandSettingType.BREATH_TYPE) == EnumCommandSettingType.BreathType.WITHOUT_BLAST) {
            List<Entity> entities = dragon.world.getEntitiesInAABBexcluding(dragon,
                    (new AxisAlignedBB(dragon.getHeadPosition().x, dragon.getHeadPosition().y, dragon.getHeadPosition().z,
                            dragon.getPosX() + 1.0d, dragon.getPosY() + 1.0d, dragon.getPosZ() + 1.0d)
                            .grow(2.0f)),
                    entityGet -> (entityGet instanceof EntityDragonCharge)
                            && (util.isShooter((ProjectileEntity) entityGet, dragon))
            );
            for (Entity charge :
                    entities) {
                charge.remove();
            }
        } else {
            // Vanilla behavior
        }

        // Do not fly/land
        if (movementType == EnumCommandSettingType.MovementType.LAND && dragon.getControllingPassenger() == null) {
            dragon.setHovering(false);
            dragon.setFlying(false);
            IafDragonBehaviorHelper.setDragonLand(dragon);
        }
        if (movementType == EnumCommandSettingType.MovementType.AIR && dragon.getControllingPassenger() == null) {
            IafDragonBehaviorHelper.setDragonTakeOff(dragon);
        }

        // Do not attack
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.DONT_HELP
                && (commandStatus != EnumCommandSettingType.CommandStatus.NONE && commandStatus != EnumCommandSettingType.CommandStatus.ATTACK)
                && dragon.getAttackTarget() != null) {
            dragon.setAttackTarget(null);
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.NONE && dragon.getAttackTarget() != null) {
            dragon.setAttackTarget(null);
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.DEFAULT) {

        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.ALWAYS_HELP) {
            if (dragon.isTamed() && dragon.getCommand() == 1) {
                LivingEntity owner = dragon.getOwner();
                // +1 for update may happen at next tick
                if (owner != null
                        && owner.getLastAttackedEntityTime() + 1 == owner.ticksExisted
                        && dragon.getAttackTarget() == null
                        && util.shouldAttack(dragon, owner.getLastAttackedEntity(), dragon.getAttributeValue(Attributes.FOLLOW_RANGE))
                ) {
                    cap.setDestination(dragon.getPosition());
                    dragon.setCommand(0);
                    dragon.setAttackTarget(owner.getLastAttackedEntity());
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
                }
            }
            if (dragon.getAttackTarget() != null &&
                    (commandStatus != EnumCommandSettingType.CommandStatus.NONE)) {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
            }
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.GUARD && commandStatus != EnumCommandSettingType.CommandStatus.NONE) {
            if (dragon.getAttackTarget() == null) {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
            } else {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
            }
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.GUARD && dragon.isMovementBlocked()) {
            if (DragonAIGuard.findNearestTarget(dragon) != null) {
                dragon.setQueuedToSit(false);
                if (dragon.getCommand() == 1) {
                    cap.setDestination(dragon.getPosition());
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
                    dragon.setCommand(0);
                }
            }
        }

        // Bug: In some circumstances elder dragons (125+) fail to sleep even navigator believes it has reached home.
        PathResult<AbstractPathJob> pathResult = ((IafAdvancedDragonPathNavigator) dragon.getNavigator()).pathResult;
        IafAdvancedDragonPathNavigator navigator = (IafAdvancedDragonPathNavigator) dragon.getNavigator();
        if (dragon.lookingForRoostAIFlag
//                && dragon.getDistanceSquared(Vector3d.copyCentered(dragon.getHomePosition())) < dragon.getWidth() * 12
        ) {
            if (navigator.noPath() && dragon.world.getGameTime() - navigator.pathStartTime < 10) {
                if (dragon.getPositionVec().distanceTo(Vector3d.copyCenteredHorizontally(dragon.getHomePosition())) < 20) {
                    if (!dragon.isInWater() && dragon.isOnGround() && !dragon.isFlying() && !dragon.isHovering() && dragon.getAttackTarget() == null) {
                        dragon.lookingForRoostAIFlag = false;
                        dragon.setQueuedToSit(true);
                    }
                } else if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                    IafDragonBehaviorHelper.setDragonTakeOff(dragon);
                }
            }
        }
        // Bug: lightning dragon won't wake up when command is set to escort
        if (dragon.getCommand() == 2 && !dragon.canMove()) {
            dragon.setQueuedToSit(false);
        }
        // Bug#4718: Stage 1 dragon can still trigger wander
//        if (dragon.getDragonStage() == 1 && dragon.isPassenger() && dragon.getCommand() == 0) {
//            dragon.setCommand(1);
//            dragon.getNavigator().clearPath();
//        }

        // Release control if the owner climbs up
        if (dragon.getControllingPassenger() != null) {
            if (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.STAY || cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.HOVER) {
                dragon.setCommand(1);
            } else if (dragon.getCommand() == 0) {
                dragon.setCommand(2);
            }
            dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
            });
            return;
        }

        // Vanilla takes over
        if (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE) {
            cap.setBreathTarget(null);
            return;
        }


        // Resets attack target if the target is dead, vanilla behavior did this in the entity AI resetTask
        if ((dragon.getAttackTarget() != null && !dragon.getAttackTarget().isAlive())
                || (dragon.getAttackTarget() == null && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.ATTACK)) {
            if (dragon.getCommand() == 2) {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
                cap.setDestination(null);
            } else {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
                if (!cap.getDestination().isPresent()) {
                    cap.setDestination(dragon.getPosition());
                }
            }
//            if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
//                cap.setCommandStatus(CommandStatus.HOVER);
//            } else {
//                cap.setCommandStatus(CommandStatus.STAY);
//            }
            dragon.setAttackTarget(null);
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
                dragon.setAnimation(IAnimatedEntity.NO_ANIMATION);
            }
        }
        // Breath to target if not empty
        if (cap.getBreathTarget().isPresent()) {
            BlockPos breathPos = cap.getBreathTarget().get();
            dragon.setQueuedToSit(false); // In case dragon is sleeping
            IafDragonBehaviorHelper.keepDragonBreathTarget(dragon, breathPos);
            IafDragonBehaviorHelper.setDragonLook(dragon, breathPos);
        } else if (dragon.getAttackTarget() == null) {
            // Maybe she decided to breathe herself when attacking
            dragon.setBreathingFire(false);
        }

        // The dragon on ground logic
        cap.getDestination().ifPresent(blockPos -> {
            if (IafDragonBehaviorHelper.shouldFlyToTarget(dragon, blockPos)) {
                IafDragonBehaviorHelper.setDragonTakeOff(dragon);
            }
            if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                switch (cap.getCommandStatus()) {
                    case REACH:
                        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                            IafDragonBehaviorHelper.keepDragonWalkTo(dragon, blockPos);
                        }
                        break;
                    case STAY:
                    case HOVER:
                        IafDragonBehaviorHelper.keepDragonStay(dragon);
                        break;

                }
            }
        });

        // The dragon in air following logic
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            switch (cap.getCommandStatus()) {
                case NONE:
                    break;
                case REACH:
                    cap.getDestination().ifPresent(blockPos -> {
                        IafDragonBehaviorHelper.keepDragonFlyTo(dragon, blockPos);
                    });
                    break;
                case STAY:
                case HOVER:
                    cap.getDestination().ifPresent(blockPos -> {
                        IafDragonBehaviorHelper.keepDragonHover(dragon, blockPos);
                    });
                    break;
            }
        }

    }

    public void $updateDragonServer() {
        PlayerEntity ridingPlayer = dragon.getRidingPlayer();
        if (ridingPlayer != null) {
            if (dragon.isGoingUp()) {
                if (!dragon.isFlying() && !dragon.isHovering()) {
                    dragon.spacebarTicks += 2;
                }
            } else if (dragon.isDismounting()) {
                if (dragon.isFlying() || dragon.isHovering()) {
                    dragon.setMotion(dragon.getMotion().add(0, -0.04, 0));
                    dragon.setFlying(false);
                    dragon.setHovering(false);
                }
            }
        }
        if (!dragon.isDismounting() && (dragon.isFlying() || dragon.isHovering())) {
            dragon.setMotion(dragon.getMotion().add(0, 0.01, 0));
        }
        if (dragon.isStriking() && dragon.getControllingPassenger() != null && dragon.getDragonStage() > 1) {
            dragon.setBreathingFire(true);
            dragon.riderShootFire(dragon.getControllingPassenger());
            dragon.fireStopTicks = 10;
        }
        if (dragon.isAttacking() && dragon.getControllingPassenger() != null && dragon.getControllingPassenger() instanceof PlayerEntity) {
            LivingEntity target = DragonUtils.riderLookingAtEntity(dragon, (PlayerEntity) dragon.getControllingPassenger(), dragon.getDragonStage() + (dragon.getBoundingBox().maxX - dragon.getBoundingBox().minX));
            if (dragon.getAnimation() != EntityDragonBase.ANIMATION_BITE) {
                dragon.setAnimation(EntityDragonBase.ANIMATION_BITE);
            }
            if (target != null && !DragonUtils.hasSameOwner(dragon, target)) {
                attackTarget(target, ridingPlayer, (int) dragon.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
            }
        }
        if (dragon.getControllingPassenger() != null && dragon.getControllingPassenger().isSneaking()) {
            if (dragon.getControllingPassenger() instanceof LivingEntity)
                MiscProperties.setDismountedDragon((LivingEntity) dragon.getControllingPassenger(), true);
            dragon.getControllingPassenger().stopRiding();
        }
        if (dragon.isFlying()) {
            if (!dragon.isHovering() && dragon.getControllingPassenger() != null && !dragon.isOnGround() && Math.max(Math.abs(dragon.getMotion().getX()), Math.abs(dragon.getMotion().getZ())) < 0.1F) {
                dragon.setHovering(true);
                dragon.setFlying(false);
            }
        } else {
            if (dragon.isHovering() && dragon.getControllingPassenger() != null && !dragon.isOnGround() && Math.max(Math.abs(dragon.getMotion().getX()), Math.abs(dragon.getMotion().getZ())) > 0.1F) {
                dragon.setFlying(true);
                dragon.usingGroundAttack = false;
                dragon.setHovering(false);
            }
        }
        if (dragon.spacebarTicks > 0) {
            dragon.spacebarTicks--;
        }
        if (dragon.spacebarTicks > 20 && dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner()) && !dragon.isFlying() && !dragon.isHovering()) {
            dragon.setHovering(true);
        }
        if (iEntityDragon.isOverAir$invoke() && !dragon.isPassenger()) {
            final double ydist = dragon.prevPosY - dragon.getPosY();//down 0.4 up -0.38
            if (!dragon.isHovering()) {
                dragon.incrementDragonPitch((float) (ydist) * 10);
            }
            dragon.setDragonPitch(MathHelper.clamp(dragon.getDragonPitch(), -60, 40));
            final float plateau = 2;
            final float planeDist = (float) ((Math.abs(dragon.getMotion().x) + Math.abs(dragon.getMotion().z)) * 6F);
            if (dragon.getDragonPitch() > plateau) {
                //down
                //this.motionY -= 0.2D;
                dragon.decrementDragonPitch(planeDist * Math.abs(dragon.getDragonPitch()) / 90);
            }
            if (dragon.getDragonPitch() < -plateau) {//-2
                //up
                dragon.incrementDragonPitch(planeDist * Math.abs(dragon.getDragonPitch()) / 90);
            }
            if (dragon.getDragonPitch() > 2F) {
                dragon.decrementDragonPitch(1);
            } else if (dragon.getDragonPitch() < -2F) {
                dragon.incrementDragonPitch(1);
            }
            if (dragon.getDragonPitch() < -45 && planeDist < 3) {
                if (dragon.isFlying() && !dragon.isHovering()) {
                    dragon.setHovering(true);
                }
            }
        } else {
            dragon.setDragonPitch(0);
        }
        if (dragon.lookingForRoostAIFlag && dragon.getRevengeTarget() != null || dragon.isSleeping()) {
            dragon.lookingForRoostAIFlag = false;
        }
        if (IafConfig.doDragonsSleep && !dragon.isSleeping() && !dragon.isTimeToWake() && dragon.getPassengers().isEmpty() && this.dragon.getCommand() != 2) {
            if (dragon.hasHomePosition
                    && dragon.getHomePosition() != null
                    && DragonUtils.isInHomeDimension(dragon)
                    && dragon.getDistanceSquared(Vector3d.copyCentered(dragon.getHomePosition())) > dragon.getWidth() * 10
                    && this.dragon.getCommand() != 2 && this.dragon.getCommand() != 1) {
                dragon.lookingForRoostAIFlag = true;
            } else {
                dragon.lookingForRoostAIFlag = false;
                if (!dragon.isInWater() && dragon.isOnGround() && !dragon.isFlying() && !dragon.isHovering() && dragon.getAttackTarget() == null) {
                    dragon.setQueuedToSit(true);
                }
            }
        } else {
            dragon.lookingForRoostAIFlag = false;
        }
        if (dragon.isSleeping() && (dragon.isFlying() || dragon.isHovering() || dragon.isInWater() || (dragon.world.canBlockSeeSky(dragon.getPosition()) && dragon.isTimeToWake() && !dragon.isTamed() || dragon.isTimeToWake() && dragon.isTamed()) || dragon.getAttackTarget() != null || !dragon.getPassengers().isEmpty())) {
            dragon.setQueuedToSit(false);
        }
        if (dragon.isQueuedToSit() && dragon.getControllingPassenger() != null) {
            dragon.setSitting(false);
        }
        if (dragon.isBeingRidden() && !iEntityDragon.isOverAir$invoke() && dragon.isFlying() && !dragon.isHovering() && dragon.flyTicks > 40) {
            dragon.setFlying(false);
        }
        if (iEntityDragon.getBlockBreakCounter() <= 0) {
            iEntityDragon.setBlockBreakCounter(IafConfig.dragonBreakBlockCooldown);
        }
        iEntityDragon.updateBurnTarget$invoke();
        if (dragon.isQueuedToSit()) {
            if (dragon.getCommand() != 1 || dragon.getControllingPassenger() != null)
                dragon.setSitting(false);
        } else {
            if (dragon.getCommand() == 1 && dragon.getControllingPassenger() == null)
                dragon.setSitting(true);
        }
        if (dragon.isQueuedToSit()) {
            dragon.getNavigator().clearPath();
        }
        if (dragon.isInLove()) {
            dragon.world.setEntityState(dragon, (byte) 18);
        }
        if ((int) dragon.prevPosX == (int) dragon.getPosX() && (int) dragon.prevPosZ == (int) dragon.getPosZ()) {
            dragon.ticksStill++;
        } else {
            dragon.ticksStill = 0;
        }
        if (dragon.isTackling() && !dragon.isFlying() && dragon.isOnGround()) {
            dragon.tacklingTicks++;
            if (dragon.tacklingTicks == 40) {
                dragon.tacklingTicks = 0;
                dragon.setTackling(false);
                dragon.setFlying(false);
            }
        }
        if (dragon.getRNG().nextInt(500) == 0 && !dragon.isModelDead() && !dragon.isSleeping()) {
            dragon.roar();
        }
        if (dragon.isFlying() && dragon.getAttackTarget() != null) {
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE)
                dragon.setTackling(true);

            if (dragon.isTackling()) {
                if (dragon.getBoundingBox().expand(2.0D, 2.0D, 2.0D).intersects(dragon.getAttackTarget().getBoundingBox())) {
                    dragon.usingGroundAttack = true;
                    dragon.randomizeAttacks();
                    attackTarget(dragon.getAttackTarget(), ridingPlayer, dragon.getDragonStage() * 3);
                    dragon.setFlying(false);
                    dragon.setHovering(false);
                }
            }
        }

        if (dragon.isTackling() && (dragon.getAttackTarget() == null || dragon.airAttack != IafDragonAttacks.Air.TACKLE)) {
            dragon.setTackling(false);
            dragon.randomizeAttacks();
        }
        if (dragon.isPassenger()) {
            dragon.setFlying(false);
            dragon.setHovering(false);
            dragon.setQueuedToSit(false);
        }
        if (dragon.isFlying() && dragon.ticksExisted % 40 == 0 || dragon.isFlying() && dragon.isSleeping()) {
            dragon.setQueuedToSit(false);
        }
        if (!dragon.canMove()) {
            if (dragon.getAttackTarget() != null) {
                dragon.setAttackTarget(null);
            }
            dragon.getNavigator().clearPath();
        }
        if (!dragon.isTamed()) {
            dragon.updateCheckPlayer();
        }
        if (dragon.isModelDead() && (dragon.isFlying() || dragon.isHovering())) {
            dragon.setFlying(false);
            dragon.setHovering(false);
        }
        if (ridingPlayer == null) {
            if ((dragon.useFlyingPathFinder() || dragon.isHovering()) && dragon.navigatorType != 1) {
                if (!dragon.getNavigator().noPath() && dragon.getNavigator().getTargetPos() != null) {
                    dragon.flightManager.setFlightTarget(Vector3d.copyCenteredHorizontally(dragon.getNavigator().getTargetPos()));
                }
                iEntityDragon.switchNavigator$invoke(1);
            }
        } else {
            if ((dragon.useFlyingPathFinder() || dragon.isHovering()) && dragon.navigatorType != 2) {
                iEntityDragon.switchNavigator$invoke(2);
            }
        }
        if (!dragon.useFlyingPathFinder() && !dragon.isHovering() && dragon.navigatorType != 0) {
            iEntityDragon.switchNavigator$invoke(0);
        }
        if (!iEntityDragon.isOverAir$invoke() && dragon.doesWantToLand() && (dragon.isFlying() || dragon.isHovering()) && !dragon.isInWater()) {
            dragon.setFlying(false);
            dragon.setHovering(false);
        }
        if (dragon.isHovering()) {
            if (dragon.isFlying() && dragon.flyTicks > 40) {
                dragon.setHovering(false);
                dragon.setFlying(true);
            }
            dragon.hoverTicks++;
        } else {
            dragon.hoverTicks = 0;
        }
        if (dragon.isHovering() && !dragon.isFlying()) {
            if (dragon.isSleeping()) {
                dragon.setHovering(false);
            }
            if (dragon.doesWantToLand() && !dragon.isOnGround() && !dragon.isInWater()) {
                dragon.setMotion(dragon.getMotion().add(0, -0.25, 0));
            } else {
                if ((dragon.getControllingPassenger() == null || dragon.getControllingPassenger() instanceof EntityDreadQueen) && !dragon.isBeyondHeight()) {
                    double up = dragon.isInWater() ? 0.12D : 0.08D;
                    dragon.setMotion(dragon.getMotion().add(0, up, 0));
                }
                if (dragon.hoverTicks > 40) {
                    dragon.setHovering(false);
                    dragon.setFlying(true);
                    iEntityDragon.setFlyHovering(0);
                    dragon.hoverTicks = 0;
                    dragon.flyTicks = 0;
                }
            }
        }
        if (dragon.isSleeping()) {
            dragon.getNavigator().clearPath();
        }
        if ((dragon.isOnGround() || dragon.isInWater()) && dragon.flyTicks != 0) {
            dragon.flyTicks = 0;
        }
        if (dragon.isAllowedToTriggerFlight() && dragon.isFlying() && dragon.doesWantToLand()) {
            dragon.setFlying(false);
            dragon.setHovering(iEntityDragon.isOverAir$invoke());
            if (!iEntityDragon.isOverAir$invoke()) {
                dragon.flyTicks = 0;
                dragon.setFlying(false);
            }
        }
        if (dragon.isFlying()) {
            dragon.flyTicks++;
        }
        if ((dragon.isHovering() || dragon.isFlying()) && dragon.isSleeping()) {
            dragon.setFlying(false);
            dragon.setHovering(false);
        }
        if (!dragon.isFlying() && !dragon.isHovering()) {
            if (dragon.isAllowedToTriggerFlight() || dragon.getPosY() < -1) {
                if (dragon.getRNG().nextInt(iEntityDragon.getFlightChancePerTick$invoke()) == 0 || dragon.getPosY() < -1 || dragon.getAttackTarget() != null && Math.abs(dragon.getAttackTarget().getPosY() - dragon.getPosY()) > 5 || dragon.isInWater() && !iEntityDragon.isIceInWater$invoke()) {
                    dragon.setHovering(true);
                    dragon.setQueuedToSit(false);
                    dragon.setSitting(false);
                    iEntityDragon.setFlyHovering(0);
                    dragon.hoverTicks = 0;
                    dragon.flyTicks = 0;
                }
            }
        }
        if (dragon.getAttackTarget() != null) {
            if (!dragon.getPassengers().isEmpty() && dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner())) {
                dragon.setAttackTarget(null);
            }
            if (!DragonUtils.isAlive(dragon.getAttackTarget())) {
                dragon.setAttackTarget(null);
            }
        }
        if (!dragon.isAgingDisabled()) {
            dragon.setAgeInTicks(dragon.getAgeInTicks() + 1);
            if (dragon.getAgeInTicks() % 24000 == 0) {
                iEntityDragon.updateAttributes$invoke();
                dragon.growDragon(0);
            }
        }
        if (dragon.ticksExisted % IafConfig.dragonHungerTickRate == 0 && IafConfig.dragonHungerTickRate > 0) {
            if (dragon.getHunger() > 0) {
                dragon.setHunger(dragon.getHunger() - 1);
            }
        }
        if ((dragon.groundAttack == IafDragonAttacks.Ground.FIRE) && dragon.getDragonStage() < 2) {
            dragon.usingGroundAttack = true;
            dragon.randomizeAttacks();
            dragon.playSound(dragon.getBabyFireSound(), 1, 1);
        }
        if (dragon.isBreathingFire()) {
            if (dragon.isSleeping() || dragon.isModelDead()) {
                dragon.setBreathingFire(false);
                dragon.randomizeAttacks();
                iEntityDragon.setFireTicks(0);
            }
            if (dragon.burningTarget == null) {
                if (iEntityDragon.getFireTicks() > dragon.getDragonStage() * 25 || dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner()) && dragon.fireStopTicks <= 0) {
                    dragon.setBreathingFire(false);
                    dragon.randomizeAttacks();
                    iEntityDragon.setFireTicks(0);
                }
            }

            if (dragon.fireStopTicks > 0 && dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner())) {
                dragon.fireStopTicks--;
            }
        }
        if (dragon.isFlying()) {
            if (dragon.getAttackTarget() != null && dragon.getBoundingBox().expand(3.0F, 3.0F, 3.0F).intersects(dragon.getAttackTarget().getBoundingBox())) {
                dragon.attackEntityAsMob(dragon.getAttackTarget());
            }
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE && (dragon.collidedHorizontally || dragon.isOnGround())) {
                dragon.usingGroundAttack = true;
                dragon.setFlying(false);
                dragon.setHovering(false);
            }
            if (dragon.usingGroundAttack) {
                dragon.airAttack = IafDragonAttacks.Air.TACKLE;
            }
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getAttackTarget() != null && dragon.isTargetBlocked(dragon.getAttackTarget().getPositionVec())) {
                dragon.randomizeAttacks();
            }
        }

    }

}
