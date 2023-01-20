package com.github.quinnfrost.dragontongue.iceandfire.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public class TestAIDontMove extends Goal {
    private final MobEntity entity;

    public TestAIDontMove(MobEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public void startExecuting() {
        entity.setNoAI(false);
    }

    @Override
    public void tick() {

    }
}
