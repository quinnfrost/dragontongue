package com.github.quinnfrost.dragontongue.client.preview;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public interface PreviewProvider {
    default void prepare() {
    }

    default Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(Player var1, Item shootable) {
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
