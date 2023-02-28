package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public class DragonTaskReturnToRoost extends Task<EntityDragonBase> {
    public DragonTaskReturnToRoost(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        return owner.canMove() && owner.lookingForRoostAIFlag
                && (owner.getAttackTarget() == null || !owner.getAttackTarget().isAlive())
                && owner.getHomePosition() != null
                && DragonUtils.isInHomeDimension(owner)
                && owner.getDistanceSquared(Vector3d.copyCentered(owner.getHomePosition())) > owner.getWidth()
                * owner.getWidth();
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return super.shouldExecute(worldIn, entityIn);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        if (owner.getHomePosition() != null) {
            final double dist = Math.sqrt(owner.getDistanceSquared(Vector3d.copyCentered(owner.getHomePosition())));
            final double xDist = Math.abs(owner.getPosX() - owner.getHomePosition().getX() - 0.5F);
            final double zDist = Math.abs(owner.getPosZ() - owner.getHomePosition().getZ() - 0.5F);
            final double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);

            if (dist < owner.getWidth()) {
                owner.setFlying(false);
                owner.setHovering(false);
                owner.getNavigator().tryMoveToXYZ(owner.getHomePosition().getX(),
                        owner.getHomePosition().getY(), owner.getHomePosition().getZ(), 1.0F);
            } else {
                double yAddition = 15 + owner.getRNG().nextInt(3);
                if (xzDist < 40) {
                    yAddition = 0;
                    if (owner.isOnGround()) {
                        owner.setFlying(false);
                        owner.setHovering(false);
                        owner.flightManager.setFlightTarget(
                                Vector3d.copyCenteredWithVerticalOffset(owner.getHomePosition(), yAddition));
                        owner.getNavigator().tryMoveToXYZ(owner.getHomePosition().getX(),
                                owner.getHomePosition().getY(), owner.getHomePosition().getZ(), 1.0F);
                        return;
                    }
                }
                if (!owner.isFlying() && !owner.isHovering() && xzDist > 40) {
                    owner.setHovering(true);
                }
                if (owner.isFlying()) {
                    owner.flightManager.setFlightTarget(
                            Vector3d.copyCenteredWithVerticalOffset(owner.getHomePosition(), yAddition));
                    owner.getNavigator().tryMoveToXYZ(owner.getHomePosition().getX(),
                            yAddition + owner.getHomePosition().getY(), owner.getHomePosition().getZ(), 1F);
                }
                owner.flyTicks = 0;
            }

        }
    }
}
