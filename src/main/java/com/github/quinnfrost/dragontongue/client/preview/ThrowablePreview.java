package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownPotion;

public class ThrowablePreview extends Entity implements PreviewEntity<ThrowableProjectile> {
    protected boolean inGround;
    protected Entity ignoreEntity;
    protected Entity shooter;
    protected int ignoreTime;
    public ThrowablePreview(Level worldIn) {
        super(EntityType.SNOWBALL, worldIn);
    }

    @Override
    public List<ThrowableProjectile> initializeEntities(Player player, ItemStack associatedItem) {
        Item item = associatedItem.getItem();
        if (item == Items.SNOWBALL) {
            this.shooter = player;
            Snowball entitySnowball = new Snowball(this.level, player);
            entitySnowball.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(entitySnowball);
        } else if (item == Items.EGG) {
            this.shooter = player;
            ThrownEgg entityEgg = new ThrownEgg(this.level, player);
            entityEgg.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(entityEgg);
        } else if (item == Items.ENDER_PEARL) {
            ThrownEnderpearl entityEnderPearl = new ThrownEnderpearl(this.level, player);
            this.shooter = player;
            entityEnderPearl.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(entityEnderPearl);
        } else if (item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) {
            return null;
        } else {
            ThrownPotion potionEntity = new ThrownPotion(this.level, player);
            this.shooter = player;
            potionEntity.shootFromRotation(player, player.xRot, player.yRot, -20.0F, 0.5F, 0.0F);
            return Collections.singletonList(potionEntity);
        }

    }

    @Override
    public void simulateShot(ThrowableProjectile simulatedEntity) {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        super.tick();
        if (this.inGround) {
            this.remove();
        }

        AABB axisalignedbb = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
        Iterator var3 = this.level.getEntities(this, axisalignedbb, (p_213881_0_) -> {
            return !p_213881_0_.isSpectator() && p_213881_0_.isPickable();
        }).iterator();

        while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            if (entity == this.ignoreEntity) {
                ++this.ignoreTime;
                break;
            }

            if (simulatedEntity.getOwner() != null && this.tickCount < 2 && this.ignoreEntity == null) {
                this.ignoreEntity = entity;
                this.ignoreTime = 3;
                break;
            }
        }

        HitResult raytraceresult = ProjectileUtil.getHitResult(this, (entityx) -> {
            return !entityx.isSpectator() && entityx.isPickable() && entityx != this.ignoreEntity;
        });
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0) {
            this.ignoreEntity = null;
        }

        if (raytraceresult.getType() != HitResult.Type.MISS) {
            this.remove();
        }

        Vec3 vec3d = this.getDeltaMovement();
        this.setPos(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
        float f = Mth.sqrt(getHorizontalDistanceSqr(vec3d));

        for(this.yRot = (float)(Mth.atan2(vec3d.x, vec3d.z) * 57.2957763671875); this.xRot - this.xRotO >= 180.0F; this.xRotO += 360.0F) {
        }

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
        this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
        float f1;
        if (this.isInWater()) {
            f1 = 0.8F;
        } else {
            f1 = 0.99F;
        }

        this.setDeltaMovement(vec3d.scale((double)f1));
        if (!this.isNoGravity()) {
            Vec3 vec3d1 = this.getDeltaMovement();
            double yy;
            if (simulatedEntity instanceof ThrownPotion) {
                yy = 0.05000000074505806;
            } else {
                yy = 0.029999999329447746;
            }

            this.setDeltaMovement(vec3d1.x, vec3d1.y - yy, vec3d1.z);
        }

    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
