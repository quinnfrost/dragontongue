package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.event.DragonFireEvent;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.enums.EnumParticles;
import com.github.alexthe666.iceandfire.message.MessageDragonSyncFire;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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

    public MixinEntityIceDragon(EntityType t, World world, DragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed) {
        super(t, world, type, minimumDamage, maximumDamage, minimumHealth, maximumHealth, minimumSpeed, maximumSpeed);
    }

    @Override
    public void travel(Vector3d travelVector) {
        if (this.isServerWorld() && this.isInWater()) {
            this.moveRelative(this.getAIMoveSpeed(), travelVector);
            this.move(MoverType.SELF, this.getMotion());
            this.setMotion(this.getMotion().scale(0.9D));
            if (this.getAttackTarget() == null) {
                this.setMotion(this.getMotion().add(0.0D, -0.005D, 0.0D));
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
        if (syncType == 1 && !world.isRemote) {
            //sync with client
            IceAndFire.sendMSGToAll(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 0));
        }
        if (syncType == 2 && world.isRemote) {
            //sync with server
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 0));
        }
        if (syncType == 3 && !world.isRemote) {
            //sync with client, fire bomb
            IceAndFire.sendMSGToAll(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 5));
        }
        if (syncType == 4 && world.isRemote) {
            //sync with server, fire bomb
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 5));
        }
        if (syncType > 2 && syncType < 6) {
            if (this.getAnimation() != ANIMATION_FIRECHARGE) {
                this.setAnimation(ANIMATION_FIRECHARGE);
            } else if (this.getAnimationTick() == 20) {
                rotationYaw = renderYawOffset;
                Vector3d headVec = this.getHeadPosition();
                double d2 = burnX - headVec.x;
                double d3 = burnY - headVec.y;
                double d4 = burnZ - headVec.z;
                float inaccuracy = 1.0F;
                d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
                d3 = d3 + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
                d4 = d4 + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
                this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                EntityDragonIceCharge entitylargefireball = new EntityDragonIceCharge(
                        IafEntityRegistry.ICE_DRAGON_CHARGE.get(), world, this, d2, d3, d4);
                float size = this.isChild() ? 0.4F : this.isAdult() ? 1.3F : 0.8F;
                entitylargefireball.setPosition(headVec.x, headVec.y, headVec.z);
                if (!world.isRemote) {
                    world.addEntity(entitylargefireball);
                }
            }
            return;
        }
        this.getNavigator().clearPath();
        this.burnParticleX = burnX;
        this.burnParticleY = burnY;
        this.burnParticleZ = burnZ;
        Vector3d headPos = getHeadPosition();
        double d2 = burnX - headPos.x;
        double d3 = burnY - headPos.y;
        double d4 = burnZ - headPos.z;
        float particleScale = MathHelper.clamp(this.getRenderSize() * 0.08F, 0.55F, 3F);
        double distance = Math.max(2.5F * this.getDistanceSq(burnX, burnY, burnZ), 0);
        double conqueredDistance = burnProgress / 40D * distance;
        int increment = (int) Math.ceil(conqueredDistance / 100);
        int particleCount = this.getDragonStage() <= 3 ? 6 : 3;
        for (int i = 0; i < conqueredDistance; i += increment) {
            double progressX = headPos.x + d2 * (i / (float) distance);
            double progressY = headPos.y + d3 * (i / (float) distance);
            double progressZ = headPos.z + d4 * (i / (float) distance);
            if (canPositionBeSeen(progressX, progressY, progressZ)) {
                if (world.isRemote && rand.nextInt(particleCount) == 0) {
                    IceAndFire.PROXY.spawnDragonParticle(EnumParticles.DragonIce, headPos.x, headPos.y, headPos.z, 0, 0, 0, this);
                }
            } else {
                if (!world.isRemote) {
                    RayTraceResult result = this.world.rayTraceBlocks(new RayTraceContext(new Vector3d(this.getPosX(), this.getPosY() + this.getEyeHeight(), this.getPosZ()), new Vector3d(progressX, progressY, progressZ), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
                    BlockPos pos = new BlockPos(result.getHitVec());
//                    if(!this.isInMaterialWater()){
                    IafDragonDestructionManager.destroyAreaIce(world, pos, this);
//                    }
                }
            }
        }
        if (burnProgress >= 40D && canPositionBeSeen(burnX, burnY, burnZ)) {
            double spawnX = burnX + (rand.nextFloat() * 3.0) - 1.5;
            double spawnY = burnY + (rand.nextFloat() * 3.0) - 1.5;
            double spawnZ = burnZ + (rand.nextFloat() * 3.0) - 1.5;
            if (!world.isRemote) {
//                if(!this.isInMaterialWater()) {
                IafDragonDestructionManager.destroyAreaIce(world, new BlockPos(spawnX, spawnY, spawnZ), this);
//                }
            }
        }
    }

}
