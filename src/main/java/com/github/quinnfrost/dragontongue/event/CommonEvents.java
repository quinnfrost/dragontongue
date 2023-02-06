package com.github.quinnfrost.dragontongue.event;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.event.IafServerEvent;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

public class CommonEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Using dragon staff right-click on a dragon will open gui
        Entity targetEntity = event.getTarget();
        PlayerEntity playerEntity = event.getPlayer();
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onEntityInteract(event);
        }
        if (targetEntity instanceof TameableEntity) {
            TameableEntity tameableEntity = (TameableEntity) targetEntity;
            if (tameableEntity instanceof WolfEntity && playerEntity.isSneaking() && tameableEntity.isOwner(playerEntity)) {
                tameableEntity.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                    if (iCapTargetHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
//                        playerEntity.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Attack decision set to guard"), true);
                        iCapTargetHolder.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.GUARD);
                    } else {
//                        playerEntity.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Attack decision set to default"), true);
                        iCapTargetHolder.setObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE, EnumCommandSettingType.AttackDecisionType.ALWAYS_HELP);
                    }
                    event.setCancellationResult(ActionResultType.SUCCESS);
                    event.setCanceled(true);
                });
            }
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

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        // This seems to be a client only event
        if (DragonTongue.isIafPresent) {
            IafServerEvent.onLivingKnockBack(event);
        }
    }
}
