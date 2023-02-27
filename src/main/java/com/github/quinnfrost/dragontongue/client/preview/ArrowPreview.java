package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ArrowPreview extends Entity implements PreviewEntity<AbstractArrowEntity> {
    protected Entity shooter;
    private boolean inGround;
    public ArrowPreview(World worldIn) {
        super(EntityType.ARROW, worldIn);
    }

    @Override
    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        int timeleft = player.getItemInUseCount();
        if (timeleft > 0) {
            int maxduration = player.getHeldItemMainhand().getUseDuration();
            int difference = maxduration - timeleft;
            float arrowVelocity = BowItem.getArrowVelocity(difference);
            if ((double)arrowVelocity >= 0.1) {
                ArrowEntity entityArrow = new ArrowEntity(this.world, player);
                entityArrow.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, 0.0F, 3.0F * arrowVelocity, 0.0F);
                this.shooter = player;
                return Collections.singletonList(entityArrow);
            }
        }

        return null;
    }

    protected float waterDrag() {
        return 0.6F;
    }

    @Override
    public void simulateShot(AbstractArrowEntity simulatedEntity) {
        super.tick();
        Vector3d motion = this.getMotion();
        double value = motion.x * motion.x + motion.z * motion.z;
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(value);
            this.rotationYaw = (float)(MathHelper.atan2(motion.x, motion.z) * 57.29577951308232);
            this.rotationPitch = (float)(MathHelper.atan2(motion.y, (double)f) * 57.29577951308232);
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }

        BlockPos blockpos = new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
        BlockState iblockstate = this.world.getBlockState(blockpos);
        if (!iblockstate.isAir(this.world, blockpos)) {
            VoxelShape voxelshape = iblockstate.getCollisionShapeUncached(this.world, blockpos);
            if (!voxelshape.isEmpty()) {
                Iterator var8 = voxelshape.toBoundingBoxList().iterator();

                while(var8.hasNext()) {
                    AxisAlignedBB axisalignedbb = (AxisAlignedBB)var8.next();
                    if (axisalignedbb.offset(blockpos).contains(new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()))) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.inGround) {
            this.remove();
        } else {
            Vector3d vec3d1 = new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ());
            Vector3d vec3d = (new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ())).add(motion);
            BlockRayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vec3d1, vec3d, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            vec3d1 = new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ());
            vec3d = new Vector3d(this.getPosX() + motion.x, this.getPosY() + motion.y, this.getPosZ() + motion.z);
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                vec3d = new Vector3d(raytraceresult.getHitVec().x, raytraceresult.getHitVec().y, raytraceresult.getHitVec().z);
            }

            Entity entity = this.findEntityOnPath(vec3d1, vec3d);
            if (entity instanceof PlayerEntity) {
                PlayerEntity entityplayer = (PlayerEntity)entity;
                if (this.shooter instanceof PlayerEntity && !((PlayerEntity)this.shooter).canAttackPlayer(entityplayer)) {
                    raytraceresult = null;
                }
            }

            if (entity != null) {
                this.remove();
            }

            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS) {
                this.remove();
            }

            this.setPosition(this.getPosX() + motion.x, this.getPosY() + motion.y, this.getPosZ() + motion.z);
            float f4 = MathHelper.sqrt(value);
            this.rotationYaw = (float)(MathHelper.atan2(motion.x, motion.z) * 57.29577951308232);

            for(this.rotationPitch = (float)(MathHelper.atan2(motion.y, (double)f4) * 57.29577951308232); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float f1 = 0.99F;
            if (this.isInWater()) {
                f1 = this.waterDrag();
            }

            this.setMotion(motion.x * (double)f1, motion.y * (double)f1, motion.z * (double)f1);
            if (!this.hasNoGravity()) {
                this.addVelocity(0.0, -0.05000000074505806, 0.0);
            }

            this.doBlockCollisions();
        }

    }

    @Nullable
    private Entity findEntityOnPath(Vector3d start, Vector3d end) {
        Entity entity = null;
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().expand(this.getMotion()).grow(1.0), (entity1x) -> {
            return !entity1x.isSpectator() && entity1x.isAlive() && entity1x.canBeCollidedWith();
        });
        double d0 = 0.0;
        Iterator var7 = list.iterator();

        while(true) {
            Entity entity1;
            double d1;
            do {
                Optional raytraceresult;
                do {
                    do {
                        if (!var7.hasNext()) {
                            return entity;
                        }

                        entity1 = (Entity)var7.next();
                    } while(entity1 == this.shooter);

                    AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(0.30000001192092896);
                    raytraceresult = axisalignedbb.rayTrace(start, end);
                } while(!raytraceresult.isPresent());

                d1 = start.squareDistanceTo((Vector3d)raytraceresult.get());
            } while(!(d1 < d0) && d0 != 0.0);

            entity = entity1;
            d0 = d1;
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
