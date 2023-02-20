package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public class CapabilityInfoHolderImpl implements ICapabilityInfoHolder {
    public static BlockPos INVALID_POS = new BlockPos(0, 0.5, 0);
    private Entity entity;
    private List<UUID> commandEntitiesUUIDs = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
    private double commandDistance = 128;
    private double selectDistance = 128;
    private BlockPos fallbackPosition = null;
    private int fallbackTimer = 0;
    private Optional<BlockPos> commandDestination = Optional.empty();
    private Optional<BlockPos> breathTarget = Optional.empty();
    private Optional<BlockPos> homePosition = Optional.empty();
    private String homeDimension = "";
    private boolean shouldReturnHome = true;
    private boolean shouldSleep = true;
    private Map<EnumCommandSettingType, Enum> commandMaps = new EnumMap<>(EnumCommandSettingType.class);
//    private Map<EnumCommandSettingType.BooleanSettings, Boolean> settingMaps = new EnumMap<EnumCommandSettingType.BooleanSettings, Boolean>(EnumCommandSettingType.BooleanSettings.class);

    public CapabilityInfoHolderImpl() {
        new CapabilityInfoHolderImpl(null);
    }

    public CapabilityInfoHolderImpl(Entity entity) {
        this.entity = entity;
        if (entity != null) {
            this.commandDestination = Optional.of(entity.getPosition());
            this.fallbackPosition = entity.getPosition();
        }
        commandMaps.put(EnumCommandSettingType.COMMAND_STATUS, EnumCommandSettingType.CommandStatus.NONE);
        commandMaps.put(EnumCommandSettingType.GROUND_ATTACK_TYPE, EnumCommandSettingType.GroundAttackType.ANY);
        commandMaps.put(EnumCommandSettingType.AIR_ATTACK_TYPE, EnumCommandSettingType.AirAttackType.ANY);
        commandMaps.put(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.DEFAULT);

        commandMaps.put(EnumCommandSettingType.MOVEMENT_TYPE, EnumCommandSettingType.MovementType.ANY);
        commandMaps.put(EnumCommandSettingType.DESTROY_TYPE, EnumCommandSettingType.DestroyType.ANY);
        commandMaps.put(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.ANY);
//        commandMaps.put(EnumCommandSettingType.SHOULD_RETURN_ROOST, true);

    }

    @Override
    public void copy(ICapabilityInfoHolder cap) {
        commandEntitiesUUIDs = cap.getCommandEntities();
        commandDistance = cap.getCommandDistance();
        selectDistance = cap.getSelectDistance();
        fallbackPosition = cap.getFallbackPosition();
        fallbackTimer = cap.getFallbackTimer();
        commandDestination = cap.getDestination();
        breathTarget = cap.getBreathTarget();
        homePosition = cap.getHomePosition();

        homeDimension = "";
        cap.getHomeDimension().ifPresent(s -> homeDimension = s);

        shouldReturnHome = cap.getReturnHome();
        shouldSleep = cap.getShouldSleep();
        commandMaps = cap.getCommandMaps();
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
    public void setDestination(@Nullable BlockPos blockPos) {
        if (blockPos != null) {
            this.commandDestination = Optional.of(blockPos);
        } else {
            this.commandDestination = Optional.empty();
        }
    }

    @Override
    public Optional<BlockPos> getDestination() {
        return commandDestination;
    }

    @Override
    public void setCommandStatus(EnumCommandSettingType.CommandStatus status) {
        commandMaps.put(EnumCommandSettingType.COMMAND_STATUS, status);
    }

    @Override
    public EnumCommandSettingType.CommandStatus getCommandStatus() {
        return (EnumCommandSettingType.CommandStatus) commandMaps.get(EnumCommandSettingType.COMMAND_STATUS);
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
    public void setHomeDimension(String dimensionName) {
        this.homeDimension = dimensionName != null ? dimensionName : "";
    }

    @Override
    public Optional<String> getHomeDimension() {
        if (homePosition.isPresent() || !homeDimension.isEmpty()) {
            return Optional.of(homeDimension);
        } else {
            homeDimension = "";
            return Optional.empty();
        }
    }

    @Override
    public void setReturnHome(boolean value) {
        this.shouldReturnHome = value;
    }

    @Override
    public boolean getReturnHome() {
        return shouldReturnHome;
    }

    @Override
    public void setShouldSleep(boolean value) {
        this.shouldSleep = value;
    }

    @Override
    public boolean getShouldSleep() {
        return shouldSleep;
    }

    @Override
    public Map<EnumCommandSettingType, Enum> getCommandMaps() {
        return commandMaps;
    }

    @Override
    public void setObjectSetting(EnumCommandSettingType type, Enum setting) {
        commandMaps.put(type, setting);
    }

    @Override
    public Enum getObjectSetting(EnumCommandSettingType type) {
        return commandMaps.get(type);
    }


}
