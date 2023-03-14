package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class DragonAICalmLook extends Goal {
    private final EntityDragonBase dragon;
    private final ICapabilityInfoHolder capabilityInfoHolder;

    public DragonAICalmLook(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn));
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return capabilityInfoHolder.getCommandStatus() == EnumCommandSettingType.CommandStatus.STAY
                || capabilityInfoHolder.getCommandStatus() == EnumCommandSettingType.CommandStatus.HOVER;
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
