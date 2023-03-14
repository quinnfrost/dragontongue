package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonFlightUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;

public class DragonTaskGlide extends Behavior<EntityDragonBase> {
    public final int GLIDE_DISTANCE = 128;
    Queue<Vec3> glidePosition = new ArrayDeque<>();
    //    List<BlockPos> glidePosition;
    Vec3 currentPosition;
    private final float speed;
    private final int maxXZ;
    private final int maxY;

    public DragonTaskGlide(float speedIn, int maxXZ, int maxY, int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HOME, MemoryStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
        this.speed = speedIn;
        this.maxXZ = maxXZ;
        this.maxY = maxY;
    }

    public DragonTaskGlide(float speedIn) {
        this(speedIn, 10, 7, 20 * 30, 20 * 45);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase dragon) {
        if (!dragon.canMove() || dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY || dragon.isFuelingForge()) {
            return false;
        }
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        Vec3 newPos = Vec3.atBottomCenterOf(IafDragonFlightUtil.getBlockInView(dragon));
        if (newPos != null) {
            glidePosition.add(newPos);
            currentPosition = newPos;
            return true;
        }
        return false;
//        return dragon.getRNG().nextFloat() < 0.02F;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
            return false;
        }
        return !glidePosition.isEmpty();
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
//        glidePosition.clear();
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
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
    protected void tick(ServerLevel worldIn, EntityDragonBase dragon, long gameTime) {
        if (currentPosition == null
                || currentPosition.distanceTo(dragon.position()) < dragon.getBoundingBox().getSize()) {
            Optional<Vec3> targetPosition = Optional.ofNullable(glidePosition.poll());
            targetPosition.ifPresent(vector3d -> {
                currentPosition = vector3d;
            });
        }
        dragon.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(currentPosition, this.speed, (int) Math.ceil(dragon.getBoundingBox().getSize()))
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

    public List<Vec3> getRandomGlidePos(EntityDragonBase dragon, Vec3 center, int minRange, int maxRange) {
        List<Vec3> posList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
//        float range = 12 * (0.7F * dragon.getRenderSize() / 3);

            int preferredFlightHeight = IafDragonFlightUtil.getPreferredFlightLevel(dragon);
            float renderYawOffset = dragon.yBodyRot;
//            float neg = dragon.getRNG().nextBoolean() ? 1 : -1;

            float range = minRange + dragon.getRandom().nextInt(maxRange + 1 - minRange);
            float anglePos = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRandom().nextFloat());
            double extraXPos = range * Mth.sin((float) (Math.PI + anglePos));
            double extraZPos = range * Mth.cos(anglePos);
            BlockPos radialPosPositive = new BlockPos(center.x() + extraXPos, 0, center.z() + extraZPos);
            BlockPos groundPos = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, radialPosPositive);
            int distFromGroundPos = (int) center.y() - groundPos.getY();
            BlockPos newPos = radialPosPositive.above(distFromGroundPos > preferredFlightHeight
                    ? (int) Math.min(IafConfig.maxDragonFlight, center.y() + dragon.getRandom().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
                    : (int) center.y() + dragon.getRandom().nextInt(preferredFlightHeight) + 1);
            BlockPos pos = dragon.doesWantToLand() ? groundPos : newPos;

            if (dragon.getDistanceSquared(Vec3.atCenterOf(newPos)) > 6 && !dragon.isTargetBlocked(Vec3.atCenterOf(newPos))) {
                posList.add(Vec3.atBottomCenterOf(pos));
            }


            float rangeNeg = minRange + dragon.getRandom().nextInt(maxRange + 1 - minRange);
            float angleNeg = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRandom().nextFloat() * -1);
            double extraXNeg = rangeNeg * Mth.sin((float) (Math.PI + angleNeg));
            double extraZNeg = rangeNeg * Mth.cos(angleNeg);
            BlockPos radialPoseNegative = new BlockPos(center.x() + extraXNeg, 0, center.z() + extraZNeg);
            BlockPos groundNeg = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, radialPoseNegative);
            int distFromGroundNeg = (int) center.y() - groundNeg.getY();
            BlockPos newPosNeg = radialPoseNegative.above(distFromGroundNeg > preferredFlightHeight
                    ? (int) Math.min(IafConfig.maxDragonFlight, center.y() + dragon.getRandom().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
                    : (int) center.y() + dragon.getRandom().nextInt(preferredFlightHeight) + 1);
            BlockPos posNeg = dragon.doesWantToLand() ? groundNeg : newPosNeg;

            if (dragon.getDistanceSquared(Vec3.atCenterOf(newPosNeg)) > 6 && !dragon.isTargetBlocked(Vec3.atCenterOf(newPosNeg))) {
                posList.add(Vec3.atBottomCenterOf(posNeg));
            }
        }

        return posList;
    }

}
