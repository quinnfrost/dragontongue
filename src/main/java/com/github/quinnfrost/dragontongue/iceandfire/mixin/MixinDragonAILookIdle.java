package com.github.quinnfrost.dragontongue.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com/github/alexthe666/iceandfire/entity/ai/DragonAILookIdle")
public class MixinDragonAILookIdle {
    @Shadow
    private EntityDragonBase dragon;

    @Inject(
            method = "shouldContinueExecuting()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $shouldContinueExecuting(CallbackInfoReturnable<Boolean> cir) {
        if (!inject$shouldContinueExecuting()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    public boolean inject$shouldContinueExecuting() {
        if (!this.dragon.canMove()) {
            return false;
        }
        return true;
    }
}
