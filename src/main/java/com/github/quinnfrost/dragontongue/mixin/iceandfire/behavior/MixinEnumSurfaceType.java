package com.github.quinnfrost.dragontongue.mixin.iceandfire.behavior;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.SurfaceType;
import net.minecraft.block.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

@Mixin(SurfaceType.class)
public abstract class MixinEnumSurfaceType {

    @Shadow
    public static boolean isWater(final BlockGetter world, final BlockPos pos, @Nullable BlockState pState, @Nullable FluidState pFluidState) {
        return false;
    };

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/SurfaceType;getSurfaceType(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/SurfaceType;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void roadblock$getSurfaceType(BlockGetter world, BlockState blockState, BlockPos pos, CallbackInfoReturnable<SurfaceType> cir) {
        cir.setReturnValue(head$getSurfaceType(world, blockState, pos));
        cir.cancel();
    }
    private static SurfaceType head$getSurfaceType(final BlockGetter world, final BlockState blockState, final BlockPos pos) {
        final Block block = blockState.getBlock();
        if (block instanceof WallBlock
                || block instanceof FireBlock
                || block instanceof CampfireBlock
                || block instanceof BambooBlock
                || block instanceof DoorBlock
                || block instanceof MagmaBlock)
        {
            return SurfaceType.NOT_PASSABLE;
        }
        if (block instanceof FenceBlock
                || block instanceof FenceGateBlock)
        {
            return SurfaceType.WALKABLE;
        }

        final VoxelShape shape = blockState.getShape(world, pos);
        if (shape.max(Direction.Axis.Y) > 1.0)
        {
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (blockState.getBlock() == Blocks.LAVA || (fluid != null && !fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA)))
        {
            return SurfaceType.NOT_PASSABLE;
        }

        if (isWater(world, pos, blockState, fluid))
        {
            return SurfaceType.WALKABLE;
        }

        if (block instanceof SignBlock || block instanceof VineBlock)
        {
            return SurfaceType.DROPABLE;
        }

        if ((blockState.getMaterial().isSolid() && (shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X)) > 0.75
                && (shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z)) > 0.75)
                || (blockState.getBlock() == Blocks.SNOW && blockState.getValue(SnowLayerBlock.LAYERS) >= 1)
                || block instanceof WoolCarpetBlock)
        {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;

    }

}
