package com.github.quinnfrost.dragontongue.iceandfire.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.PathNavigationRegion;

public class NodeProcessorWalk extends WalkNodeEvaluator {
    public void prepare(PathNavigationRegion p_225578_1_, Mob p_225578_2_) {
        super.prepare(p_225578_1_, p_225578_2_);
    }

    public void setEntitySize(float width, float height){
        this.entityWidth = Mth.floor(width + 1.0F);
        this.entityHeight = Mth.floor(height + 1.0F);
        this.entityDepth = Mth.floor(width + 1.0F);
    }
}
