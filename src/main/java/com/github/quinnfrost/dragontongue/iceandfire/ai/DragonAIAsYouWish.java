package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

public class DragonAIAsYouWish extends Goal {
    private final EntityDragonBase dragon;
    private final ICapTargetHolder capabilityInfoHolder;
    private LivingEntity attackTarget;
    private boolean isTargetAir;
    private BlockPos shouldStay;

    public DragonAIAsYouWish(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn));
        // 不能直接用shouldHover,因为在onEntityJoinWorld的时候区块似乎还没有加载，isAir会导致读取存档时永远等下去
        this.isTargetAir = (dragon.isFlying() || dragon.isHovering());
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute() {
        return (
                capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE
                        && dragon.getAttackTarget() == null
        );
    }

    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }

    @Override
    public void startExecuting() {
        BlockPos pos = capabilityInfoHolder.getDestination();
    }

    @Override
    public void tick() {

    }

    public boolean shouldHover(EntityDragonBase dragon) {
        BlockPos targetPos = capabilityInfoHolder.getDestination();

        return (dragon.world.getBlockState(targetPos).isAir()
                && dragon.world.getBlockState(targetPos.add(0, -1, 0)).isAir());
    }
}
