package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonAICalmLook extends Goal {
    private final EntityDragonBase dragon;
    private final ICapabilityInfoHolder capabilityInfoHolder;

    public DragonAICalmLook(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn));
        this.setMutexFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        return capabilityInfoHolder.getCommandStatus() == EnumCommandSettingType.CommandStatus.STAY
                || capabilityInfoHolder.getCommandStatus() == EnumCommandSettingType.CommandStatus.HOVER;
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

    }
}
