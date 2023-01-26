package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public interface ICapTargetHolder {
    List<UUID> getCommandEntities();
    void setCommandEntities(List<UUID> uuids);
    void setCommandEntity(UUID uuid);
    void addCommandEntity(UUID uuid);
    void removeCommandEntity(UUID uuid);
    BlockPos getFallbackPosition();
    void setFallbackPosition(BlockPos blockPos);
    void tickFallbackTimer();
    int getFallbackTimer();
    void setFallbackTimer(int value);
    void setDestination(BlockPos blockPos);
    BlockPos getDestination();
    void setCommandStatus(EnumCommandStatus status);
    EnumCommandStatus getCommandStatus();
    void setCommandDistance(double distance);
    double getCommandDistance();
    double modifyCommandDistance(double offset);
}
