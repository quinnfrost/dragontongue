package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.DragonTongue;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilitySerializable<ListNBT> {
    private final CapTargetHolderImpl data;
    private final LazyOptional<ICapTargetHolder> dataOptional;

    public void invalidate(){
        dataOptional.invalidate();
    }

//    public CapabilityProvider() {
//        this.data = new CapTargetHolderImpl();
//        this.dataOptional = LazyOptional.of(()->data);
//        DragonTongue.LOGGER.warn("CapabilityProvider with no arg called");
//    }

    public CapabilityProvider(Entity entity) {
        this.data = new CapTargetHolderImpl(entity);
        this.dataOptional = LazyOptional.of(()->data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapTargetHolder.TARGET_HOLDER) {
            return dataOptional.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public ListNBT serializeNBT() {
        if (CapTargetHolder.TARGET_HOLDER == null){
            return new ListNBT();
        }else {
            return (ListNBT) CapTargetHolder.TARGET_HOLDER.writeNBT(data,null);
        }
    }

    @Override
    public void deserializeNBT(ListNBT nbt) {
        if (CapTargetHolder.TARGET_HOLDER != null){
            CapTargetHolder.TARGET_HOLDER.readNBT(data,null,nbt);
        }
    }

}
