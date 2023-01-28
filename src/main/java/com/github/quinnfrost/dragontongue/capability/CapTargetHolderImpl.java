package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapTargetHolderImpl implements ICapTargetHolder {
    private List<UUID> commandEntitiesUUID = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
    private BlockPos commandDestination = null;
    private EnumCommandStatus status = EnumCommandStatus.NONE;
    private double commandDistance = 128;
    private BlockPos fallbackPosition = null;
    private int fallbackTimer = 0;

    public CapTargetHolderImpl() {

    }

    public CapTargetHolderImpl(Entity entity) {
        this.commandDestination = entity.getPosition();
        this.fallbackPosition = entity.getPosition();
    }

    @Override
    public List<UUID> getCommandEntities() {
        return commandEntitiesUUID;
    }

    @Override
    public void setCommandEntities(List<UUID> uuids) {
        commandEntitiesUUID = uuids;
    }

    @Override
    public void setCommandEntity(UUID uuid) {
        List<UUID> uuids = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
        uuids.add(uuid);
        setCommandEntities(uuids);
    }

    @Override
    public void addCommandEntity(UUID uuid) {
        if (!commandEntitiesUUID.contains(uuid)) {
            commandEntitiesUUID.add(uuid);
        }
    }

    @Override
    public void removeCommandEntity(UUID uuid) {
        commandEntitiesUUID.remove(uuid);
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
        if (fallbackTimer > 0){
            --fallbackTimer;
        }else if (fallbackTimer < 0){
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
        this.status = status;
    }

    @Override
    public EnumCommandStatus getCommandStatus() {
        return status;
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
        if (commandDistance + offset < 0 ) {
            this.commandDistance = 0;

        } else if (commandDistance + offset > Config.COMMAND_DISTANCE_MAX.get()) {
            this.commandDistance = Config.COMMAND_DISTANCE_MAX.get();
        } else {
            this.commandDistance = commandDistance + offset;
        }
        return this.commandDistance;
    }

}
