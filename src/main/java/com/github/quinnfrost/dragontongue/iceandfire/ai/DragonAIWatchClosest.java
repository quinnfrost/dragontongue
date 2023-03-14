package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class DragonAIWatchClosest extends LookAtPlayerGoal {

    public DragonAIWatchClosest(PathfinderMob LivingEntityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance) {
        super(LivingEntityIn, watchTargetClass, maxDistance);
    }

    @Override
    public boolean canUse() {
        if (this.mob instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) this.mob;
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY
                    || !dragon.canMove()) {
                return false;
            }
        }
        return super.canUse();
    }
}
