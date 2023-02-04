package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public class FollowCommandGoal extends Goal {
    MobEntity mobEntity;
    ICapTargetHolder capabilityInfoHolder;
    public FollowCommandGoal(MobEntity entity) {
        this.mobEntity = entity;
        this.capabilityInfoHolder = entity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(entity));
    }

    @Override
    public boolean shouldExecute() {
        return capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE
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
