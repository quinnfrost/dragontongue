package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonFlightUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;

public class DragonTaskGlide extends Task<EntityDragonBase> {
    public final int GLIDE_DISTANCE = 128;
    Queue<Vector3d> glidePosition = new ArrayDeque<>();
    //    List<BlockPos> glidePosition;
    Vector3d currentPosition;
    private final float speed;
    private final int maxXZ;
    private final int maxY;

    public DragonTaskGlide(float speedIn, int maxXZ, int maxY, int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
                MemoryModuleType.HOME, MemoryModuleStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
        this.speed = speedIn;
        this.maxXZ = maxXZ;
        this.maxY = maxY;
    }

    public DragonTaskGlide(float speedIn) {
        this(speedIn, 10, 7, 20 * 30, 20 * 45);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase dragon) {
        if (!dragon.canMove() || dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY || dragon.isFuelingForge()) {
            return false;
        }
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        Vector3d newPos = Vector3d.copyCenteredHorizontally(IafDragonFlightUtil.getBlockInView(dragon));
        if (newPos != null) {
            glidePosition.add(newPos);
            currentPosition = newPos;
            return true;
        }
        return false;
//        return dragon.getRNG().nextFloat() < 0.02F;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        return !glidePosition.isEmpty();
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
//        glidePosition.clear();
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (glidePosition == null) {
            glidePosition = new ArrayDeque<>();
        }

        GlobalPos homePos = dragon.getBrain().getMemory(MemoryModuleType.HOME).orElse(null);
//        if (dragon.hasHomePosition && homePos != null && worldIn.getDimensionKey() == homePos.getDimension()) {
//            glidePosition.addAll(getRandomGlidePos(dragon, Vector3d.copyCenteredHorizontally(homePos.getPos()), GLIDE_DISTANCE, GLIDE_DISTANCE));
//        } else {
//            glidePosition.addAll(getRandomGlidePos(dragon, dragon.getPositionVec(), 16, 16));
//        }


    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase dragon, long gameTime) {
        if (currentPosition == null
                || currentPosition.distanceTo(dragon.getPositionVec()) < dragon.getBoundingBox().getAverageEdgeLength()) {
            Optional<Vector3d> targetPosition = Optional.ofNullable(glidePosition.poll());
            targetPosition.ifPresent(vector3d -> {
                currentPosition = vector3d;
            });
        }
        dragon.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(currentPosition, this.speed, (int) Math.ceil(dragon.getBoundingBox().getAverageEdgeLength()))
        );
    }

//    public List<Vector3d> getRoostRandomGlidePos(EntityDragonBase dragon, Vector3d center, int minRange, int maxRange) {
//        int preferredFlightHeight = IafDragonFlightUtil.getPreferredFlightLevel(dragon);
//
//        float radius = 12 * (0.7F * dragon.getRenderSize() / 3);
//        float neg = dragon.getRNG().nextBoolean() ? 1 : -1;
//        float renderYawOffset = dragon.renderYawOffset;
//        BlockPos dragonPos = dragon.getPosition();
//        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dragonPos);
//        int distFromGround = (int) dragon.getPosY() - ground.getY();
//        for (int i = 0; i < 10; i++) {
//            BlockPos homePos = dragon.homePos.getPosition();
//            // Get a random position
//            BlockPos pos = new BlockPos(
//                    homePos.getX() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance,
//                    (distFromGround > preferredFlightHeight
//                            ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
//                            : (int) dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) + 1),
//                    (homePos.getZ() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance));
//            if (dragon.getDistanceSquared(Vector3d.copyCentered(pos)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(pos))) {
//                return pos;
//            }
//        }
//    }

    public List<Vector3d> getRandomGlidePos(EntityDragonBase dragon, Vector3d center, int minRange, int maxRange) {
        List<Vector3d> posList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
//        float range = 12 * (0.7F * dragon.getRenderSize() / 3);

            int preferredFlightHeight = IafDragonFlightUtil.getPreferredFlightLevel(dragon);
            float renderYawOffset = dragon.renderYawOffset;
//            float neg = dragon.getRNG().nextBoolean() ? 1 : -1;

            float range = minRange + dragon.getRNG().nextInt(maxRange + 1 - minRange);
            float anglePos = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRNG().nextFloat());
            double extraXPos = range * MathHelper.sin((float) (Math.PI + anglePos));
            double extraZPos = range * MathHelper.cos(anglePos);
            BlockPos radialPosPositive = new BlockPos(center.getX() + extraXPos, 0, center.getZ() + extraZPos);
            BlockPos groundPos = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPosPositive);
            int distFromGroundPos = (int) center.getY() - groundPos.getY();
            BlockPos newPos = radialPosPositive.up(distFromGroundPos > preferredFlightHeight
                    ? (int) Math.min(IafConfig.maxDragonFlight, center.getY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
                    : (int) center.getY() + dragon.getRNG().nextInt(preferredFlightHeight) + 1);
            BlockPos pos = dragon.doesWantToLand() ? groundPos : newPos;

            if (dragon.getDistanceSquared(Vector3d.copyCentered(newPos)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(newPos))) {
                posList.add(Vector3d.copyCenteredHorizontally(pos));
            }


            float rangeNeg = minRange + dragon.getRNG().nextInt(maxRange + 1 - minRange);
            float angleNeg = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRNG().nextFloat() * -1);
            double extraXNeg = rangeNeg * MathHelper.sin((float) (Math.PI + angleNeg));
            double extraZNeg = rangeNeg * MathHelper.cos(angleNeg);
            BlockPos radialPoseNegative = new BlockPos(center.getX() + extraXNeg, 0, center.getZ() + extraZNeg);
            BlockPos groundNeg = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPoseNegative);
            int distFromGroundNeg = (int) center.getY() - groundNeg.getY();
            BlockPos newPosNeg = radialPoseNegative.up(distFromGroundNeg > preferredFlightHeight
                    ? (int) Math.min(IafConfig.maxDragonFlight, center.getY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
                    : (int) center.getY() + dragon.getRNG().nextInt(preferredFlightHeight) + 1);
            BlockPos posNeg = dragon.doesWantToLand() ? groundNeg : newPosNeg;

            if (dragon.getDistanceSquared(Vector3d.copyCentered(newPosNeg)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(newPosNeg))) {
                posList.add(Vector3d.copyCenteredHorizontally(posNeg));
            }
        }

        return posList;
    }

}
