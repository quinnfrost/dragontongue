package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CapabilityInfoHolder {
    @CapabilityInject(ICapabilityInfoHolder.class)
    public static Capability<ICapabilityInfoHolder> ENTITY_DATA_STORAGE = null;

    public static void register(){
        CapabilityManager.INSTANCE.register(ICapabilityInfoHolder.class,new Storage(),CapabilityInfoHolderImplementation::new);
    }

    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof LivingEntity){
            CapabilityInfoHolderProvider provider = new CapabilityInfoHolderProvider();
            event.addCapability(new ResourceLocation(References.MOD_ID,"dragonstaff"),provider);
            event.addListener(provider::invalidate);
        }
    }

    public static class Storage implements Capability.IStorage<ICapabilityInfoHolder>{

        @Nullable
        @Override
        public INBT writeNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side) {
            CompoundNBT nbt = new CompoundNBT();
            List<Long> uuidM = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
            List<Long> uuidL = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
            for (UUID uuid:instance.getCommandEntities()) {
                uuidM.add(uuid.getMostSignificantBits());
                uuidL.add(uuid.getLeastSignificantBits());
            }
            nbt.putLongArray("EntitiesUUIDM",uuidM);
            nbt.putLongArray("EntitiesUUIDL",uuidL);
//            nbt.putUniqueId("LastCommand",instance.getUUID());
            nbt.putLong("PosL",instance.getPos().toLong());
            nbt.putInt("FallbackTimer",instance.getFallbackTimer());
            nbt.putBoolean("HasDestination",instance.getDestinationSet());
            return nbt;
        }

        @Override
        public void readNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side, INBT nbt) {
            try {
                List<Long> uuidM = Arrays.stream(((CompoundNBT)nbt).getLongArray("EntitiesUUIDM")).boxed().collect(Collectors.toList());
                List<Long> uuidL = Arrays.stream(((CompoundNBT)nbt).getLongArray("EntitiesUUIDL")).boxed().collect(Collectors.toList());
                List<UUID> uuids = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
                for (int i = 0; i < Config.COMMAND_ENTITIES_MAX.get(); i++) {
                    UUID uuid = new UUID(uuidM.get(i), uuidL.get(i));
                    uuids.add(uuid);
                }
                instance.setCommandEntities(uuids);
            } catch (Exception ignored)  {

            }


//            UUID uuid = ((CompoundNBT)nbt).getUniqueId("LastCommand");
            BlockPos blockPos = BlockPos.fromLong(((CompoundNBT) nbt).getLong("PosL"));
            int fallbackTimer = ((CompoundNBT)nbt).getInt("FallbackTimer");
            boolean destinationSet = ((CompoundNBT)nbt).getBoolean("HasDestination");

//            instance.setUUID(uuid);
            instance.setPos(blockPos);
            instance.setFallbackTimer(fallbackTimer);
            instance.setDestinationSet(destinationSet);
        }
    }

}
