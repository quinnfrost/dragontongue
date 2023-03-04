package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.Registration;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.sensors.DragonFlightSensor;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.*;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.schedule.ScheduleBuilder;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.util.RangedInteger;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Optional;

public class RegistryBrains extends Schedule {
    // Our custom memory items
    public static final MemoryModuleType<String> MEMORY_TEST = new MemoryModuleType<>(Optional.of(Codec.STRING));
//    public static final MemoryModuleType<WalkTarget> DESTINATION = new MemoryModuleType<>(Optional.empty());

    // Our custom sensors
    public static final SensorType<DragonFlightSensor> SENSOR_TEST = new SensorType<>(DragonFlightSensor::new);

    // Our custom activities
    public static final Activity ACTIVITY_DRAGON_DEFAULT = new Activity("activity_default");
    public static final Activity ACTIVITY_IDLE = new Activity("dragon_idle");
    public static final Activity ACTIVITY_REST = new Activity("dragon_rest");
    public static final Activity ACTIVITY_SLEEP = new Activity("dragon_sleep");
    public static final Activity ACTIVITY_HUNT = new Activity("dragon_hunt");
    public static final Activity ACTIVITY_ATTACK = new Activity("dragon_attack");

    // Our schedules
    public static final Schedule SCHEDULE_DEFAULT = getScheduleBuilder().build();
    public static final Schedule SCHEDULE_DAY_DRAGON = getScheduleBuilder().add(10, ACTIVITY_IDLE).add(2000, ACTIVITY_HUNT).add(5000, ACTIVITY_REST).add(7000, ACTIVITY_IDLE).add(9000, ACTIVITY_HUNT).add(11000, ACTIVITY_IDLE).add(12010, ACTIVITY_SLEEP).build();
    public static final Schedule SCHEDULE_NIGHT_DRAGON = getScheduleBuilder().add(10, ACTIVITY_SLEEP).add(12010, ACTIVITY_IDLE).add(14000, ACTIVITY_HUNT).add(17000, ACTIVITY_IDLE).add(19000, ACTIVITY_HUNT).add(21000, ACTIVITY_IDLE).add(23000, ACTIVITY_SLEEP).build();

    public static final Schedule FROST_WILD = getScheduleBuilder().build();
    public static final Schedule FIRE_WILD = getScheduleBuilder().build();
    public static final Schedule LIGHTNING_WILD = getScheduleBuilder().build();
    public static final Schedule FROST_TAMED = getScheduleBuilder().build();
    public static final Schedule FIRE_TAMED = getScheduleBuilder().build();
    public static final Schedule LIGHTNING_TAMED = getScheduleBuilder().build();

    public static ScheduleBuilder getScheduleBuilder() {
        return new ScheduleBuilder(new Schedule());
    }

    public static Schedule makeDefaultSchedule() {
        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(new Schedule());
        scheduleBuilder.add(0, Activity.IDLE);
        return scheduleBuilder.build();
    }

    public static void register(IEventBus eventBus) {
        Registration.ACTIVITY.register(ACTIVITY_DRAGON_DEFAULT.getKey(), () -> ACTIVITY_DRAGON_DEFAULT);
        Registration.ACTIVITY.register(ACTIVITY_IDLE.getKey(), () -> ACTIVITY_IDLE);
        Registration.ACTIVITY.register(ACTIVITY_REST.getKey(), () -> ACTIVITY_REST);
        Registration.ACTIVITY.register(ACTIVITY_SLEEP.getKey(), () -> ACTIVITY_SLEEP);
        Registration.ACTIVITY.register(ACTIVITY_ATTACK.getKey(), () -> ACTIVITY_ATTACK);


        Registration.SCHEDULES.register("schedule_default", () -> SCHEDULE_DEFAULT);
        Registration.SCHEDULES.register("schedule_day_dragon", () -> SCHEDULE_DAY_DRAGON);
        Registration.SCHEDULES.register("schedule_night_dragon", () -> SCHEDULE_NIGHT_DRAGON);

        Registration.MEMORY.register("memory_test", () -> MEMORY_TEST);

        Registration.SENSOR.register("sensor_test", () -> SENSOR_TEST);

        Registration.ACTIVITY.register(eventBus);
        Registration.SCHEDULES.register(eventBus);
        Registration.MEMORY.register(eventBus);
        Registration.SENSOR.register(eventBus);
    }

    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> vanilla() {
        return ImmutableList.of(
                Pair.of(9, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new DragonVanillaTaskRide<>(60, 60), 0),
                                Pair.of(new DragonVanillaTaskSit(60, 60), 1),
                                Pair.of(new DragonVanillaTaskMate(60, 60, 1.0f), 2),
                                Pair.of(new DragonVanillaTaskReturnToRoost(60, 60), 3),

                                Pair.of(new DragonVanillaTaskEscort(60, 60), 4),
                                Pair.of(new DragonVanillaTaskAttackMelee(60, 60, 1.0f, true), 5),
                                Pair.of(new DragonVanillaTaskWander(1.0f), 5),
                                Pair.of(new DragonVanillaTaskLookIdle(), 6)
                        )
                )),
                Pair.of(10, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
//                                Pair.of(new DragonVanillaTaskRide<>(10, 10), 0),
//                                Pair.of(new DragonVanillaTaskSit(10, 10), 1),
//                                Pair.of(new DragonVanillaTaskMate(10, 10, 1.0f), 2),
//                                Pair.of(new DragonVanillaTaskReturnToRoost(10, 10), 3),
//
//                                Pair.of(new DragonVanillaTaskEscort(10, 10), 4),
//                                Pair.of(new DragonVanillaTaskAttackMelee(10, 10, 1.0f, true), 5),
//                                Pair.of(new DragonVanillaTaskWander(1.0f), 5),
//                                Pair.of(new DragonVanillaTaskLookIdle(), 6)
                        )
                ))

//                Pair.of(10, new DragonVanillaTaskRide<>(60,60)),
//                Pair.of(11, new DragonVanillaTaskSit(60,60)),
//                Pair.of(12, new DragonVanillaTaskMate(60,60, 1.0f)),
//                Pair.of(13, new DragonVanillaTaskReturnToRoost(60,60)),
//
//                Pair.of(14, new DragonVanillaTaskEscort(60,60)),
//                Pair.of(15, new DragonVanillaTaskAttackMelee(60,60, 1.0f, true)),
//                Pair.of(16, new DragonVanillaTaskWander(0.4f)),
//                Pair.of(17, new DragonVanillaTaskLookIdle())


//                Pair.of(10, new RunSometimesTask(
//                        new LookAtEntityTask(8.0F), RangedInteger.createRangedInteger(30, 60)
//                        )),
//                Pair.of(11, new RunSometimesTask(
//                        new LookAtEntityTask(8.0F), RangedInteger.createRangedInteger(30, 60)
//                        )),
//                Pair.of(12, new FirstShuffledTask<>(ImmutableList.of(
//                        Pair.of(new DragonVanillaTaskWander(0.4F), 2),
//                        Pair.of(new DragonVanillaTaskLookIdle(), 2)
//                )))
        );
    }

    /**
     * Core activity can run at any time.
     * Put basic movement control and activity switcher here.
     *
     * @return
     */
    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> core() {
        return ImmutableList.of(
                Pair.of(0, new DragonBehaviorLook()),
                Pair.of(1, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new DragonBehaviorFlight(), 0),
                                Pair.of(new DragonBehaviorWalk(), 1)
                        )

                )),

                Pair.of(2, new DragonCoreTaskTarget())
        );
    }

    /**
     * Idle activity executes when there is no better things to do.
     * Usually implemented by random walking and looking, flight shows go here as well.
     *
     * @return
     */
    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> idle() {
        return ImmutableList.of(
                Pair.of(9, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new DragonBehaviorAttack(), 0),
                                Pair.of(new FirstShuffledTask<>(ImmutableMap.of(
                                        MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT
                                ), ImmutableList.of(
                                        Pair.of(new RunSometimesTask<>(new DragonTaskGlide(1.0f), RangedInteger.createRangedInteger(30, 60)), 0),
                                        Pair.of(new RunSometimesTask<>(new DragonTaskWander(1.0f), RangedInteger.createRangedInteger(30, 60)), 1)
                                )), 1)
                        ))),

                Pair.of(99, new UpdateActivityTask())
        );
    }

    /**
     * Attack activity, this activity is used when attack target is set, so the daily routine can be bypassed
     *
     * @return
     */
    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> attack() {
        return ImmutableList.of(


                Pair.of(99, new UpdateActivityTask())
        );
    }

    /**
     * Sleep activity runs when dragon goes to sleep at night
     * Note that the {@link EntityDragonBase#isMovementBlocked()} must be overwritten to stop {@link EntityDragonBase#isQueuedToSit()} from blocking the task system
     *
     * @return
     */
    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> sleep() {
        return ImmutableList.of(
                Pair.of(9, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new DragonTaskReturnRoost(), 0),
                                Pair.of(new DragonTaskSleep(), 1)
                        )
                )),

                Pair.of(99, new UpdateActivityTask())
        );
    }

    /**
     * Rest activity runs when dragon stays at roost. This usually executes in noon and will take random chances to sleep.
     *
     * @return
     */
    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> rest() {
        return ImmutableList.of(
                Pair.of(9, new MultiTask<>(ImmutableMap.of(

                ), ImmutableSet.of(

                ), MultiTask.Ordering.ORDERED, MultiTask.RunType.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new DragonTaskReturnRoost(), 0)
                        )
                )),

                Pair.of(99, new UpdateActivityTask())
        );
    }


    public static ImmutableList<Pair<Integer, ? extends Task<? super EntityDragonBase>>> escort() {
        return ImmutableList.of(

        );
    }
}
