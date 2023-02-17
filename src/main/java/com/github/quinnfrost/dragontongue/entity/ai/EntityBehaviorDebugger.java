package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityBehaviorDebugger {
    public static List<String> getTargetInfoString(MobEntity mobEntity) {
        CompoundNBT compoundNBT = new CompoundNBT();
        DragonTongue.debugTarget.writeAdditional(compoundNBT);

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
                mobEntity.getEntityString() + "[" + mobEntity.getCustomName() + "]",
                "Pos:" + mobEntity.getPosition(),
                "Motion:" + mobEntity.getMotion(),
                "Goals:",
                mobEntity.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                mobEntity.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                "Targets:" + targetString,
                "Current dest:" + targetPosString,
                "Command status:" + capabilityInfoHolder.getCommandStatus().toString(),
                "Command dest:" + destinationString,
                "AttackDecision:" + capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE),
                "StepHeight:" + mobEntity.stepHeight
        );
        if (DragonTongue.isIafPresent) {
            List<String> additional = IafHelperClass.getAdditionalDragonDebugStrings(mobEntity);
            debugMsg = Stream.concat(debugMsg.stream(), additional.stream())
                    .collect(Collectors.toList());
        }

        return debugMsg;
    }
}
