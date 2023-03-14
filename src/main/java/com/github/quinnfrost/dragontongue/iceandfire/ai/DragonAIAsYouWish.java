package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class DragonAIAsYouWish extends Goal {
    private final EntityDragonBase dragon;
    private final ICapabilityInfoHolder capabilityInfoHolder;
    private LivingEntity attackTarget;
    private boolean isTargetAir;
    private BlockPos shouldStay;

    public DragonAIAsYouWish(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragonIn));
        this.isTargetAir = (dragon.isFlying() || dragon.isHovering());
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return (
                capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                        && dragon.getTarget() == null
        );
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
