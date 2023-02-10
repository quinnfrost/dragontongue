package com.github.quinnfrost.dragontongue.iceandfire.mixin;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.DragonTongue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityDragonBase.class)
public abstract class MixinTestClass extends TameableEntity {

    protected MixinTestClass(EntityType<? extends TameableEntity> type, World worldIn) {
        super(type, worldIn);
    }



}
