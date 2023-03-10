package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafAdvancedDragonFlightManager;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.MessageClientDraw;
import com.github.quinnfrost.dragontongue.message.MessageDebugEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide debug info of a living entity
 */
public class EntityBehaviorDebugger {
    public static MobEntity targetEntity;
    public static ServerPlayerEntity requestedPlayer;

    public static void startDebugFor(ServerPlayerEntity playerIn, MobEntity targetEntityIn) {
        if (targetEntityIn == null) {
            return;
        }

        IafHelperClass.startIafPathDebug(playerIn, targetEntityIn);

        requestedPlayer = playerIn;
        targetEntity = targetEntityIn;

    }

    public static void stopDebug() {
        IafHelperClass.stopIafPathDebug();
        RegistryMessages.sendToAll(new MessageDebugEntity());

        requestedPlayer = null;
        targetEntity = null;

    }

    public static void updateDebugMessage() {
        if (targetEntity != null) {
            RegistryMessages.sendToClient(new MessageDebugEntity(targetEntity.getEntityId(), getAssociatedTargetFor(targetEntity), getTargetInfoString(targetEntity)), requestedPlayer);
        }
    }

    public static List<Vector3d> getAssociatedTargetFor(MobEntity mobEntity) {
        List<Vector3d> associatedTarget = new ArrayList<>();
        if (DragonTongue.isIafPresent) {
            if (IafAdvancedDragonFlightManager.getCurrentFlightTargetFor(mobEntity) != null) {
                associatedTarget.add(IafAdvancedDragonFlightManager.getCurrentFlightTargetFor(mobEntity));
            }
            if (IafHelperClass.getReachTarget(mobEntity) != null) {
                associatedTarget.add(Vector3d.copyCenteredHorizontally(IafHelperClass.getReachTarget(mobEntity)));
            }
        } else if (mobEntity.getNavigator().getTargetPos() != null) {
            associatedTarget.add(Vector3d.copyCenteredHorizontally(mobEntity.getNavigator().getTargetPos()));
        }
        return associatedTarget;
    }
    public static String formatBlockPos(BlockPos pos) {
        if (pos != null) {
            return String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
        } else {
            return "-, -, -";
        }
    }

    public static String formatVector(Vector3d vector3d) {
        if (vector3d != null) {
            return String.format("%.4f, %.4f, %.4f", vector3d.getX(), vector3d.getY(), vector3d.getZ());
        } else {
            return "-, -, -";
        }
    }

    public static List<String> getMemoryInfoString(MobEntity mobEntity) {
        Brain<?> brain = mobEntity.getBrain();
        List<String> stringList = new ArrayList<>();
        try {
            if (brain.hasMemory(MemoryModuleType.WALK_TARGET)) {
                brain.getMemory(MemoryModuleType.WALK_TARGET).ifPresent(target -> {
                    stringList.add("WalkTarget: " + formatBlockPos(target.getTarget().getBlockPos()));
                });
            }
            if (brain.hasMemory(MemoryModuleType.ATTACK_TARGET)) {
                brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                    stringList.add("AttackTarget: " + target.getName().getUnformattedComponentText());
                });
            }
            if (brain.hasMemory(MemoryModuleType.LOOK_TARGET)) {
                brain.getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(iPosWrapper -> {
                    stringList.add("LookTarget: " + formatBlockPos(iPosWrapper.getBlockPos()));
                });
            }
            if (brain.hasMemory(MemoryModuleType.VISIBLE_MOBS)) {
                brain.getMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent(entityList -> {
                    stringList.add("VisibleMobs: " + entityList);
                });
            }
            return stringList;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static List<String> getTargetInfoString(MobEntity mobEntity) {
        if (mobEntity == null) {
            return new ArrayList<>();
        }
        mobEntity.world.getProfiler().startSection("debugString");

        ICapabilityInfoHolder capabilityInfoHolder = mobEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(mobEntity));

        String scheduleString = (mobEntity.getBrain().getSchedule() == null ? "" : mobEntity.getBrain().getSchedule().getRegistryName().getPath()) + String.format(" [%s]", (mobEntity.getBrain().getSchedule() == null ? "-" : mobEntity.getBrain().getSchedule().getScheduledActivity((int) mobEntity.world.getDayTime())));
        BlockPos targetPos = DragonTongue.isIafPresent ? IafHelperClass.getReachTarget(mobEntity) : mobEntity.getNavigator().getTargetPos();
        String targetPosString = (targetPos == null ? "-" :
                String.format(" %d, %d, %d (%.2f)",
                        targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                        mobEntity.getPositionVec().distanceTo(Vector3d.copyCenteredHorizontally(targetPos))
                ));
        Entity targetEntity = mobEntity.getAttackTarget();
        String targetString = (targetEntity == null ? "-" :
                String.format("%s [%s] [%d, %d, %d] (%.2f)",
                        targetEntity.getName().getString(),
                        targetEntity.getEntityString(),
                        mobEntity.getAttackTarget().getPosition().getX(), mobEntity.getAttackTarget().getPosition().getY(), mobEntity.getAttackTarget().getPosition().getZ(),
                        mobEntity.getPositionVec().distanceTo(targetEntity.getPositionVec())
                ));
        String destinationString = capabilityInfoHolder.getDestination().isPresent() ? String.format(" %d, %d, %d (%.2f)",
                capabilityInfoHolder.getDestination().get().getX(), capabilityInfoHolder.getDestination().get().getY(), capabilityInfoHolder.getDestination().get().getZ(),
                util.getDistance(capabilityInfoHolder.getDestination().get(), mobEntity.getPosition())) : "-";
        String reachesTarget;
        if (mobEntity.getNavigator().getPath() != null && mobEntity.getNavigator().getPath().reachesTarget()) {
            reachesTarget = "true";
        } else if (mobEntity.getNavigator().getPath() == null) {
            reachesTarget = "null";
        } else {
            reachesTarget = "false";
        }

        List<String> debugMsg = new ArrayList<>();

        debugMsg.addAll(Arrays.asList(
                String.format("%s \"%s\" [%s] (%.1f/%s)", mobEntity.getName().getString(), mobEntity.getCustomName() == null ? "-" : mobEntity.getCustomName(), mobEntity.getEntityString(), mobEntity.getHealth(), Objects.toString((mobEntity.getAttribute(Attributes.MAX_HEALTH).getValue()), "-")),
                "Pos: " + String.format("%.5f, %.5f, %.5f ", mobEntity.getPositionVec().x, mobEntity.getPositionVec().y, mobEntity.getPositionVec().z) + String.format("[%d, %d, %d]", mobEntity.getPosition().getX(), mobEntity.getPosition().getY(), mobEntity.getPosition().getZ()),
                "Motion: " + String.format("%.5f, %.5f, %.5f ", mobEntity.getMotion().x, mobEntity.getMotion().y, mobEntity.getMotion().z),
                "Facing: " + String.format(" %s", formatVector(mobEntity.getLookVec())),
                "Goals:",
                mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                "Tasks:",
                " Schedule: " + scheduleString,
                " Activity: " + String.format("%s + ", mobEntity.getBrain().persistentActivities.toString()) + String.format("(%s)", mobEntity.getBrain().getTemporaryActivity().orElse(new Activity(""))),
                " Running",
                mobEntity.getBrain().getRunningTasks().toString(),
                " Memory"
        ));
        debugMsg.addAll(getMemoryInfoString(mobEntity));
        debugMsg.addAll(Arrays.asList(
                "Targets: " + targetString,
                "StepHeight:" + mobEntity.stepHeight,
                "isInWater:" + mobEntity.isInWater(),
                "Move:" + String.format("%f - %f - %f", mobEntity.moveForward, mobEntity.moveStrafing, mobEntity.moveVertical),
                "Current dest: " + targetPosString,
                "Command status:" + capabilityInfoHolder.getCommandStatus().toString(),
                "Command dest:" + destinationString,
                "AttackDecision:" + capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE)
        ));
        if (DragonTongue.isIafPresent) {
            List<String> additional = IafHelperClass.getAdditionalDragonDebugStrings(mobEntity);
            debugMsg = Stream.concat(debugMsg.stream(), additional.stream())
                    .collect(Collectors.toList());
        }

        mobEntity.world.getProfiler().endSection();

        return debugMsg;
    }

}
