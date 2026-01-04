package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = References.MOD_ID)
public class CapabilityInfoHolder {
    public static final BlockPos INVALID_POS = new BlockPos(0, 0, 0);

    public static final Capability<ICapabilityInfoHolder> TARGET_HOLDER = CapabilityManager.get(new CapabilityToken<>(){});

    @Mod.EventBusSubscriber(modid = References.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void register(RegisterCapabilitiesEvent event) {
            event.register(ICapabilityInfoHolder.class);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            CapabilityProvider provider = new CapabilityProvider(event.getObject());
            event.addCapability(new ResourceLocation(References.MOD_ID, "extend_command_data"), provider);
            event.addListener(provider::invalidate);
        }
    }

    public static Tag writeNBT(ICapabilityInfoHolder instance) {
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

    public static void readNBT(ICapabilityInfoHolder instance, Tag nbt) {
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

