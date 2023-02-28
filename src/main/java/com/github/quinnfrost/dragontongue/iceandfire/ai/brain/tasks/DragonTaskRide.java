package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.IFlyingMount;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public class DragonTaskRide<T extends MobEntity & IFlyingMount> extends Task<EntityDragonBase> {
    private PlayerEntity player;
    public DragonTaskRide(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        player = owner.getRidingPlayer();

        return player != null;
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.getNavigator().clearPath();
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        owner.getNavigator().clearPath();
        owner.setAttackTarget(null);
        double x = owner.getPosX();
        double y = owner.getPosY();
        double z = owner.getPosZ();
        double speed = 1.8F * owner.getFlightSpeedModifier();
        Vector3d lookVec = player.getLookVec();
        if (player.moveForward < 0) {
            lookVec = lookVec.rotateYaw((float) Math.PI);
        } else if (player.moveStrafing > 0) {
            lookVec = lookVec.rotateYaw((float) Math.PI * 0.5f);
        } else if (player.moveStrafing < 0) {
            lookVec = lookVec.rotateYaw((float) Math.PI * -0.5f);
        }
        if (Math.abs(player.moveStrafing) > 0.0) {
            speed *= 0.25D;
        }
        if (player.moveForward < 0.0) {
            speed *= 0.15D;
        }
        if (owner.isGoingUp()) {
            lookVec = lookVec.add(0, 1, 0);
        } else if (owner.isGoingDown()) {
            lookVec = lookVec.add(0, -1, 0);
        }
        if (player.moveStrafing != 0 || player.moveForward != 0 || (owner.fliesLikeElytra())) {
            x += lookVec.x * 10;
            z += lookVec.z * 10;
        }
        if ((owner.isFlying() || hovering(owner)) && (owner.fliesLikeElytra() || owner.isGoingUp() || owner.isGoingDown())) {
            y += lookVec.y * owner.getYSpeedMod();
        }
        if (owner.fliesLikeElytra() && lookVec.y == -1 || !(owner.isFlying() || hovering(owner)) && !owner.isOnGround()) {
            y -= 1;
        }
        owner.getMoveHelper().setMoveTo(x, y, z, speed);

    }
    private boolean hovering(EntityDragonBase entityIn) {
        return entityIn.isHovering() || entityIn instanceof EntityDragonBase && ((EntityDragonBase) entityIn).useFlyingPathFinder();
    }
}
