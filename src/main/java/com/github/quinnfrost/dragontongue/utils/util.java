package com.github.quinnfrost.dragontongue.utils;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class util {
    /**
     * Test if a class is present
     * @param className The name of the class
     * @return  True if the class can be loaded
     */
    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isShooter(@Nullable ProjectileEntity target, @Nullable LivingEntity owner) {
        if (target == null || owner == null) {
            return false;
        }
        return target.getShooter() == owner;
    }

    /**
     * Find out if the target's owner nbt tag matches the input owner's UUID
     * If any of the input is null, return false
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
        } catch (Exception ignored) {
            return false;
        }
    }
    //! Not usable
    public static boolean shouldAttack(@Nullable LivingEntity attacker, @Nullable LivingEntity target, double checkDistance) {
        if (attacker == null || target == null) {
            return false;
        }
        EntityPredicate entityPredicate = EntityPredicate.DEFAULT;
        entityPredicate.setIgnoresLineOfSight();
        if (checkDistance > 0) {
            entityPredicate.setDistance(checkDistance);
        }
        return entityPredicate.canTarget(attacker, target);
    }

    /**
     * Get entity looking at
     * This will return the first block or entity the ray encounters, for entity ray trace this won't go through walls.
     * Other ray trace methods: EntityDragonBase#1914
     *
     * @param entity
     * @param maxDistance
     * @param excludeEntity
     * @return  A BlockRayTraceResult or EntityRayTraceResult, separated by its type
     */
    public static RayTraceResult getTargetBlockOrEntity(Entity entity, float maxDistance, @Nullable Predicate<? super Entity> excludeEntity) {
        BlockRayTraceResult blockRayTraceResult = getTargetBlock(entity, maxDistance, 1.0f, RayTraceContext.BlockMode.COLLIDER);
        float entityRayTraceDistance = maxDistance;
        if (blockRayTraceResult.getType() != RayTraceResult.Type.MISS) {
            entityRayTraceDistance = (float) Math.sqrt(entity.getDistanceSq(blockRayTraceResult.getHitVec()));
        }
        // Limit the max ray trace distance to the first block it sees
        EntityRayTraceResult entityRayTraceResult = getTargetEntity(entity, entityRayTraceDistance, 1.0f, null);
        if (entityRayTraceResult != null) {
            return entityRayTraceResult;
        } else {
            return blockRayTraceResult;
        }
    }

    /**
     * Get the entity that is looking at
     * Note that this will trace through walls
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
            excludeEntity = (Predicate<Entity>) notExclude -> notExclude instanceof LivingEntity;
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

        EntityRayTraceResult entityraytraceresult = ProjectileHelper.rayTraceEntities(entity.world, entity, vector3d, vector3d2,
                axisalignedbb, ((Predicate<Entity>) notExclude -> !notExclude.isSpectator()
                        && notExclude.canBeCollidedWith())
                        .and(excludeEntity)
        );
        return entityraytraceresult;

    }

    /**
     * Get the block that entity is looking at
     *
     * @param entity       The entity whom you want to trace its vision
     * @param maxDistance  Only blocks within the distance in block is traced
     * @param partialTicks Time in ticks to smooth the movement(linear interpolation
     *                     or 'lerp'), use 1.0F to disable
     * @param blockMode
     * @return Result of ray trace, or RayTraceResult.Type.MISS if nothing within the distance is found
     */
    public static BlockRayTraceResult getTargetBlock(Entity entity, float maxDistance, float partialTicks, RayTraceContext.BlockMode blockMode) {
        final RayTraceContext.FluidMode fluidMode = RayTraceContext.FluidMode.NONE;

        Vector3d vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vector3d vector3d1 = entity.getLook(1.0F);
        // 结束位置向量
        Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

        BlockRayTraceResult blockRayTraceResult = entity.world.rayTraceBlocks(
                new RayTraceContext(vector3d, vector3d2, blockMode, fluidMode, entity)
        );
        return blockRayTraceResult;
    }

    @Deprecated
    public static BlockRayTraceResult rayTraceBlock(World world, Vector3d startVec, Vector3d endVec) {
        return world.rayTraceBlocks(new RayTraceContext(startVec, endVec, RayTraceContext.BlockMode.VISUAL,
                RayTraceContext.FluidMode.NONE, null));
    }

    public static Vector3d getDirectionOffset(Vector3d startIn, Vector3d direction, float length) {
        return startIn.add(direction.normalize().scale(length));
    }


//    public static List<Entity> getEntitiesAround(BlockPos blockPos, World world, float radius, Predicate<? super Entity> excludeEntity) {
//        if (excludeEntity == null) {
//            excludeEntity = (Predicate<Entity>) entity -> true;
//        }
//
//    }

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
     * Get block facing relative to the entity
     * @param clickedBlock  Block in question
     * @param entity
     * @return
     */
    public static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        return Direction.getFacingFromVector((float) (entity.getPosX() - clickedBlock.getX()),
                (float) (entity.getPosY() - clickedBlock.getY()), (float) (entity.getPosZ() - clickedBlock.getZ()));
    }

    /**
     * Determine if an entity's bounding box contains the target position
     * @param entity
     * @param pos
     * @return
     */
    public static boolean hasArrived(LivingEntity entity, BlockPos pos, @Nullable Double accuracy) {
        double targetX = pos.getX();
        double targetY = pos.getY();
        double targetZ = pos.getZ();

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(targetX,targetY,targetZ,targetX,targetY,targetZ).grow(accuracy == null ? entity.getBoundingBox().getAverageEdgeLength() : accuracy);
        if (axisAlignedBB.intersects(entity.getBoundingBox())) {
            return true;
        } else {
            return false;
        }

//        return getDistance(entity.getPosition(), pos) <= Math.sqrt(8f);
    }

    public static double getDistance(BlockPos start, BlockPos end) {
        return getDistance(Vector3d.copy(start), Vector3d.copy(end));
    }
    public static double getDistance(Vector3d start, Vector3d end) {
        return start.distanceTo(end);
    }

    public static double getSpeed(MobEntity entity) {
        return 43.178 * entity.getBaseAttributeValue(Attributes.MOVEMENT_SPEED) - 0.02141;
    }

    public static <T extends IParticleData> int spawnParticleForce(ServerWorld serverWorld, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
        int i = 0;

        for(int j = 0; j < serverWorld.getPlayers().size(); ++j) {
            ServerPlayerEntity serverplayerentity = serverWorld.getPlayers().get(j);
            if (serverWorld.spawnParticle(serverplayerentity, type, true, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed)) {
                ++i;
            }
        }

        return i;
    }

    public static boolean isHostile(LivingEntity livingEntity) {
        if (livingEntity instanceof IMob) {
            return true;
        }
        if (DragonTongue.isIafPresent && IafHelperClass.isIafHostile(livingEntity)) {
            return true;
        }
        return false;
    }

//    public static <T extends Enum> T getNextEnum(T enumType) {
//        return Enum.class.getEnumConstants()[(enumType.ordinal() + 1) % ]
//    }

    public static boolean resetGoals(GoalSelector goalSelectorIn) {
        List<Goal> currentTargetGoalList = goalSelectorIn.getRunningGoals().map(goal -> goal.getGoal()).collect(Collectors.toList());
        if (currentTargetGoalList.isEmpty()) {
            return false;
        }
        for (Goal targetGoal :
                currentTargetGoalList) {
            targetGoal.resetTask();
        }
        return true;
    }

    public static void mixinDebugger() {
        String str = "A breakpoint over here will do the trick";
//        DragonTongue.LOGGER.debug(str);
    }

    public static boolean canSwimInLava(Entity entityIn) {
        if (DragonTongue.isIafPresent && IafHelperClass.canSwimInLava(entityIn)) {
            return true;
        }
        if (entityIn instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entityIn;
            if (playerEntity.isSpectator() || playerEntity.isCreative()) {
//                return true;
            }
            return playerEntity.areEyesInFluid(FluidTags.LAVA) && playerEntity.isPotionActive(Effects.FIRE_RESISTANCE);
        }
        return false;
    }

    public static double getDistanceXZ(Vector3d vector1, Vector3d vector2) {
        if (vector1 == null || vector2 == null) {
            return 0;
        }
        double f = vector1.x - vector2.x;
        double f2 = vector1.z - vector2.z;
        return Math.sqrt(f * f + f2 * f2);
    }

    @OnlyIn(Dist.CLIENT)
    public static PlayerEntity getClientSidePlayer() {
        return Minecraft.getInstance().player;
    }

}
