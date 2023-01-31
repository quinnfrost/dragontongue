package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonLogic;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

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
        // Resets attack target if the target is dead
        if (dragon.getAttackTarget() != null && !dragon.getAttackTarget().isAlive()) {
            if (cap.getCommandStatus() == EnumCommandStatus.ATTACK) {
//                IafDragonBehaviorHelper.setDragonReach(dragon, dragon.getPosition());
                cap.setDestination(dragon.getPosition());
                if (dragon.isFlying() || dragon.isHovering()) {
                    cap.setCommandStatus(EnumCommandStatus.HOVER);
                } else {
                    cap.setCommandStatus(EnumCommandStatus.STAY);
                }
            }
            dragon.setAttackTarget(null);
        }
        // Breath to target if not empty
        cap.getBreathTarget().ifPresent(breathPos -> {
            dragon.setQueuedToSit(false); // In case dragon is sleeping
            IafDragonBehaviorHelper.keepDragonBreathTarget(dragon, breathPos);
            IafDragonBehaviorHelper.setDragonLook(dragon, breathPos);
        });

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
