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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IafAdvancedDragonLogic extends IafDragonLogic {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    private IEntityDragonAccess iEntityDragon;

    public IafAdvancedDragonLogic(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
        this.cap = ICapabilityInfoHolder.getCapability(dragon);
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
    public void updateDragonAttack() {
        Player ridingPlayer = dragon.getRidingPlayer();
        if (iEntityDragon.isPlayingAttackAnimation$invoke() && dragon.getTarget() != null && dragon.hasLineOfSight(dragon.getTarget())) {
            LivingEntity target = dragon.getTarget();
            final double dist = dragon.distanceTo(target);
            if (dist < dragon.getRenderSize() * 0.2574 * 2 + 2) {
                if (dragon.getAnimation() == EntityDragonBase.ANIMATION_BITE) {
                    if (dragon.getAnimationTick() > 15 && dragon.getAnimationTick() < 25) {
                        attackTarget(target, ridingPlayer, (int) dragon.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                        dragon.usingGroundAttack = dragon.getRandom().nextBoolean();
                        dragon.randomizeAttacks();
                    }
                } else if (dragon.getAnimation() == EntityDragonBase.ANIMATION_TAILWHACK) {
                    if (dragon.getAnimationTick() > 20 && dragon.getAnimationTick() < 30) {
                        attackTarget(target, ridingPlayer, (int) dragon.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                        target.knockback( dragon.getDragonStage() * 0.6F, Mth.sin(dragon.yRot * 0.017453292F), -Mth.cos(dragon.yRot * 0.017453292F));
                        dragon.usingGroundAttack = dragon.getRandom().nextBoolean();
                        dragon.randomizeAttacks();
                    }
                } else if (dragon.getAnimation() == EntityDragonBase.ANIMATION_WINGBLAST) {
                    if ((dragon.getAnimationTick() == 15 || dragon.getAnimationTick() == 25 || dragon.getAnimationTick() == 35)) {
                        attackTarget(target, ridingPlayer, (int) dragon.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                        target.knockback( dragon.getDragonStage() * 0.6F, Mth.sin(dragon.yRot * 0.017453292F), -Mth.cos(dragon.yRot * 0.017453292F));
                        dragon.usingGroundAttack = dragon.getRandom().nextBoolean();
                        dragon.randomizeAttacks();
                    }
                }
            }
        }
    }

    @Override
    public void updateDragonServer() {
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        LivingEntity attackTarget = dragon.getTarget();
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

        if (dragon.isDismounting()) {
            dragon.setNoGravity(false);
        }

//        $updateDragonServer();
        super.updateDragonServer();

        // At IafDragonLogic#320, dragon takes random chance to flight if she is idle on ground
        if (movementType != EnumCommandSettingType.MovementType.AIR && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.STAY) {
            // Prevent flying
            dragon.setHovering(false);
            dragon.setFlying(false);
        }
        // At IafDragonLogic#227, dragon's target is reset if she can't move, cause issue when commanding sit dragons to attack
        if (cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.ATTACK && dragon.getTarget() != null && dragon.getTarget() != attackTarget) {
            dragon.setTarget(attackTarget);
        }

        // At IafDragonLogic#230, dragon's path is reset if she can't move

        // At IafDragonFlightManager$FlightMoveHelper#237, dragons will instantly turn around if she hit the wall
        // However this doesn't really help to solve the issue that she is behind a montain, so try to make her fly a little higher


        // Do not sleep logic
        if (!cap.getShouldSleep()) {
            dragon.setInSittingPose(false);
        }

        // Return to roost logic
        // Update home position if valid
        if (cap.getReturnHome()) {
            if (dragon.hasHomePosition) {
                // Vanilla behavior: return to roost
                // In LivingUpdateEvent, original Iaf dragon staff use event is hijacked to do the same plus invalidate
                // the home position in capability
                cap.setHomePosition(dragon.getRestrictCenter());
            } else {
                // Recover home pos
                // The home position optional must be invalidated manually when removing home position
                cap.getHomePosition().ifPresent(blockPos -> {
                    cap.getHomeDimension().ifPresent(homeDimensionName -> {
                        if (dragon.level.dimension().location().toString().equals(homeDimensionName)) {
                            dragon.homePos = new HomePosition(blockPos, dragon.level);
                            dragon.hasHomePosition = true;
                            // Get up so she can return to roost
                            dragon.setInSittingPose(false);
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
            List<Entity> entities = dragon.level.getEntities(dragon,
                    (new AABB(dragon.getHeadPosition().x, dragon.getHeadPosition().y, dragon.getHeadPosition().z,
                            dragon.getX() + 1.0d, dragon.getY() + 1.0d, dragon.getZ() + 1.0d)
                            .inflate(2.0f)),
                    entityGet -> (entityGet instanceof EntityDragonCharge)
                            && (util.isShooter((Projectile) entityGet, dragon))
            );
            for (Entity charge :
                    entities) {
                charge.remove(Entity.RemovalReason.DISCARDED);
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
                && dragon.getTarget() != null) {
            dragon.setTarget(null);
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.NONE && dragon.getTarget() != null) {
            dragon.setTarget(null);
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.DEFAULT) {

        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.ALWAYS_HELP) {
            if (dragon.isTame() && dragon.getCommand() == 1) {
                LivingEntity owner = dragon.getOwner();
                // +1 for update may happen at next tick
                if (owner != null
                        && owner.getLastHurtMobTimestamp() + 1 == owner.tickCount
                        && dragon.getTarget() == null
                        && util.shouldAttack(dragon, owner.getLastHurtMob(), dragon.getAttributeValue(Attributes.FOLLOW_RANGE))
                ) {
                    cap.setDestination(dragon.blockPosition());
                    dragon.setCommand(0);
                    dragon.setTarget(owner.getLastHurtMob());
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
                }
            }
            if (dragon.getTarget() != null &&
                    (commandStatus != EnumCommandSettingType.CommandStatus.NONE)) {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
            }
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.GUARD && commandStatus != EnumCommandSettingType.CommandStatus.NONE) {
            if (dragon.getTarget() == null) {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
            } else {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
            }
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.GUARD && dragon.isImmobile()) {
            if (DragonAIGuard.findNearestTarget(dragon) != null) {
                dragon.setInSittingPose(false);
                if (dragon.getCommand() == 1) {
                    cap.setDestination(dragon.blockPosition());
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
                    dragon.setCommand(0);
                }
            }
        }

        // Bug: In some circumstances elder dragons (125+) fail to sleep even navigator believes it has reached home.
//        PathResult<AbstractPathJob> pathResult = ((IafAdvancedDragonPathNavigator) dragon.getNavigation()).pathResult;
//        IafAdvancedDragonPathNavigator navigator = (IafAdvancedDragonPathNavigator) dragon.getNavigation();
        if (dragon.lookingForRoostAIFlag
//                && dragon.getDistanceSquared(Vector3d.copyCentered(dragon.getHomePosition())) < dragon.getWidth() * 12
        ) {
            if (dragon.getNavigation().isDone()) {
                if (IafHelperClass.getXZDistanceSq(dragon.position(), Vec3.atBottomCenterOf(dragon.getRestrictCenter())) < 100) {
                    if (!dragon.isInWater() && dragon.isOnGround() && !dragon.isFlying() && !dragon.isHovering() && dragon.getTarget() == null) {
                        dragon.lookingForRoostAIFlag = false;
                        dragon.setInSittingPose(true);
                    }
                } else if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                    IafDragonBehaviorHelper.setDragonTakeOff(dragon);
                }
            }
        }
//        // Bug: lightning dragon won't wake up when command is set to escort
//        if (dragon.getCommand() == 2 && !dragon.canMove()) {
//            dragon.setInSittingPose(false);
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
        if ((dragon.getTarget() != null && !dragon.getTarget().isAlive())
                || (dragon.getTarget() == null && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.ATTACK)) {
            if (dragon.getCommand() == 2) {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
                cap.setDestination(null);
            } else {
                cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
                if (!cap.getDestination().isPresent()) {
                    cap.setDestination(dragon.blockPosition());
                }
            }
//            if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
//                cap.setCommandStatus(CommandStatus.HOVER);
//            } else {
//                cap.setCommandStatus(CommandStatus.STAY);
//            }
            dragon.setTarget(null);
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
                dragon.setAnimation(IAnimatedEntity.NO_ANIMATION);
            }
        }
        // Breath to target if not empty
        if (cap.getBreathTarget().isPresent()) {
            BlockPos breathPos = cap.getBreathTarget().get();
            dragon.setInSittingPose(false); // In case dragon is sleeping
            IafDragonBehaviorHelper.keepDragonBreathTarget(dragon, breathPos);
            IafDragonBehaviorHelper.setDragonLook(dragon, breathPos);
        } else if (dragon.getTarget() == null) {
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
        updateDragonRider();
        updateDragonPitch();

        Player ridingPlayer = dragon.getRidingPlayer();

        // Update look for roost AI flag
        if (dragon.lookingForRoostAIFlag && dragon.getLastHurtByMob() != null || dragon.isSleeping()) {
            dragon.lookingForRoostAIFlag = false;
        }
        if (IafConfig.doDragonsSleep && !dragon.isSleeping() && !dragon.isTimeToWake() && dragon.getPassengers().isEmpty() && this.dragon.getCommand() != 2) {
            if (dragon.hasHomePosition
                    && dragon.getRestrictCenter() != null
                    && DragonUtils.isInHomeDimension(dragon)
                    && dragon.getDistanceSquared(Vec3.atCenterOf(dragon.getRestrictCenter())) > dragon.getBbWidth() * 10
                    && this.dragon.getCommand() != 2 && this.dragon.getCommand() != 1) {
                dragon.lookingForRoostAIFlag = true;
            } else {
                dragon.lookingForRoostAIFlag = false;
                if (!dragon.isInWater() && dragon.isOnGround() && !dragon.isFlying() && !dragon.isHovering() && dragon.getTarget() == null) {
                    dragon.setInSittingPose(true);
                }
            }
        } else {
            dragon.lookingForRoostAIFlag = false;
        }

        // Conditions that should stand
        if (dragon.isSleeping() && (dragon.isFlying() || dragon.isHovering() || dragon.isInWater() || (dragon.level.canSeeSkyFromBelowWater(dragon.blockPosition()) && dragon.isTimeToWake() && !dragon.isTame() || dragon.isTimeToWake() && dragon.isTame()) || dragon.getTarget() != null || !dragon.getPassengers().isEmpty())) {
            dragon.setInSittingPose(false);
        }
        if (dragon.isOrderedToSit() && dragon.getControllingPassenger() != null) {
            dragon.setOrderedToSit(false);
        }
        // Land dragon for rider
        if (dragon.isVehicle() && !iEntityDragon.isOverAir$invoke() && dragon.isFlying() && !dragon.isHovering() && dragon.flyTicks > 40) {
            dragon.setFlying(false);
        }
        // Update block break cooldown. The bigger the value the stucker when dragon trying to break blocks.
        if (iEntityDragon.getBlockBreakCounter() <= 0) {
            iEntityDragon.setBlockBreakCounter(IafConfig.dragonBreakBlockCooldown);
        }
        // Update dragon burn target
        iEntityDragon.updateBurnTarget$invoke();
        // Conditions that should sit
        if (dragon.isOrderedToSit()) {
            // Stand when command is set or the rider climbs up.
            if (dragon.getCommand() != 1 || dragon.getControllingPassenger() != null)
                dragon.setOrderedToSit(false);
        } else {
            // Sit when command is set
            if (dragon.getCommand() == 1 && dragon.getControllingPassenger() == null)
                dragon.setOrderedToSit(true);
        }
        // When sat, don't move
        if (dragon.isOrderedToSit()) {
            dragon.getNavigation().stop();
        }
        // When aroused, send entity state package. 18 means mating (AnimalEntity#handleStatusUpdate).
        if (dragon.isInLove()) {
            dragon.level.broadcastEntityEvent(dragon, (byte) 18);
        }
        // Update still ticks
        if ((int) dragon.xo == (int) dragon.getX() && (int) dragon.zo == (int) dragon.getZ()) {
            dragon.ticksStill++;
        } else {
            dragon.ticksStill = 0;
        }
        // Update tackle ticks (the bite animation)
        if (dragon.isTackling() && !dragon.isFlying() && dragon.isOnGround()) {
            dragon.tacklingTicks++;
            if (dragon.tacklingTicks == 40) {
                dragon.tacklingTicks = 0;
                dragon.setTackling(false);
                dragon.setFlying(false);
            }
        }
        // Dragon random roar
        if (dragon.getRandom().nextInt(500) == 0 && !dragon.isModelDead() && !dragon.isSleeping()) {
            dragon.roar();
        }
        // Flying dragon attack logic
        if (dragon.isFlying() && dragon.getTarget() != null) {
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE) {
                dragon.setTackling(true);
            }
            // The flying bite attack
            if (dragon.isTackling()) {
                // If the target is within range, dealt the damage
                if (dragon.getBoundingBox().expandTowards(2.0D, 2.0D, 2.0D).intersects(dragon.getTarget().getBoundingBox())) {
                    dragon.usingGroundAttack = true;
                    dragon.randomizeAttacks();
                    attackTarget(dragon.getTarget(), ridingPlayer, dragon.getDragonStage() * 3);
                    dragon.setFlying(false);
                    dragon.setHovering(false);
                }
            }
        }
        // Conditions to cancel tackling
        if (dragon.isTackling() && (dragon.getTarget() == null || dragon.airAttack != IafDragonAttacks.Air.TACKLE)) {
            dragon.setTackling(false);
            dragon.randomizeAttacks();
        }
        // When dragon is riding some else (stage 1)
        if (dragon.isPassenger()) {
            dragon.setFlying(false);
            dragon.setHovering(false);
            dragon.setInSittingPose(false);
        }
        // Conditions to stand: the flying sleep?
        if (dragon.isFlying() && dragon.tickCount % 40 == 0 || dragon.isFlying() && dragon.isSleeping()) {
            dragon.setInSittingPose(false);
        }
        // If dragon can't move, remove attack target and path
        if (!dragon.canMove()) {
            if (dragon.getTarget() != null) {
                dragon.setTarget(null);
            }
            dragon.getNavigation().stop();
        }
        // For wild dragons
        if (!dragon.isTame()) {
            // For wild, sleeping dragons, wake up if any player approaches
            dragon.updateCheckPlayer();
        }
        // Deceased dragon shouldn't fly
        if (dragon.isModelDead() && (dragon.isFlying() || dragon.isHovering())) {
            dragon.setFlying(false);
            dragon.setHovering(false);
        }
        // Switch navigator based on conditions
        if (ridingPlayer == null) {
            // For airborne dragons, use flight navigator
            if ((dragon.useFlyingPathFinder() || dragon.isHovering()) && dragon.navigatorType != 1) {
                if (!dragon.getNavigation().isDone() && dragon.getNavigation().getTargetPos() != null) {
                    dragon.flightManager.setFlightTarget(Vec3.atBottomCenterOf(dragon.getNavigation().getTargetPos()));
                }
                iEntityDragon.switchNavigator$invoke(1);
            }
        } else {
            // For airborne with rider dragons, use player flight navigator
            if ((dragon.useFlyingPathFinder() || dragon.isHovering()) && dragon.navigatorType != 2) {
                iEntityDragon.switchNavigator$invoke(2);
            }
        }
        // Landed dragons use ground path navigator
        if (!dragon.useFlyingPathFinder() && !dragon.isHovering() && dragon.navigatorType != 0) {
            iEntityDragon.switchNavigator$invoke(0);
        }
        // For dragons that wants to land, land.
        if (!iEntityDragon.isOverAir$invoke() && dragon.doesWantToLand() && (dragon.isFlying() || dragon.isHovering()) && !dragon.isInWater()) {
            dragon.setFlying(false);
            dragon.setHovering(false);
        }
        // For hovering dragons, remove hovering state if we are flying
        if (dragon.isHovering()) {
            if (dragon.isFlying() && dragon.flyTicks > 40) {
                dragon.setHovering(false);
                dragon.setFlying(true);
            }
            dragon.hoverTicks++;
        } else {
            dragon.hoverTicks = 0;
        }
        // For hovering dragons
        if (dragon.isHovering() && !dragon.isFlying()) {
            // That is sleeping
            if (dragon.isSleeping()) {
                dragon.setHovering(false);
            }
            // That wants to land
            if (dragon.doesWantToLand() && !dragon.isOnGround() && !dragon.isInWater() && dragon.isControlledByLocalInstance()) {
                dragon.setDeltaMovement(dragon.getDeltaMovement().add(0, -0.25, 0));
            } else {
                // If rider onboard, try to neutralize the gravity
                if ((dragon.getControllingPassenger() == null || dragon.getControllingPassenger() instanceof EntityDreadQueen) && !dragon.isBeyondHeight()) {
//                    double up = dragon.isInWater() ? 0.12D : 0.08D;
//                    dragon.setDeltaMovement(dragon.getDeltaMovement().add(0, up, 0));
                }
                // Start flying after they have hovered for 2 secs
                if (dragon.hoverTicks > 40) {
                    dragon.setHovering(false);
                    dragon.setFlying(true);
                    iEntityDragon.setFlyHovering(0);
                    dragon.hoverTicks = 0;
                    dragon.flyTicks = 0;
                }
            }
        }
        // For sleeping dragons, don't move
        if (dragon.isSleeping()) {
            dragon.getNavigation().stop();
        }
        // Reset fly ticks when dragon is not flying
        if ((dragon.isOnGround() || dragon.isInWater()) && dragon.flyTicks != 0) {
            dragon.flyTicks = 0;
        }
        // When dragons wants to land but there is still space for flight, wait for land
        if (dragon.isAllowedToTriggerFlight() && dragon.isFlying() && dragon.doesWantToLand()) {
            dragon.setFlying(false);
            dragon.setHovering(iEntityDragon.isOverAir$invoke());
            if (!iEntityDragon.isOverAir$invoke()) {
                dragon.flyTicks = 0;
                dragon.setFlying(false);
            }
        }
        // Update flying ticks
        if (dragon.isFlying()) {
            dragon.flyTicks++;
        }
        // The flying sleeping dragons, let them land
        if ((dragon.isHovering() || dragon.isFlying()) && dragon.isSleeping()) {
            dragon.setFlying(false);
            dragon.setHovering(false);
        }
        // When dragon is landed, take off for, random chances, when below the world, target is running far, or in water
        if (!dragon.isFlying() && !dragon.isHovering()) {
            if (dragon.isAllowedToTriggerFlight() || dragon.getY() < -1) {
                if (dragon.getRandom().nextInt(iEntityDragon.getFlightChancePerTick$invoke()) == 0 && ICapabilityInfoHolder.getCapability(dragon).getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE && dragon.getCommand() == 0
                        || dragon.getY() < -1 || dragon.getTarget() != null && Math.abs(dragon.getTarget().getY() - dragon.getY()) > 5 || dragon.isInWater() && !iEntityDragon.isIceInWater$invoke()) {
                    dragon.setHovering(true);
                    dragon.setInSittingPose(false);
                    dragon.setOrderedToSit(false);
                    iEntityDragon.setFlyHovering(0);
                    dragon.hoverTicks = 0;
                    dragon.flyTicks = 0;
                }
            }
        }
        // Conditions that attack target should be invalidated
        if (dragon.getTarget() != null) {
            // When rider climbs up
            if (!dragon.getPassengers().isEmpty() && dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner())) {
                dragon.setTarget(null);
            }
            // When the target is dead
            if (!DragonUtils.isAlive(dragon.getTarget())) {
                dragon.setTarget(null);
            }
        }
        // Update dragon's age
        if (!dragon.isAgingDisabled()) {
            dragon.setAgeInTicks(dragon.getAgeInTicks() + 1);
            if (dragon.getAgeInTicks() % 24000 == 0) {
                iEntityDragon.updateAttributes$invoke();
                dragon.growDragon(0);
            }
        }
        // Update dragon's hunger
        if (dragon.tickCount % IafConfig.dragonHungerTickRate == 0 && IafConfig.dragonHungerTickRate > 0) {
            if (dragon.getHunger() > 0) {
                dragon.setHunger(dragon.getHunger() - 1);
            }
        }
        // For dragons that is too young to breath
        if ((dragon.groundAttack == IafDragonAttacks.Ground.FIRE) && dragon.getDragonStage() < 2) {
            dragon.usingGroundAttack = true;
            dragon.randomizeAttacks();
            dragon.playSound(dragon.getBabyFireSound(), 1, 1);
        }
        // Conditions that fire breathe should stop
        if (dragon.isBreathingFire()) {
            // The sleeping dragons should be quiet, so does the dead
            if (dragon.isSleeping() || dragon.isModelDead()) {
                dragon.setBreathingFire(false);
                dragon.randomizeAttacks();
                iEntityDragon.setFireTicks(0);
            }
            // If there is no breathing target, for example, rider commanded breathing
            if (dragon.burningTarget == null) {
                // We can't breathe continually for too long, reset after a while
                if (iEntityDragon.getFireTicks() > dragon.getDragonStage() * 25 || dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner()) && dragon.fireStopTicks <= 0) {
                    dragon.setBreathingFire(false);
                    dragon.randomizeAttacks();
                    iEntityDragon.setFireTicks(0);
                }
            }
            // Update fire stop ticks, for minimum breathe time (10 ticks)
            if (dragon.fireStopTicks > 0 && dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner())) {
                dragon.fireStopTicks--;
            }
        }
        // For flying dragon attacks
        if (dragon.isFlying()) {
            // Airborne strike, for those target within reach, dealt the damage
            if (dragon.getTarget() != null && dragon.getBoundingBox().expandTowards(3.0F, 3.0F, 3.0F).intersects(dragon.getTarget().getBoundingBox())) {
                dragon.doHurtTarget(dragon.getTarget());
            }
            // When airborne tackle is failed, switch to ground attack
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE && (dragon.horizontalCollision || dragon.isOnGround())) {
                dragon.usingGroundAttack = true;
                dragon.setFlying(false);
                dragon.setHovering(false);
            }

            // Set default air attack type since we're not using
            if (dragon.usingGroundAttack) {
                dragon.airAttack = IafDragonAttacks.Air.TACKLE;
            }
            // Change another attack type if our target is blocked
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getTarget() != null && dragon.isTargetBlocked(dragon.getTarget().position())) {
                dragon.randomizeAttacks();
            }
        }

    }

    public void updateDragonRider() {
        Player ridingPlayer = dragon.getRidingPlayer();
        // If there is a rider
        if (ridingPlayer != null) {
            // Update space bar pressed ticks
            if (dragon.isGoingUp()) {
                if (!dragon.isFlying() && !dragon.isHovering()) {
                    // 2 because we decrease this value by 1 every tick
                    dragon.spacebarTicks += 2;
                }
            }
            // When the rider dismounts, land immediately
            else if (dragon.isDismounting()) {
                if (dragon.isFlying() || dragon.isHovering()) {
                    if (dragon.getCommand() == 1) {
                        cap.setCommandStatus(EnumCommandSettingType.CommandStatus.HOVER);
                        cap.setDestination(dragon.blockPosition());
//                    dragon.setFlying(false);
//                    dragon.setHovering(false);
                    } else {
//                        dragon.setMotion(dragon.getMotion().add(0, -0.04, 0));
                    }
                }
            }
        }
        // When we're flying, counter the gravity?
        if (!dragon.isDismounting() && (dragon.isFlying() || dragon.isHovering())) {
            dragon.setDeltaMovement(dragon.getDeltaMovement().add(0, 0.01, 0));
        }
        // If the fire attack key is pressed
        if (dragon.isStriking() && dragon.getControllingPassenger() != null && dragon.getDragonStage() > 1) {
            dragon.setBreathingFire(true);
            dragon.riderShootFire(dragon.getControllingPassenger());
            dragon.fireStopTicks = 10;
        }
        // If the strike key is pressed
        if (dragon.isAttacking() && dragon.getControllingPassenger() != null && dragon.getControllingPassenger() instanceof Player) {
            LivingEntity target = DragonUtils.riderLookingAtEntity(dragon, (Player) dragon.getControllingPassenger(), dragon.getDragonStage() + (dragon.getBoundingBox().maxX - dragon.getBoundingBox().minX));
            if (dragon.getAnimation() != EntityDragonBase.ANIMATION_BITE) {
                dragon.setAnimation(EntityDragonBase.ANIMATION_BITE);
            }
            if (target != null && !DragonUtils.hasSameOwner(dragon, target)) {
                attackTarget(target, ridingPlayer, (int) dragon.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
            }
        }
        // If the sneak key is pressed
        if (dragon.getControllingPassenger() != null && dragon.getControllingPassenger().isShiftKeyDown()) {
            if (dragon.getControllingPassenger() instanceof LivingEntity)
                MiscProperties.setDismountedDragon((LivingEntity) dragon.getControllingPassenger(), true);
            dragon.getControllingPassenger().stopRiding();
        }
        // Conditions for hovering
        if (dragon.isFlying()) {
            if (!dragon.isHovering() && dragon.getControllingPassenger() != null && !dragon.isOnGround() && Math.max(Math.abs(dragon.getDeltaMovement().x()), Math.abs(dragon.getDeltaMovement().z())) < 0.1F) {
                dragon.setHovering(true);
                dragon.setFlying(false);
            }
        }
        // Conditions for flying
        else {
            if (dragon.isHovering() && dragon.getControllingPassenger() != null && !dragon.isOnGround() && Math.max(Math.abs(dragon.getDeltaMovement().x()), Math.abs(dragon.getDeltaMovement().z())) > 0.1F) {
                dragon.setFlying(true);
                dragon.usingGroundAttack = false;
                dragon.setHovering(false);
            }
        }
        // Update space bar ticks
        if (dragon.spacebarTicks > 0) {
            dragon.spacebarTicks--;
        }
        // Press space bar for more than 1 sec to take off
        if (dragon.spacebarTicks > 20 && dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner()) && !dragon.isFlying() && !dragon.isHovering()) {
            dragon.setHovering(true);
        }
    }

    private void updateDragonPitch() {
        // For dragons airborne
        if (iEntityDragon.isOverAir$invoke() && !dragon.isPassenger() && !dragon.isControlledByLocalInstance()) {
            final double ydist = dragon.yo - dragon.getY();//down 0.4 up -0.38
            // For flying dragons
            if (!dragon.isHovering()) {
                dragon.incrementDragonPitch((float) (ydist) * 10);
            }
            // Clamp the pitch into 40 ~ -60, 40 is the max for Y- (down), -60 is the max for Y+ (up)
            dragon.setDragonPitch(Mth.clamp(dragon.getDragonPitch(), -60, 40));
            // The plane flight range
            final float plateau = 2;
            final float speedXZ = (float) ((Math.abs(dragon.getDeltaMovement().x) + Math.abs(dragon.getDeltaMovement().z)) * 6F);
            // 0 ± 2 is considered as plane flight
            if (dragon.getDragonPitch() > plateau) {
                //down
                //this.motionY -= 0.2D;
                // Try to go back to plane flight
                dragon.decrementDragonPitch(speedXZ * Math.abs(dragon.getDragonPitch()) / 90);
            }
            if (dragon.getDragonPitch() < -plateau) {//-2
                //up
                // Try to go back to plane flight
                dragon.incrementDragonPitch(speedXZ * Math.abs(dragon.getDragonPitch()) / 90);
            }
            // Same as above?
            if (dragon.getDragonPitch() > 2F) {
                dragon.decrementDragonPitch(1);
            } else if (dragon.getDragonPitch() < -2F) {
                dragon.incrementDragonPitch(1);
            }
            // Climbing with slow speed is considered as hovering?
            if (dragon.getDragonPitch() < -45 && speedXZ < 3) {
                if (dragon.isFlying() && !dragon.isHovering()) {
                    dragon.setHovering(true);
                }
            }
        }
        // On ground, or on shoulder
        else {
            dragon.setDragonPitch(0);
        }
    }

}
