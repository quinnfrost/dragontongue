package com.github.quinnfrost.dragontongue.client.preview;


import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

@PreviewPlugin(
        mod = "minecraft"
)
public class BasicPlugin implements PreviewProvider {
    public BasicPlugin() {
    }

    public Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(PlayerEntity player, Item shootable) {
        if (shootable == Items.BOW || shootable == IafItemRegistry.DRAGON_BOW) {
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
