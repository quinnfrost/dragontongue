package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class DragonVanillaTaskWander extends Task<EntityDragonBase> {
    private final float speed;
    private final int maxXZ;
    private final int maxY;
    public DragonVanillaTaskWander(float speedIn, int maxXZ, int maxY, int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
        this.speed = speedIn;
        this.maxXZ = maxXZ;
        this.maxY = maxY;
    }

    public DragonVanillaTaskWander(float speedIn) {
        this(speedIn, 10, 7, 60, 60);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        if (!(ICapabilityInfoHolder.getCapability(owner).getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE)) {
            return false;
        }

        if (!owner.canMove() || owner.isFuelingForge()) {
            return false;
        }
        if (owner.getControllingPassenger() != null) {
            return false;
        }
        if (owner.isFlying() || owner.isHovering()) {
            return false;
        }

        return true;
    }


    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return shouldExecute(worldIn, entityIn);
    }

    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
//        Optional<Vector3d> optional = Optional.ofNullable(RandomPositionGenerator.getLandPos(entityIn, this.maxXZ, this.maxY));
//        entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((vector3d) ->
//                new WalkTarget(vector3d, this.speed, 0)
//        ));

        Vector3d Vector3d = RandomPositionGenerator.findRandomTarget(entityIn, 10, 7);
        if (Vector3d != null) {
            double xPosition = Vector3d.x;
            double yPosition = Vector3d.y;
            double zPosition = Vector3d.z;
            entityIn.getNavigator().tryMoveToXYZ(xPosition, yPosition, zPosition, this.speed);
        }
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.resetTask(worldIn, entityIn, gameTimeIn);
        entityIn.getNavigator().clearPath();
    }
}
