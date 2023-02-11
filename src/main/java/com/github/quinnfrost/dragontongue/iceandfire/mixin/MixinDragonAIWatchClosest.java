package com.github.quinnfrost.dragontongue.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIWatchClosest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DragonAIWatchClosest.class)
public abstract class MixinDragonAIWatchClosest extends LookAtGoal {
    public MixinDragonAIWatchClosest(MobEntity p_i1631_1_, Class<? extends LivingEntity> p_i1631_2_, float p_i1631_3_) {
        super(p_i1631_1_, p_i1631_2_, p_i1631_3_);
    }

    /**
     * @author
     * @reason Fix the issue that dragon's heading got change after dismount, this AI now will not trigger when there is a passenger.
     */
    @Overwrite(remap = false)
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
