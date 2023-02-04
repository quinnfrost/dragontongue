package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonAICalmLook extends Goal {
    private final EntityDragonBase dragon;
    private final ICapTargetHolder capabilityInfoHolder;

    public DragonAICalmLook(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn));
        this.setMutexFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        return true;
//        return capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE;
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
