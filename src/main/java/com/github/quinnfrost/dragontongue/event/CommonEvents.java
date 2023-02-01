package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

public class CommonEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Using dragon staff right-click on a dragon will open gui
        if (DragonTongue.isIafPresent) {
            Entity targetEntity = event.getTarget();
            if (IafHelperClass.isDragon(targetEntity) && event.getEntityLiving() instanceof PlayerEntity) {
                LivingEntity dragon = (LivingEntity) targetEntity;
                PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
                Hand hand = event.getHand();
                ItemStack itemStack = playerEntity.getHeldItem(hand);

                if (playerEntity.isSneaking()) {
                    IafHelperClass.onEntityInteract(event);
                    if (itemStack.isEmpty()
                            && playerEntity.getDistance(dragon) < 5) {
                        ScreenDragon.openGui(playerEntity, dragon);
                        event.setCancellationResult(ActionResultType.SUCCESS);
                        event.setCanceled(true);
                    }

                }


            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        // Display hit mark
        Entity source = event.getSource().getTrueSource();
        LivingEntity hurtEntity = event.getEntityLiving();
        if (source instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) source;
            float damageAmount = event.getAmount();
            if (hurtEntity.isAlive()) {
                RegistryMessages.sendToClient(new MessageClientDisplay(
                                EnumClientDisplay.DAMAGE, 1, Collections.singletonList(String.valueOf(damageAmount))),
                        playerEntity
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        // Display critical hit mark
        Entity source = event.getSource().getTrueSource();
        if (source instanceof ServerPlayerEntity) {
            ServerPlayerEntity attacker = (ServerPlayerEntity) source;
            RegistryMessages.sendToClient(new MessageClientDisplay(
                            EnumClientDisplay.CRITICAL, 1, Collections.singletonList("")),
                    attacker
            );
        }
    }

}
