package com.github.quinnfrost.dragontongue.mixin.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.github.alexthe666.iceandfire.api.FoodUtils")
public abstract class MixinFoodUtils {
    @Inject(
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/api/FoodUtils;getFoodPoints(Lnet/minecraft/entity/Entity;)I",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void roadblock$getFoodPoints(Entity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(head$getFoodPoints(entity));
        cir.cancel();
    }
    private static int head$getFoodPoints(Entity entity) {
        int foodPoints = Math.round(entity.getWidth() * entity.getHeight() * 10);
        if (entity instanceof AgeableEntity) {
            return foodPoints;
        }
        if (entity instanceof PlayerEntity) {
            return 15;
        }

        return Math.min(foodPoints, 10);
    }

}
