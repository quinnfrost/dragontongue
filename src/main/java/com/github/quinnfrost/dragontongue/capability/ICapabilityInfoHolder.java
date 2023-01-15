package com.github.quinnfrost.dragontongue.capability;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public interface ICapabilityInfoHolder {
    List<UUID> getCommandEntities();
    void setCommandEntities(List<UUID> uuids);
    void setCommandEntity(UUID uuid);
    void addCommandEntity(UUID uuid);
    void removeCommandEntity(UUID uuid);
//    UUID getUUID();
//    void setUUID(UUID uuid);
    BlockPos getPos();
    void setPos(BlockPos blockPos);
    void fallbackTimerTick();
    int getFallbackTimer();
    void setFallbackTimer(int value);
    boolean getDestinationSet();
    void setDestinationSet(boolean set);
}
