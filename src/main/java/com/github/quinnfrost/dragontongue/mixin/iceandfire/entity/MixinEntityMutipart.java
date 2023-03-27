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


    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        wasTouchingWater = false;
        if (this.tickCount > 10) {
            Entity parent = getParent();
            refreshDimensions();
            if (parent != null && !level.isClientSide) {
                float renderYawOffset = parent.getYRot();
                if (parent instanceof LivingEntity) {
                    renderYawOffset = ((LivingEntity) parent).yBodyRot;
                }
                if (isSlowFollow()) {
                    this.setPos(parent.xo + this.radius * Mth.cos((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)), parent.yo + this.offsetY, parent.zo + this.radius * Mth.sin((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)));
                    double d0 = parent.getX() - this.getX();
                    double d1 = parent.getY() - this.getY();
                    double d2 = parent.getZ() - this.getZ();
                    float f2 = -((float) (Mth.atan2(d1, Mth.sqrt((float) (d0 * d0 + d2 * d2))) * (180F / (float) Math.PI)));
                    this.setXRot(this.limitAngle(this.getXRot(), f2, 5.0F));
                    this.markHurt();
                    this.setYRot(renderYawOffset);
                    this.setPartYaw(getYRot());
                    if (!this.level.isClientSide) {
                        this.collideWithNearbyEntities();
                    }
                } else {
                    this.setPos(parent.getX() + this.radius * Mth.cos((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)), parent.getY() + this.offsetY, parent.getZ() + this.radius * Mth.sin((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)));
                    this.markHurt();
                }
                if (!this.level.isClientSide) {
                    this.collideWithNearbyEntities();
                }
                if (parent.isRemoved() && !level.isClientSide) {
                    this.remove(RemovalReason.DISCARDED);
                }
            } else if (tickCount > 20 && !level.isClientSide) {
                remove(RemovalReason.DISCARDED);
            }
        }
        super.tick();
    }

}
