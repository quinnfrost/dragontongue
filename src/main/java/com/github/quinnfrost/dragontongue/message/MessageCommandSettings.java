package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageCommandSettings {
    private UUID entityUUID;
    private EnumCommandSettingType objectSettings;
    private int objectValue;

    public MessageCommandSettings(Entity entity, EnumCommandSettingType objectSettings, Enum settingEnum) {
        this.entityUUID = entity.getUUID();
        this.objectSettings = objectSettings;
        this.objectValue = settingEnum.ordinal();
    }

    public MessageCommandSettings(FriendlyByteBuf buffer) {
        this.entityUUID = buffer.readUUID();
        this.objectSettings = buffer.readEnum(EnumCommandSettingType.class);
        this.objectValue = buffer.readInt();
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeUUID(entityUUID);
        buffer.writeEnum(objectSettings);
        buffer.writeInt(objectValue);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            boolean response = contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER;
            ServerLevel serverWorld = contextSupplier.get().getSender().getLevel();
            Entity entity = serverWorld.getEntity(entityUUID);
            ICapabilityInfoHolder cap = entity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(entity));

            switch (objectSettings) {
                case COMMAND_STATUS:
                    cap.setObjectSetting(EnumCommandSettingType.COMMAND_STATUS, EnumCommandSettingType.CommandStatus.class.getEnumConstants()[objectValue]);
                    break;
                case GROUND_ATTACK_TYPE:
                    cap.setObjectSetting(EnumCommandSettingType.GROUND_ATTACK_TYPE, EnumCommandSettingType.GroundAttackType.class.getEnumConstants()[objectValue]);
                    break;
                case AIR_ATTACK_TYPE:
                    cap.setObjectSetting(EnumCommandSettingType.AIR_ATTACK_TYPE, EnumCommandSettingType.AirAttackType.class.getEnumConstants()[objectValue]);
                    break;
                case MOVEMENT_TYPE:
                    cap.setObjectSetting(EnumCommandSettingType.MOVEMENT_TYPE, EnumCommandSettingType.MovementType.class.getEnumConstants()[objectValue]);
                    break;
                case DESTROY_TYPE:
                    cap.setObjectSetting(EnumCommandSettingType.DESTROY_TYPE, EnumCommandSettingType.DestroyType.class.getEnumConstants()[objectValue]);
                    break;
                case BREATH_TYPE:
                    cap.setObjectSetting(EnumCommandSettingType.BREATH_TYPE, EnumCommandSettingType.BreathType.class.getEnumConstants()[objectValue]);
                    break;
            }

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                MessageSyncCapability.syncCapabilityToClients(entity);
            }



        });
        return true;
    }
}
