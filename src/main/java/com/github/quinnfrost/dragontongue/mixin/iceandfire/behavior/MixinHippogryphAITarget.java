package com.github.quinnfrost.dragontongue.mixin.iceandfire.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.ai.HippogryphAITarget;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HippogryphAITarget.class)
public abstract class MixinHippogryphAITarget extends NearestAttackableTargetGoal {

    @Shadow(remap = false) private EntityHippogryph hippogryph;

    public MixinHippogryphAITarget(Mob goalOwnerIn, Class targetClassIn, boolean checkSight) {
        super(goalOwnerIn, targetClassIn, checkSight);
    }

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    public void $shouldExecute(CallbackInfoReturnable<Boolean> cir) {
        if (this.hippogryph.isTame()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
