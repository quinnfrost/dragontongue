package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;

public class DragonAIWatchClosest extends LookAtGoal {

    public DragonAIWatchClosest(CreatureEntity LivingEntityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance) {
        super(LivingEntityIn, watchTargetClass, maxDistance);
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) this.entity;
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY
                    || !dragon.canMove()) {
                return false;
            }
        }
        return super.shouldExecute();
    }
}
