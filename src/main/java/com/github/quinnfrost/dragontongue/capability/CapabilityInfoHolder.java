package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.References;
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
import java.util.UUID;

public class CapabilityInfoHolder {
    @CapabilityInject(ICapabilityInfoHolder.class)
    public static Capability<ICapabilityInfoHolder> ENTITY_TEST_CAPABILITY = null;

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
            nbt.putUniqueId("LastCommand",instance.getUUID());
            nbt.putLong("PosL",instance.getPos().toLong());
            nbt.putInt("FallbackTimer",instance.getFallbackTimer());
            nbt.putBoolean("HasDestination",instance.getDestinationSet());
            return nbt;
        }

        @Override
        public void readNBT(Capability<ICapabilityInfoHolder> capability, ICapabilityInfoHolder instance, Direction side, INBT nbt) {
            UUID uuid = ((CompoundNBT)nbt).getUniqueId("LastCommand");
            BlockPos blockPos = BlockPos.fromLong(((CompoundNBT) nbt).getLong("PosL"));
            int fallbackTimer = ((CompoundNBT)nbt).getInt("FallbackTimer");
            boolean destinationSet = ((CompoundNBT)nbt).getBoolean("HasDestination");

            instance.setUUID(uuid);
            instance.setPos(blockPos);
            instance.setFallbackTimer(fallbackTimer);
            instance.setDestinationSet(destinationSet);
        }
    }

}
