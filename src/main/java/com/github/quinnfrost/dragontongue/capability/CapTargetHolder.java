package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.config.Config;
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

// TODO: 检查实体身上错误的caps并且重置（解决ConcurrentModificationException问题）
public class CapTargetHolder {
    @CapabilityInject(ICapTargetHolder.class)
    public static Capability<ICapTargetHolder> TARGET_HOLDER = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ICapTargetHolder.class,new Storage(), CapTargetHolderImpl::new);
    }

    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity){
            CapabilityProvider provider = new CapabilityProvider(event.getObject());
            event.addCapability(new ResourceLocation(References.MOD_ID,"dragontongue"), provider);
            event.addListener(provider::invalidate);
        }
    }

    public static class Storage implements Capability.IStorage<ICapTargetHolder> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<ICapTargetHolder> capability, ICapTargetHolder instance, Direction side) {
            ListNBT listNBT = new ListNBT();

            CompoundNBT dataNBT = new CompoundNBT();
            dataNBT.putLong("FallbackPosL",instance.getFallbackPosition().toLong());
            dataNBT.putInt("FallbackTimer",instance.getFallbackTimer());
            dataNBT.putString("CommandStatus",instance.getCommandStatus().toString());
            dataNBT.putLong("Destination",instance.getDestination().toLong());
            dataNBT.putDouble("CommandDistance",instance.getCommandDistance());
            listNBT.add(dataNBT);

            List<UUID> uuids = instance.getCommandEntities();
            for (int i = 0; i < uuids.size(); i++) {
                CompoundNBT uuidNBT = new CompoundNBT();
                uuidNBT.putUniqueId(String.valueOf(i),uuids.get(i));
                listNBT.add(uuidNBT);
            }

            return listNBT;
        }

        @Override
        public void readNBT(Capability<ICapTargetHolder> capability, ICapTargetHolder instance, Direction side, INBT nbt) {
            ListNBT listNBT = (ListNBT) nbt;

            CompoundNBT dataNBT = listNBT.getCompound(0);
            BlockPos blockPos = BlockPos.fromLong(dataNBT.getLong("FallbackPosL"));
            int fallbackTimer = dataNBT.getInt("FallbackTimer");
            EnumCommandStatus commandStatus = EnumCommandStatus.valueOf(dataNBT.getString("CommandStatus"));
            BlockPos destination = BlockPos.fromLong(dataNBT.getLong("Destination"));
            double commandDistance = dataNBT.getDouble("CommandDistance");

            instance.setFallbackPosition(blockPos);
            instance.setFallbackTimer(fallbackTimer);
            instance.setCommandStatus(commandStatus);
            instance.setDestination(destination);
            instance.setCommandDistance(commandDistance);

            List<UUID> uuids = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
            for (int i = 1; i < listNBT.size(); i++) {
                CompoundNBT uuidNBT = listNBT.getCompound(i);
                uuids.add(uuidNBT.getUniqueId(String.valueOf(i - 1)));
            }
            instance.setCommandEntities(uuids);

        }
    }

}
