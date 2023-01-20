package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;

public class FollowCommandGoal extends Goal {
    MobEntity mobEntity;
    ICapabilityInfoHolder capabilityInfoHolder;
    public FollowCommandGoal(MobEntity entity) {
        this.mobEntity = entity;
        this.capabilityInfoHolder = entity.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null);
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
