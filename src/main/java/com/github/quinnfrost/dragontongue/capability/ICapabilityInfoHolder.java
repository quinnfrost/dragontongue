package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ICapabilityInfoHolder {
    static ICapabilityInfoHolder getCapability(Entity entity) {
        return entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));
    }
    void copy(ICapabilityInfoHolder cap);
    List<UUID> getCommandEntities();
    void setCommandEntities(List<UUID> uuids);
    void addCommandEntity(UUID uuid);
    void removeCommandEntity(UUID uuid);
    void setCommandDistance(double distance);
    double getCommandDistance();
    double modifyCommandDistance(double offset);
    void setSelectDistance(double distance);
    double getSelectDistance();
    double modifySelectDistance(double offset);
    BlockPos getFallbackPosition();
    void setFallbackPosition(BlockPos blockPos);
    void tickFallbackTimer();
    int getFallbackTimer();
    void setFallbackTimer(int value);


    void setDestination(BlockPos blockPos);
    Optional<BlockPos> getDestination();
    void setCommandStatus(EnumCommandSettingType.CommandStatus status);
    EnumCommandSettingType.CommandStatus getCommandStatus();
    void setBreathTarget(BlockPos target);
    Optional<BlockPos> getBreathTarget();
    void setHomePosition(BlockPos blockPos);
    Optional<BlockPos> getHomePosition();
    void setHomeDimension(String dimensionName);
    Optional<String> getHomeDimension();
    void setReturnHome(boolean value);
    boolean getReturnHome();
    void setShouldSleep(boolean value);
    boolean getShouldSleep();
    Map<EnumCommandSettingType, Enum> getCommandMaps();
    void setObjectSetting(EnumCommandSettingType type, Enum setting);
    Enum getObjectSetting(EnumCommandSettingType type);

}
