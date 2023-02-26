package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.NodeProcessorFly;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.NodeProcessorWalk;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.*;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.PathJobMoveAwayFromLocation;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.PathJobMoveToLocation;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.PathJobRandomPos;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class IafAdvancedDragonPathNavigator extends AdvancedPathNavigate {
    private final EntityDragonBase dragon;

    public IafAdvancedDragonPathNavigator(EntityDragonBase dragon, World world, MovementType valueOf, float width, float height) {
        super(dragon, world, valueOf, width, height);
        this.dragon = dragon;
    }

}
