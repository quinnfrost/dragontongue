package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
                BlockPos pos = new BlockPos(
                        homePos.getX() + dragon.getRNG().nextInt(IafConfig.dragonWanderFromHomeDistance * 2) - IafConfig.dragonWanderFromHomeDistance,
                        (distFromGround > preferredFlightHeight
                                ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
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
                ? (int) Math.min(IafConfig.maxDragonFlight, dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) - preferredFlightHeight / 2)
                : (int) dragon.getPosY() + dragon.getRNG().nextInt(preferredFlightHeight) + 1);
        BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
        if (dragon.getDistanceSquared(Vector3d.copyCentered(newPos)) > 6 && !dragon.isTargetBlocked(Vector3d.copyCentered(newPos))) {
            return pos;
        }
        return null;
    }

}
