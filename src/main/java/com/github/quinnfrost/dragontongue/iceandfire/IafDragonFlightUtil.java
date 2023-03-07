package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.utils.util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

// This class came from DragonUtils, with some modification
public class IafDragonFlightUtil {
    public static int getPreferredFlightLevel(EntityDragonBase dragon) {
        return (int) Math.ceil(dragon.getRenderSize() + dragon.getBoundingBox().getYSize());
    }
    public static BlockPos getBlockInViewEscort(EntityDragonBase dragon) {
        int preferredFlightHeight = getPreferredFlightLevel(dragon);

        BlockPos escortPos = dragon.getEscortPosition();
        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, escortPos);
        int distFromGround = escortPos.getY() - ground.getY();
        for (int i = 0; i < 10; i++) {
            BlockPos pos = new BlockPos(escortPos.getX() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance / 2,
                    (distFromGround > preferredFlightHeight
                            ? escortPos.getY() + preferredFlightHeight - dragon.getRNG().nextInt(preferredFlightHeight / 2)
                            : escortPos.getY() + preferredFlightHeight + dragon.getRNG().nextInt(preferredFlightHeight / 2)),
                    (escortPos.getZ() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance) - IafConfig.dragonWanderFromHomeDistance / 2));
            if (dragon.getDistanceSquared(Vector3d.copyCentered(pos)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(pos))) {
                return pos;
            }
        }
        return null;
    }

    public static BlockPos getWaterBlockInViewEscort(EntityDragonBase dragon) {
        // In water escort
        BlockPos inWaterEscortPos = dragon.getEscortPosition();
        // We don't need to get too close
        if (MathHelper.abs(dragon.getPosition().getX() - inWaterEscortPos.getX()) < dragon.getBoundingBox().getAverageEdgeLength()
                && MathHelper.abs(dragon.getPosition().getZ() - inWaterEscortPos.getZ()) < dragon.getBoundingBox().getAverageEdgeLength()) {
            return dragon.getPosition();
        }
        // Takes off if the escort position is no longer in water, mainly for using elytra to fly out of the water
        if (inWaterEscortPos.getY() - dragon.getPosY() > 8 + dragon.getYNavSize() && !dragon.world.getFluidState(inWaterEscortPos.down()).isTagged(FluidTags.WATER)) {
            dragon.setHovering(true);
        }
        // Swim directly to the escort position
        return inWaterEscortPos;
    }

    public static BlockPos getWaterBlockInView(EntityDragonBase dragon) {
        float radius = 0.75F * (0.7F * dragon.getRenderSize() / 3) * -7 - dragon.getRNG().nextInt(dragon.getDragonStage() * 6);
        float neg = dragon.getRNG().nextBoolean() ? 1 : -1;
        float angle = (0.01745329251F * dragon.renderYawOffset) + 3.15F + (dragon.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(dragon.getPosX() + extraX, 0, dragon.getPosZ() + extraZ);
        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) dragon.getPosY() - ground.getY();
        BlockPos newPos = radialPos.up(distFromGround > 16 ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(16) - 8) : (int) dragon.getPosY() + dragon.getRNG().nextInt(16) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        BlockPos surface = dragon.world.getFluidState(newPos.down(2)).isTagged(FluidTags.WATER) ? newPos.down(dragon.getRNG().nextInt(10) + 1) : newPos;
        if (dragon.getDistanceSquared(Vector3d.copyCentered(surface)) > 6 && dragon.world.getFluidState(surface).isTagged(FluidTags.WATER)) {
            return surface;
        }
        return null;
    }

    public static BlockPos getBlockInView(EntityDragonBase dragon) {
        // Get flight level
        int preferredFlightHeight = getPreferredFlightLevel(dragon);

        float radius = 12 * (0.7F * dragon.getRenderSize() / 3);
        float neg = dragon.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = dragon.renderYawOffset;
        // Wander around roost
        if (dragon.hasHomePosition && dragon.homePos != null) {
            BlockPos dragonPos = dragon.getPosition();
            BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dragonPos);
            int distFromGround = (int) dragon.getPosY() - ground.getY();
            for (int i = 0; i < 10; i++) {
                BlockPos homePos = dragon.homePos.getPosition();
                // Get a random position
                BlockPos pos = new BlockPos(
                        homePos.getX() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance,
                        (distFromGround > preferredFlightHeight
                                ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight * 2f / 3f)
                                : (int) dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) + 1),
                        (homePos.getZ() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance));
                if (dragon.getDistanceSquared(Vector3d.copyCentered(pos)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(pos))) {
                    return pos;
                }
            }
        }
        // Wander for homeless
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (dragon.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(dragon.getPosX() + extraX, 0, dragon.getPosZ() + extraZ);
        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, radialPos);
        int distFromGround = (int) dragon.getPosY() - ground.getY();
        BlockPos newPos = radialPos.up(distFromGround > preferredFlightHeight
                ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight * 2f / 3f)
                : (int) dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        if (dragon.getDistanceSquared(Vector3d.copyCentered(newPos)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(newPos))) {
            return pos;
        }
        return null;
    }

    public static int getTerrainHeight(World worldIn, BlockPos positionIn) {
        return getHighestBlock(worldIn, positionIn).getY();
    }
    public static BlockPos getHighestBlock(World worldIn, BlockPos positionIn) {
        return worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, positionIn);
    }

    public static Pair<BlockPos, BlockPos> getTerrainFeatureInRadius(World worldIn, BlockPos positionIn, int radius) {
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

    public static boolean canAreaSeeSky(World worldIn, BlockPos blockPosIn, int radius) {
        for (int i = blockPosIn.getX() - radius; i <= blockPosIn.getX() + radius; i++) {
            for (int j = blockPosIn.getZ() - radius; j <= blockPosIn.getZ() + radius; j++) {
                if (!worldIn.canBlockSeeSky(new BlockPos(i, blockPosIn.getY(), j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BlockPos getHighestPosOnPath(EntityDragonBase dragonIn, Vector3d targetPos) {
        return getHighestPosOnPath(dragonIn.world, dragonIn.getHeadPosition(), targetPos, dragonIn.getWidth());
    }

    public static BlockPos getHighestPosOnPath(World worldIn, Vector3d startIn, Vector3d endIn, float width) {
        double length = endIn.distanceTo(startIn);
        Vector3d direction = endIn.subtract(startIn).normalize();
        Vector3d directionXZ = new Vector3d(direction.x, 0, direction.z).normalize();

        Vector3d central = startIn;
        Vector3d leftWing = central.add(directionXZ.rotateYaw(90 * ((float) Math.PI / 180F)).scale(width));
        leftWing = worldIn.isAirBlock(new BlockPos(leftWing)) || width == 0 ? leftWing : null;
        Vector3d rightWing = central.add(directionXZ.rotateYaw(-90 * ((float) Math.PI / 180F)).scale(width));
        rightWing = worldIn.isAirBlock(new BlockPos(rightWing)) || width == 0 ? rightWing : null;

        Vector3d centralTarget = endIn;
        Vector3d leftWingTarget = centralTarget.add(directionXZ.rotateYaw(90 * ((float) Math.PI / 180F)).scale(width));
        Vector3d rightWingTarget = centralTarget.add(directionXZ.rotateYaw(-90 * ((float) Math.PI / 180F)).scale(width));

        BlockPos highestBlock = new BlockPos(startIn.x, 0, startIn.z);
        int highestDistance = 0;
        for (int i = 0; i < length; i++) {
            BlockPos currentCentral = getHighestBlock(worldIn, new BlockPos(util.getDirectionOffset(central, direction, i)));
            if (currentCentral.getY() > highestBlock.getY()) {
                highestBlock = currentCentral;
                highestDistance = i;
            }
            if (leftWing != null) {
                BlockPos currentLeft = getHighestBlock(worldIn, new BlockPos(util.getDirectionOffset(leftWing, direction, i)));
                if (currentLeft.getY() > highestBlock.getY()) {
                    highestBlock = currentLeft;
                    highestDistance = i;
                }
            }
            if (rightWing != null) {
                BlockPos currentRight = getHighestBlock(worldIn, new BlockPos(util.getDirectionOffset(rightWing, direction, i)));
                if (currentRight.getY() > highestBlock.getY()) {
                    highestBlock = currentRight;
                    highestDistance = i;
                }
            }
        }
        BlockPos centralPos = new BlockPos(util.getDirectionOffset(central, direction, highestDistance));
        return new BlockPos(centralPos.getX(), highestBlock.getY(), centralPos.getZ());
    }

    public static double getFlightHeight(Entity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return 0;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        return dragon.getPositionVec().y - getTerrainHeight(dragon.world, dragon.getPosition());
    }
}
