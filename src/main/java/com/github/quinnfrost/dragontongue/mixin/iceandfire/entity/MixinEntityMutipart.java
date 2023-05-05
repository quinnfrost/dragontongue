package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityMutlipartPart.class)
public abstract class MixinEntityMutipart extends Entity {

    @Shadow public abstract Entity getParent();

    @Shadow protected abstract boolean isSlowFollow();

    @Shadow protected float radius;

    @Shadow protected abstract float limitAngle(float sourceAngle, float targetAngle, float maximumChange);

    @Shadow public abstract void collideWithNearbyEntities();

    @Shadow protected abstract void setPartYaw(float yaw);

    @Shadow protected float angleYaw;

    @Shadow protected float offsetY;

    public MixinEntityMutipart(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

}
