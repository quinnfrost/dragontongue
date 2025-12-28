package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.RegistryBrains;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class DragonTaskWander extends Behavior<EntityDragonBase> {
    @Nullable
    WalkTarget currentTarget;
    private final float speed;
    private final int maxXZ;
    private final int maxY;

    public DragonTaskWander(float speedIn, int maxXZ, int maxY, int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HOME, MemoryStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
        this.speed = speedIn;
        this.maxXZ = maxXZ;
        this.maxY = maxY;
    }

    public DragonTaskWander(float speedIn) {
        this(speedIn, 10, 7, 60, 60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase dragon) {
        if (!dragon.canMove() || dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY || dragon.isFuelingForge()) {
            return false;
        }
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        Optional<Vec3> optional = Optional.ofNullable(RandomPos.getLandPos(entityIn, this.maxXZ, this.maxY));
        entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((vector3d) -> {
            return new WalkTarget(vector3d, this.speed, (int) Math.ceil(entityIn.getBoundingBox().getSize()));
        }));

    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        super.tick(worldIn, owner, gameTime);
    }
}
