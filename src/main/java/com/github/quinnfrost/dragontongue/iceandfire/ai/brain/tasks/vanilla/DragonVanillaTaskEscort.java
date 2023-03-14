package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class DragonVanillaTaskEscort extends Behavior<EntityDragonBase> {
    private BlockPos previousPosition;
    private final float maxRange = 2000F;
    public DragonVanillaTaskEscort(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        return owner.canMove() && owner.getTarget() == null && owner.getOwner() != null && owner.getCommand() == 2;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return entityIn.canMove() && entityIn.getTarget() == null && entityIn.getOwner() != null && entityIn.getOwner().isAlive() && (entityIn.distanceTo(entityIn.getOwner()) > 15 || !entityIn.getNavigation().isDone());
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        if (owner.getOwner() != null) {
            final float dist = owner.distanceTo(owner.getOwner());
            if (dist > maxRange){
                return;
            }
            if (dist > owner.getBoundingBox().getSize() && (!owner.isFlying() && !owner.isHovering() || !owner.isAllowedToTriggerFlight())) {
                if(previousPosition == null || previousPosition.distSqr(owner.getOwner().blockPosition()) > 9) {
                    owner.getNavigation().moveTo(owner.getOwner(), 1F);
                    previousPosition = owner.getOwner().blockPosition();
                }
            }
            if ((dist > 30F || owner.getOwner().getY() - owner.getY() > 8) && !owner.isFlying() && !owner.isHovering() && owner.isAllowedToTriggerFlight()) {
                owner.setHovering(true);
                owner.setInSittingPose(false);
                owner.setOrderedToSit(false);
                owner.flyTicks = 0;
            }
        }
    }

}
