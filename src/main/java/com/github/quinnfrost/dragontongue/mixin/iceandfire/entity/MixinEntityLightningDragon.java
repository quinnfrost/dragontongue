package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityLightningDragon;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityLightningDragon.class)
public abstract class MixinEntityLightningDragon extends EntityDragonBase {
    public MixinEntityLightningDragon(EntityType t, World world, DragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed) {
        super(t, world, type, minimumDamage, maximumDamage, minimumHealth, maximumHealth, minimumSpeed, maximumSpeed);
    }
}
