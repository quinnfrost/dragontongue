package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Handles the dragon's on ground walking <br>
 * Required memory state: <br>
 * WALK_TARGET: value present <br>
 * CANT_REACH_WALK_TARGET_SINCE: registered <br>
 */
public class DragonBehaviorWalk extends Behavior<EntityDragonBase> {
    // We can't simply extend vanilla WalkToTargetTask since they use vanilla path find method

    @Nullable
    WalkTarget target;

    public DragonBehaviorWalk(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
    }

    public DragonBehaviorWalk() {
        this(150, 250);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase dragon) {
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        Optional<WalkTarget> destination = dragon.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (destination.isPresent()) {
            target = destination.get();
            return true;
        }

        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        if (target != null && !hasArrived(dragon)) {
            return true;
        }
        dragon.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        return false;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        this.target = null;
        dragon.getNavigation().stop();
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        target = owner.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (target != null) {
            IafDragonBehaviorHelper.setDragonWalkTarget(owner, target.getTarget().currentBlockPosition());
        }
    }

    private boolean hasArrived(EntityDragonBase dragon) {
        if (dragon.getNavigation().isDone() && util.hasArrived(dragon, target.getTarget().currentBlockPosition(), null)) {
            return true;
        }
        return false;
    }

    private boolean hasReachedTarget(EntityDragonBase mobEntity, WalkTarget targetPos) {
        return targetPos.getTarget().currentBlockPosition().distManhattan(mobEntity.blockPosition()) <= targetPos.getCloseEnoughDist();
    }
}

