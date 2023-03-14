package com.github.quinnfrost.dragontongue.client.preview;

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
import net.minecraft.util.math.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ArrowPreview extends Entity implements PreviewEntity<AbstractArrow> {
    protected Entity shooter;
    private boolean inGround;
    public ArrowPreview(Level worldIn) {
        super(EntityType.ARROW, worldIn);
    }

    @Override
    public List<AbstractArrow> initializeEntities(Player player, ItemStack associatedItem) {
        int timeleft = player.getUseItemRemainingTicks();
        if (timeleft > 0) {
            int maxduration = player.getMainHandItem().getUseDuration();
            int difference = maxduration - timeleft;
            float arrowVelocity = BowItem.getPowerForTime(difference);
            if ((double)arrowVelocity >= 0.1) {
                Arrow entityArrow = new Arrow(this.level, player);
                entityArrow.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 3.0F * arrowVelocity, 0.0F);
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
    public void simulateShot(AbstractArrow simulatedEntity) {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        double value = motion.x * motion.x + motion.z * motion.z;
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = Mth.sqrt(value);
            this.yRot = (float)(Mth.atan2(motion.x, motion.z) * 57.29577951308232);
            this.xRot = (float)(Mth.atan2(motion.y, (double)f) * 57.29577951308232);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

        BlockPos blockpos = new BlockPos(this.getX(), this.getY(), this.getZ());
        BlockState iblockstate = this.level.getBlockState(blockpos);
        if (!iblockstate.isAir(this.level, blockpos)) {
            VoxelShape voxelshape = iblockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Iterator var8 = voxelshape.toAabbs().iterator();

                while(var8.hasNext()) {
                    AABB axisalignedbb = (AABB)var8.next();
                    if (axisalignedbb.move(blockpos).contains(new Vec3(this.getX(), this.getY(), this.getZ()))) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.inGround) {
            this.remove();
        } else {
            Vec3 vec3d1 = new Vec3(this.getX(), this.getY(), this.getZ());
            Vec3 vec3d = (new Vec3(this.getX(), this.getY(), this.getZ())).add(motion);
            BlockHitResult raytraceresult = this.level.clip(new ClipContext(vec3d1, vec3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            vec3d1 = new Vec3(this.getX(), this.getY(), this.getZ());
            vec3d = new Vec3(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            if (raytraceresult.getType() != HitResult.Type.MISS) {
                vec3d = new Vec3(raytraceresult.getLocation().x, raytraceresult.getLocation().y, raytraceresult.getLocation().z);
            }

            Entity entity = this.findEntityOnPath(vec3d1, vec3d);
            if (entity instanceof Player) {
                Player entityplayer = (Player)entity;
                if (this.shooter instanceof Player && !((Player)this.shooter).canHarmPlayer(entityplayer)) {
                    raytraceresult = null;
                }
            }

            if (entity != null) {
                this.remove();
            }

            if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS) {
                this.remove();
            }

            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            float f4 = Mth.sqrt(value);
            this.yRot = (float)(Mth.atan2(motion.x, motion.z) * 57.29577951308232);

            for(this.xRot = (float)(Mth.atan2(motion.y, (double)f4) * 57.29577951308232); this.xRot - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
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

            this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
            this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;
            float f1 = 0.99F;
            if (this.isInWater()) {
                f1 = this.waterDrag();
            }

            this.setDeltaMovement(motion.x * (double)f1, motion.y * (double)f1, motion.z * (double)f1);
            if (!this.isNoGravity()) {
                this.push(0.0, -0.05000000074505806, 0.0);
            }

            this.checkInsideBlocks();
        }

    }

    @Nullable
    private Entity findEntityOnPath(Vec3 start, Vec3 end) {
        Entity entity = null;
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), (entity1x) -> {
            return !entity1x.isSpectator() && entity1x.isAlive() && entity1x.isPickable();
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

                    AABB axisalignedbb = entity1.getBoundingBox().inflate(0.30000001192092896);
                    raytraceresult = axisalignedbb.clip(start, end);
                } while(!raytraceresult.isPresent());

                d1 = start.distanceToSqr((Vec3)raytraceresult.get());
            } while(!(d1 < d0) && d0 != 0.0);

            entity = entity1;
            d0 = d1;
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
