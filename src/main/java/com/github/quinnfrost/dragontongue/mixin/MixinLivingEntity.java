package com.github.quinnfrost.dragontongue.mixin;

import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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
    public abstract ModifiableAttributeInstance getAttribute(Attribute attribute);

    @Shadow
    public abstract boolean isPotionActive(Effect potionIn);

    @Shadow
    @Final
    private static AttributeModifier SLOW_FALLING;

    @Shadow
    protected abstract boolean func_241208_cS_();

    @Shadow
    public abstract boolean func_230285_a_(Fluid p_230285_1_);

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract float getAIMoveSpeed();

    @Shadow
    public abstract boolean isOnLadder();

    @Shadow
    public abstract Vector3d func_233626_a_(double p_233626_1_, boolean p_233626_3_, Vector3d p_233626_4_);

    @Shadow
    public abstract boolean isElytraFlying();

    @Shadow
    protected abstract SoundEvent getFallSound(int heightIn);

    @Shadow
    public abstract Vector3d func_233633_a_(Vector3d p_233633_1_, float p_233633_2_);

    @Shadow
    @Nullable
    public abstract EffectInstance getActivePotionEffect(Effect potionIn);

    @Shadow
    public abstract void func_233629_a_(LivingEntity p_233629_1_, boolean p_233629_2_);

    public MixinLivingEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(
            method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $travel(Vector3d travelVector, CallbackInfo ci) {
        roadblock$travel(travelVector);
        ci.cancel();
    }

    public void roadblock$travel(Vector3d travelVector) {
        if (this.isServerWorld() || this.canPassengerSteer()) {
            double d0 = 0.08D;
            ModifiableAttributeInstance gravity = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
            boolean flag = this.getMotion().y <= 0.0D;
            if (flag && this.isPotionActive(Effects.SLOW_FALLING)) {
                if (!gravity.hasModifier(SLOW_FALLING)) gravity.applyNonPersistentModifier(SLOW_FALLING);
                this.fallDistance = 0.0F;
            } else if (gravity.hasModifier(SLOW_FALLING)) {
                gravity.removeModifier(SLOW_FALLING);
            }
            d0 = gravity.getValue();

            FluidState fluidstate = this.world.getFluidState(this.getPosition());
            if (
                    (this.isInWater() && this.func_241208_cS_() && !this.func_230285_a_(fluidstate.getFluid()))
                            || (util.canSwimInLava(this) && this.isInLava() && this.func_241208_cS_() && !this.func_230285_a_(fluidstate.getFluid()))
            ) {
                double d8 = this.getPosY();
                float f5 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float f6 = 0.02F;
                float f7 = (float) EnchantmentHelper.getDepthStriderModifier((LivingEntity) (Object) this);
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

                if (this.isPotionActive(Effects.DOLPHINS_GRACE)) {
                    f5 = 0.96F;
                }

                f6 *= (float) this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
                this.moveRelative(f6, travelVector);
                this.move(MoverType.SELF, this.getMotion());
                Vector3d vector3d6 = this.getMotion();
                if (this.collidedHorizontally && this.isOnLadder()) {
                    vector3d6 = new Vector3d(vector3d6.x, 0.2D, vector3d6.z);
                }

                this.setMotion(vector3d6.mul((double) f5, (double) 0.8F, (double) f5));
                Vector3d vector3d2 = this.func_233626_a_(d0, flag, this.getMotion());
                this.setMotion(vector3d2);
                if (this.collidedHorizontally && this.isOffsetPositionInLiquid(vector3d2.x, vector3d2.y + (double) 0.6F - this.getPosY() + d8, vector3d2.z)) {
                    this.setMotion(vector3d2.x, (double) 0.3F, vector3d2.z);
                }
            } else if (this.isInLava() && this.func_241208_cS_() && !this.func_230285_a_(fluidstate.getFluid())) {
                double d7 = this.getPosY();
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getMotion());
                if (this.func_233571_b_(FluidTags.LAVA) <= this.getFluidJumpHeight()) {
                    this.setMotion(this.getMotion().mul(0.5D, (double) 0.8F, 0.5D));
                    Vector3d vector3d3 = this.func_233626_a_(d0, flag, this.getMotion());
                    this.setMotion(vector3d3);
                } else {
                    this.setMotion(this.getMotion().scale(0.5D));
                }

                if (!this.hasNoGravity()) {
                    this.setMotion(this.getMotion().add(0.0D, -d0 / 4.0D, 0.0D));
                }

                Vector3d vector3d4 = this.getMotion();
                if (this.collidedHorizontally && this.isOffsetPositionInLiquid(vector3d4.x, vector3d4.y + (double) 0.6F - this.getPosY() + d7, vector3d4.z)) {
                    this.setMotion(vector3d4.x, (double) 0.3F, vector3d4.z);
                }
            } else if (this.isElytraFlying()) {
                Vector3d vector3d = this.getMotion();
                if (vector3d.y > -0.5D) {
                    this.fallDistance = 1.0F;
                }

                Vector3d vector3d1 = this.getLookVec();
                float f = this.rotationPitch * ((float) Math.PI / 180F);
                double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
                double d3 = Math.sqrt(horizontalMag(vector3d));
                double d4 = vector3d1.length();
                float f1 = MathHelper.cos(f);
                f1 = (float) ((double) f1 * (double) f1 * Math.min(1.0D, d4 / 0.4D));
                vector3d = this.getMotion().add(0.0D, d0 * (-1.0D + (double) f1 * 0.75D), 0.0D);
                if (vector3d.y < 0.0D && d1 > 0.0D) {
                    double d5 = vector3d.y * -0.1D * (double) f1;
                    vector3d = vector3d.add(vector3d1.x * d5 / d1, d5, vector3d1.z * d5 / d1);
                }

                if (f < 0.0F && d1 > 0.0D) {
                    double d9 = d3 * (double) (-MathHelper.sin(f)) * 0.04D;
                    vector3d = vector3d.add(-vector3d1.x * d9 / d1, d9 * 3.2D, -vector3d1.z * d9 / d1);
                }

                if (d1 > 0.0D) {
                    vector3d = vector3d.add((vector3d1.x / d1 * d3 - vector3d.x) * 0.1D, 0.0D, (vector3d1.z / d1 * d3 - vector3d.z) * 0.1D);
                }

                this.setMotion(vector3d.mul((double) 0.99F, (double) 0.98F, (double) 0.99F));
                this.move(MoverType.SELF, this.getMotion());
                if (this.collidedHorizontally && !this.world.isRemote) {
                    double d10 = Math.sqrt(horizontalMag(this.getMotion()));
                    double d6 = d3 - d10;
                    float f2 = (float) (d6 * 10.0D - 3.0D);
                    if (f2 > 0.0F) {
                        this.playSound(this.getFallSound((int) f2), 1.0F, 1.0F);
                        this.attackEntityFrom(DamageSource.FLY_INTO_WALL, f2);
                    }
                }

                if (this.onGround && !this.world.isRemote) {
                    this.setFlag(7, false);
                }
            } else {
                BlockPos blockpos = this.getPositionUnderneath();
                float f3 = this.world.getBlockState(this.getPositionUnderneath()).getSlipperiness(world, this.getPositionUnderneath(), this);
                float f4 = this.onGround ? f3 * 0.91F : 0.91F;
                Vector3d vector3d5 = this.func_233633_a_(travelVector, f3);
                double d2 = vector3d5.y;
                if (this.isPotionActive(Effects.LEVITATION)) {
                    d2 += (0.05D * (double) (this.getActivePotionEffect(Effects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    this.fallDistance = 0.0F;
                } else if (this.world.isRemote && !this.world.isBlockLoaded(blockpos)) {
                    if (this.getPosY() > 0.0D) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                } else if (!this.hasNoGravity()) {
                    d2 -= d0;
                }

                this.setMotion(vector3d5.x * (double) f4, d2 * (double) 0.98F, vector3d5.z * (double) f4);
            }
        }

        this.func_233629_a_((LivingEntity) (Object) this, this instanceof IFlyingAnimal);
    }

}
