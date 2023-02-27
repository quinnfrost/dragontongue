package com.github.quinnfrost.dragontongue.iceandfire.ai.task;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.ZoglinEntity;
import net.minecraft.util.RangedInteger;

public class DragonTasks {
    public static ImmutableList<Task<? super EntityDragonBase>> core(float speed) {
        return ImmutableList.of(new LookTask(45, 90), new WalkToTargetTask());
    }

    public static ImmutableList<Task<? super EntityDragonBase>> idle() {
        return ImmutableList.<Task<? super ZoglinEntity>>of(
                new RunSometimesTask(
                        new LookAtEntityTask(8.0F), RangedInteger.createRangedInteger(30, 60)),
                new FirstShuffledTask(ImmutableList.of(
                        Pair.of(new WalkRandomlyTask(0.4F), 2),
                        Pair.of(new WalkTowardsLookTargetTask(0.4F, 3), 2),
                        Pair.of(new DummyTask(30, 60), 1)
                ))
        );
    }
}
