package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.quinnfrost.dragontongue.DragonTongue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IafTestClass {
    public static void getDragonData(@Nullable Entity entity) {
        if (!DragonTongue.isIafPresent || !(entity instanceof EntityDragonBase)) {return;}
        DragonTongue.LOGGER.info("Found dragon type {}", ((EntityIceDragon)entity).dragonType);
    }

    public static boolean setDragonAttackTarget(@Nullable LivingEntity entity, @Nullable LivingEntity target) {
        if (!DragonTongue.isIafPresent || !(entity instanceof EntityDragonBase)) {return false;}
        EntityDragonBase dragon = (EntityDragonBase) entity;
        dragon.setAttackTarget(target);
        return true;
    }

    public static boolean setDragonFlightTarget(@Nullable LivingEntity entity, BlockPos blockPos) {
        if (!DragonTongue.isIafPresent || !(entity instanceof EntityDragonBase)) {return false;}
        EntityDragonBase dragon = (EntityDragonBase) entity;

        dragon.flightManager.setFlightTarget(new Vector3d(blockPos.getX(),blockPos.getY(),blockPos.getZ()));
        return true;
    }

}
