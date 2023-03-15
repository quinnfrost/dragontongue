package com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms;


import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Check if we can walk on a surface, drop into, or neither.
 */
public enum SurfaceType {
    WALKABLE,
    DROPABLE,
    NOT_PASSABLE,
    FLYABLE;

    /**
     * Is the block solid and can be stood upon.
     *
     * @param blockState Block to check.
     * @param pos        the position.
     * @return true if the block at that location can be walked on.
     */
    public static SurfaceType getSurfaceType(final BlockGetter world, final BlockState blockState, final BlockPos pos) {
        final Block block = blockState.getBlock();
        if (block instanceof WallBlock
                || block instanceof FireBlock
                || block instanceof CampfireBlock
                || block instanceof BambooBlock
                || block instanceof DoorBlock
                || block instanceof MagmaBlock) {
            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof FenceBlock
                || block instanceof FenceGateBlock) {
            return SurfaceType.WALKABLE;
        }

        final VoxelShape shape = blockState.getShape(world, pos);
        if (shape.max(Direction.Axis.Y) > 1.0) {
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (blockState.getBlock() == Blocks.LAVA || (fluid != null && !fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA))) {
            return SurfaceType.NOT_PASSABLE;
        }

        if (isWater(world, pos, blockState, fluid)) {
            return SurfaceType.WALKABLE;
        }

        if (block instanceof SignBlock || block instanceof VineBlock) {
            return SurfaceType.DROPABLE;
        }

        if ((blockState.getMaterial().isSolid() && (shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X)) > 0.75
                && (shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z)) > 0.75)
                || (blockState.getBlock() == Blocks.SNOW && blockState.getValue(SnowLayerBlock.LAYERS) >= 1)
                || block instanceof WoolCarpetBlock) {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     *
     * @param pos the pos in the world.
     * @return true if so.
     */
    public static boolean isWater(final LevelReader world, final BlockPos pos) {
        return isWater(world, pos, null, null);
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     *
     * @param pos         the pos in the world.
     * @param pState      existing blockstate or null
     * @param pFluidState existing fluidstate or null
     * @return true if so.
     */
    public static boolean isWater(final BlockGetter world, final BlockPos pos, @Nullable BlockState pState, @Nullable FluidState pFluidState) {
        BlockState state = pState;
        if (state == null) {
            state = world.getBlockState(pos);
        }

        if (state.canOcclude()) {
            return false;
        }
        if (state.getBlock() == Blocks.WATER) {
            return true;
        }

        FluidState fluidState = pFluidState;
        if (fluidState == null) {
            fluidState = world.getFluidState(pos);
        }

        if (fluidState == null || fluidState.isEmpty()) {
            return false;
        }

        final Fluid fluid = fluidState.getType();
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }
}
