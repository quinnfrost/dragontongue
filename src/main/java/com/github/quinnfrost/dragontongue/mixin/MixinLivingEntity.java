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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
    public abstract boolean isServerWorld();

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Attribute attribute);

    @Shadow
    public abstract boolean isPotionActive(MobEffect potionIn);

    @Shadow
    @Final
    private static AttributeModifier SLOW_FALLING;

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Shadow
    public abstract boolean canStandOnFluid(Fluid p_230285_1_);

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract float getAIMoveSpeed();

    @Shadow
    public abstract boolean isOnLadder();

    @Shadow
    public abstract Vec3 getFluidFallingAdjustedMovement(double p_233626_1_, boolean p_233626_3_, Vec3 p_233626_4_);

    @Shadow
    public abstract boolean isElytraFlying();

    @Shadow
    protected abstract SoundEvent getFallSound(int heightIn);

    @Shadow
    public abstract Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 p_233633_1_, float p_233633_2_);

    @Shadow
    @Nullable
    public abstract MobEffectInstance getActivePotionEffect(MobEffect potionIn);

    @Shadow
    public abstract void calculateEntityAnimation(LivingEntity p_233629_1_, boolean p_233629_2_);

    public MixinLivingEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(
            method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $travel(Vec3 travelVector, CallbackInfo ci) {
        roadblock$travel(travelVector);
        ci.cancel();
    }

    public void roadblock$travel(Vec3 travelVector) {
        if (this.isServerWorld() || this.isControlledByLocalInstance()) {
            double d0 = 0.08D;
            AttributeInstance gravity = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
            boolean flag = this.getDeltaMovement().y <= 0.0D;
            if (flag && this.isPotionActive(MobEffects.SLOW_FALLING)) {
                if (!gravity.hasModifier(SLOW_FALLING)) gravity.addTransientModifier(SLOW_FALLING);
                this.fallDistance = 0.0F;
            } else if (gravity.hasModifier(SLOW_FALLING)) {
                gravity.removeModifier(SLOW_FALLING);
            }
            d0 = gravity.getValue();

            FluidState fluidstate = this.level.getFluidState(this.blockPosition());
            if (
                    (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType()))
                            || (util.canSwimInLava(this) && this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType()))
            ) {
                util.mixinDebugger(this);
                double d8 = this.getY();
                float f5 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float f6 = 0.02F;
                float f7 = (float) EnchantmentHelper.getDepthStrider((LivingEntity) (Object) this);
                if (f7 > 3.0F) {
                    f7 = 3.0F;
                }

                if (!this.onGround) {
                    f7 *= 0.5F;
                }

                if (f7 > 0.0F) {
                    f5 += (0.54600006F - f5) * f7 / 3.0F;
                    f6 += (this.getAIMoveSpeed() - f6) * f7 / 3.0F;
                }

                if (this.isPotionActive(MobEffects.DOLPHINS_GRACE)) {
                    f5 = 0.96F;
                }

                f6 *= (float) this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
                this.moveRelative(f6, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 vector3d6 = this.getDeltaMovement();
                if (this.horizontalCollision && this.isOnLadder()) {
                    vector3d6 = new Vec3(vector3d6.x, 0.2D, vector3d6.z);
                }

                this.setDeltaMovement(vector3d6.multiply((double) f5, (double) 0.8F, (double) f5));
                Vec3 vector3d2 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                this.setDeltaMovement(vector3d2);
                if (this.horizontalCollision && this.isFree(vector3d2.x, vector3d2.y + (double) 0.6F - this.getY() + d8, vector3d2.z)) {
                    this.setDeltaMovement(vector3d2.x, (double) 0.3F, vector3d2.z);
                }
            } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType())) {
                double d7 = this.getY();
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, (double) 0.8F, 0.5D));
                    Vec3 vector3d3 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                    this.setDeltaMovement(vector3d3);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
                }

                if (!this.isNoGravity()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
                }

                Vec3 vector3d4 = this.getDeltaMovement();
                if (this.horizontalCollision && this.isFree(vector3d4.x, vector3d4.y + (double) 0.6F - this.getY() + d7, vector3d4.z)) {
                    this.setDeltaMovement(vector3d4.x, (double) 0.3F, vector3d4.z);
                }
            } else if (this.isElytraFlying()) {
                Vec3 vector3d = this.getDeltaMovement();
                if (vector3d.y > -0.5D) {
                    this.fallDistance = 1.0F;
                }

                Vec3 vector3d1 = this.getLookAngle();
                float f = this.xRot * ((float) Math.PI / 180F);
                double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
                double d3 = Math.sqrt(getHorizontalDistanceSqr(vector3d));
                double d4 = vector3d1.length();
                float f1 = Mth.cos(f);
                f1 = (float) ((double) f1 * (double) f1 * Math.min(1.0D, d4 / 0.4D));
                vector3d = this.getDeltaMovement().add(0.0D, d0 * (-1.0D + (double) f1 * 0.75D), 0.0D);
                if (vector3d.y < 0.0D && d1 > 0.0D) {
                    double d5 = vector3d.y * -0.1D * (double) f1;
                    vector3d = vector3d.add(vector3d1.x * d5 / d1, d5, vector3d1.z * d5 / d1);
                }

                if (f < 0.0F && d1 > 0.0D) {
                    double d9 = d3 * (double) (-Mth.sin(f)) * 0.04D;
                    vector3d = vector3d.add(-vector3d1.x * d9 / d1, d9 * 3.2D, -vector3d1.z * d9 / d1);
                }

                if (d1 > 0.0D) {
                    vector3d = vector3d.add((vector3d1.x / d1 * d3 - vector3d.x) * 0.1D, 0.0D, (vector3d1.z / d1 * d3 - vector3d.z) * 0.1D);
                }

                this.setDeltaMovement(vector3d.multiply((double) 0.99F, (double) 0.98F, (double) 0.99F));
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.horizontalCollision && !this.level.isClientSide) {
                    double d10 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
                    double d6 = d3 - d10;
                    float f2 = (float) (d6 * 10.0D - 3.0D);
                    if (f2 > 0.0F) {
                        this.playSound(this.getFallSound((int) f2), 1.0F, 1.0F);
                        this.hurt(DamageSource.FLY_INTO_WALL, f2);
                    }
                }

                if (this.onGround && !this.level.isClientSide) {
                    this.setSharedFlag(7, false);
                }
            } else {
                BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
                float f3 = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getSlipperiness(level, this.getBlockPosBelowThatAffectsMyMovement(), this);
                float f4 = this.onGround ? f3 * 0.91F : 0.91F;
                Vec3 vector3d5 = this.handleRelativeFrictionAndCalculateMovement(travelVector, f3);
                double d2 = vector3d5.y;
                if (this.isPotionActive(MobEffects.LEVITATION)) {
                    d2 += (0.05D * (double) (this.getActivePotionEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    this.fallDistance = 0.0F;
                } else if (this.level.isClientSide && !this.level.hasChunkAt(blockpos)) {
                    if (this.getY() > 0.0D) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                } else if (!this.isNoGravity()) {
                    d2 -= d0;
                }

                this.setDeltaMovement(vector3d5.x * (double) f4, d2 * (double) 0.98F, vector3d5.z * (double) f4);
            }
        }

        this.calculateEntityAnimation((LivingEntity) (Object) this, this instanceof FlyingAnimal);
    }

}
