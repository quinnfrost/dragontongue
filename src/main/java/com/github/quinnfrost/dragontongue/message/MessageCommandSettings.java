package com.github.quinnfrost.dragontongue.message;

import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageCommandSettings {
    private UUID entityUUID;
    private EnumCommandSettingType objectSettings;
    private int objectValue;

    public MessageCommandSettings(Entity entity, EnumCommandSettingType objectSettings, Enum settingEnum) {
        this.entityUUID = entity.getUniqueID();
        this.objectSettings = objectSettings;
        this.objectValue = settingEnum.ordinal();
    }

    public MessageCommandSettings(PacketBuffer buffer) {
        this.entityUUID = buffer.readUniqueId();
        this.objectSettings = buffer.readEnumValue(EnumCommandSettingType.class);
        this.objectValue = buffer.readInt();
    }

    public void encoder(PacketBuffer buffer) {
        buffer.writeUniqueId(entityUUID);
        buffer.writeEnumValue(objectSettings);
        buffer.writeInt(objectValue);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            boolean response = contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER;
            ServerWorld serverWorld = contextSupplier.get().getSender().getServerWorld();
            Entity entity = serverWorld.getEntityByUuid(entityUUID);
            ICapTargetHolder cap = entity.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(entity));

            switch (objectSettings) {
                case COMMAND_STATUS:
                    cap.setObjectSetting(EnumCommandSettingType.COMMAND_STATUS, EnumCommandStatus.class.getEnumConstants()[objectValue]);
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
