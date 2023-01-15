package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.config.Config;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapabilityInfoHolderImplementation implements ICapabilityInfoHolder {
    private List<UUID> commandEntitiesUUID = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
    private List<UUID> commandEntitiesUUIDM = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
    private List<UUID> commandEntitiesUUIDL = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());

    private UUID lastCommand = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private BlockPos blockPos = new BlockPos(0,128,0);
    private int fallbackTimer = 0;
    private boolean destinationSet = false;

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
        commandEntitiesUUID.add(uuid);
    }

    @Override
    public void removeCommandEntity(UUID uuid) {
        commandEntitiesUUID.remove(uuid);
    }

//    @Override
//    public UUID getUUID() {
//        return this.lastCommand;
//    }


//    @Override
//    public void setUUID(UUID uuid) {
//        this.lastCommand = uuid;
//    }

    @Override
    public BlockPos getPos() {
        return blockPos;
    }

    @Override
    public void setPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    public void fallbackTimerTick() {
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
    public boolean getDestinationSet() {
        return destinationSet;
    }

    @Override
    public void setDestinationSet(boolean set) {
        this.destinationSet = set;
    }

}
