package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapabilityProvider implements ICapabilitySerializable<ListTag> {
    public static final BlockPos INVALID_POS = new BlockPos(0, 0, 0);
    private final CapabilityInfoHolderImpl data;
    private final LazyOptional<ICapabilityInfoHolder> dataOptional;

    public void invalidate(){
        dataOptional.invalidate();
    }

//    public CapabilityProvider() {
//        this.data = new CapabilityInfoHolderImpl();
//        this.dataOptional = LazyOptional.of(()->data);
//        DragonTongue.LOGGER.warn("CapabilityProvider with no arg called");
//    }

    public CapabilityProvider(Entity entity) {
        this.data = new CapabilityInfoHolderImpl(entity);
        this.dataOptional = LazyOptional.of(()->data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityInfoHolder.TARGET_HOLDER) {
            return dataOptional.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public ListTag serializeNBT() {
        if (CapabilityInfoHolder.TARGET_HOLDER == null){
            return new ListTag();
        }else {
            return (ListTag) writeNBT(CapabilityInfoHolder.TARGET_HOLDER, data,null);
        }
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        if (CapabilityInfoHolder.TARGET_HOLDER != null){
            readNBT(CapabilityInfoHolder.TARGET_HOLDER, data,null,nbt);
        }
    }

    public Tag writeNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side) {
        ListTag listNBT = new ListTag();
        try {
            CompoundTag dataNBT = new CompoundTag();
            dataNBT.putLong("FallbackPosL", instance.getFallbackPosition().asLong());
            dataNBT.putInt("FallbackTimer", instance.getFallbackTimer());
            dataNBT.putLong("Destination", instance.getDestination().orElse(INVALID_POS).asLong());
            dataNBT.putDouble("CommandDistance", instance.getCommandDistance());
            dataNBT.putDouble("SelectDistance", instance.getSelectDistance());

            dataNBT.putLong("BreathTarget", instance.getBreathTarget().orElse(INVALID_POS).asLong());
            dataNBT.putLong("HomePosition", instance.getHomePosition().orElse(INVALID_POS).asLong());
            dataNBT.putString("HomeDimension", instance.getHomeDimension().orElse(""));
            dataNBT.putBoolean("ReturnRoost", instance.getReturnHome());
            dataNBT.putBoolean("ShouldSleep", instance.getShouldSleep());

            dataNBT.putInt("CommandStatus", instance.getObjectSetting(EnumCommandSettingType.COMMAND_STATUS).ordinal());
            dataNBT.putInt("GroundAttack", instance.getObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE).ordinal());
            dataNBT.putInt("AirAttack", instance.getObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE).ordinal());
            dataNBT.putInt("AttackDecision", instance.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE).ordinal());
            dataNBT.putInt("Movement", instance.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE).ordinal());
            dataNBT.putInt("Destroy", instance.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE).ordinal());
            dataNBT.putInt("Breath", instance.getObjectSetting(EnumCommandSettingType.BREATH_TYPE).ordinal());
//                dataNBT.putInt("ReturnRoost", ((Boolean) instance.getObjectSetting(EnumCommandSettingType.SHOULD_RETURN_ROOST)) ? 1 : 0);
            listNBT.add(dataNBT);

            List<UUID> uuids = instance.getCommandEntities();
            for (int i = 0; i < uuids.size(); i++) {
                CompoundTag uuidNBT = new CompoundTag();
                uuidNBT.putUUID(String.valueOf(i), uuids.get(i));
                listNBT.add(uuidNBT);
            }
        } catch (Exception e) {
            DragonTongue.LOGGER.warn("Error in writing custom cap: " + e.getMessage());
        }

        return listNBT;
    }

    public void readNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side, Tag nbt) {
        ListTag listNBT = (ListTag) nbt;
        try {
            CompoundTag dataNBT = listNBT.getCompound(0);
//                BlockPos blockPos = ;
//                int fallbackTimer = ;
////                CommandStatus commandStatus = CommandStatus.valueOf(dataNBT.getString("CommandStatus"));
//                BlockPos destination = ;
//                double commandDistance = ;

            instance.setFallbackPosition(BlockPos.of(dataNBT.getLong("FallbackPosL")));
            instance.setFallbackTimer(dataNBT.getInt("FallbackTimer"));
//                instance.setCommandStatus(commandStatus);

            BlockPos destinationPos = BlockPos.of(dataNBT.getLong("Destination"));
            instance.setDestination(!destinationPos.equals(INVALID_POS) ? destinationPos : null);
            instance.setCommandDistance(dataNBT.getDouble("CommandDistance"));
            instance.setSelectDistance(dataNBT.getDouble("SelectDistance"));

            BlockPos breathTarget = BlockPos.of(dataNBT.getLong("BreathTarget"));
            instance.setBreathTarget(!breathTarget.equals(INVALID_POS) ? breathTarget : null);
            BlockPos homePos = BlockPos.of(dataNBT.getLong("HomePosition"));
            instance.setHomePosition(!homePos.equals(INVALID_POS) ? homePos : null);
            instance.setHomeDimension(!homePos.equals(INVALID_POS) ? dataNBT.getString("HomeDimension") : "");
            instance.setReturnHome(dataNBT.getBoolean("ReturnRoost"));
            instance.setShouldSleep(dataNBT.getBoolean("ShouldSleep"));

            instance.setObjectSetting(
                    EnumCommandSettingType.COMMAND_STATUS, EnumCommandSettingType.CommandStatus.class.getEnumConstants()[dataNBT.getInt("CommandStatus")]);
            instance.setObjectSetting(
                    EnumCommandSettingType.GROUND_ATTACK_TYPE, EnumCommandSettingType.GroundAttackType.class.getEnumConstants()[dataNBT.getInt("GroundAttack")]);
            instance.setObjectSetting(
                    EnumCommandSettingType.AIR_ATTACK_TYPE, EnumCommandSettingType.AirAttackType.class.getEnumConstants()[dataNBT.getInt("AirAttack")]);
            instance.setObjectSetting(
                    EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.class.getEnumConstants()[dataNBT.getInt("AttackDecision")]);
            instance.setObjectSetting(
                    EnumCommandSettingType.MOVEMENT_TYPE, EnumCommandSettingType.MovementType.class.getEnumConstants()[dataNBT.getInt("Movement")]);
            instance.setObjectSetting(
                    EnumCommandSettingType.DESTROY_TYPE, EnumCommandSettingType.DestroyType.class.getEnumConstants()[dataNBT.getInt("Destroy")]);
            instance.setObjectSetting(
                    EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.class.getEnumConstants()[dataNBT.getInt("Breath")]);
//                instance.setObjectSetting(
//                        EnumCommandSettingType.SHOULD_RETURN_ROOST, dataNBT.getInt("ReturnRoost") == 1);

            List<UUID> uuids = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
            for (int i = 1; i < listNBT.size(); i++) {
                CompoundTag uuidNBT = listNBT.getCompound(i);
                uuids.add(uuidNBT.getUUID(String.valueOf(i - 1)));
            }
            instance.setCommandEntities(uuids);
        } catch (Exception e) {
            DragonTongue.LOGGER.warn("Error in retrieving custom cap data: " + e.getMessage());
        }
    }

}
