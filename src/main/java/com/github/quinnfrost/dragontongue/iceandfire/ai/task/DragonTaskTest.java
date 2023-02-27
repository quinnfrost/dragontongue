package com.github.quinnfrost.dragontongue.iceandfire.ai.task;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;

import java.util.Map;

public class DragonTaskTest extends Task<EntityDragonBase> {
    public DragonTaskTest(Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryStateIn) {
        this(requiredMemoryStateIn, 60);
    }

    public DragonTaskTest(Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryStateIn, int duration) {
        this(requiredMemoryStateIn, duration, duration);
    }

    public DragonTaskTest(Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryStateIn, int durationMinIn, int durationMaxIn) {
        super(requiredMemoryStateIn, durationMinIn, durationMaxIn);
    }
}
