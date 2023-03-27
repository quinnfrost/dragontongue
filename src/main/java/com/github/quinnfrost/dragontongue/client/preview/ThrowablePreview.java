package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ThrowablePreview extends Entity implements PreviewEntity<ThrowableItemProjectile> {
    public ThrowablePreview(Level p_19871_) {
        super(EntityType.SNOWBALL, p_19871_);
    }

    public List<ThrowableItemProjectile> initializeEntities(Player player, ItemStack associatedItem) {
        Item item = associatedItem.getItem();
        if (item instanceof SnowballItem) {
            Snowball snowball = new Snowball(this.level, player);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(snowball);
        } else if (item instanceof EggItem) {
            ThrownEgg egg = new ThrownEgg(this.level, player);
            egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(egg);
        } else if (item instanceof EnderpearlItem) {
            ThrownEnderpearl thrownEnderpearl = new ThrownEnderpearl(this.level, player);
            thrownEnderpearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
            return Collections.singletonList(thrownEnderpearl);
        } else if (!(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
            if (item instanceof ExperienceBottleItem) {
                ThrownExperienceBottle experienceBottle = new ThrownExperienceBottle(this.level, player);
                experienceBottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.7F, 0.0F);
                return Collections.singletonList(experienceBottle);
            } else {
                return null;
            }
        } else {
            ThrownPotion thrownPotion = new ThrownPotion(this.level, player);
            thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 0.0F);
            return Collections.singletonList(thrownPotion);
        }
    }

    public void simulateShot(ThrowableItemProjectile simulatedEntity) {
        super.tick();
        HitResult hitresult = ProjectileUtil.getHitResult(this, (entity) -> {
            return !entity.isSpectator() && entity.isAlive() && entity.isPickable();
        });
        boolean flag = false;
        if (hitresult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (blockstate.is(Blocks.NETHER_PORTAL)) {
                this.discard();
            } else if (blockstate.is(Blocks.END_GATEWAY)) {
                this.discard();
            }
        }

        if (hitresult.getType() != HitResult.Type.MISS) {
            this.discard();
        }

        this.checkInsideBlocks();
        Vec3 vec3 = this.getDeltaMovement();
        double d2 = this.getX() + vec3.x;
        double d0 = this.getY() + vec3.y;
        double d1 = this.getZ() + vec3.z;
        float f = 0.99F;
        if (this.isInWater()) {
            this.discard();
        }

        this.setDeltaMovement(vec3.scale((double)f));
        if (!this.isNoGravity()) {
            Vec3 vec31 = this.getDeltaMovement();
            this.setDeltaMovement(vec31.x, vec31.y - (double)this.getGravity(simulatedEntity), vec31.z);
        }

        this.setPos(d2, d0, d1);
    }

    protected void defineSynchedData() {
    }

    protected void readAdditionalSaveData(CompoundTag p_20052_) {
    }

    protected void addAdditionalSaveData(CompoundTag p_20139_) {
    }
    protected void doWaterSplashEffect() {
    }

    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    private float getGravity(Entity simulated) {
        if (simulated instanceof ThrownExperienceBottle) {
            return 0.07F;
        } else {
            return simulated instanceof ThrownPotion ? 0.05F : 0.03F;
        }
    }

}
