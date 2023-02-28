package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.sensors;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class DragonFlightSensor extends Sensor<EntityDragonBase> {
    @Override
    protected void update(ServerWorld worldIn, EntityDragonBase entityIn) {

    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of();
    }
}
