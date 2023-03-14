package com.github.quinnfrost.dragontongue.iceandfire.pathfinding;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class NodeProcessorDeathWorm extends NodeEvaluator {

    public Node getStart() {
        return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override
    public Target getGoal(double x, double y, double z) {
        return new Target(this.getNode(Mth.floor(x - 0.4), Mth.floor(y + 0.5D), Mth.floor(z - 0.4)));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockaccessIn, int x, int y, int z, Mob entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        return this.getBlockPathType(blockaccessIn, x, y, z);
    }

    public BlockPathTypes getBlockPathType(BlockGetter worldIn, int x, int y, int z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (!isPassable(worldIn, blockpos.below()) && (blockstate.isAir() || isPassable(worldIn, blockpos))) {
            return BlockPathTypes.BREACH;
        } else {
            return isPassable(worldIn, blockpos) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
        }
    }

    public int getNeighbors(Node[] p_222859_1_, Node p_222859_2_) {
        int i = 0;

        for(Direction direction : Direction.values()) {
            Node pathpoint = this.getSandNode(p_222859_2_.x + direction.getStepX(), p_222859_2_.y + direction.getStepY(), p_222859_2_.z + direction.getStepZ());
            if (pathpoint != null && !pathpoint.closed) {
                p_222859_1_[i++] = pathpoint;
            }
        }

        return i;
    }

    @Nullable
    private Node getSandNode(int p_186328_1_, int p_186328_2_, int p_186328_3_) {
        BlockPathTypes pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
        return pathnodetype != BlockPathTypes.BREACH && pathnodetype != BlockPathTypes.WATER ? null : this.getNode(p_186328_1_, p_186328_2_, p_186328_3_);
    }

    private BlockPathTypes isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        for (int i = p_186327_1_; i < p_186327_1_ + this.entityWidth; ++i) {
            for (int j = p_186327_2_; j < p_186327_2_ + this.entityHeight; ++j) {
                for (int k = p_186327_3_; k < p_186327_3_ + this.entityDepth; ++k) {
                    BlockState blockstate = this.level.getBlockState(blockpos$mutable.set(i, j, k));
                    if (!isPassable(this.level, blockpos$mutable.below()) && (blockstate.isAir() || isPassable(this.level, blockpos$mutable))) {
                        return BlockPathTypes.BREACH;
                    }

                }
            }
        }

        BlockState blockstate1 = this.level.getBlockState(blockpos$mutable);
        return isPassable(blockstate1) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
    }


    private boolean isPassable(BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.SAND || world.getBlockState(pos).getMaterial() == Material.AIR;
    }

    private boolean isPassable(BlockState state) {
        return state.getMaterial() == Material.SAND || state.getMaterial() == Material.AIR;
    }
}