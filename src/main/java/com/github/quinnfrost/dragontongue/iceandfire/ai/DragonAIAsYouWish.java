package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

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
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute() {
        return (
                capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                        && dragon.getAttackTarget() == null
        );
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
