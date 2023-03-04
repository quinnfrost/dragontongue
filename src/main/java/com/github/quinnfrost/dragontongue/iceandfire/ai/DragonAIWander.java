package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonFlightUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

public class DragonAIWander extends Goal {
    private EntityDragonBase dragon;
    private int failedToFindPlainPenalty;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private double speed;
    private int executionChance;
    private boolean mustUpdate;

    public DragonAIWander(EntityDragonBase creatureIn, double speedIn) {
        this(creatureIn, speedIn, 20);
    }

    public DragonAIWander(EntityDragonBase creatureIn, double speedIn, int chance) {
        this.dragon = creatureIn;
        this.speed = speedIn;
        this.executionChance = chance;
        this.setMutexFlags(EnumSet.of(Flag.MOVE));

        this.failedToFindPlainPenalty = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (!dragon.canMove() || dragon.isFuelingForge()) {
            return false;
        }
        if (dragon.getControllingPassenger() != null) {
            return false;
        }
        if (dragon.isFlying() || dragon.isHovering()) {
            return false;
        }
        if (!this.mustUpdate) {
            if (this.dragon.getRNG().nextInt(executionChance) != 0) {
                return false;
            }
        }
        Vector3d Vector3d = RandomPositionGenerator.findRandomTarget(this.dragon, 10 + 5 * dragon.getDragonStage(), 7 + dragon.getDragonStage());
        if (Vector3d == null) {
            return false;
        } else {
            if (dragon.hasHomePosition && Vector3d.distanceTo(dragon.getPositionVec()) > IafConfig.dragonWanderFromHomeDistance) {
                return false;
            }
//            Pair<BlockPos, BlockPos> feature = IafDragonFlightUtil.getTerrainFeatureInRadius(dragon.world, new BlockPos(Vector3d), dragon.getDragonStage());
//            if (Math.abs(feature.getFirst().getY() - feature.getSecond().getY()) >= 2) {
//                if (this.dragon.getRNG().nextInt(++failedToFindPlainPenalty) > 5) {
//                    IafDragonBehaviorHelper.setDragonTakeOff(dragon);
//                }
//                return false;
//            }
            this.xPosition = Vector3d.x;
            this.yPosition = Vector3d.y;
            this.zPosition = Vector3d.z;
            this.mustUpdate = false;

            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.dragon.getNavigator().noPath() && this.dragon.canMove();
    }

    @Override
    public void startExecuting() {
        failedToFindPlainPenalty = 0;
        this.dragon.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.dragon.getNavigator().clearPath();
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    public void setExecutionChance(int newchance) {
        this.executionChance = newchance;
    }
}