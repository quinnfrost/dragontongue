package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class CalmLookGoal extends Goal {
    private final Mob mobEntity;
    private final ICapabilityInfoHolder capabilityInfoHolder;

    public CalmLookGoal(Mob entity) {
        this.mobEntity = entity;
        this.capabilityInfoHolder = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE;
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

    }

}
