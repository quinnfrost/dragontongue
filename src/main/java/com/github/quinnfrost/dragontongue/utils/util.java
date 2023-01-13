package com.github.quinnfrost.dragontongue.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class util {

    /**
     * Find out if the target's owner nbt tag matches the input owner's UUID
     *
     * @param target The entity's owner to determine
     * @param owner  Owner to determine
     * @return True if two matches, False if two do not match or cannot be owned
     */
    public static boolean isOwner(@Nullable LivingEntity target, @Nullable LivingEntity owner) {
        if (target == null || owner == null) {
            return false;
        }
        try {
            CompoundNBT compoundNBT = new CompoundNBT();
            target.writeAdditional(compoundNBT);
            if (compoundNBT.getUniqueId("Owner").equals(owner.getUniqueID())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the entity that is looking at
     *
     * @param entity        The entity whom you want to trace its vision
     * @param maxDistance   Only entity within the distance in block is traced
     * @param partialTicks  Time in ticks to smooth the movement(linear
     *                      interpolation
     *                      or 'lerp'), use 1.0F to disable
     * @param excludeEntity Entity to exclude in tracing
     * @return Result of ray trace, or null if nothing within the distance is found
     */
    @Nullable
    public static EntityRayTraceResult getTargetEntity(Entity entity, float maxDistance, float partialTicks,
                                                       @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) notExclude -> true;
        }

        Vector3d vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vector3d vector3d1 = entity.getLook(1.0F);
        // 结束位置向量
        Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
        float f = 1.0F;
        // 计算结束位置向量构成的区域(Bounding Box)
        AxisAlignedBB axisalignedbb = entity.getBoundingBox().expand(vector3d1.scale(d0)).grow(1.0D, 1.0D, 1.0D);
        EntityRayTraceResult entityraytraceresult = ProjectileHelper.rayTraceEntities(entity, vector3d, vector3d2,
                axisalignedbb, ((Predicate<Entity>) entity1 -> !entity1.isSpectator()
                        && entity1.canBeCollidedWith()
                        && entity1 instanceof LivingEntity)
                        .and(excludeEntity),
                d1);
        return entityraytraceresult;

    }

    /**
     * Get the block that entity is looking at
     *
     * @param entity       The entity whom you want to trace its vision
     * @param maxDistance  Only blocks within the distance in block is traced
     * @param partialTicks Time in ticks to smooth the movement(linear interpolation
     *                     or 'lerp'), use 1.0F to disable
     * @return Result of ray trace, or null if nothing within the distance is found
     */
    @Nullable
    public static BlockRayTraceResult getTargetBlock(Entity entity, float maxDistance, float partialTicks) {
        Vector3d vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vector3d vector3d1 = entity.getLook(1.0F);
        // 结束位置向量
        Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
        return entity.world.rayTraceBlocks(new RayTraceContext(vector3d, vector3d2, RayTraceContext.BlockMode.VISUAL,
                RayTraceContext.FluidMode.NONE, entity));
    }

    /**
     * Set NBT tag on target entity
     * @param target    Target entity to hold the tag
     * @param path      Tag path
     * @param value     Tag value
     * @return
     */
    public static boolean setByteTag(LivingEntity target, String path, byte value) {
        try {
            CompoundNBT compoundNBT = new CompoundNBT();
            target.writeAdditional(compoundNBT);
            compoundNBT.putByte(path, value);
            target.readAdditional(compoundNBT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Read NBT tag from target entity
     * @param target
     * @param path
     * @return
     */
    public static Optional<Byte> getByteTag(LivingEntity target, String path) {
        try {
            CompoundNBT compoundNBT = new CompoundNBT();
            target.writeAdditional(compoundNBT);
            if (compoundNBT.contains(path)) {
                return Optional.of(compoundNBT.getByte(path));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Teleport player to target block position
     * @param player
     * @param pos
     */
    public static void teleportPlayer(PlayerEntity player, BlockPos pos) {
        player.teleportKeepLoaded(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * TODO: What does it for?
     * @param clickedBlock
     * @param entity
     * @return
     */
    public static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        return Direction.getFacingFromVector((float) (entity.getPosX() - clickedBlock.getX()),
                (float) (entity.getPosY() - clickedBlock.getY()), (float) (entity.getPosZ() - clickedBlock.getZ()));
    }
}
