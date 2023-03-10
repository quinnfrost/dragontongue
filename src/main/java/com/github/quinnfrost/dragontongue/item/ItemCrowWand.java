package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCrowWand;
import com.github.quinnfrost.dragontongue.message.MessageCrowWand;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemCrowWand extends Item {
    public static int CROW_WAND_MAX_DISTANCE = 512;

    public ItemCrowWand() {
        super(new Properties()
//                .group(Registration.TAB_DRAGONTONGUE)
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

        // Send wand use message at client side
        if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.TELEPORT));
        } else if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.FANG));
        } else if (!player.isSneaking() && hand == Hand.OFF_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.TELEPORT));
        } else if (player.isSneaking() && hand == Hand.OFF_HAND) {
            RegistryMessages.sendToServer(new MessageCrowWand(EnumCrowWand.LIGHTNING));
        }
        // Set usage cooldown
        // player.getCooldownTracker().setCooldown(this,20);

        return super.onItemRightClick(world, player, hand);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (isSelected) {
            BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(entityIn, CROW_WAND_MAX_DISTANCE, 1.0f, RayTraceContext.BlockMode.COLLIDER);
            if (blockRayTraceResult.getType() == RayTraceResult.Type.MISS) {
                OverlayCrossHair.setCrossHairDisplay(null, 0, 2, OverlayCrossHair.IconType.WARN, true);
            }
        }
    }
}
