package com.github.quinnfrost.dragontongue.mixin.iceandfire;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.Node;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.SurfaceType;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

@Mixin(AbstractPathJob.class)
public abstract class MixinAbstractPathJob {

    @Shadow private boolean allowJumpPointSearchTypeWalk;

    @Shadow protected abstract boolean walk(Node parent, BlockPos dPos);

    @Shadow @Final protected IWorldReader world;

    @Shadow protected WeakReference<LivingEntity> entity;

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/pathjobs/AbstractPathJob;performJumpPointSearch(Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/Node;Lnet/minecraft/util/math/BlockPos;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/Node;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$performJumpPointSearch(Node parent, BlockPos dPos, Node node, CallbackInfo ci) {
//        head$performJumpPointSearch(parent, dPos, node);
//        ci.cancel();
    }
    private void head$performJumpPointSearch(final Node parent, final BlockPos dPos, final Node node) {
        if (node.getHeuristic() <= parent.getHeuristic()) {
            walk(node, dPos);
        }
    }

    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/pathjobs/AbstractPathJob;isWalkableSurface(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/SurfaceType;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$isWalkableSurface(BlockState blockState, BlockPos pos, CallbackInfoReturnable<SurfaceType> cir) {
        cir.setReturnValue(head$isWalkableSurface(blockState, pos));
        cir.cancel();
    }
    protected SurfaceType head$isWalkableSurface(final BlockState blockState, final BlockPos pos) {
        final Block block = blockState.getBlock();
        if (block instanceof WallBlock
                || block instanceof FireBlock
                || block instanceof CampfireBlock
                || block instanceof BambooBlock
                || (blockState.getShape(world, pos).getEnd(Direction.Axis.Y) > 1.0)) {
            return SurfaceType.NOT_PASSABLE;
        }
        if (block instanceof FenceBlock
                || block instanceof FenceGateBlock) {
            if (entity.get() != null && entity.get().stepHeight > 1.5f) {
                return SurfaceType.WALKABLE;
            }
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (fluid != null && !fluid.isEmpty() && (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA)) {
            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof AbstractSignBlock) {
            return SurfaceType.DROPABLE;
        }

        if (blockState.getMaterial().isSolid()
                || (blockState.getBlock() == Blocks.SNOW && blockState.get(SnowBlock.LAYERS) > 1)
                || block instanceof CarpetBlock) {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;
    }


}
