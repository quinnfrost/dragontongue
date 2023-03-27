package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

public class TridentPreview extends BowArrowPreview {
    public TridentPreview(Level level) {
        super(level);
    }

    public List<AbstractArrow> initializeEntities(Player player, ItemStack associatedItem) {
        if (associatedItem.getItem() instanceof TridentItem) {
            int timeLeft = player.getUseItemRemainingTicks();
            if (timeLeft > 0) {
                int maxDuration = player.getMainHandItem().getUseDuration();
                int difference = maxDuration - timeLeft;
                if (difference >= 10) {
                    ThrownTrident trident = new ThrownTrident(this.level, player, associatedItem);
                    trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + (float)EnchantmentHelper.getRiptide(associatedItem) * 0.5F, 0.0F);
                    return Collections.singletonList(trident);
                }
            }
        }

        return null;
    }

    protected void doWaterSplashEffect() {
    }

}
