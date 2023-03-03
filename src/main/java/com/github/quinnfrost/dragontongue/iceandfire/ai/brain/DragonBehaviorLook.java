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
     * @param durationMin
     * @param durationMax
     */
    public DragonBehaviorLook(int durationMin, int durationMax) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_PRESENT
        ), durationMin, durationMax);
    }
    public DragonBehaviorLook() {
        this(60,60);
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
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.resetTask(worldIn, entityIn, gameTimeIn);
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
        owner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((posWrapper) -> {
            owner.getLookController().setLookPosition(posWrapper.getPos());
        });
    }
}
