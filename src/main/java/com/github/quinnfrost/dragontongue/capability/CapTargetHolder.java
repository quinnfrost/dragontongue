package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapTargetHolder {
    public static final BlockPos INVALID_POS = new BlockPos(0, 0, 0);
    @CapabilityInject(ICapTargetHolder.class)
    public static Capability<ICapTargetHolder> TARGET_HOLDER = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ICapTargetHolder.class, new Storage(), CapTargetHolderImpl::new);
    }

    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            CapabilityProvider provider = new CapabilityProvider(event.getObject());
            event.addCapability(new ResourceLocation(References.MOD_ID, "dragontongue"), provider);
            event.addListener(provider::invalidate);
        }
    }

    public static class Storage implements Capability.IStorage<ICapTargetHolder> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<ICapTargetHolder> capability, ICapTargetHolder instance, Direction side) {
            ListNBT listNBT = new ListNBT();
            try {
                CompoundNBT dataNBT = new CompoundNBT();
                dataNBT.putLong("FallbackPosL", instance.getFallbackPosition().toLong());
                dataNBT.putInt("FallbackTimer", instance.getFallbackTimer());
                dataNBT.putLong("Destination", instance.getDestination().toLong());
                dataNBT.putDouble("CommandDistance", instance.getCommandDistance());
                dataNBT.putDouble("SelectDistance", instance.getSelectDistance());

                dataNBT.putLong("BreathTarget", instance.getBreathTarget().orElse(new BlockPos(0, 0, 0)).toLong());
                dataNBT.putLong("HomePosition", instance.getHomePosition().orElse(new BlockPos(0, 0, 0)).toLong());

                dataNBT.putInt("CommandStatus", ((Enum) instance.getObjectSetting(EnumCommandSettingType.COMMAND_STATUS)).ordinal());
                dataNBT.putInt("GroundAttack", ((Enum) instance.getObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE)).ordinal());
                dataNBT.putInt("AirAttack", ((Enum) instance.getObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE)).ordinal());
                dataNBT.putInt("Movement", ((Enum) instance.getObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE)).ordinal());
                dataNBT.putInt("Destroy", ((Enum) instance.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE)).ordinal());
                dataNBT.putInt("Breath", ((Enum) instance.getObjectSetting(EnumCommandSettingType.BREATH_TYPE)).ordinal());
                dataNBT.putInt("ReturnRoost", ((Boolean) instance.getObjectSetting(EnumCommandSettingType.SHOULD_RETURN_ROOST)) ? 1 : 0);
                listNBT.add(dataNBT);

                List<UUID> uuids = instance.getCommandEntities();
                for (int i = 0; i < uuids.size(); i++) {
                    CompoundNBT uuidNBT = new CompoundNBT();
                    uuidNBT.putUniqueId(String.valueOf(i), uuids.get(i));
                    listNBT.add(uuidNBT);
                }
            } catch (Exception e) {
                DragonTongue.LOGGER.warn("Error in writing custom cap: " + e.getMessage());
            }

            return listNBT;
        }

        @Override
        public void readNBT(Capability<ICapTargetHolder> capability, ICapTargetHolder instance, Direction side, INBT nbt) {
            ListNBT listNBT = (ListNBT) nbt;
            try {
                CompoundNBT dataNBT = listNBT.getCompound(0);
//                BlockPos blockPos = ;
//                int fallbackTimer = ;
////                EnumCommandStatus commandStatus = EnumCommandStatus.valueOf(dataNBT.getString("CommandStatus"));
//                BlockPos destination = ;
//                double commandDistance = ;

                instance.setFallbackPosition(BlockPos.fromLong(dataNBT.getLong("FallbackPosL")));
                instance.setFallbackTimer(dataNBT.getInt("FallbackTimer"));
//                instance.setCommandStatus(commandStatus);
                instance.setDestination(BlockPos.fromLong(dataNBT.getLong("Destination")));
                instance.setCommandDistance(dataNBT.getDouble("CommandDistance"));
                instance.setSelectDistance(dataNBT.getDouble("SelectDistance"));

                BlockPos breathTarget = BlockPos.fromLong(dataNBT.getLong("BreathTarget"));
                instance.setBreathTarget(!breathTarget.equals(INVALID_POS) ? breathTarget : null);
                BlockPos homePos = BlockPos.fromLong(dataNBT.getLong("HomePosition"));
                instance.setHomePosition(!homePos.equals(INVALID_POS) ? homePos : null);

                // Todo: serializer!
                instance.setObjectSetting(
                        EnumCommandSettingType.COMMAND_STATUS, EnumCommandStatus.class.getEnumConstants()[dataNBT.getInt("CommandStatus")]);
                instance.setObjectSetting(
                        EnumCommandSettingType.GROUND_ATTACK_TYPE, EnumCommandSettingType.GroundAttackType.class.getEnumConstants()[dataNBT.getInt("GroundAttack")]);
                instance.setObjectSetting(
                        EnumCommandSettingType.AIR_ATTACK_TYPE, EnumCommandSettingType.AirAttackType.class.getEnumConstants()[dataNBT.getInt("AirAttack")]);
                instance.setObjectSetting(
                        EnumCommandSettingType.MOVEMENT_TYPE, EnumCommandSettingType.MovementType.class.getEnumConstants()[dataNBT.getInt("Movement")]);
                instance.setObjectSetting(
                        EnumCommandSettingType.DESTROY_TYPE, EnumCommandSettingType.DestroyType.class.getEnumConstants()[dataNBT.getInt("Destroy")]);
                instance.setObjectSetting(
                        EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.class.getEnumConstants()[dataNBT.getInt("Breath")]);
                instance.setObjectSetting(
                        EnumCommandSettingType.SHOULD_RETURN_ROOST, dataNBT.getInt("ReturnRoost") == 1);

                List<UUID> uuids = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
                for (int i = 1; i < listNBT.size(); i++) {
                    CompoundNBT uuidNBT = listNBT.getCompound(i);
                    uuids.add(uuidNBT.getUniqueId(String.valueOf(i - 1)));
                }
                instance.setCommandEntities(uuids);
            } catch (Exception e) {
                DragonTongue.LOGGER.warn("Error in retrieving custom cap data: " + e.getMessage());
            }
        }
    }

}
