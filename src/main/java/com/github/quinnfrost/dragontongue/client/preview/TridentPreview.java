package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class TridentPreview extends ArrowPreview {
    public TridentPreview(World worldIn) {
        super(worldIn);
    }

    protected float waterDrag() {
        return 0.99F;
    }

    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        int timeleft = player.getItemInUseCount();
        if (timeleft > 0) {
            int maxduration = associatedItem.getUseDuration();
            int difference = maxduration - timeleft;
            if (difference >= 10) {
                int j = EnchantmentHelper.getRiptideModifier(associatedItem);
                if ((j <= 0 || player.isWet()) && j == 0) {
                    TridentEntity tridententity = new TridentEntity(player.world, player, associatedItem);
                    tridententity.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F + (float)j * 0.5F, 0.0F);
                    this.shooter = player;
                    return Collections.singletonList(tridententity);
                }
            }
        }

        return null;
    }

}
