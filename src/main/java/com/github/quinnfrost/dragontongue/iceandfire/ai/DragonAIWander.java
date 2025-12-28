package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonFlightUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

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
        this.setFlags(EnumSet.of(Flag.MOVE));

        this.failedToFindPlainPenalty = 0;
    }

    @Override
    public boolean canUse() {
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
            if (this.dragon.getRandom().nextInt(executionChance) != 0) {
                return false;
            }
        }
        Vec3 randomTarget = RandomPos.getPos(this.dragon, 10 + 5 * dragon.getDragonStage(), 7 + dragon.getDragonStage());
        for (int i = 0; i < 5; i++) {
            if (randomTarget == null) {
                continue;
            }
            if (dragon.hasHomePosition && randomTarget.distanceTo(dragon.position()) > IafConfig.dragonWanderFromHomeDistance) {
                randomTarget = null;
                continue;
            }
            Pair<BlockPos, BlockPos> feature = IafDragonFlightUtil.getTerrainFeatureInRadius(dragon.level, new BlockPos(randomTarget), dragon.getDragonStage());
            if (Math.abs(feature.getFirst().getY() - feature.getSecond().getY()) >= 2) {
                randomTarget = null;
                continue;
            }
            break;
        }
        if (randomTarget == null) {
            if (this.dragon.getRandom().nextInt(++failedToFindPlainPenalty) > 10) {
                IafDragonBehaviorHelper.setDragonTakeOff(dragon);
            }
            return false;
        } else {
//            if (dragon.hasHomePosition && randomTarget.distanceTo(dragon.getPositionVec()) > IafConfig.dragonWanderFromHomeDistance) {
//                return false;
//            }
            this.xPosition = randomTarget.x;
            this.yPosition = randomTarget.y;
            this.zPosition = randomTarget.z;
            this.mustUpdate = false;

            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.dragon.getNavigation().isDone() && this.dragon.canMove();
    }

    @Override
    public void start() {
        failedToFindPlainPenalty = 0;
        this.dragon.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }

    @Override
    public void stop() {
        super.stop();
        this.dragon.getNavigation().stop();
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    public void setExecutionChance(int newchance) {
        this.executionChance = newchance;
    }
}