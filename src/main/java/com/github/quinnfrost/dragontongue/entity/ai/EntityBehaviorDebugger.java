package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.client.render.RenderNode;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.MessageClientDraw;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityBehaviorDebugger {
    public static List<String> getTargetInfoString(MobEntity mobEntity) {
//        CompoundNBT compoundNBT = new CompoundNBT();
//        DragonTongue.debugTarget.writeAdditional(compoundNBT);
        if (mobEntity == null) {
            return new ArrayList<>();
        }
        ICapabilityInfoHolder capabilityInfoHolder = mobEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(mobEntity));
        BlockPos targetPos = DragonTongue.isIafPresent ? IafHelperClass.getReachTarget(mobEntity) : mobEntity.getNavigator().getTargetPos();
        String targetPosString = (targetPos == null ? "" :
                targetPos + "(" + String.valueOf(util.getDistance(mobEntity.getPosition(), targetPos)) + ")");
        Entity targetEntity = mobEntity.getAttackTarget();
        String targetString = targetEntity == null ? "" :
                targetEntity.getEntityString() + " " + mobEntity.getAttackTarget().getPosition();
        String destinationString = capabilityInfoHolder.getDestination().isPresent() ?
                capabilityInfoHolder.getDestination().get() + "(" + util.getDistance(capabilityInfoHolder.getDestination().get(), mobEntity.getPosition()) + ")" : "";

        List<String> debugMsg = Arrays.asList(
                String.format("%s \"%s\" [%s]", mobEntity.getName().getString(),  mobEntity.getCustomName() == null ? "-" : mobEntity.getCustomName(), mobEntity.getEntityString()),
                "Pos:" + mobEntity.getPosition(),
                "Motion:" + mobEntity.getMotion(),
                "Goals:",
                mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                "Tasks:",
                mobEntity.getBrain().getRunningTasks().toString(),
                "Targets:" + targetString,
                "Current dest:" + targetPosString,
                "Command status:" + capabilityInfoHolder.getCommandStatus().toString(),
                "Command dest:" + destinationString,
                "AttackDecision:" + capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE),
                "StepHeight:" + mobEntity.stepHeight,
                "isInWater:" + mobEntity.isInWater(),
                "Move:" + String.format("%f - %f - %f", mobEntity.moveForward, mobEntity.moveStrafing, mobEntity.moveVertical)
        );
        if (DragonTongue.isIafPresent) {
            List<String> additional = IafHelperClass.getAdditionalDragonDebugStrings(mobEntity);
            debugMsg = Stream.concat(debugMsg.stream(), additional.stream())
                    .collect(Collectors.toList());
        }

        return debugMsg;
    }

    public static void sendDebugMessage() {
        // Ask all client to display entity debug string
        if (DragonTongue.debugTarget != null) {
            MobEntity mobEntity = DragonTongue.debugTarget;
            RegistryMessages.sendToClient(new MessageClientDisplay(
                    EnumClientDisplay.ENTITY_DEBUG,
                    mobEntity.getEntityId(),
                    1,
                    EntityBehaviorDebugger.getTargetInfoString(mobEntity)
            ), (ServerPlayerEntity) DragonTongue.debugger);
        }

    }

    public static void sendDestinationMessage() {
        // Ask all clients to draw entity destination
        if (DragonTongue.debugTarget != null) {
            PlayerEntity playerEntity = DragonTongue.debugger;
            ICapabilityInfoHolder cap = playerEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(playerEntity));

            for (UUID entityUUID :
                    cap.getCommandEntities()) {
                MobEntity mobEntity = (MobEntity) ((ServerWorld) playerEntity.world).getEntityByUuid(entityUUID);
                if (!DragonTongue.isIafPresent) {
                    if (mobEntity.getNavigator().getTargetPos() != null) {
                        RegistryMessages.sendToClient(new MessageClientDraw(
                                mobEntity.getEntityId(), Vector3d.copyCentered(mobEntity.getNavigator().getTargetPos()),
                                mobEntity.getPositionVec()
                        ), (ServerPlayerEntity) DragonTongue.debugger);
                    }
                } else {
                    if (IafHelperClass.isDragon(mobEntity)) {
                        IafHelperClass.drawDragonFlightDestination(mobEntity);
                    }
                    BlockPos pos = IafHelperClass.getReachTarget(mobEntity);
                    if (pos != null) {
                        RegistryMessages.sendToClient(new MessageClientDraw(
                                mobEntity.getEntityId(), Vector3d.copyCentered(pos),
                                mobEntity.getPositionVec()
                        ), (ServerPlayerEntity) DragonTongue.debugger);
                    }
                }
            }
        }
    }
}
