package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import java.util.EnumSet;

public class FollowCommandAndAttackGoal extends MeleeAttackGoal {
    protected PathfinderMob creature;
    protected ICapabilityInfoHolder capabilityInfoHolder;

    public FollowCommandAndAttackGoal(PathfinderMob creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
        this.creature = creature;
        this.capabilityInfoHolder = creature.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(creature));
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    };

    @Override
    public boolean canUse() {
        if (super.canUse()) {
            return true;
        }
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public boolean canContinueToUse() {
        if (super.canContinueToUse()) {
            return true;
        }
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public void start() {
        if (creature.getTarget() != null) {
            super.start();
        }
    }

    @Override
    public void tick() {
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                && creature.getTarget() == null) {
            capabilityInfoHolder.getDestination().ifPresent(blockPos -> {
                creature.getNavigation().moveTo(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        1.1D
                );

            });
        }
        if (creature.getTarget() != null) {
            super.tick();
        }
    }
}
