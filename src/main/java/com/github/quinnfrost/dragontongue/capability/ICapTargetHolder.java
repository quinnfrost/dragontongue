package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICapTargetHolder {
    List<UUID> getCommandEntities();
    void setCommandEntities(List<UUID> uuids);
    void addCommandEntity(UUID uuid);
    void removeCommandEntity(UUID uuid);
    void setCommandDistance(double distance);
    double getCommandDistance();
    double modifyCommandDistance(double offset);
    BlockPos getFallbackPosition();
    void setFallbackPosition(BlockPos blockPos);
    void tickFallbackTimer();
    int getFallbackTimer();
    void setFallbackTimer(int value);


    void setDestination(BlockPos blockPos);
    BlockPos getDestination();
    void setCommandStatus(EnumCommandStatus status);
    EnumCommandStatus getCommandStatus();
    void setBreathTarget(BlockPos target);
    Optional<BlockPos> getBreathTarget();

}
