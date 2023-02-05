package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonCharge;
import com.github.alexthe666.iceandfire.entity.IafDragonLogic;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIGuard;
import com.github.quinnfrost.dragontongue.message.MessageSyncCapability;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
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
        ICapTargetHolder cap = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
        LivingEntity attackTarget = dragon.getAttackTarget();
        boolean isFlying = IafDragonBehaviorHelper.isDragonInAir(dragon);
        EnumCommandStatus commandStatus = cap.getCommandStatus();
        EnumCommandSettingType.MovementType movementType = (EnumCommandSettingType.MovementType) cap.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE);
        EnumCommandSettingType.AttackDecisionType attackDecision = (EnumCommandSettingType.AttackDecisionType) cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE);

        super.updateDragonServer();

        // At IafDragonLogic#320, dragon takes random chance to flight if she is idle on ground
        if (movementType != EnumCommandSettingType.MovementType.AIR && cap.getCommandStatus() == EnumCommandStatus.STAY) {
            // Prevent flying
            dragon.setHovering(false);
            dragon.setFlying(false);
        }
        // At IafDragonLogic#227, dragon's target is reset if she can't move, cause issue when commanding sit dragons to attack
        if (cap.getCommandStatus() == EnumCommandStatus.ATTACK && dragon.getAttackTarget() != null) {
            dragon.setAttackTarget(attackTarget);
        }

        // At IafDragonLogic#230, dragon's path is reset if she can't move
//        if (cap.getCommandStatus() == EnumCommandStatus.REACH && !dragon.canMove()) {
//            cap.getDestination().ifPresent(blockPos -> {
//                IafDragonBehaviorHelper.setDragonWalkTarget(dragon, blockPos);
//            });
//        }

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
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.NONE && dragon.getAttackTarget() != null) {
            dragon.setAttackTarget(null);
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.ALWAYS_HELP && dragon.getAttackTarget() != null) {
            cap.setCommandStatus(EnumCommandStatus.ATTACK);
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.GUARD && commandStatus != EnumCommandStatus.NONE) {
            if (dragon.getAttackTarget() == null) {
                cap.setCommandStatus(EnumCommandStatus.REACH);
            } else {
                cap.setCommandStatus(EnumCommandStatus.ATTACK);
            }
        }
        if (attackDecision == EnumCommandSettingType.AttackDecisionType.GUARD && dragon.isMovementBlocked() ) {
            if (DragonAIGuard.findNearestTarget(dragon) != null) {
                dragon.setQueuedToSit(false);
                if (dragon.getCommand() == 1) {
                    cap.setDestination(dragon.getPosition());
                    cap.setCommandStatus(EnumCommandStatus.REACH);
                    dragon.setCommand(0);
                }
            }
        }

        // Bug: lightning dragon won't wake up when command is set to escort
        if (dragon.getCommand() == 2 && !dragon.canMove()) {
            dragon.setQueuedToSit(false);
        }
        // Bug#4718: Stage 1 dragon can still trigger wander
        if (dragon.getDragonStage() == 1 && dragon.isPassenger() && dragon.getCommand() == 0) {
            dragon.getNavigator().clearPath();
            dragon.setCommand(1);
        }
        // Release control if the owner climbs up
        if (dragon.getControllingPassenger() != null) {
            if (cap.getCommandStatus() != EnumCommandStatus.NONE) {
                dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                    iCapTargetHolder.setCommandStatus(EnumCommandStatus.NONE);
                });
            }
            if (dragon.getCommand() == 0) {
                dragon.setCommand(2);
            }
            return;
        }
        // Vanilla takes over
        if (cap.getCommandStatus() == EnumCommandStatus.NONE) {
            cap.setBreathTarget(null);
            return;
        }


        // Resets attack target if the target is dead, vanilla behavior did this in the entity AI resetTask
        if ((dragon.getAttackTarget() != null && !dragon.getAttackTarget().isAlive())
                || (dragon.getAttackTarget() == null && cap.getCommandStatus() == EnumCommandStatus.ATTACK)) {
            cap.setDestination(dragon.getPosition());
            if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                cap.setCommandStatus(EnumCommandStatus.HOVER);
            } else {
                cap.setCommandStatus(EnumCommandStatus.STAY);
            }
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
