package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.Registration;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.message.MessageCrowWand;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemCrowWand extends Item {

    public ItemCrowWand() {
        super(new Properties()
                .group(Registration.TAB_DRAGONTONGUE)
                .maxStackSize(1)
                .isImmuneToFire()
                .rarity(Rarity.EPIC)
        );
    }

    /**
     * Send usage to the server and server will do the ray trace and teleport player
     * Crow wand function:
     *      Main hand right click: teleport to crosshair location
     *      Offhand right click: same, also use sneak key to fallback
     * @param world
     * @param player
     * @param hand
     * @return
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        // Skip if on server side
        if (!world.isRemote) {
            return super.onItemRightClick(world, player, hand);
        }

        if (Config.DEBUG.get().booleanValue()) {
            DragonTongue.LOGGER.debug("Crow wand used: sneaking " + player.isSneaking() + " hand " + hand);
        }

        // Send wand use message at client side
        // player.getCooldownTracker().setCooldown(this,20);
        if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.TELEPORT));
        } else if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.LIGHTNING));
        } else if (!player.isSneaking() && hand == Hand.OFF_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.FALLBACK));
        } else if (player.isSneaking() && hand == Hand.OFF_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.FANG));
        }

        return super.onItemRightClick(world, player, hand);

    }
}
