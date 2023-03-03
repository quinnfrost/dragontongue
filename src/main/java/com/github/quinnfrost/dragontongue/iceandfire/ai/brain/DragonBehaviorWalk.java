package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Handles the dragon's on ground walking <br>
 * Required memory state: <br>
 * WALK_TARGET: value present <br>
 * CANT_REACH_WALK_TARGET_SINCE: registered <br>
 */
public class DragonBehaviorWalk extends Task<EntityDragonBase> {
    // We can't simply extend vanilla WalkToTargetTask since they use vanilla path find method

    @Nullable
    WalkTarget target;

    public DragonBehaviorWalk(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_PRESENT,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
    }

    public DragonBehaviorWalk() {
        this(150, 250);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase dragon) {
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
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        if (target != null && !hasArrived(dragon)) {
            return true;
        }
        dragon.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        return false;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        this.target = null;
        dragon.getNavigator().clearPath();

    }

    @Override
    protected boolean isTimedOut(long gameTime) {
        return false;
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        target = owner.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (target != null) {
            IafDragonBehaviorHelper.setDragonWalkTarget(owner, target.getTarget().getBlockPos());
        }
    }

    private boolean hasArrived(EntityDragonBase dragon) {
        if (dragon.getNavigator().noPath() && util.hasArrived(dragon, target.getTarget().getBlockPos(), null)) {
            return true;
        }
        return false;
    }

    private boolean hasReachedTarget(EntityDragonBase mobEntity, WalkTarget targetPos) {
        return targetPos.getTarget().getBlockPos().manhattanDistance(mobEntity.getPosition()) <= targetPos.getDistance();
    }
}

