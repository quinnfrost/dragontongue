package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonCharge;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonLogic;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathResult;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIGuard;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import com.github.quinnfrost.dragontongue.access.IMixinAdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class IafAdvancedDragonLogic extends IafDragonLogic {
    private EntityDragonBase dragon;


    public IafAdvancedDragonLogic(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
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

        super.updateDragonServer();

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
        PathResult<AbstractPathJob> pathResult = ((IMixinAdvancedPathNavigate) dragon.getNavigator()).getPathResult();
        IafAdvancedDragonPathNavigator navigator = (IafAdvancedDragonPathNavigator) dragon.getNavigator();
        if (dragon.lookingForRoostAIFlag
//                && dragon.getDistanceSquared(Vector3d.copyCentered(dragon.getHomePosition())) < dragon.getWidth() * 12
        ) {
            if (navigator.noPath() && dragon.world.getGameTime() - ((IMixinAdvancedPathNavigate) navigator).getPathStartTime() < 10) {
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
        if (dragon.getDragonStage() == 1 && dragon.isPassenger() && dragon.getCommand() == 0) {
            dragon.setCommand(1);
            dragon.getNavigator().clearPath();
        }

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

}
