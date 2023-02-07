package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FollowCommandGoal extends Goal {
    MobEntity mobEntity;
    ICapabilityInfoHolder capabilityInfoHolder;
    public FollowCommandGoal(MobEntity entity) {
        this.mobEntity = entity;
        this.capabilityInfoHolder = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean shouldExecute() {
        return capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                && mobEntity.getAttackTarget() == null;
    }
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }
    @Override
    public void startExecuting() {

    }

    @Override
    public void tick() {
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                && mobEntity.getAttackTarget() == null) {
            capabilityInfoHolder.getDestination().ifPresent(blockPos -> {
                mobEntity.getNavigator().tryMoveToXYZ(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        1.1D
                );

            });
        }
    }
}
