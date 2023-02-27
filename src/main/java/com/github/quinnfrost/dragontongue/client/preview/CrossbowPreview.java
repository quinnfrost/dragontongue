package com.github.quinnfrost.dragontongue.client.preview;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CrossbowPreview extends Entity implements PreviewEntity<AbstractArrowEntity> {
    public CrossbowPreview(EntityType<?> entityTypeIn, World worldIn) {
        super(EntityType.ARROW, worldIn);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundNBT compoundnbt = crossbow.getTag();
        if (compoundnbt != null && compoundnbt.contains("ChargedProjectiles", 9)) {
            ListNBT listnbt = compoundnbt.getList("ChargedProjectiles", 10);
            if (listnbt != null) {
                for(int i = 0; i < listnbt.size(); ++i) {
                    CompoundNBT compoundnbt1 = listnbt.getCompound(i);
                    list.add(ItemStack.read(compoundnbt1));
                }
            }
        }

        return list;
    }
    private static AbstractArrowEntity createArrow(World worldIn, LivingEntity shooter, ItemStack crossbow, ItemStack ammo) {
        ArrowItem arrowitem = (ArrowItem)((ArrowItem)(ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW));
        AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(worldIn, ammo, shooter);
        if (shooter instanceof PlayerEntity) {
            abstractarrowentity.setIsCritical(true);
        }

        abstractarrowentity.setHitSound(SoundEvents.ITEM_CROSSBOW_HIT);
        abstractarrowentity.setShotFromCrossbow(true);
        return abstractarrowentity;
    }
    @Override
    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        if (associatedItem.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(associatedItem)) {
            List<ItemStack> allProjectiles = getChargedProjectiles(associatedItem);
            List<AbstractArrowEntity> abstractArrowEntities = new ArrayList(3);

            for(int i = 0; i < allProjectiles.size(); ++i) {
                ItemStack itemStack = (ItemStack)allProjectiles.get(i);
                if (itemStack.getItem() instanceof ArrowItem) {
                    AbstractArrowEntity abstractArrowEntity = createArrow(this.world, player, associatedItem, new ItemStack(Items.ARROW));
                    Vector3d vec3d1 = player.getUpVector(1.0F);
                    float angle = 0.0F;
                    if (i == 1) {
                        angle = -10.0F;
                    } else if (i == 2) {
                        angle = 10.0F;
                    }

                    Quaternion quaternion = new Quaternion(new Vector3f(vec3d1), angle, true);
                    Vector3d vec3d = player.getLook(1.0F);
                    Vector3f vector3f = new Vector3f(vec3d);
                    vector3f.transform(quaternion);
                    float velocity = 3.15F;
                    abstractArrowEntity.shoot((double)vector3f.getX(), (double)vector3f.getY(), (double)vector3f.getZ(), velocity, 0.0F);
                    abstractArrowEntities.add(abstractArrowEntity);
                }
            }

            return abstractArrowEntities;
        } else {
            return null;
        }

    }

    @Override
    public void simulateShot(AbstractArrowEntity simulatedEntity) {
        super.tick();
        boolean flag = simulatedEntity.getNoClip();
        Vector3d vec3d = this.getMotion();
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(horizontalMag(vec3d));
            this.rotationYaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875);
            this.rotationPitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * 57.2957763671875);
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }

        BlockPos blockpos = new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
        BlockState blockstate = this.world.getBlockState(blockpos);
        if (!blockstate.isAir(this.world, blockpos) && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShapeUncached(this.world, blockpos);
            if (!voxelshape.isEmpty()) {
                Iterator var7 = voxelshape.toBoundingBoxList().iterator();

                while(var7.hasNext()) {
                    AxisAlignedBB axisalignedbb = (AxisAlignedBB)var7.next();
                    if (axisalignedbb.offset(blockpos).contains(new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()))) {
                        this.remove();
                        return;
                    }
                }
            }
        }

        Vector3d vec3d1 = new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ());
        Vector3d vec3d2 = vec3d1.add(vec3d);
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vec3d1, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
        if (((RayTraceResult)raytraceresult).getType() != RayTraceResult.Type.MISS) {
            vec3d2 = ((RayTraceResult)raytraceresult).getHitVec();
        }

        while(this.isAlive()) {
            EntityRayTraceResult entityraytraceresult = ProjectileHelper.rayTraceEntities(this.world, this, vec3d, vec3d2, simulatedEntity.getBoundingBox().expand(this.getMotion()).grow(1.0), (p_213871_1_) -> {
                return !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.canBeCollidedWith() && p_213871_1_ != simulatedEntity.getShooter();
            });
            if (entityraytraceresult != null) {
                raytraceresult = entityraytraceresult;
            }

            if (raytraceresult != null && ((RayTraceResult)raytraceresult).getType() == RayTraceResult.Type.ENTITY) {
                Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
                Entity entity1 = simulatedEntity.getShooter();
                if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
                    raytraceresult = null;
                    entityraytraceresult = null;
                }
            }

            if (raytraceresult != null && !flag) {
                this.isAirBorne = true;
            }

            if (entityraytraceresult == null || simulatedEntity.getPierceLevel() <= 0) {
                break;
            }

            raytraceresult = null;
        }

        vec3d = this.getMotion();
        double d1 = vec3d.x;
        double d2 = vec3d.y;
        double d0 = vec3d.z;
        this.setPosition(this.getPosX() + d1, this.getPosY() + d2, this.getPosZ() + d0);
        float f4 = MathHelper.sqrt(horizontalMag(vec3d));
        if (flag) {
            this.rotationYaw = (float)(MathHelper.atan2(-d1, -d0) * 57.2957763671875);
        } else {
            this.rotationYaw = (float)(MathHelper.atan2(d1, d0) * 57.2957763671875);
        }

        while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
        this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
        float f1 = 0.99F;
        if (this.isInWater()) {
            f1 = 0.6F;
        }

        this.setMotion(vec3d.scale((double)f1));
        if (!this.hasNoGravity() && !flag) {
            Vector3d vec3d3 = this.getMotion();
            this.setMotion(vec3d3.x, vec3d3.y - 0.05000000074505806, vec3d3.z);
        }

        this.doBlockCollisions();

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
