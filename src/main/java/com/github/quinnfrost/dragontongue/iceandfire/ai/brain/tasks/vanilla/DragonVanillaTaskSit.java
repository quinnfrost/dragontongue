package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

public class DragonVanillaTaskSit extends Task<EntityDragonBase> {
    public DragonVanillaTaskSit(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        if (!owner.isTamed()) {
            return false;
        } else if (owner.isInWaterOrBubbleColumn()) {
            return false;
        } else if (!owner.isOnGround()) {
            return false;
        } else {
            LivingEntity livingentity = owner.getOwner();
            if (livingentity == null) {
                return true;
            } else {
                return owner.getDistanceSq(livingentity) < 144.0D && livingentity.getRevengeTarget() != null ? false : owner.isQueuedToSit();
            }
        }
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return entityIn.isQueuedToSit();
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.getNavigator().clearPath();
        entityIn.setQueuedToSit(true);
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.setQueuedToSit(false);
    }
}
