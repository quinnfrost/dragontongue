package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

/**
 * Handles the dragon's look position <br>
 * Required memory state: <br>
 *      LOOK_TARGET: value present <br>
 */
public class DragonBehaviorLook extends Task<EntityDragonBase> {
    /**
     * @param durationMin The duration is the period of grace for the task to run, when the {@link Task#shouldContinueExecuting(ServerWorld, LivingEntity, long)} criteria is meet while the {@link Task#shouldExecute(ServerWorld, LivingEntity)} doesn't. This is the lower boundary.
     * @param durationMax The maximum period of grace. The actual period of grace is a random number between the two.
     */
    public DragonBehaviorLook(int durationMin, int durationMax) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_PRESENT
        ), durationMin, durationMax);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        return super.shouldExecute(worldIn, owner);
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return entityIn.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((posWrapper) -> {
            return posWrapper.isVisibleTo(entityIn);
        }).isPresent();
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.startExecuting(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.getBrain().removeMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        owner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((posWrapper) -> {
            owner.getLookController().setLookPosition(posWrapper.getPos());
        });
    }
}
