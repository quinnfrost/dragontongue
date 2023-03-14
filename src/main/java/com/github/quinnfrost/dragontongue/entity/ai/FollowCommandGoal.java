package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FollowCommandGoal extends Goal {
    Mob mobEntity;
    ICapabilityInfoHolder capabilityInfoHolder;
    public FollowCommandGoal(Mob entity) {
        this.mobEntity = entity;
        this.capabilityInfoHolder = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                && mobEntity.getTarget() == null;
    }
    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
    @Override
    public void start() {

    }

    @Override
    public void tick() {
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                && mobEntity.getTarget() == null) {
            capabilityInfoHolder.getDestination().ifPresent(blockPos -> {
                mobEntity.getNavigation().moveTo(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        1.1D
                );

            });
        }
    }
}
