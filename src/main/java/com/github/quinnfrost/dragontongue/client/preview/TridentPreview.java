package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

public class TridentPreview extends ArrowPreview {
    public TridentPreview(Level worldIn) {
        super(worldIn);
    }

    protected float waterDrag() {
        return 0.99F;
    }

    public List<AbstractArrow> initializeEntities(Player player, ItemStack associatedItem) {
        int timeleft = player.getUseItemRemainingTicks();
        if (timeleft > 0) {
            int maxduration = associatedItem.getUseDuration();
            int difference = maxduration - timeleft;
            if (difference >= 10) {
                int j = EnchantmentHelper.getRiptide(associatedItem);
                if ((j <= 0 || player.isInWaterOrRain()) && j == 0) {
                    ThrownTrident tridententity = new ThrownTrident(player.level, player, associatedItem);
                    tridententity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 2.5F + (float)j * 0.5F, 0.0F);
                    this.shooter = player;
                    return Collections.singletonList(tridententity);
                }
            }
        }

        return null;
    }

}
