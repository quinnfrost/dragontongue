package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public class CapTargetHolderImpl implements ICapTargetHolder {
    private List<UUID> commandEntitiesUUIDs = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
    private double commandDistance = 128;
    private BlockPos fallbackPosition = null;
    private int fallbackTimer = 0;
    private BlockPos commandDestination = null;
    private Optional<BlockPos> breathTarget = Optional.empty();
    private int lastCommandState = 0;
    private Map<EnumCommandType, Object> commandMaps = new EnumMap<EnumCommandType, Object>(EnumCommandType.class);

    public CapTargetHolderImpl() {
        new CapTargetHolderImpl(null);
    }

    public CapTargetHolderImpl(Entity entity) {
        if (entity != null) {
            this.commandDestination = entity.getPosition();
            this.fallbackPosition = entity.getPosition();
        }
        commandMaps.put(EnumCommandType.COMMAND_STATUS, EnumCommandStatus.NONE);
    }

    @Override
    public List<UUID> getCommandEntities() {
        return commandEntitiesUUIDs;
    }

    @Override
    public void setCommandEntities(List<UUID> uuids) {
        commandEntitiesUUIDs = uuids;
    }

    @Override
    public void addCommandEntity(UUID uuid) {
        if (!commandEntitiesUUIDs.contains(uuid)) {
            commandEntitiesUUIDs.add(uuid);
        }
    }

    @Override
    public void removeCommandEntity(UUID uuid) {
        commandEntitiesUUIDs.remove(uuid);
    }
    @Override
    public void setCommandDistance(double distance) {
        if (distance > 0 && distance <= Config.COMMAND_DISTANCE_MAX.get()) {
            this.commandDistance = distance;
        }
    }

    @Override
    public double getCommandDistance() {
        return commandDistance;
    }

    @Override
    public double modifyCommandDistance(double offset) {
        if (commandDistance + offset < 0) {
            this.commandDistance = 0;

        } else if (commandDistance + offset > Config.COMMAND_DISTANCE_MAX.get()) {
            this.commandDistance = Config.COMMAND_DISTANCE_MAX.get();
        } else {
            this.commandDistance = commandDistance + offset;
        }
        return this.commandDistance;
    }
    @Override
    public BlockPos getFallbackPosition() {
        return fallbackPosition;
    }

    @Override
    public void setFallbackPosition(BlockPos blockPos) {
        this.fallbackPosition = blockPos;
    }

    @Override
    public void tickFallbackTimer() {
        if (fallbackTimer > 0) {
            --fallbackTimer;
        } else if (fallbackTimer < 0) {
            fallbackTimer = 0;
        }
    }

    @Override
    public int getFallbackTimer() {
        return fallbackTimer;
    }

    @Override
    public void setFallbackTimer(int value) {
        if (value >= 0) {
            this.fallbackTimer = value;
        }
    }


    @Override
    public void setDestination(BlockPos blockPos) {
        this.commandDestination = blockPos;
    }

    @Override
    public BlockPos getDestination() {
        return commandDestination;
    }

    @Override
    public void setCommandStatus(EnumCommandStatus status) {
        commandMaps.put(EnumCommandType.COMMAND_STATUS, status);
    }

    @Override
    public EnumCommandStatus getCommandStatus() {
        return (EnumCommandStatus) commandMaps.get(EnumCommandType.COMMAND_STATUS);
    }

    @Override
    public void setBreathTarget(@Nullable BlockPos target) {
        if (target != null) {
            this.breathTarget = Optional.of(target);
        } else {
            this.breathTarget = Optional.empty();
        }
    }

    @Override
    public Optional<BlockPos> getBreathTarget() {
        return breathTarget;
    }


}
