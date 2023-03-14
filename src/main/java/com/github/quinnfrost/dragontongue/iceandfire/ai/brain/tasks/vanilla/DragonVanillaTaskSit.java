package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.server.level.ServerLevel;

public class DragonVanillaTaskSit extends Behavior<EntityDragonBase> {
    public DragonVanillaTaskSit(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        if (!owner.isTame()) {
            return false;
        } else if (owner.isInWaterOrBubble()) {
            return false;
        } else if (!owner.isOnGround()) {
            return false;
        } else {
            LivingEntity livingentity = owner.getOwner();
            if (livingentity == null) {
                return true;
            } else {
                return owner.distanceToSqr(livingentity) < 144.0D && livingentity.getLastHurtByMob() != null ? false : owner.isOrderedToSit();
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return entityIn.isOrderedToSit();
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.getNavigation().stop();
        entityIn.setInSittingPose(true);
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.setInSittingPose(false);
    }
}
