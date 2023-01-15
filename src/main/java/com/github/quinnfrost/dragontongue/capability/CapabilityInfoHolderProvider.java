package com.github.quinnfrost.dragontongue.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityInfoHolderProvider implements ICapabilitySerializable<CompoundNBT> {
    private final CapabilityInfoHolderImplementation data = new CapabilityInfoHolderImplementation();
    private final LazyOptional<ICapabilityInfoHolder> dataOptional = LazyOptional.of(()->data);

    public void invalidate(){
        dataOptional.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityInfoHolder.ENTITY_DATA_STORAGE) {
            return dataOptional.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        if (CapabilityInfoHolder.ENTITY_DATA_STORAGE == null){
            return new CompoundNBT();
        }else {
            return (CompoundNBT) CapabilityInfoHolder.ENTITY_DATA_STORAGE.writeNBT(data,null);
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (CapabilityInfoHolder.ENTITY_DATA_STORAGE != null){
            CapabilityInfoHolder.ENTITY_DATA_STORAGE.readNBT(data,null,nbt);
        }
    }

}
