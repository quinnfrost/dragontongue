package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafAdvancedDragonFlightManager;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.message.MessageDebugEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

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
            if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) {
                brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(entityList -> {
                    stringList.add("VisibleMobs: " + entityList);
                });
            }
            return stringList;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static List<String> getTargetAIString(Mob mob) {
        String scheduleString = (mob.getBrain().getSchedule() == null ? "" : mob.getBrain().getSchedule().getRegistryName().getPath()) + String.format(" [%s]", (mob.getBrain().getSchedule() == null ? "-" : mob.getBrain().getSchedule().getActivityAt((int) mob.level.getDayTime())));
        List<String> aiString = new ArrayList<>(List.of(
                "Goals:",
                mob.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                mob.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                "Tasks:",
                " Schedule: " + scheduleString,
                " Activity: " + String.format("%s + ", mob.getBrain().coreActivities.toString()) + String.format("(%s)", mob.getBrain().getActiveNonCoreActivity().orElse(new Activity(""))),
                " Running",
                mob.getBrain().getRunningBehaviors().toString(),
                " Memory"
        ));
        aiString.addAll(getMemoryInfoString(mob));

        return aiString;
    }

    public static List<String> getTargetRiderString(LivingEntity livingEntity) {
        LivingEntity rider = (LivingEntity) livingEntity.getControllingPassenger();
        String riderTravel = (rider == null ? "" :
                String.format("Forward:%f - Strafing:%f - Vertical:%f", rider.zza, rider.xxa, rider.yya));
        List<String> riderString = new ArrayList<>(List.of(
                "Rider:"
        ));
        riderString.addAll(getTargetTravelString(rider));

        return riderString;
    }

    public static List<String> getTargetTravelString(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return new ArrayList<>();
        }
        List<String> travelString = new ArrayList<>(List.of(
                String.format("Speed:%.2f", livingEntity.getSpeed()),
                String.format("Forward:%f - Strafing:%f - Vertical:%f", livingEntity.zza, livingEntity.xxa, livingEntity.yya),
                String.format("XRot:%.2f, YRot:%.2f", livingEntity.getXRot(), livingEntity.getYRot()),
                String.format("Vertical:%.2f, Forward:%.2f",
                        Mth.abs(Mth.sin(livingEntity.getXRot() * ((float) Math.PI / 180F))),
                        Mth.abs(Mth.cos(livingEntity.getXRot() * ((float) Math.PI / 180F)))
                ),
                String.format("FallDistance:%.2f", livingEntity.fallDistance),
                String.format("MyValue: %.2f, %.2f, %.2f",
                        Mth.abs(Mth.cos(livingEntity.getXRot() * ((float) Math.PI / 180F))),
                        livingEntity.xxa,
                        Mth.abs(Mth.sin(Mth.map(livingEntity.getXRot() * livingEntity.getXRot(), 0, 8100, 0, 90) * ((float) Math.PI / 180F)))
                        )
        ));

        return travelString;
    }

    public static double getSpeed(Mob mob) {
//        double dX = mob.getX() - mob.xOld;
//        double dY = mob.getY() - mob.yOld;
//        double dZ = mob.getZ() - mob.zOld;
        return mob.getPosition(1.0f).distanceTo(new Vec3(mob.xOld, mob.yOld, mob.zOld)) / 0.05;
    }

    public static List<String> getTargetInfoString(Mob mobEntity) {
        if (mobEntity == null) {
            return new ArrayList<>();
        }
        mobEntity.level.getProfiler().push("debugString");

        ICapabilityInfoHolder capabilityInfoHolder = mobEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(mobEntity));

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
//                " OldPos: " + String.format("%.2f, %.2f, %.2f ", mobEntity.xo, mobEntity.yo, mobEntity.zo) + String.format("%.2f, %.2f, %.2f ", mobEntity.xOld, mobEntity.yOld, mobEntity.zOld),
                "Rot: " + String.format("%.2f, %.2f ", mobEntity.xRot, mobEntity.yRot),
                "Motion: " + String.format("%.5f, %.5f, %.5f (%.2f)", mobEntity.getDeltaMovement().x, mobEntity.getDeltaMovement().y, mobEntity.getDeltaMovement().z, getSpeed(mobEntity)),
                "Facing: " + String.format(" %s", formatVector(mobEntity.getLookAngle())),
                "Current dest: " + targetPosString,
                "Targets: " + targetString
                ));

        debugMsg.addAll(getTargetTravelString(mobEntity));
        debugMsg.addAll(getTargetRiderString(mobEntity));
        debugMsg.addAll(getTargetAIString(mobEntity));

        debugMsg.addAll(Arrays.asList(
                "StepHeight:" + mobEntity.maxUpStep,
                "OnGround: " + mobEntity.isOnGround(),
                "Command status:" + capabilityInfoHolder.getCommandStatus().toString(),
                "Command dest:" + destinationString,
                "AttackDecision:" + capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE),
                "isInWater? " + mobEntity.isInWater(),
                "FluidHeight: " + mobEntity.getFluidHeight(FluidTags.WATER),
                "HorizontalCollide? " + mobEntity.horizontalCollision,
                "VerticalCollide? " + (mobEntity.verticalCollision ? (mobEntity.verticalCollisionBelow ? "↓" : "↑") : "")

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
