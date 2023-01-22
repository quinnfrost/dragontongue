package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.Registration;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImplementation;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.iceandfire.IafTestClass;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemDragonStaff extends Item {
    public ItemDragonStaff() {
        super(new Properties()
                .group(Registration.TAB_DRAGONTONGUE)
                .maxStackSize(1)
                .defaultMaxDamage(6)
                .isImmuneToFire()
                .rarity(Rarity.RARE)
        );
    }

    /**
     * Dragon staff function
     *      Right click on tamed: select context entity
     *
     *
     * @param worldIn
     * @param playerIn
     * @param handIn
     * @return
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote){
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }
        ICapabilityInfoHolder capabilityInfoHolder = playerIn.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(new CapabilityInfoHolderImplementation());

        // Get target entity
        EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(playerIn,
                Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f,
                null);
        BlockRayTraceResult blockRayTraceResult = util.getTargetBlock(playerIn, (float) capabilityInfoHolder.getCommandDistance(), 1.0f);


        // Different function based on holding hands and sneaking
        if (!playerIn.isSneaking() && handIn == Hand.MAIN_HAND) {
            RegistryMessages
                    .sendToServer(new MessageCommandEntity(EnumCommandEntity.CIRCLE, playerIn.getUniqueID(), blockRayTraceResult
                            ));
        } else if (playerIn.isSneaking() && handIn == Hand.MAIN_HAND) {
            if (entityRayTraceResult == null || entityRayTraceResult.getType() == RayTraceResult.Type.MISS) {
                return super.onItemRightClick(worldIn, playerIn, handIn);
            }
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandEntity.FOLLOW, playerIn.getUniqueID(),
                            entityRayTraceResult.getEntity().getUniqueID()));
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandEntity.LAND, playerIn.getUniqueID(),
                            entityRayTraceResult.getEntity().getUniqueID()));

        } else if (!playerIn.isSneaking() && handIn == Hand.OFF_HAND) {
            if (entityRayTraceResult == null || entityRayTraceResult.getType() == RayTraceResult.Type.MISS) {
                return super.onItemRightClick(worldIn, playerIn, handIn);
            }
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandEntity.WONDER, playerIn.getUniqueID(),
                            entityRayTraceResult.getEntity().getUniqueID()));

        } else if (playerIn.isSneaking() && handIn == Hand.OFF_HAND) {
            if (entityRayTraceResult == null || entityRayTraceResult.getType() == RayTraceResult.Type.MISS) {
                return super.onItemRightClick(worldIn, playerIn, handIn);
            }
            RegistryMessages.sendToServer(
                    new MessageCommandEntity(EnumCommandEntity.HALT, playerIn.getUniqueID(),
                            entityRayTraceResult.getEntity().getUniqueID()));
//            RegistryMessages.sendToServer(
//                    new MessageCommandEntity(EnumCommandEntity.LAND, playerIn.getUniqueID(),
//                            entityRayTraceResult.getEntity().getUniqueID()));
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

}
