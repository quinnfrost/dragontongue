package com.github.quinnfrost.dragontongue.client.preview;

import com.google.common.collect.Lists;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CrossbowPreview extends Entity implements PreviewEntity<AbstractArrow> {
    public CrossbowPreview(EntityType<?> entityTypeIn, Level worldIn) {
        super(EntityType.ARROW, worldIn);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundnbt = crossbow.getTag();
        if (compoundnbt != null && compoundnbt.contains("ChargedProjectiles", 9)) {
            ListTag listnbt = compoundnbt.getList("ChargedProjectiles", 10);
            if (listnbt != null) {
                for(int i = 0; i < listnbt.size(); ++i) {
                    CompoundTag compoundnbt1 = listnbt.getCompound(i);
                    list.add(ItemStack.of(compoundnbt1));
                }
            }
        }

        return list;
    }
    private static AbstractArrow createArrow(Level worldIn, LivingEntity shooter, ItemStack crossbow, ItemStack ammo) {
        ArrowItem arrowitem = (ArrowItem)((ArrowItem)(ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW));
        AbstractArrow abstractarrowentity = arrowitem.createArrow(worldIn, ammo, shooter);
        if (shooter instanceof Player) {
            abstractarrowentity.setCritArrow(true);
        }

        abstractarrowentity.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        abstractarrowentity.setShotFromCrossbow(true);
        return abstractarrowentity;
    }
    @Override
    public List<AbstractArrow> initializeEntities(Player player, ItemStack associatedItem) {
        if (associatedItem.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(associatedItem)) {
            List<ItemStack> allProjectiles = getChargedProjectiles(associatedItem);
            List<AbstractArrow> abstractArrowEntities = new ArrayList(3);

            for(int i = 0; i < allProjectiles.size(); ++i) {
                ItemStack itemStack = (ItemStack)allProjectiles.get(i);
                if (itemStack.getItem() instanceof ArrowItem) {
                    AbstractArrow abstractArrowEntity = createArrow(this.level, player, associatedItem, new ItemStack(Items.ARROW));
                    Vec3 vec3d1 = player.getUpVector(1.0F);
                    float angle = 0.0F;
                    if (i == 1) {
                        angle = -10.0F;
                    } else if (i == 2) {
                        angle = 10.0F;
                    }

                    Quaternion quaternion = new Quaternion(new Vector3f(vec3d1), angle, true);
                    Vec3 vec3d = player.getViewVector(1.0F);
                    Vector3f vector3f = new Vector3f(vec3d);
                    vector3f.transform(quaternion);
                    float velocity = 3.15F;
                    abstractArrowEntity.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), velocity, 0.0F);
                    abstractArrowEntities.add(abstractArrowEntity);
                }
            }

            return abstractArrowEntities;
        } else {
            return null;
        }

    }

    @Override
    public void simulateShot(AbstractArrow simulatedEntity) {
        super.tick();
        boolean flag = simulatedEntity.isNoPhysics();
        Vec3 vec3d = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = Mth.sqrt(getHorizontalDistanceSqr(vec3d));
            this.yRot = (float)(Mth.atan2(vec3d.x, vec3d.z) * 57.2957763671875);
            this.xRot = (float)(Mth.atan2(vec3d.y, (double)f) * 57.2957763671875);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

        BlockPos blockpos = new BlockPos(this.getX(), this.getY(), this.getZ());
        BlockState blockstate = this.level.getBlockState(blockpos);
        if (!blockstate.isAir(this.level, blockpos) && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Iterator var7 = voxelshape.toAabbs().iterator();

                while(var7.hasNext()) {
                    AABB axisalignedbb = (AABB)var7.next();
                    if (axisalignedbb.move(blockpos).contains(new Vec3(this.getX(), this.getY(), this.getZ()))) {
                        this.remove();
                        return;
                    }
                }
            }
        }

        Vec3 vec3d1 = new Vec3(this.getX(), this.getY(), this.getZ());
        Vec3 vec3d2 = vec3d1.add(vec3d);
        HitResult raytraceresult = this.level.clip(new ClipContext(vec3d1, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (((HitResult)raytraceresult).getType() != HitResult.Type.MISS) {
            vec3d2 = ((HitResult)raytraceresult).getLocation();
        }

        while(this.isAlive()) {
            EntityHitResult entityraytraceresult = ProjectileUtil.getEntityHitResult(this.level, this, vec3d, vec3d2, simulatedEntity.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), (p_213871_1_) -> {
                return !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.isPickable() && p_213871_1_ != simulatedEntity.getOwner();
            });
            if (entityraytraceresult != null) {
                raytraceresult = entityraytraceresult;
            }

            if (raytraceresult != null && ((HitResult)raytraceresult).getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult)raytraceresult).getEntity();
                Entity entity1 = simulatedEntity.getOwner();
                if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                    raytraceresult = null;
                    entityraytraceresult = null;
                }
            }

            if (raytraceresult != null && !flag) {
                this.hasImpulse = true;
            }

            if (entityraytraceresult == null || simulatedEntity.getPierceLevel() <= 0) {
                break;
            }

            raytraceresult = null;
        }

        vec3d = this.getDeltaMovement();
        double d1 = vec3d.x;
        double d2 = vec3d.y;
        double d0 = vec3d.z;
        this.setPos(this.getX() + d1, this.getY() + d2, this.getZ() + d0);
        float f4 = Mth.sqrt(getHorizontalDistanceSqr(vec3d));
        if (flag) {
            this.yRot = (float)(Mth.atan2(-d1, -d0) * 57.2957763671875);
        } else {
            this.yRot = (float)(Mth.atan2(d1, d0) * 57.2957763671875);
        }

        while(this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
        this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
        float f1 = 0.99F;
        if (this.isInWater()) {
            f1 = 0.6F;
        }

        this.setDeltaMovement(vec3d.scale((double)f1));
        if (!this.isNoGravity() && !flag) {
            Vec3 vec3d3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3d3.x, vec3d3.y - 0.05000000074505806, vec3d3.z);
        }

        this.checkInsideBlocks();

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
