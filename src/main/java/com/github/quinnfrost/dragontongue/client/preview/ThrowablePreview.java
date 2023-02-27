package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ThrowablePreview extends Entity implements PreviewEntity<ThrowableEntity> {
    protected boolean inGround;
    protected Entity ignoreEntity;
    protected Entity shooter;
    protected int ignoreTime;
    public ThrowablePreview(World worldIn) {
        super(EntityType.SNOWBALL, worldIn);
    }

    @Override
    public List<ThrowableEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        Item item = associatedItem.getItem();
        if (item == Items.SNOWBALL) {
            this.shooter = player;
            SnowballEntity entitySnowball = new SnowballEntity(this.world, player);
            entitySnowball.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(entitySnowball);
        } else if (item == Items.EGG) {
            this.shooter = player;
            EggEntity entityEgg = new EggEntity(this.world, player);
            entityEgg.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(entityEgg);
        } else if (item == Items.ENDER_PEARL) {
            EnderPearlEntity entityEnderPearl = new EnderPearlEntity(this.world, player);
            this.shooter = player;
            entityEnderPearl.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(entityEnderPearl);
        } else if (item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) {
            return null;
        } else {
            PotionEntity potionEntity = new PotionEntity(this.world, player);
            this.shooter = player;
            potionEntity.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, -20.0F, 0.5F, 0.0F);
            return Collections.singletonList(potionEntity);
        }

    }

    @Override
    public void simulateShot(ThrowableEntity simulatedEntity) {
        this.lastTickPosX = this.getPosX();
        this.lastTickPosY = this.getPosY();
        this.lastTickPosZ = this.getPosZ();
        super.tick();
        if (this.inGround) {
            this.remove();
        }

        AxisAlignedBB axisalignedbb = this.getBoundingBox().expand(this.getMotion()).grow(1.0);
        Iterator var3 = this.world.getEntitiesInAABBexcluding(this, axisalignedbb, (p_213881_0_) -> {
            return !p_213881_0_.isSpectator() && p_213881_0_.canBeCollidedWith();
        }).iterator();

        while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            if (entity == this.ignoreEntity) {
                ++this.ignoreTime;
                break;
            }

            if (simulatedEntity.getShooter() != null && this.ticksExisted < 2 && this.ignoreEntity == null) {
                this.ignoreEntity = entity;
                this.ignoreTime = 3;
                break;
            }
        }

        RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, (entityx) -> {
            return !entityx.isSpectator() && entityx.canBeCollidedWith() && entityx != this.ignoreEntity;
        });
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0) {
            this.ignoreEntity = null;
        }

        if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
            this.remove();
        }

        Vector3d vec3d = this.getMotion();
        this.setPosition(this.getPosX() + vec3d.x, this.getPosY() + vec3d.y, this.getPosZ() + vec3d.z);
        float f = MathHelper.sqrt(horizontalMag(vec3d));

        for(this.rotationYaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875); this.rotationPitch - this.prevRotationPitch >= 180.0F; this.prevRotationPitch += 360.0F) {
        }

        while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
        this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
        float f1;
        if (this.isInWater()) {
            f1 = 0.8F;
        } else {
            f1 = 0.99F;
        }

        this.setMotion(vec3d.scale((double)f1));
        if (!this.hasNoGravity()) {
            Vector3d vec3d1 = this.getMotion();
            double yy;
            if (simulatedEntity instanceof PotionEntity) {
                yy = 0.05000000074505806;
            } else {
                yy = 0.029999999329447746;
            }

            this.setMotion(vec3d1.x, vec3d1.y - yy, vec3d1.z);
        }

    }

    @Override
    protected void registerData() {

    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }
}
