package com.github.quinnfrost.dragontongue.item;

import com.github.quinnfrost.dragontongue.Registration;
import com.github.quinnfrost.dragontongue.config.Config;
import com.github.quinnfrost.dragontongue.enums.EnumCommandEntity;
import com.github.quinnfrost.dragontongue.message.MessageCommandEntity;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemDragonStaff extends Item {
    public ItemDragonStaff() {
        super(new Properties()
                .group(Registration.TAB_DRAGONTONGUE)
                .maxStackSize(1)
                .defaultMaxDamage(6)
                .isImmuneToFire());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote){
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }

        EntityRayTraceResult entityRayTraceResult = util.getTargetEntity(playerIn,
                Config.COMMAND_DISTANCE_MAX.get().floatValue(), 1.0f,
                null);
        try {
            if (!playerIn.isSneaking() && handIn == Hand.MAIN_HAND) {
                RegistryMessages
                        .sendToServer(new MessageCommandEntity(EnumCommandEntity.NEARBY_ATTACK, playerIn,
                                entityRayTraceResult));
            } else if (playerIn.isSneaking() && handIn == Hand.MAIN_HAND && entityRayTraceResult != null) {
                RegistryMessages.sendToServer(
                        new MessageCommandEntity(EnumCommandEntity.FOLLOW, playerIn.getUniqueID(),
                                entityRayTraceResult.getEntity().getUniqueID()));
                RegistryMessages.sendToServer(
                        new MessageCommandEntity(EnumCommandEntity.LAND, playerIn.getUniqueID(),
                                entityRayTraceResult.getEntity().getUniqueID()));

            } else if (!playerIn.isSneaking() && handIn == Hand.OFF_HAND && entityRayTraceResult != null) {
                RegistryMessages.sendToServer(
                        new MessageCommandEntity(EnumCommandEntity.WONDER, playerIn.getUniqueID(),
                                entityRayTraceResult.getEntity().getUniqueID()));

            } else if (playerIn.isSneaking() && handIn == Hand.OFF_HAND && entityRayTraceResult != null) {
                RegistryMessages.sendToServer(
                        new MessageCommandEntity(EnumCommandEntity.SIT, playerIn.getUniqueID(),
                                entityRayTraceResult.getEntity().getUniqueID()));
                RegistryMessages.sendToServer(
                        new MessageCommandEntity(EnumCommandEntity.LAND, playerIn.getUniqueID(),
                                entityRayTraceResult.getEntity().getUniqueID()));
            }
        } catch (Exception ignored) {

        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

}
