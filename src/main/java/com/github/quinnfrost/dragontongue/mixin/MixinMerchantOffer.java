package com.github.quinnfrost.dragontongue.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
public abstract class MixinMerchantOffer {
    @Shadow private int maxUses;

    @Inject(
            method = "Lnet/minecraft/world/item/trading/MerchantOffer;<init>(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;IIIFI)V",
            at = @At(value = "RETURN")
    )
    private void $MerchantOffer(ItemStack pBaseCostA, ItemStack pCostB, ItemStack pResult, int pUses, int pMaxUses, int pXp, float pPriceMultiplier, int pDemand, CallbackInfo ci) {
        this.maxUses = 4 * pMaxUses;
    }
}
