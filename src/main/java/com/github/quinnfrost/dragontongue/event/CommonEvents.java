package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
            IafServerEvent.onEntityInteract(event);

        }
    }

    @SubscribeEvent
    public static void onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityUseItem(event);
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

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {


    }

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent event) {
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityDamage(event);
        }
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
    public static void onEntityAttack(LivingAttackEvent event) {
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityAttacked(event);
        }
    }

}
