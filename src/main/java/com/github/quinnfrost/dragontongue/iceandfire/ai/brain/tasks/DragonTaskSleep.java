package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

public class DragonTaskSleep extends Task<EntityDragonBase> {
    public DragonTaskSleep(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }
    public DragonTaskSleep() {
        this(60,60);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        return super.shouldExecute(worldIn, owner);
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return super.shouldContinueExecuting(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.startExecuting(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        super.updateTask(worldIn, owner, gameTime);
    }

}
