package com.github.quinnfrost.dragontongue.mixin;

import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Attribute attribute);

    @Shadow
    @Final
    private static AttributeModifier SLOW_FALLING;

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract Vec3 getFluidFallingAdjustedMovement(double p_233626_1_, boolean p_233626_3_, Vec3 p_233626_4_);

    @Shadow
    public abstract Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 p_233633_1_, float p_233633_2_);

    @Shadow
    public abstract void calculateEntityAnimation(LivingEntity p_233629_1_, boolean p_233629_2_);

    @Shadow
    public abstract boolean hasEffect(MobEffect pEffect);

    @Shadow
    public abstract boolean isEffectiveAi();

    @Shadow
    public abstract boolean canStandOnFluid(FluidState p_204042_);

    @Shadow
    public abstract float getSpeed();

    @Shadow
    public abstract boolean onClimbable();

    public MixinLivingEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

//    @Inject(
//            method = "travel",
//            at = @At(value = "HEAD"),
//            cancellable = true
//    )
//    public void inject$travel(Vec3 travelVector, CallbackInfo ci) {
//        if (head$travel(travelVector)) {
//            ci.cancel();
//        }
//    }

    public boolean head$travel(Vec3 pTravelVector) {
        boolean shouldCancel = false;
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            double d0 = 0.08D;
            AttributeInstance gravity = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
            boolean flag = this.getDeltaMovement().y <= 0.0D;
            if (flag && this.hasEffect(MobEffects.SLOW_FALLING)) {
                if (!gravity.hasModifier(SLOW_FALLING)) gravity.addTransientModifier(SLOW_FALLING);
                this.resetFallDistance();
            } else if (gravity.hasModifier(SLOW_FALLING)) {
                gravity.removeModifier(SLOW_FALLING);
            }
            d0 = gravity.getValue();

            FluidState fluidstate = this.level.getFluidState(this.blockPosition());
            if (
                    (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate))
                            || (util.canSwimInLava(this) && this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate))
            ) {
                double d9 = this.getY();
                float f4 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float f5 = 0.02F;
                float f6 = (float) EnchantmentHelper.getDepthStrider((LivingEntity) (Object) this);
                if (f6 > 3.0F) {
                    f6 = 3.0F;
                }

                if (!this.onGround) {
                    f6 *= 0.5F;
                }

                if (f6 > 0.0F) {
                    f4 += (0.54600006F - f4) * f6 / 3.0F;
                    f5 += (this.getSpeed() - f5) * f6 / 3.0F;
                }

                if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    f4 = 0.96F;
                }

                f5 *= (float) this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
                this.moveRelative(f5, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 vec36 = this.getDeltaMovement();
                if (this.horizontalCollision && this.onClimbable()) {
                    vec36 = new Vec3(vec36.x, 0.2D, vec36.z);
                }

                this.setDeltaMovement(vec36.multiply((double) f4, (double) 0.8F, (double) f4));
                Vec3 vec32 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                this.setDeltaMovement(vec32);
                if (this.horizontalCollision && this.isFree(vec32.x, vec32.y + (double) 0.6F - this.getY() + d9, vec32.z)) {
                    this.setDeltaMovement(vec32.x, (double) 0.3F, vec32.z);
                }


                shouldCancel = true;

                this.calculateEntityAnimation((LivingEntity) (Object) this, this instanceof FlyingAnimal);
            }
        }
        return shouldCancel;
    }
}
