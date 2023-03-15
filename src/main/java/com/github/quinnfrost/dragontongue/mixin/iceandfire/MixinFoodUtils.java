package com.github.quinnfrost.dragontongue.mixin.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(targets = "com.github.alexthe666.iceandfire.api.FoodUtils")
public abstract class MixinFoodUtils {
    @Inject(
            remap = false,
            method = "getFoodPoints(Lnet/minecraft/world/entity/Entity;)I",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void roadblock$getFoodPoints(Entity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(head$getFoodPoints(entity));
        cir.cancel();
    }
    private static int head$getFoodPoints(Entity entity) {
        int foodPoints = Math.round(entity.getBbWidth() * entity.getBbHeight() * 10);
        if (entity instanceof AgeableMob) {
            return foodPoints;
        }
        if (entity instanceof Player) {
            return 15;
        }

        return Math.min(foodPoints, 10);
    }

}
