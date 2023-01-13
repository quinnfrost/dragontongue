package com.github.quinnfrost.dragontongue.capability;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface ICapabilityInfoHolder {
    UUID getUUID();
    void setUUID(UUID uuid);
    BlockPos getPos();
    void setPos(BlockPos blockPos);
    void fallbackTimerTick();
    int getFallbackTimer();
    void setFallbackTimer(int value);
    boolean getDestinationSet();
    void setDestinationSet(boolean set);
}
