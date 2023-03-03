package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.RegistryBrains;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class DragonTaskWander extends Task<EntityDragonBase> {
    @Nullable
    WalkTarget currentTarget;
    private final float speed;
    private final int maxXZ;
    private final int maxY;

    public DragonTaskWander(float speedIn, int maxXZ, int maxY, int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
                MemoryModuleType.HOME, MemoryModuleStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
        this.speed = speedIn;
        this.maxXZ = maxXZ;
        this.maxY = maxY;
    }

    public DragonTaskWander(float speedIn) {
        this(speedIn, 10, 7, 60, 60);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase dragon) {
        if (!dragon.canMove() || dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY || dragon.isFuelingForge()) {
            return false;
        }
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        return true;
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        Optional<Vector3d> optional = Optional.ofNullable(RandomPositionGenerator.getLandPos(entityIn, this.maxXZ, this.maxY));
        entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((vector3d) -> {
            return new WalkTarget(vector3d, this.speed, (int) Math.ceil(entityIn.getBoundingBox().getAverageEdgeLength()));
        }));

    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        super.updateTask(worldIn, owner, gameTime);
    }
}
