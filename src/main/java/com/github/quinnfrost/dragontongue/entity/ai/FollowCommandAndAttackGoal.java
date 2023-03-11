package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

import java.util.EnumSet;

public class FollowCommandAndAttackGoal extends MeleeAttackGoal {
    protected CreatureEntity creature;
    protected ICapabilityInfoHolder capabilityInfoHolder;

    public FollowCommandAndAttackGoal(CreatureEntity creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
        this.creature = creature;
        this.capabilityInfoHolder = creature.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(creature));
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    };

    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
            return true;
        }
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE)
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
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE)
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
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
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
