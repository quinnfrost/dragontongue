package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.server.level.ServerLevel;

/**
 * Handles the dragon's look position <br>
 * Required memory state: <br>
 *      LOOK_TARGET: value present <br>
 */
public class DragonBehaviorLook extends Behavior<EntityDragonBase> {
    /**
     * @param durationMin
     * @param durationMax
     */
    public DragonBehaviorLook(int durationMin, int durationMax) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT
        ), durationMin, durationMax);
    }
    public DragonBehaviorLook() {
        this(60,60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        return super.checkExtraStartConditions(worldIn, owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return entityIn.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((posWrapper) -> {
            return posWrapper.isVisibleBy(entityIn);
        }).isPresent();
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.stop(worldIn, entityIn, gameTimeIn);
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
        owner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((posWrapper) -> {
            owner.getLookControl().setLookAt(posWrapper.currentPosition());
        });
    }
}
