package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class DragonTaskReturnRoost extends Task<EntityDragonBase> {
    public DragonTaskReturnRoost(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
            MemoryModuleType.HOME, MemoryModuleStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
    }
    public DragonTaskReturnRoost() {
        this(60,60);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        return owner.getAttackTarget() == null && !isAtHome(owner);
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return super.shouldExecute(worldIn, entityIn);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.startExecuting(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        super.updateTask(worldIn, owner, gameTime);
    }

    private boolean isAtHome(EntityDragonBase dragonIn) {
        if (dragonIn.getBrain().getMemory(MemoryModuleType.HOME).isPresent()) {
            GlobalPos homePos = dragonIn.getBrain().getMemory(MemoryModuleType.HOME).get();
            if (dragonIn.world.getDimensionKey().equals(homePos.getDimension())
            && util.hasArrived(dragonIn, homePos.getPos(), dragonIn.getBoundingBox().getAverageEdgeLength())) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }
}
