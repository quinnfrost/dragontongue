package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

public class DragonBehaviorFlight extends Task<EntityDragonBase> {
    @Nullable
    WalkTarget target;

    public DragonBehaviorFlight(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_PRESENT
        ), durationMinIn, durationMaxIn);
    }

    public DragonBehaviorFlight() {
        this(60, 60);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase dragon) {
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        Optional<WalkTarget> destination = dragon.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (destination.isPresent()) {
            if (IafDragonBehaviorHelper.shouldFlyToTarget(dragon, destination.get().getTarget().getBlockPos())) {
                target = destination.get();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        if (target != null && !util.hasArrived(dragon, target.getTarget().getBlockPos(), null)) {
            return true;
        }
        dragon.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        return false;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        target = null;
        IafDragonBehaviorHelper.setDragonFlightTarget(entityIn, null);

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
            IafDragonBehaviorHelper.setDragonFlightTarget(owner, target.getTarget().getPos());
        }
    }
}
