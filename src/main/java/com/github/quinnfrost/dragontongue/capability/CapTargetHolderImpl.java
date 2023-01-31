package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public class CapTargetHolderImpl implements ICapTargetHolder {
    public static BlockPos INVALID_POS = new BlockPos(0, 0.5, 0);

    private List<UUID> commandEntitiesUUIDs = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
    private double commandDistance = 128;
    private double selectDistance = 128;
    private BlockPos fallbackPosition = null;
    private int fallbackTimer = 0;
    private BlockPos commandDestination = null;
    private EnumCommandStatus status = EnumCommandStatus.NONE;
    private Optional<BlockPos> breathTarget = Optional.empty();
    private Optional<BlockPos> homePosition = Optional.empty();
    private Map<EnumCommandSettingType, Object> commandMaps = new EnumMap<EnumCommandSettingType, Object>(EnumCommandSettingType.class);
//    private Map<EnumCommandSettingType.BooleanSettings, Boolean> settingMaps = new EnumMap<EnumCommandSettingType.BooleanSettings, Boolean>(EnumCommandSettingType.BooleanSettings.class);

    public CapTargetHolderImpl() {
        new CapTargetHolderImpl(null);
    }

    public CapTargetHolderImpl(Entity entity) {
        if (entity != null) {
            this.commandDestination = entity.getPosition();
            this.fallbackPosition = entity.getPosition();
        }
        commandMaps.put(EnumCommandSettingType.COMMAND_STATUS, EnumCommandStatus.NONE);
        commandMaps.put(EnumCommandSettingType.GROUND_ATTACK_TYPE, EnumCommandSettingType.GroundAttackType.BITE);
        commandMaps.put(EnumCommandSettingType.AIR_ATTACK_TYPE, EnumCommandSettingType.AirAttackType.HOVER_BLAST);

        commandMaps.put(EnumCommandSettingType.MOVEMENT_TYPE, EnumCommandSettingType.MovementType.ANY);
        commandMaps.put(EnumCommandSettingType.DESTROY_TYPE, EnumCommandSettingType.DestroyType.ANY);
        commandMaps.put(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.ANY);
        commandMaps.put(EnumCommandSettingType.SHOULD_RETURN_ROOST, true);


//        for (EnumCommandSettingType.BooleanSettings type :
//                EnumCommandSettingType.BooleanSettings.values()) {
//            settingMaps.put(type, true);
//        }
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
    public void setSelectDistance(double distance) {
        this.selectDistance = distance;
    }

    @Override
    public double getSelectDistance() {
        return selectDistance;
    }

    @Override
    public double modifySelectDistance(double offset) {
        if (selectDistance + offset < 0) {
            this.selectDistance = 0;

        } else if (selectDistance + offset > Config.NEARBY_RANGE.get()) {
            this.selectDistance = Config.NEARBY_RANGE.get();
        } else {
            this.selectDistance = selectDistance + offset;
        }
        return this.selectDistance;
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
        commandMaps.put(EnumCommandSettingType.COMMAND_STATUS, status);
    }

    @Override
    public EnumCommandStatus getCommandStatus() {
        return (EnumCommandStatus) commandMaps.get(EnumCommandSettingType.COMMAND_STATUS);
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

    @Override
    public void setHomePosition(BlockPos blockPos) {
        if (blockPos != null) {
            this.homePosition = Optional.of(blockPos);
        } else {
            this.homePosition = Optional.empty();
        }
    }

    @Override
    public Optional<BlockPos> getHomePosition() {
        return homePosition;
    }

    @Override
    public void setObjectSetting(EnumCommandSettingType type, Object setting) {
        commandMaps.put(type, setting);
    }

    @Override
    public Object getObjectSetting(EnumCommandSettingType type) {
        return commandMaps.get(type);
    }


}
