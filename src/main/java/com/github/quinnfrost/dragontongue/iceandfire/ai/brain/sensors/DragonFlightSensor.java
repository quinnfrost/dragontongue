package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.sensors;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public class DragonFlightSensor extends Sensor<EntityDragonBase> {
    @Override
    protected void doTick(ServerLevel worldIn, EntityDragonBase entityIn) {

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of();
    }
}
