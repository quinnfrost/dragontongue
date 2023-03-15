package com.github.quinnfrost.dragontongue.mixin;

import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

//    @Redirect(
//            method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z", ordinal = 0)
//    )
//    private static boolean clearview$setupFog(Entity entity) {
//        if (entity instanceof Player) {
//            Player player = (Player) entity;
//            if (player.isCreative() || player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
//                return true;
//            }
//        }
//        return entity.isSpectator();
//    }

}
