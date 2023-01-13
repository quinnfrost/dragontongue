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
        return dataOptional.cast();
    }

    @Override
    public CompoundNBT serializeNBT() {
        if (CapabilityInfoHolder.ENTITY_TEST_CAPABILITY == null){
            return new CompoundNBT();
        }else {
            return (CompoundNBT) CapabilityInfoHolder.ENTITY_TEST_CAPABILITY.writeNBT(data,null);
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (CapabilityInfoHolder.ENTITY_TEST_CAPABILITY != null){
            CapabilityInfoHolder.ENTITY_TEST_CAPABILITY.readNBT(data,null,nbt);
        }
    }

}
