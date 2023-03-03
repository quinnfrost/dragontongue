package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class DragonVanillaTaskEscort extends Task<EntityDragonBase> {
    private BlockPos previousPosition;
    private final float maxRange = 2000F;
    public DragonVanillaTaskEscort(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        return owner.canMove() && owner.getAttackTarget() == null && owner.getOwner() != null && owner.getCommand() == 2;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return entityIn.canMove() && entityIn.getAttackTarget() == null && entityIn.getOwner() != null && entityIn.getOwner().isAlive() && (entityIn.getDistance(entityIn.getOwner()) > 15 || !entityIn.getNavigator().noPath());
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        if (owner.getOwner() != null) {
            final float dist = owner.getDistance(owner.getOwner());
            if (dist > maxRange){
                return;
            }
            if (dist > owner.getBoundingBox().getAverageEdgeLength() && (!owner.isFlying() && !owner.isHovering() || !owner.isAllowedToTriggerFlight())) {
                if(previousPosition == null || previousPosition.distanceSq(owner.getOwner().getPosition()) > 9) {
                    owner.getNavigator().tryMoveToEntityLiving(owner.getOwner(), 1F);
                    previousPosition = owner.getOwner().getPosition();
                }
            }
            if ((dist > 30F || owner.getOwner().getPosY() - owner.getPosY() > 8) && !owner.isFlying() && !owner.isHovering() && owner.isAllowedToTriggerFlight()) {
                owner.setHovering(true);
                owner.setQueuedToSit(false);
                owner.setSitting(false);
                owner.flyTicks = 0;
            }
        }
    }

}
