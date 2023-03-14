package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.message.MessageCrowWand;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.world.item.Item.Properties;

public class ItemCrowWand extends Item {
    public static int CROW_WAND_MAX_DISTANCE = 512;

    public ItemCrowWand() {
        super(new Properties()
//                .group(Registration.TAB_DRAGONTONGUE)
                .stacksTo(1)
                .fireResistant()
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        // Skip if on server side
        if (!world.isClientSide) {
            return super.use(world, player, hand);
        }

        // Send wand use message at client side
        if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.TELEPORT));
        } else if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.FANG));
        } else if (!player.isShiftKeyDown() && hand == InteractionHand.OFF_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.TELEPORT));
        } else if (player.isShiftKeyDown() && hand == InteractionHand.OFF_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.LIGHTNING));
        }
        // Set usage cooldown
        // player.getCooldownTracker().setCooldown(this,20);

        return super.use(world, player, hand);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (isSelected) {
            BlockHitResult blockRayTraceResult = util.getTargetBlock(entityIn, CROW_WAND_MAX_DISTANCE, 1.0f, ClipContext.Block.COLLIDER);
            if (blockRayTraceResult.getType() == HitResult.Type.MISS) {
                OverlayCrossHair.setCrossHairDisplay(null, 0, 2, OverlayCrossHair.IconType.WARN, true);
            }
        }
    }
}
