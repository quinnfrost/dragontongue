package com.github.quinnfrost.dragontongue.iceandfire.ai;

import net.minecraft.entity.ai.goal.Goal;

public class DragonAIHalt extends Goal {
    @Override
    public boolean shouldExecute() {
        return false;
    }
}
