package com.github.quinnfrost.dragontongue.client.preview;


import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@PreviewPlugin(
        mod = "minecraft"
)
public class BasicPlugin implements PreviewProvider {
    public BasicPlugin() {
    }

    public Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(Player player, Item shootable) {
        if (shootable == Items.BOW || shootable == IafItemRegistry.DRAGON_BOW.get()) {
            return BowArrowPreview.class;
        } else if (shootable != Items.SNOWBALL && shootable != Items.ENDER_PEARL && shootable != Items.EGG && shootable != Items.SPLASH_POTION && shootable != Items.LINGERING_POTION) {
            if (shootable == Items.CROSSBOW) {
                return CrossbowArrowPreview.class;
            } else {
                return shootable == Items.TRIDENT ? TridentPreview.class : null;
            }
        } else {
            return ThrowablePreview.class;
        }
    }
}
