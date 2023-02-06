package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

import java.util.EnumSet;

public class FollowCommandAndAttackGoal extends MeleeAttackGoal {
    CreatureEntity creature;
    ICapTargetHolder capabilityInfoHolder;

    public FollowCommandAndAttackGoal(CreatureEntity creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
        this.creature = creature;
        this.capabilityInfoHolder = creature.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(creature));
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    };

    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
            return true;
        }
        if (capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD
                || capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public boolean shouldContinueExecuting() {
        if (super.shouldContinueExecuting()) {
            return true;
        }
        if (capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD
                || capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public void startExecuting() {
        if (creature.getAttackTarget() != null) {
            super.startExecuting();
        }
    }

    @Override
    public void tick() {
        // Todo: don't pathfind all the time
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE
                && creature.getAttackTarget() == null) {
            capabilityInfoHolder.getDestination().ifPresent(blockPos -> {
                creature.getNavigator().tryMoveToXYZ(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        1.1D
                );

            });
        }
        if (creature.getAttackTarget() != null) {
            super.tick();
        }
    }
}
