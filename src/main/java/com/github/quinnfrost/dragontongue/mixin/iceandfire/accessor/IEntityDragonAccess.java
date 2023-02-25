package com.github.quinnfrost.dragontongue.mixin.iceandfire.accessor;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityDragonBase.class)
public interface IEntityDragonAccess {
    @Accessor("blockBreakCounter")
    int getBlockBreakCounter();
    @Accessor("blockBreakCounter")
    void setBlockBreakCounter(int counter);
    @Accessor("flyHovering")
    int getFlyHovering();
    @Accessor("flyHovering")
    void setFlyHovering(int flyHovering);
    @Accessor("fireTicks")
    int getFireTicks();
    @Accessor("fireTicks")
    void setFireTicks(int fireTicks);

    @Invoker("isOverAir")
    boolean invoke$isOverAir();
    @Invoker("updateBurnTarget")
    void invoke$updateBurnTarget();
    @Invoker("updateAttributes")
    void invoke$updateAttributes();
    @Invoker("switchNavigator")
    void invoke$switchNavigator(int type);
    @Invoker("getFlightChancePerTick")
    int invoke$getFlightChancePerTick();
    @Invoker("isIceInWater")
    boolean invoke$isIceInWater();

}
