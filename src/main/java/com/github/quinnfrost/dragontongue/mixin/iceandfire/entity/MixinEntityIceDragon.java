package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.event.DragonFireEvent;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.enums.EnumParticles;
import com.github.alexthe666.iceandfire.message.MessageDragonSyncFire;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityIceDragon.class)
public abstract class MixinEntityIceDragon extends EntityDragonBase {
    @Shadow
    protected abstract boolean isIceInWater();

    @Shadow
    public abstract boolean isInMaterialWater();

    public MixinEntityIceDragon(EntityType t, Level world, DragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed) {
        super(t, world, type, minimumDamage, maximumDamage, minimumHealth, maximumHealth, minimumSpeed, maximumSpeed);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98f;
    }

    @Override
    public void travel(Vec3 travelVector) {
//        if (this.isVehicle() && this.canBeControlledByRider()) {
//            Vec3 pTravelVector = this.getDeltaMovement();
//            LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
//            float f = livingentity.xxa * 0.5F;
//            float f1 = livingentity.zza;
//            if (this.isControlledByLocalInstance()) {
//                this.setSpeed(1);
//                super.travel(new Vec3((double) f, pTravelVector.y, (double) f1));
//            } else if (livingentity instanceof Player) {
//                this.setDeltaMovement(Vec3.ZERO);
//            }
//        }
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Inject(
            remap = false,
            method = "isInMaterialWater",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$isInMaterialWater(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(head$isInMaterialWater());
        cir.cancel();
    }

    public boolean head$isInMaterialWater() {
        return isInWater();
    }

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/entity/EntityIceDragon;useFlyingPathFinder()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$useFlyingPathFinder(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(head$useFlyingPathFinder());
        cir.cancel();
    }
    public boolean head$useFlyingPathFinder() {
        return super.useFlyingPathFinder() || this.isInMaterialWater();
    }

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/entity/EntityIceDragon;stimulateFire(DDDI)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$stimulateFire(double burnX, double burnY, double burnZ, int syncType, CallbackInfo ci) {
        head$stimulateFire(burnX, burnY, burnZ, syncType);
        ci.cancel();
    }
    public void head$stimulateFire(double burnX, double burnY, double burnZ, int syncType) {
        if (MinecraftForge.EVENT_BUS.post(new DragonFireEvent(this, burnX, burnY, burnZ))) return;
        if (syncType == 1 && !level.isClientSide) {
            //sync with client
            IceAndFire.sendMSGToAll(new MessageDragonSyncFire(this.getId(), burnX, burnY, burnZ, 0));
        }
        if (syncType == 2 && level.isClientSide) {
            //sync with server
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonSyncFire(this.getId(), burnX, burnY, burnZ, 0));
        }
        if (syncType == 3 && !level.isClientSide) {
            //sync with client, fire bomb
            IceAndFire.sendMSGToAll(new MessageDragonSyncFire(this.getId(), burnX, burnY, burnZ, 5));
        }
        if (syncType == 4 && level.isClientSide) {
            //sync with server, fire bomb
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonSyncFire(this.getId(), burnX, burnY, burnZ, 5));
        }
        if (syncType > 2 && syncType < 6) {
            if (this.getAnimation() != ANIMATION_FIRECHARGE) {
                this.setAnimation(ANIMATION_FIRECHARGE);
            } else if (this.getAnimationTick() == 20) {
                yRot = yBodyRot;
                Vec3 headVec = this.getHeadPosition();
                double d2 = burnX - headVec.x;
                double d3 = burnY - headVec.y;
                double d4 = burnZ - headVec.z;
                float inaccuracy = 1.0F;
                d2 = d2 + this.random.nextGaussian() * 0.007499999832361937D * inaccuracy;
                d3 = d3 + this.random.nextGaussian() * 0.007499999832361937D * inaccuracy;
                d4 = d4 + this.random.nextGaussian() * 0.007499999832361937D * inaccuracy;
                this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                EntityDragonIceCharge entitylargefireball = new EntityDragonIceCharge(
                        IafEntityRegistry.ICE_DRAGON_CHARGE.get(), level, this, d2, d3, d4);
                float size = this.isBaby() ? 0.4F : this.shouldDropLoot() ? 1.3F : 0.8F;
                entitylargefireball.setPos(headVec.x, headVec.y, headVec.z);
                if (!level.isClientSide) {
                    level.addFreshEntity(entitylargefireball);
                }
            }
            return;
        }
        this.getNavigation().stop();
        this.burnParticleX = burnX;
        this.burnParticleY = burnY;
        this.burnParticleZ = burnZ;
        Vec3 headPos = getHeadPosition();
        double d2 = burnX - headPos.x;
        double d3 = burnY - headPos.y;
        double d4 = burnZ - headPos.z;
        float particleScale = Mth.clamp(this.getRenderSize() * 0.08F, 0.55F, 3F);
        double distance = Math.max(2.5F * this.distanceToSqr(burnX, burnY, burnZ), 0);
        double conqueredDistance = burnProgress / 40D * distance;
        int increment = (int) Math.ceil(conqueredDistance / 100);
        int particleCount = this.getDragonStage() <= 3 ? 6 : 3;
        for (int i = 0; i < conqueredDistance; i += increment) {
            double progressX = headPos.x + d2 * (i / (float) distance);
            double progressY = headPos.y + d3 * (i / (float) distance);
            double progressZ = headPos.z + d4 * (i / (float) distance);
            if (canPositionBeSeen(progressX, progressY, progressZ)) {
                if (level.isClientSide && random.nextInt(particleCount) == 0) {
                    IceAndFire.PROXY.spawnDragonParticle(EnumParticles.DragonIce, headPos.x, headPos.y, headPos.z, 0, 0, 0, this);
                }
            } else {
                if (!level.isClientSide) {
                    HitResult result = this.level.clip(new ClipContext(new Vec3(this.getX(), this.getY() + this.getEyeHeight(), this.getZ()), new Vec3(progressX, progressY, progressZ), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    BlockPos pos = new BlockPos(result.getLocation());
//                    if(!this.isInMaterialWater()){
                    IafDragonDestructionManager.destroyAreaIce(level, pos, this);
//                    }
                }
            }
        }
        if (burnProgress >= 40D && canPositionBeSeen(burnX, burnY, burnZ)) {
            double spawnX = burnX + (random.nextFloat() * 3.0) - 1.5;
            double spawnY = burnY + (random.nextFloat() * 3.0) - 1.5;
            double spawnZ = burnZ + (random.nextFloat() * 3.0) - 1.5;
            if (!level.isClientSide) {
//                if(!this.isInMaterialWater()) {
                IafDragonDestructionManager.destroyAreaIce(level, new BlockPos(spawnX, spawnY, spawnZ), this);
//                }
            }
        }
    }

}
