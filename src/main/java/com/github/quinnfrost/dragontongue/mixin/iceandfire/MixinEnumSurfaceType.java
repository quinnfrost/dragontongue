package com.github.quinnfrost.dragontongue.mixin.iceandfire;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.SurfaceType;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(SurfaceType.class)
public abstract class MixinEnumSurfaceType {

    @Shadow
    public static boolean isWater(final IBlockReader world, final BlockPos pos, @Nullable BlockState pState, @Nullable FluidState pFluidState) {
        return false;
    };

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/SurfaceType;getSurfaceType(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/SurfaceType;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void roadblock$getSurfaceType(IBlockReader world, BlockState blockState, BlockPos pos, CallbackInfoReturnable<SurfaceType> cir) {
        cir.setReturnValue(head$getSurfaceType(world, blockState, pos));
        cir.cancel();
    }
    private static SurfaceType head$getSurfaceType(final IBlockReader world, final BlockState blockState, final BlockPos pos) {
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
        if (shape.getEnd(Direction.Axis.Y) > 1.0)
        {
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (blockState.getBlock() == Blocks.LAVA || (fluid != null && !fluid.isEmpty() && (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA)))
        {
            return SurfaceType.NOT_PASSABLE;
        }

        if (isWater(world, pos, blockState, fluid))
        {
            return SurfaceType.WALKABLE;
        }

        if (block instanceof AbstractSignBlock || block instanceof VineBlock)
        {
            return SurfaceType.DROPABLE;
        }

        if ((blockState.getMaterial().isSolid() && (shape.getEnd(Direction.Axis.X) - shape.getStart(Direction.Axis.X)) > 0.75
                && (shape.getEnd(Direction.Axis.Z) - shape.getStart(Direction.Axis.Z)) > 0.75)
                || (blockState.getBlock() == Blocks.SNOW && blockState.get(SnowBlock.LAYERS) >= 1)
                || block instanceof CarpetBlock)
        {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;

    }

}
