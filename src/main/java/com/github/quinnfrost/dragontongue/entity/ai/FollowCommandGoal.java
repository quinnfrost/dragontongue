package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public class FollowCommandGoal extends Goal {
    MobEntity mobEntity;
    ICapTargetHolder capabilityInfoHolder;
    public FollowCommandGoal(MobEntity entity) {
        this.mobEntity = entity;
        this.capabilityInfoHolder = entity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(null);
    }

    @Override
    public boolean shouldExecute() {
        return capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE;
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
        mobEntity.getNavigator().tryMoveToXYZ(
                capabilityInfoHolder.getDestination().getX(),
                capabilityInfoHolder.getDestination().getY(),
                capabilityInfoHolder.getDestination().getZ(),
        1.1D
        );
    }
}
