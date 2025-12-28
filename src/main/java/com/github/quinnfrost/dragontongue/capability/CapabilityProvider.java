package com.github.quinnfrost.dragontongue.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilitySerializable<ListTag> {
    private final CapabilityInfoHolderImpl data;
    private final LazyOptional<ICapabilityInfoHolder> dataOptional;

    public void invalidate(){
        dataOptional.invalidate();
    }

//    public CapabilityProvider() {
//        this.data = new CapabilityInfoHolderImpl();
//        this.dataOptional = LazyOptional.of(()->data);
//        DragonTongue.LOGGER.warn("CapabilityProvider with no arg called");
//    }

    public CapabilityProvider(Entity entity) {
        this.data = new CapabilityInfoHolderImpl(entity);
        this.dataOptional = LazyOptional.of(()->data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityInfoHolder.TARGET_HOLDER) {
            return dataOptional.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public ListTag serializeNBT() {
        if (CapabilityInfoHolder.TARGET_HOLDER == null){
            return new ListTag();
        }else {
            return (ListTag) CapabilityInfoHolder.TARGET_HOLDER.writeNBT(data,null);
        }
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        if (CapabilityInfoHolder.TARGET_HOLDER != null){
            CapabilityInfoHolder.TARGET_HOLDER.readNBT(data,null,nbt);
        }
    }

}
