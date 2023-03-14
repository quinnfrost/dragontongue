package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandType;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.world.item.Item.Properties;

public class ItemDragonStaff extends Item {
    public ItemDragonStaff() {
        super(new Properties()
//                .group(Registration.TAB_DRAGONTONGUE)
                .stacksTo(1)
                .defaultDurability(6)
                .fireResistant()
                .rarity(Rarity.RARE)
        );
    }

    /**
     * Dragon staff function
     * Right click on tamed: select context entity
     *
     * @param worldIn
     * @param playerIn
     * @param handIn
     * @return
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!worldIn.isClientSide) {
            return super.use(worldIn, playerIn, handIn);
        }
        ICapabilityInfoHolder capabilityInfoHolder = playerIn.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(playerIn));
        ItemStack itemStack = playerIn.getItemInHand(InteractionHand.MAIN_HAND);

        // Get target entity
        EntityHitResult entityRayTraceResult = util.getTargetEntity(playerIn,
                Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f,
                null);
        BlockHitResult blockRayTraceResult = util.getTargetBlock(playerIn, (float) capabilityInfoHolder.getCommandDistance(), 1.0f, ClipContext.Block.COLLIDER);

        if (entityRayTraceResult == null || entityRayTraceResult.getType() == HitResult.Type.MISS) {
            return super.use(worldIn, playerIn, handIn);
        }

        Entity entity = entityRayTraceResult.getEntity();
        // Different function based on holding hands and sneaking
        if (!playerIn.isShiftKeyDown() && handIn == InteractionHand.MAIN_HAND) {
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandType.FOLLOW, playerIn.getUUID(),
                            entityRayTraceResult.getEntity().getUUID()));
        } else if (playerIn.isShiftKeyDown() && handIn == InteractionHand.MAIN_HAND) {
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandType.SIT, playerIn.getUUID(),
                            entityRayTraceResult.getEntity().getUUID()));


        } else if (!playerIn.isShiftKeyDown() && handIn == InteractionHand.OFF_HAND) {
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandType.WONDER, playerIn.getUUID(),
                            entityRayTraceResult.getEntity().getUUID()));

        } else if (playerIn.isShiftKeyDown() && handIn == InteractionHand.OFF_HAND) {
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandType.FOLLOW, playerIn.getUUID(),
                            entityRayTraceResult.getEntity().getUUID()));
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandType.LAND, playerIn.getUUID(),
                            entityRayTraceResult.getEntity().getUUID()));
        }
        return super.use(worldIn, playerIn, handIn);
    }


}
