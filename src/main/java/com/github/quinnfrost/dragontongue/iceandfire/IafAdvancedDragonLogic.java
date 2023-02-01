package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonCharge;
import com.github.alexthe666.iceandfire.entity.IafDragonLogic;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Predicate;

public class IafAdvancedDragonLogic extends IafDragonLogic {
    private EntityDragonBase dragon;

    public IafAdvancedDragonLogic(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
    }

    @Override
    public void updateDragonServer() {
        ICapTargetHolder cap = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
        // At IafDragonLogic#227, dragon's target is reset if she can't move, cause issue when commanding sit dragons to attack
        if (dragon.getAttackTarget() != null && cap.getCommandStatus() == EnumCommandStatus.ATTACK) {
            LivingEntity attackTarget = dragon.getAttackTarget();
            super.updateDragonServer();
            dragon.setAttackTarget(attackTarget);
        } else {
            super.updateDragonServer();
        }

        // Return to roost logic
        // Update home position if valid
        if (cap.getReturnHome()) {
            if (dragon.hasHomePosition) {
                // Vanilla behavior: return to roost
                // In LivingUpdateEvent, original Iaf dragon staff use event is hijacked to do the same plus invalidate
                // the home position in capability
//                cap.setHomePosition(dragon.getHomePosition());
            } else {
                // Recover home pos
                cap.getHomePosition().ifPresent(blockPos -> {
                    dragon.homePos = new HomePosition(blockPos, dragon.world);
                    dragon.hasHomePosition = true;
                    // Get up so she can return to roost
                    dragon.setQueuedToSit(false);
                });
            }
        } else {
            // Don't return to roost
            if (dragon.hasHomePosition) {
                cap.setHomePosition(dragon.homePos.getPosition());
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

        // Resets everything to vanilla
        if (cap.getCommandStatus() == EnumCommandStatus.NONE) {
            cap.setBreathTarget(null);
            return;
        }

        BlockPos targetPos = cap.getDestination();

        // Release control if the owner climbs up
        if (dragon.isOnePlayerRiding() && cap.getCommandStatus() != EnumCommandStatus.REACH) {
            dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.NONE);
            });
            dragon.setCommand(2);
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
        }
        // Breath to target if not empty
        if (cap.getBreathTarget().isPresent()) {
            BlockPos breathPos = cap.getBreathTarget().get();
            dragon.setQueuedToSit(false); // In case dragon is sleeping
            IafDragonBehaviorHelper.keepDragonBreathTarget(dragon, breathPos);
            IafDragonBehaviorHelper.setDragonLook(dragon, breathPos);
        } else {
            dragon.setBreathingFire(false);
        }

        switch (cap.getCommandStatus()) {
            case REACH:
                IafDragonBehaviorHelper.keepDragonReach(dragon, targetPos);
                break;
            case STAY:
                IafDragonBehaviorHelper.keepDragonStay(dragon);
                break;
            case HOVER:
                IafDragonBehaviorHelper.keepDragonHover(dragon, targetPos);
                break;
            case ATTACK:
                break;
            case BREATH:
                break;
        }

    }

    public static boolean applyDragonLogic(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.logic = new IafAdvancedDragonLogic(dragon);
        return true;
    }
}
