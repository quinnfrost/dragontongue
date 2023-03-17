package com.github.quinnfrost.dragontongue.mixin;

import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(
            method = "Lnet/minecraft/world/entity/Entity;fireImmune()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void head$fireImmune(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player && util.canSwimInLava((Player)(Object) this)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
