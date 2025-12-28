package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public class DragonVanillaTaskReturnToRoost extends Behavior<EntityDragonBase> {
    public DragonVanillaTaskReturnToRoost(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        return owner.canMove() && owner.lookingForRoostAIFlag
                && (owner.getTarget() == null || !owner.getTarget().isAlive())
                && owner.getRestrictCenter() != null
                && DragonUtils.isInHomeDimension(owner)
                && owner.getDistanceSquared(Vec3.atCenterOf(owner.getRestrictCenter())) > owner.getBbWidth()
                * owner.getBbWidth();
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return super.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        if (owner.getRestrictCenter() != null) {
            final double dist = Math.sqrt(owner.getDistanceSquared(Vec3.atCenterOf(owner.getRestrictCenter())));
            final double xDist = Math.abs(owner.getX() - owner.getRestrictCenter().getX() - 0.5F);
            final double zDist = Math.abs(owner.getZ() - owner.getRestrictCenter().getZ() - 0.5F);
            final double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);

            if (dist < owner.getBbWidth()) {
                owner.setFlying(false);
                owner.setHovering(false);
                owner.getNavigation().moveTo(owner.getRestrictCenter().getX(),
                        owner.getRestrictCenter().getY(), owner.getRestrictCenter().getZ(), 1.0F);
            } else {
                double yAddition = 15 + owner.getRandom().nextInt(3);
                if (xzDist < 40) {
                    yAddition = 0;
                    if (owner.isOnGround()) {
                        owner.setFlying(false);
                        owner.setHovering(false);
                        owner.flightManager.setFlightTarget(
                                Vec3.upFromBottomCenterOf(owner.getRestrictCenter(), yAddition));
                        owner.getNavigation().moveTo(owner.getRestrictCenter().getX(),
                                owner.getRestrictCenter().getY(), owner.getRestrictCenter().getZ(), 1.0F);
                        return;
                    }
                }
                if (!owner.isFlying() && !owner.isHovering() && xzDist > 40) {
                    owner.setHovering(true);
                }
                if (owner.isFlying()) {
                    owner.flightManager.setFlightTarget(
                            Vec3.upFromBottomCenterOf(owner.getRestrictCenter(), yAddition));
                    owner.getNavigation().moveTo(owner.getRestrictCenter().getX(),
                            yAddition + owner.getRestrictCenter().getY(), owner.getRestrictCenter().getZ(), 1F);
                }
                owner.flyTicks = 0;
            }

        }
    }
}
