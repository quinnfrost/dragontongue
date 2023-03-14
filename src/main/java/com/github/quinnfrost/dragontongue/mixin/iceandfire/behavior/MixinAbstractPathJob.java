package com.github.quinnfrost.dragontongue.mixin.iceandfire.behavior;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.Node;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.SurfaceType;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.block.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AbstractPathJob.class)
public abstract class MixinAbstractPathJob {

    @Shadow private boolean allowJumpPointSearchTypeWalk;

    @Shadow protected abstract boolean walk(Node parent, BlockPos dPos);

    @Shadow @Final protected LevelReader world;

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
                || (blockState.getShape(world, pos).max(Direction.Axis.Y) > 1.0)) {
            return SurfaceType.NOT_PASSABLE;
        }
        if (block instanceof FenceBlock
                || block instanceof FenceGateBlock) {
            if (entity.get() != null && entity.get().maxUpStep > 1.5f) {
                return SurfaceType.WALKABLE;
            }
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (fluid != null && !fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA)) {
            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof SignBlock) {
            return SurfaceType.DROPABLE;
        }

        if (blockState.getMaterial().isSolid()
                || (blockState.getBlock() == Blocks.SNOW && blockState.getValue(SnowLayerBlock.LAYERS) > 1)
                || block instanceof WoolCarpetBlock) {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;
    }


}
