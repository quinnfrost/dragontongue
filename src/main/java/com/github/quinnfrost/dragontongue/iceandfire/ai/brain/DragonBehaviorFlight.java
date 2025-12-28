package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Optional;

public class DragonBehaviorFlight extends Behavior<EntityDragonBase> {
    @Nullable
    WalkTarget target;

    public DragonBehaviorFlight(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT
        ), durationMinIn, durationMaxIn);
    }

    public DragonBehaviorFlight() {
        this(60, 60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase dragon) {
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        Optional<WalkTarget> destination = dragon.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (destination.isPresent()) {
            if (IafDragonBehaviorHelper.shouldFlyToTarget(dragon, destination.get().getTarget().currentBlockPosition())) {
                target = destination.get();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        if (target != null && !util.hasArrived(dragon, target.getTarget().currentBlockPosition(), null)) {
            return true;
        }
        dragon.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        return false;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        target = null;
        IafDragonBehaviorHelper.setDragonFlightTarget(entityIn, null);

    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {

    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        target = owner.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (target != null) {
            IafDragonBehaviorHelper.setDragonFlightTarget(owner, target.getTarget().currentPosition());
        }
    }
}
