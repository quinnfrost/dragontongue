package com.github.quinnfrost.dragontongue.client.preview;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public interface PreviewProvider {
    default void prepare() {
    }

    default Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(PlayerEntity var1, Item shootable) {
        if (shootable == Items.BOW) {
            return ArrowPreview.class;
        } else if (shootable != Items.SNOWBALL && shootable != Items.ENDER_PEARL && shootable != Items.EGG && shootable != Items.SPLASH_POTION && shootable != Items.LINGERING_POTION) {
            if (shootable == Items.CROSSBOW) {
                return CrossbowPreview.class;
            } else {
                return shootable == Items.TRIDENT ? TridentPreview.class : null;
            }
        } else {
            return ThrowablePreview.class;
        }
    }

}
