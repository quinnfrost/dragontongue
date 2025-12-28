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
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide debug info of a living entity
 */
public class EntityBehaviorDebugger {
    public static Mob targetEntity;
    public static ServerPlayer requestedPlayer;

    public static void startDebugFor(ServerPlayer playerIn, Mob targetEntityIn) {
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
            RegistryMessages.sendToClient(new MessageDebugEntity(targetEntity.getId(), getAssociatedTargetFor(targetEntity), getTargetInfoString(targetEntity)), requestedPlayer);
        }
    }

    public static List<Vec3> getAssociatedTargetFor(Mob mobEntity) {
        List<Vec3> associatedTarget = new ArrayList<>();
        if (DragonTongue.isIafPresent) {
            if (IafAdvancedDragonFlightManager.getCurrentFlightTargetFor(mobEntity) != null) {
                associatedTarget.add(IafAdvancedDragonFlightManager.getCurrentFlightTargetFor(mobEntity));
            }
            if (IafHelperClass.getReachTarget(mobEntity) != null) {
                associatedTarget.add(Vec3.atBottomCenterOf(IafHelperClass.getReachTarget(mobEntity)));
            }
        } else if (mobEntity.getNavigation().getTargetPos() != null) {
            associatedTarget.add(Vec3.atBottomCenterOf(mobEntity.getNavigation().getTargetPos()));
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

    public static String formatVector(Vec3 vector3d) {
        if (vector3d != null) {
            return String.format("%.4f, %.4f, %.4f", vector3d.x(), vector3d.y(), vector3d.z());
        } else {
            return "-, -, -";
        }
    }

    public static List<String> getMemoryInfoString(Mob mobEntity) {
        Brain<?> brain = mobEntity.getBrain();
        List<String> stringList = new ArrayList<>();
        try {
            if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                brain.getMemory(MemoryModuleType.WALK_TARGET).ifPresent(target -> {
                    stringList.add("WalkTarget: " + formatBlockPos(target.getTarget().currentBlockPosition()));
                });
            }
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                    stringList.add("AttackTarget: " + target.getName().getContents());
                });
            }
            if (brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
                brain.getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(iPosWrapper -> {
                    stringList.add("LookTarget: " + formatBlockPos(iPosWrapper.currentBlockPosition()));
                });
            }
            if (brain.hasMemoryValue(MemoryModuleType.VISIBLE_LIVING_ENTITIES)) {
                brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent(entityList -> {
                    stringList.add("VisibleMobs: " + entityList);
                });
            }
            return stringList;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static List<String> getTargetInfoString(Mob mobEntity) {
        if (mobEntity == null) {
            return new ArrayList<>();
        }
        mobEntity.level.getProfiler().push("debugString");

        ICapabilityInfoHolder capabilityInfoHolder = mobEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(mobEntity));

        String scheduleString = (mobEntity.getBrain().getSchedule() == null ? "" : mobEntity.getBrain().getSchedule().getRegistryName().getPath()) + String.format(" [%s]", (mobEntity.getBrain().getSchedule() == null ? "-" : mobEntity.getBrain().getSchedule().getActivityAt((int) mobEntity.level.getDayTime())));
        BlockPos targetPos = DragonTongue.isIafPresent ? IafHelperClass.getReachTarget(mobEntity) : mobEntity.getNavigation().getTargetPos();
        String targetPosString = (targetPos == null ? "-" :
                String.format(" %d, %d, %d (%.2f)",
                        targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                        mobEntity.position().distanceTo(Vec3.atBottomCenterOf(targetPos))
                ));
        Entity targetEntity = mobEntity.getTarget();
        String targetString = (targetEntity == null ? "-" :
                String.format("%s [%s] [%d, %d, %d] (%.2f)",
                        targetEntity.getName().getString(),
                        targetEntity.getEncodeId(),
                        mobEntity.getTarget().blockPosition().getX(), mobEntity.getTarget().blockPosition().getY(), mobEntity.getTarget().blockPosition().getZ(),
                        mobEntity.position().distanceTo(targetEntity.position())
                ));
        String destinationString = capabilityInfoHolder.getDestination().isPresent() ? String.format(" %d, %d, %d (%.2f)",
                capabilityInfoHolder.getDestination().get().getX(), capabilityInfoHolder.getDestination().get().getY(), capabilityInfoHolder.getDestination().get().getZ(),
                util.getDistance(capabilityInfoHolder.getDestination().get(), mobEntity.blockPosition())) : "-";
        String reachesTarget;
        if (mobEntity.getNavigation().getPath() != null && mobEntity.getNavigation().getPath().canReach()) {
            reachesTarget = "true";
        } else if (mobEntity.getNavigation().getPath() == null) {
            reachesTarget = "null";
        } else {
            reachesTarget = "false";
        }

        List<String> debugMsg = new ArrayList<>();

        debugMsg.addAll(Arrays.asList(
                String.format("%s \"%s\" [%s] (%.1f/%s)", mobEntity.getName().getString(), mobEntity.getCustomName() == null ? "-" : mobEntity.getCustomName(), mobEntity.getEncodeId(), mobEntity.getHealth(), Objects.toString((mobEntity.getAttribute(Attributes.MAX_HEALTH).getValue()), "-")),
                "Pos: " + String.format("%.5f, %.5f, %.5f ", mobEntity.position().x, mobEntity.position().y, mobEntity.position().z) + String.format("[%d, %d, %d]", mobEntity.blockPosition().getX(), mobEntity.blockPosition().getY(), mobEntity.blockPosition().getZ()),
                "Motion: " + String.format("%.5f, %.5f, %.5f ", mobEntity.getDeltaMovement().x, mobEntity.getDeltaMovement().y, mobEntity.getDeltaMovement().z),
                "Facing: " + String.format(" %s", formatVector(mobEntity.getLookAngle())),
                "Goals:",
                mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                "Tasks:",
                " Schedule: " + scheduleString,
                " Activity: " + String.format("%s + ", mobEntity.getBrain().coreActivities.toString()) + String.format("(%s)", mobEntity.getBrain().getActiveNonCoreActivity().orElse(new Activity(""))),
                " Running",
                mobEntity.getBrain().getRunningBehaviors().toString(),
                " Memory"
        ));
        debugMsg.addAll(getMemoryInfoString(mobEntity));
        debugMsg.addAll(Arrays.asList(
                "Targets: " + targetString,
                "StepHeight:" + mobEntity.maxUpStep,
                "isInWater:" + mobEntity.isInWater(),
                "Move:" + String.format("%f - %f - %f", mobEntity.zza, mobEntity.xxa, mobEntity.yya),
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

        mobEntity.level.getProfiler().pop();

        return debugMsg;
    }

}
