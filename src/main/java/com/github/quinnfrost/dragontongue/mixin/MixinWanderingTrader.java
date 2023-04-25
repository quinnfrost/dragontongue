package com.github.quinnfrost.dragontongue.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class MixinWanderingTrader extends AbstractVillager {
    public MixinWanderingTrader(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

//    @ModifyConstant(
//            method = "updateTrades",
//            constant = @Constant(intValue = 5)
//    )
//    private int injectedMaxNumber(int value) {
//        return 10;
//    }

    @Inject(
            method = "updateTrades",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void roadblock$updateTrades(CallbackInfo ci) {
        $updateTrades();
        ci.cancel();
    }

    protected void $updateTrades() {
        VillagerTrades.ItemListing[] avillagertrades$itemlisting = VillagerTrades.WANDERING_TRADER_TRADES.get(1);
        VillagerTrades.ItemListing[] avillagertrades$itemlisting1 = VillagerTrades.WANDERING_TRADER_TRADES.get(2);
        if (avillagertrades$itemlisting != null && avillagertrades$itemlisting1 != null) {
            MerchantOffers merchantoffers = this.getOffers();
            this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, 10);
            this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting1, 2);
//            int i = this.random.nextInt(avillagertrades$itemlisting1.length);
//            VillagerTrades.ItemListing villagertrades$itemlisting = avillagertrades$itemlisting1[i];
//            MerchantOffer merchantoffer = villagertrades$itemlisting.getOffer(this, this.random);
//            if (merchantoffer != null) {
//                merchantoffers.add(merchantoffer);
//            }

        }
    }
}
