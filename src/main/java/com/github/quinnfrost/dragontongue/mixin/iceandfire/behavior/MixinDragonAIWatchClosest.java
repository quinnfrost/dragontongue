package com.github.quinnfrost.dragontongue.mixin.iceandfire.behavior;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.ai.DragonAIWatchClosest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonAIWatchClosest.class)
public abstract class MixinDragonAIWatchClosest extends LookAtGoal {
    public MixinDragonAIWatchClosest(MobEntity p_i1631_1_, Class<? extends LivingEntity> p_i1631_2_, float p_i1631_3_) {
        super(p_i1631_1_, p_i1631_2_, p_i1631_3_);
    }

    @Inject(
            method = "shouldExecute()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $shouldExecute(CallbackInfoReturnable<Boolean> cir) {
        if (!head$shouldExecute()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    public boolean head$shouldExecute() {
        if (this.entity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) this.entity;
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY
                    || !dragon.canMove()) {
                return false;
            }
        }
        return true;
    }
}
