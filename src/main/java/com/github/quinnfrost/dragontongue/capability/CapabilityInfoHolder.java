package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.config.Config;
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
import javax.xml.stream.events.DTD;
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

    // TODO: 改用NBTTagList
    //       数据同步
    public static class Storage implements Capability.IStorage<ICapabilityInfoHolder>{

        @Nullable
        @Override
        public INBT writeNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side) {
            ListNBT listNBT = new ListNBT();

            CompoundNBT dataNBT = new CompoundNBT();
            dataNBT.putLong("PosL",instance.getPos().toLong());
            dataNBT.putInt("FallbackTimer",instance.getFallbackTimer());
            dataNBT.putBoolean("HasDestination",instance.getDestinationSet());
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
        public void readNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side, INBT nbt) {
            try {
                ListNBT listNBT = (ListNBT) nbt;

                CompoundNBT dataNBT = listNBT.getCompound(0);
                BlockPos blockPos = BlockPos.fromLong(dataNBT.getLong("PosL"));
                int fallbackTimer = dataNBT.getInt("FallbackTimer");
                boolean destinationSet = dataNBT.getBoolean("HasDestination");

                instance.setPos(blockPos);
                instance.setFallbackTimer(fallbackTimer);
                instance.setDestinationSet(destinationSet);

                List<UUID> uuids = new ArrayList<>(Config.COMMAND_ENTITIES_MAX.get());
                for (int i = 1; i < listNBT.size(); i++) {
                    CompoundNBT uuidNBT = listNBT.getCompound(i);
                    uuids.add(uuidNBT.getUniqueId(String.valueOf(i - 1)));
                }
                instance.setCommandEntities(uuids);
            } catch (Exception e){
                e.printStackTrace();
                DragonTongue.LOGGER.warn("Cannot case NBT to ListNBT");
            }

        }
    }

}
