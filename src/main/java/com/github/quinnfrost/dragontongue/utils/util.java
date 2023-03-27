package com.github.quinnfrost.dragontongue.utils;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

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

    public static boolean isModPresent(String modName) {
        try {
            return ModList.get().isLoaded(modName);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isShooter(@Nullable Projectile target, @Nullable LivingEntity owner) {
        if (target == null || owner == null) {
            return false;
        }
        return target.getOwner() == owner;
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
            CompoundTag compoundNBT = new CompoundTag();
            target.addAdditionalSaveData(compoundNBT);
            if (compoundNBT.getUUID("Owner").equals(owner.getUUID())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean shouldAttack(@Nullable LivingEntity attacker, @Nullable LivingEntity target, double checkDistance) {
        if (attacker == null || target == null) {
            return false;
        }
        TargetingConditions entityPredicate = TargetingConditions.DEFAULT;
        entityPredicate.ignoreLineOfSight();
        if (checkDistance > 0) {
            entityPredicate.range(checkDistance);
        }
        return entityPredicate.test(attacker, target);
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
    public static HitResult getTargetBlockOrEntity(Entity entity, float maxDistance, @Nullable Predicate<? super Entity> excludeEntity) {
        BlockHitResult blockRayTraceResult = getTargetBlock(entity, maxDistance, 1.0f, ClipContext.Block.COLLIDER);
        float entityRayTraceDistance = maxDistance;
        if (blockRayTraceResult.getType() != HitResult.Type.MISS) {
            entityRayTraceDistance = (float) Math.sqrt(entity.distanceToSqr(blockRayTraceResult.getLocation()));
        }
        // Limit the max ray trace distance to the first block it sees
        EntityHitResult entityRayTraceResult = getTargetEntity(entity, entityRayTraceDistance, 1.0f, null);
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
    public static EntityHitResult getTargetEntity(Entity entity, float maxDistance, float partialTicks,
                                                       @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) notExclude -> notExclude instanceof LivingEntity;
        }

        Vec3 vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vec3 vector3d1 = entity.getViewVector(1.0F);
        // 结束位置向量
        Vec3 vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
        float f = 1.0F;
        // 计算结束位置向量构成的区域(Bounding Box)
        AABB axisalignedbb = entity.getBoundingBox().expandTowards(vector3d1.scale(d0)).inflate(1.0D, 1.0D, 1.0D);

        EntityHitResult entityraytraceresult = ProjectileUtil.getEntityHitResult(entity.level, entity, vector3d, vector3d2,
                axisalignedbb, ((Predicate<Entity>) notExclude -> !notExclude.isSpectator()
                        && notExclude.isPickable())
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
    public static BlockHitResult getTargetBlock(Entity entity, float maxDistance, float partialTicks, ClipContext.Block blockMode) {
        final ClipContext.Fluid fluidMode = ClipContext.Fluid.NONE;

        Vec3 vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vec3 vector3d1 = entity.getViewVector(1.0F);
        // 结束位置向量
        Vec3 vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

        BlockHitResult blockRayTraceResult = entity.level.clip(
                new ClipContext(vector3d, vector3d2, blockMode, fluidMode, entity)
        );
        return blockRayTraceResult;
    }

    @Deprecated
    public static BlockHitResult rayTraceBlock(Level world, Vec3 startVec, Vec3 endVec) {
        return world.clip(new ClipContext(startVec, endVec, ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE, null));
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
    @Deprecated
    public static boolean setByteTag(LivingEntity target, String path, byte value) {
        try {
            CompoundTag compoundNBT = new CompoundTag();
            target.addAdditionalSaveData(compoundNBT);
            compoundNBT.putByte(path, value);
            target.readAdditionalSaveData(compoundNBT);
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
    @Deprecated
    public static Optional<Byte> getByteTag(LivingEntity target, String path) {
        try {
            CompoundTag compoundNBT = new CompoundTag();
            target.addAdditionalSaveData(compoundNBT);
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
    public static void teleportPlayer(Player player, BlockPos pos) {
        player.teleportToWithTicket(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Get block facing relative to the entity
     * @param clickedBlock  Block in question
     * @param entity
     * @return
     */
    public static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        return Direction.getNearest((float) (entity.getX() - clickedBlock.getX()),
                (float) (entity.getY() - clickedBlock.getY()), (float) (entity.getZ() - clickedBlock.getZ()));
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

        AABB axisAlignedBB = new AABB(targetX,targetY,targetZ,targetX,targetY,targetZ).inflate(accuracy == null ? entity.getBoundingBox().getSize() : accuracy);
//        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(targetX,targetY,targetZ,targetX,targetY,targetZ).grow(accuracy == null ? (float) Math.sqrt(2f) : accuracy);
        if (axisAlignedBB.intersects(entity.getBoundingBox())) {
            return true;
        } else {
            return false;
        }

//        return getDistance(entity.getPosition(), pos) <= Math.sqrt(8f);
    }

    public static double getDistance(BlockPos start, BlockPos end) {
        return getDistance(Vec3.atLowerCornerOf(start), Vec3.atLowerCornerOf(end));
    }
    public static double getDistance(Vec3 start, Vec3 end) {
        return start.distanceTo(end);
    }

    public static double getSpeed(Mob entity) {
        return 43.178 * entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - 0.02141;
    }

    public static <T extends ParticleOptions> int spawnParticleForce(ServerLevel serverWorld, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
        int i = 0;

        for(int j = 0; j < serverWorld.players().size(); ++j) {
            ServerPlayer serverplayerentity = serverWorld.players().get(j);
            if (serverWorld.sendParticles(serverplayerentity, type, true, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed)) {
                ++i;
            }
        }

        return i;
    }

    public static boolean isHostile(LivingEntity livingEntity) {
        if (livingEntity instanceof Enemy) {
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
            targetGoal.stop();
        }
        return true;
    }

    public static double oldValue = 0;
    public static void mixinDebugger(Object... param) {
        String str = "A breakpoint over here will do the trick";

//        DragonTongue.LOGGER.debug(str);
    }

    public static boolean canSwimInLava(Entity entityIn) {
        if (DragonTongue.isIafPresent && IafHelperClass.canSwimInLava(entityIn)) {
            return true;
        }
        if (entityIn instanceof Player) {
            Player playerEntity = (Player) entityIn;
            if (playerEntity.isSpectator() || playerEntity.isCreative()) {
//                return true;
            }
            return playerEntity.isEyeInFluid(FluidTags.LAVA) && playerEntity.hasEffect(MobEffects.FIRE_RESISTANCE);
        }
        return false;
    }

    public static double getDistanceXZ(Vec3 vector1, Vec3 vector2) {
        if (vector1 == null || vector2 == null) {
            return 0;
        }
        double f = vector1.x - vector2.x;
        double f2 = vector1.z - vector2.z;
        return Math.sqrt(f * f + f2 * f2);
    }

    @OnlyIn(Dist.CLIENT)
    public static Player getClientSidePlayer() {
        return Minecraft.getInstance().player;
    }

}
