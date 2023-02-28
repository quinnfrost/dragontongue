package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Handles the dragon's on ground walking <br>
 * Required memory state: <br>
 *      WALK_TARGET: value present <br>
 *      CANT_REACH_WALK_TARGET_SINCE: registered <br>
 */
public class DragonBehaviorWalk extends Task<EntityDragonBase> {
    // We can't simply extend vanilla WalkToTargetTask since they use vanilla path find method

    @Nullable
    BlockPos targetPosition;
    public DragonBehaviorWalk(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_PRESENT
        ), durationMinIn, durationMaxIn);
    }
    public DragonBehaviorWalk() {
        this(150,250);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        this.targetPosition = owner.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().getBlockPos();
        return true;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (targetPosition != null) {
            Optional<WalkTarget> optionalWalkTarget = entityIn.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            if (optionalWalkTarget.isPresent() && !this.hasReachedTarget(entityIn, optionalWalkTarget.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.targetPosition = null;
        entityIn.getNavigator().clearPath();
        entityIn.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.getNavigator().tryMoveToXYZ(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ(), 1.0f);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        super.updateTask(worldIn, owner, gameTime);
    }

    private boolean hasReachedTarget(EntityDragonBase mobEntity, WalkTarget targetPos) {
        return targetPos.getTarget().getBlockPos().manhattanDistance(mobEntity.getPosition()) <= targetPos.getDistance();
    }
}

