package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;

// This class came from DragonUtils, with some modification
public class IafDragonFlightUtil {
    public static int getPreferredFlightLevel(EntityDragonBase dragon) {
        return (int) Math.ceil(dragon.getRenderSize() + dragon.getBoundingBox().getYsize());
    }
    public static BlockPos getBlockInViewEscort(EntityDragonBase dragon) {
        int preferredFlightHeight = getPreferredFlightLevel(dragon);

        BlockPos escortPos = dragon.getEscortPosition();
        BlockPos ground = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, escortPos);
        int distFromGround = escortPos.getY() - ground.getY();
        for (int i = 0; i < 10; i++) {
            BlockPos pos = new BlockPos(escortPos.getX() + dragon.getRandom().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance / 2,
                    (distFromGround > preferredFlightHeight
                            ? escortPos.getY() + preferredFlightHeight - dragon.getRandom().nextInt(preferredFlightHeight / 2)
                            : escortPos.getY() + preferredFlightHeight + dragon.getRandom().nextInt(preferredFlightHeight / 2)),
                    (escortPos.getZ() + dragon.getRandom().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance / 2));
            if (dragon.getDistanceSquared(Vec3.atCenterOf(pos)) > 6 && !dragon.isTargetBlocked(Vec3.atCenterOf(pos))) {
                return pos;
            }
        }
        return null;
    }

    public static BlockPos getWaterBlockInViewEscort(EntityDragonBase dragon) {
        // In water escort
        BlockPos inWaterEscortPos = dragon.getEscortPosition();
        // We don't need to get too close
        if (Mth.abs(dragon.blockPosition().getX() - inWaterEscortPos.getX()) < dragon.getBoundingBox().getSize()
                && Mth.abs(dragon.blockPosition().getZ() - inWaterEscortPos.getZ()) < dragon.getBoundingBox().getSize()) {
            return dragon.blockPosition();
        }
        // Takes off if the escort position is no longer in water, mainly for using elytra to fly out of the water
        if (inWaterEscortPos.getY() - dragon.getY() > 8 + dragon.getYNavSize() && !dragon.level.getFluidState(inWaterEscortPos.below()).is(FluidTags.WATER)) {
            dragon.setHovering(true);
        }
        // Swim directly to the escort position
        return inWaterEscortPos;
    }

    public static BlockPos getWaterBlockInView(EntityDragonBase dragon) {
        float radius = 0.75F * (0.7F * dragon.getRenderSize() / 3) * -7 - dragon.getRandom().nextInt(dragon.getDragonStage() * 6);
        float neg = dragon.getRandom().nextBoolean() ? 1 : -1;
        float angle = (0.01745329251F * dragon.yBodyRot) + 3.15F + (dragon.getRandom().nextFloat() * neg);
        double extraX = radius * Mth.sin((float) (Math.PI + angle));
        double extraZ = radius * Mth.cos(angle);
        BlockPos radialPos = new BlockPos(dragon.getX() + extraX, 0, dragon.getZ() + extraZ);
        BlockPos ground = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) dragon.getY() - ground.getY();
        BlockPos newPos = radialPos.above(distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getY() + dragon.getRandom().nextInt(16) - 8) : (int) dragon.getY() + dragon.getRandom().nextInt(16) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        BlockPos surface = dragon.level.getFluidState(newPos.below(2)).is(FluidTags.WATER) ? newPos.below(dragon.getRandom().nextInt(10) + 1) : newPos;
        if (dragon.getDistanceSquared(Vec3.atCenterOf(surface)) > 6 && dragon.level.getFluidState(surface).is(FluidTags.WATER)) {
            return surface;
        }
        return null;
    }

    public static BlockPos getBlockInView(EntityDragonBase dragon) {
        // Get flight level
        int preferredFlightHeight = getPreferredFlightLevel(dragon);

        float radius = 12 * (0.7F * dragon.getRenderSize() / 3);
        float neg = dragon.getRandom().nextBoolean() ? 1 : -1;
        float renderYawOffset = dragon.yBodyRot;
        // Wander around roost
        if (dragon.hasHomePosition && dragon.homePos != null) {
            BlockPos dragonPos = dragon.blockPosition();
            BlockPos ground = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, dragonPos);
            int distFromGround = (int) dragon.getY() - ground.getY();
            for (int i = 0; i < 10; i++) {
                BlockPos homePos = dragon.homePos.getPosition();
                // Get a random position
                BlockPos pos = new BlockPos(
                        homePos.getX() + dragon.getRandom().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance,
                        (distFromGround > preferredFlightHeight
                                ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getY() + dragon.getRandom().nextInt(preferredFlightHeight) - preferredFlightHeight * 2f / 3f)
                                : (int) dragon.getY() + dragon.getRandom().nextInt(preferredFlightHeight) + 1),
                        (homePos.getZ() + dragon.getRandom().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance));
                if (dragon.getDistanceSquared(Vec3.atCenterOf(pos)) > 6 && !dragon.isTargetBlocked(Vec3.atCenterOf(pos))) {
                    return pos;
                }
            }
        }
        // Wander for homeless
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRandom().nextFloat() * neg);
        double extraX = radius * Mth.sin((float) (Math.PI + angle));
        double extraZ = radius * Mth.cos(angle);
        BlockPos radialPos = new BlockPos(dragon.getX() + extraX, 0, dragon.getZ() + extraZ);
        BlockPos ground = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) dragon.getY() - ground.getY();
        BlockPos newPos = radialPos.above(distFromGround > preferredFlightHeight
                ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getY() + dragon.getRandom().nextInt(preferredFlightHeight) - preferredFlightHeight * 2f / 3f)
                : (int) dragon.getY() + dragon.getRandom().nextInt(preferredFlightHeight) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        if (dragon.getDistanceSquared(Vec3.atCenterOf(newPos)) > 6 && !dragon.isTargetBlocked(Vec3.atCenterOf(newPos))) {
            return pos;
        }
        return null;
    }

    public static BlockPos getBlockUp(Level world, Heightmap.Types heightmapType, BlockPos blockPos) {
        while (!heightmapType.isOpaque().test(world.getBlockState(blockPos)) && blockPos.getY() < world.getMaxBuildHeight()) {
            blockPos = blockPos.above();
        }
        return blockPos;
    }

    public static BlockPos getBlockUnder(Level world, Heightmap.Types heightmapType, BlockPos blockPos) {
        while (!heightmapType.isOpaque().test(world.getBlockState(blockPos)) && blockPos.getY() > 0) {
            blockPos = blockPos.below();
        }
        return blockPos;
    }

    // Return the block up to match the World#getHeight
    public static BlockPos getGround(Level world, BlockPos blockPos) {
        return getBlockUnder(world, Heightmap.Types.MOTION_BLOCKING, blockPos).above();
    }
    public static BlockPos getGround(LivingEntity entityIn) {
        return getBlockUnder(entityIn.level, Heightmap.Types.MOTION_BLOCKING, entityIn.blockPosition()).above();
    }

    public static BlockPos getHighestBlock(Level worldIn, BlockPos positionIn) {
        return worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, positionIn);
    }

    public static int getTerrainHeight(Level worldIn, BlockPos positionIn) {
        return getHighestBlock(worldIn, positionIn).getY();
    }
    public static int getTerrainHeight(LivingEntity entityIn) {
        return getHighestBlock(entityIn.level, entityIn.blockPosition()).getY();
    }

    public static Pair<BlockPos, BlockPos> getTerrainFeatureInRadius(Level worldIn, BlockPos positionIn, int radius) {
        BlockPos areaTerrainHighest = getHighestBlock(worldIn, positionIn);
        BlockPos areaTerrainLowest = getHighestBlock(worldIn, positionIn);
        for (int i = positionIn.getX() - radius; i <= positionIn.getX() + radius; i++) {
            for (int j = positionIn.getZ() - radius; j <= positionIn.getZ() + radius; j++) {
                BlockPos currentBlock = getHighestBlock(worldIn, new BlockPos(i, 0, j));
                if (currentBlock.getY() > areaTerrainHighest.getY()) {
                    areaTerrainHighest = currentBlock;
                }
                if (currentBlock.getY() < areaTerrainLowest.getY()) {
                    areaTerrainLowest = currentBlock;
                }
            }
        }
        return Pair.of(areaTerrainLowest, areaTerrainHighest);
    }

    public static BlockPos getSkyPosOnPath(EntityDragonBase dragonIn) {
        return null;
    }
    @Nullable
    public static BlockPos getSkyPosOnPath(Level worldIn, Vec3 startIn, Vec3 direction, float maxLength, int radius) {
        Vec3 directionXZ = new Vec3(direction.x, 0, direction.z).normalize();

        for (int i = 0; i < maxLength; i++) {
            BlockPos currentBlockpos = new BlockPos(getDirectionOffset(startIn, directionXZ, i));
            if (canAreaSeeSky(worldIn, currentBlockpos, radius)) {
                return new BlockPos(currentBlockpos.getX(), startIn.y(), currentBlockpos.getZ());
            }
        }
        return null;
    }

    public static boolean canAreaSeeSky(Level worldIn, BlockPos blockPosIn, int radius) {
        for (int i = blockPosIn.getX() - radius; i <= blockPosIn.getX() + radius; i++) {
            for (int j = blockPosIn.getZ() - radius; j <= blockPosIn.getZ() + radius; j++) {
                if (!worldIn.canSeeSkyFromBelowWater(new BlockPos(i, blockPosIn.getY(), j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAreaPassable(Level worldIn, BlockPos blockPosIn, int radius) {
        for (int i = blockPosIn.getX() - radius; i <= blockPosIn.getX() + radius; i++) {
            for (int j = blockPosIn.getZ() - radius; j <= blockPosIn.getZ() + radius; j++) {
                if (Heightmap.Types.MOTION_BLOCKING.isOpaque().test(worldIn.getBlockState(blockPosIn))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BlockPos getHighestPosOnPath(EntityDragonBase dragonIn, Vec3 targetPos) {
        return getHighestPosOnPath(dragonIn.level, dragonIn.getHeadPosition(), targetPos, dragonIn.getBbWidth());
    }

    public static BlockPos getHighestPosOnPath(Level worldIn, Vec3 startIn, Vec3 endIn, float width) {
        double length = endIn.distanceTo(startIn);
        Vec3 direction = endIn.subtract(startIn).normalize();
        Vec3 directionXZ = new Vec3(direction.x, 0, direction.z).normalize();

        Vec3 central = startIn;
        Vec3 leftWing = central.add(directionXZ.yRot(90 * ((float) Math.PI / 180F)).scale(width));
        leftWing = worldIn.isEmptyBlock(new BlockPos(leftWing)) || width == 0 ? leftWing : null;
        Vec3 rightWing = central.add(directionXZ.yRot(-90 * ((float) Math.PI / 180F)).scale(width));
        rightWing = worldIn.isEmptyBlock(new BlockPos(rightWing)) || width == 0 ? rightWing : null;

        Vec3 centralTarget = endIn;
        Vec3 leftWingTarget = centralTarget.add(directionXZ.yRot(90 * ((float) Math.PI / 180F)).scale(width));
        Vec3 rightWingTarget = centralTarget.add(directionXZ.yRot(-90 * ((float) Math.PI / 180F)).scale(width));

        BlockPos highestBlock = new BlockPos(startIn.x, 0, startIn.z);
        int highestDistance = 0;
        for (int i = 0; i < length; i++) {
            BlockPos currentCentral = getHighestBlock(worldIn, new BlockPos(getDirectionOffset(central, direction, i)));
            if (currentCentral.getY() > highestBlock.getY()) {
                highestBlock = currentCentral;
                highestDistance = i;
            }
            if (leftWing != null) {
                BlockPos currentLeft = getHighestBlock(worldIn, new BlockPos(getDirectionOffset(leftWing, direction, i)));
                if (currentLeft.getY() > highestBlock.getY()) {
                    highestBlock = currentLeft;
                    highestDistance = i;
                }
            }
            if (rightWing != null) {
                BlockPos currentRight = getHighestBlock(worldIn, new BlockPos(getDirectionOffset(rightWing, direction, i)));
                if (currentRight.getY() > highestBlock.getY()) {
                    highestBlock = currentRight;
                    highestDistance = i;
                }
            }
        }
        BlockPos centralPos = new BlockPos(getDirectionOffset(central, direction, highestDistance));
        return new BlockPos(centralPos.getX(), highestBlock.getY(), centralPos.getZ());
    }

    public static double getFlightHeight(Entity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return 0;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        return dragon.position().y - getGround(dragon.level, dragon.blockPosition()).getY();
    }

    public static Vec3 getDirectionOffset(Vec3 startIn, Vec3 direction, float length) {
        return startIn.add(direction.normalize().scale(length));
    }

    public static double degree2Radian(double degreeIn) {
        return degreeIn * 0.0174532925199D;
    }

    public static double radian2Degree(double radianIn) {
        return radianIn * 57.2957763671875D;
    }
}
