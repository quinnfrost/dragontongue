package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;

public class BowArrowPreview extends Entity implements PreviewEntity<AbstractArrow> {
    private boolean inGround;

    public BowArrowPreview(Level level) {
        super(EntityType.ARROW, level);
    }

    public List<AbstractArrow> initializeEntities(Player player, ItemStack associatedItem) {
        int timeLeft = player.getUseItemRemainingTicks();
        if (timeLeft > 0) {
            int maxDuration = player.getMainHandItem().getUseDuration();
            int difference = maxDuration - timeLeft;
            float arrowVelocity = BowItem.getPowerForTime(difference);
            if ((double)arrowVelocity >= 0.1) {
                Arrow arrow = new Arrow(this.level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F * arrowVelocity, 0.0F);
                return Collections.singletonList(arrow);
            }
        }

        return null;
    }

    public void simulateShot(AbstractArrow simulatedEntity) {
        super.tick();
        boolean flag = this.noPhysics;
        Vec3 vec3 = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = vec3.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2(vec3.y, d0) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        Vec3 vec33;
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                vec33 = this.position();
                Iterator var8 = voxelshape.toAabbs().iterator();

                while(var8.hasNext()) {
                    AABB aabb = (AABB)var8.next();
                    if (aabb.move(blockpos).contains(vec33)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            this.discard();
        } else {
            Vec3 vec32 = this.position();
            vec33 = vec32.add(vec3);
            HitResult hitresult = this.level.clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (((HitResult)hitresult).getType() != HitResult.Type.MISS) {
                vec33 = ((HitResult)hitresult).getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = simulatedEntity.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && ((HitResult)hitresult).getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    Entity entity1 = simulatedEntity.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && ((HitResult)hitresult).getType() != HitResult.Type.MISS && !flag) {
                    this.hasImpulse = true;
                }

                if (entityhitresult == null || simulatedEntity.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;
            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            double d4 = vec3.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d5, -d1) * 57.2957763671875));
            } else {
                this.setYRot((float)(Mth.atan2(d5, d1) * 57.2957763671875));
            }

            this.setXRot((float)(Mth.atan2(d6, d4) * 57.2957763671875));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;
            if (this.isInWater()) {
                this.discard();
            }

            this.setDeltaMovement(vec3.scale((double)f));
            if (!this.isNoGravity() && !flag) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - 0.05000000074505806, vec34.z);
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }

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

    protected static float lerpRotation(float p_37274_, float p_37275_) {
        while(p_37275_ - p_37274_ < -180.0F) {
            p_37274_ -= 360.0F;
        }

        while(p_37275_ - p_37274_ >= 180.0F) {
            p_37274_ += 360.0F;
        }

        return Mth.lerp(0.2F, p_37274_, p_37275_);
    }
}
