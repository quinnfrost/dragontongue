package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.Registration;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.sensors.DragonFlightSensor;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.schedule.ScheduleBuilder;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.MultiTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Optional;

public class RegistryBrains extends Schedule {
    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> core() {
        return ImmutableList.of(
                Pair.of(0, new DragonBehaviorLook(45, 90)),
                Pair.of(1, new DragonBehaviorWalk(150, 250))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> idle() {
        return ImmutableList.of(
                Pair.of(9, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new DragonTaskRide<>(10, 10), 0),
                                Pair.of(new DragonTaskSit(10, 10), 1),
                                Pair.of(new DragonTaskMate(10, 10, 1.0f), 2),
                                Pair.of(new DragonTaskReturnToRoost(10, 10), 3),

                                Pair.of(new DragonTaskEscort(10, 10), 4),
                                Pair.of(new DragonTaskAttackMelee(10, 10, 1.0f, true), 5),
                                Pair.of(new DragonTaskWander(1.0f), 5),
                                Pair.of(new DragonTaskLookIdle(), 6)
                        )
                )),
                Pair.of(10, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
//                                Pair.of(new DragonTaskRide<>(10, 10), 0),
//                                Pair.of(new DragonTaskSit(10, 10), 1),
//                                Pair.of(new DragonTaskMate(10, 10, 1.0f), 2),
//                                Pair.of(new DragonTaskReturnToRoost(10, 10), 3),
//
//                                Pair.of(new DragonTaskEscort(10, 10), 4),
//                                Pair.of(new DragonTaskAttackMelee(10, 10, 1.0f, true), 5),
//                                Pair.of(new DragonTaskWander(1.0f), 5),
//                                Pair.of(new DragonTaskLookIdle(), 6)
                        )
                ))

//                Pair.of(10, new DragonTaskRide<>(60,60)),
//                Pair.of(11, new DragonTaskSit(60,60)),
//                Pair.of(12, new DragonTaskMate(60,60, 1.0f)),
//                Pair.of(13, new DragonTaskReturnToRoost(60,60)),
//
//                Pair.of(14, new DragonTaskEscort(60,60)),
//                Pair.of(15, new DragonTaskAttackMelee(60,60, 1.0f, true)),
//                Pair.of(16, new DragonTaskWander(0.4f)),
//                Pair.of(17, new DragonTaskLookIdle())


//                Pair.of(10, new RunSometimesTask(
//                        new LookAtEntityTask(8.0F), RangedInteger.createRangedInteger(30, 60)
//                        )),
//                Pair.of(11, new RunSometimesTask(
//                        new LookAtEntityTask(8.0F), RangedInteger.createRangedInteger(30, 60)
//                        )),
//                Pair.of(12, new FirstShuffledTask<>(ImmutableList.of(
//                        Pair.of(new DragonTaskWander(0.4F), 2),
//                        Pair.of(new DragonTaskLookIdle(), 2)
//                )))
        );
    }

    public static final MemoryModuleType<String> MEMORY_TEST = new MemoryModuleType<>(Optional.of(Codec.STRING));
    public static final SensorType<DragonFlightSensor> SENSOR_TEST = new SensorType<>(DragonFlightSensor::new);
    public static final Schedule SCHEDULE_TEST = getScheduleBuilder().add(1000, Activity.IDLE).build();
    public static final Schedule DEFAULT = getScheduleBuilder().build();
    public static ScheduleBuilder getScheduleBuilder() {
        return new ScheduleBuilder(new Schedule());
    }

    public static Schedule makeDefaultSchedule() {
        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(new Schedule());
        scheduleBuilder.add(0, Activity.IDLE);
        return scheduleBuilder.build();
    }

    public static void register(IEventBus eventBus) {
        Registration.SCHEDULES.register("schedule_test", () -> SCHEDULE_TEST);
        Registration.SCHEDULES.register("default", () -> DEFAULT);

        Registration.MEMORY.register("memory_test", () -> MEMORY_TEST);
        Registration.SENSOR.register("sensor_test", () -> SENSOR_TEST);

        Registration.SCHEDULES.register(eventBus);
        Registration.MEMORY.register(eventBus);
        Registration.SENSOR.register(eventBus);
    }

}
