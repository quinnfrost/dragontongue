package com.github.quinnfrost.dragontongue.iceandfire.pathfinding;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import org.jetbrains.annotations.NotNull;

public class NodeProcessorFly extends FlyNodeEvaluator {

    @Override
    public void prepare(@NotNull PathNavigationRegion p_225578_1_, @NotNull Mob p_225578_2_) {
        super.prepare(p_225578_1_, p_225578_2_);
    }

    public void setEntitySize(float width, float height) {
        this.entityWidth = Mth.floor(width + 1.0F);
        this.entityHeight = Mth.floor(height + 1.0F);
        this.entityDepth = Mth.floor(width + 1.0F);
    }
}
