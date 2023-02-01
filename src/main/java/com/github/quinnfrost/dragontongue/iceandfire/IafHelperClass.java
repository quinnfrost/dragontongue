package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import com.github.quinnfrost.dragontongue.item.RegistryItems;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IafHelperClass {
    public static boolean isDragon(Entity dragonIn) {
        return DragonTongue.isIafPresent && dragonIn instanceof EntityDragonBase;
    }

    /**
     * Try to get an entity's target position
     *
     * @param entity
     * @return
     */
    public static BlockPos getReachTarget(MobEntity entity) {
        if (isDragon(entity)) {
            EntityDragonBase dragon = (EntityDragonBase) entity;
            AdvancedPathNavigate navigate = (AdvancedPathNavigate) dragon.getNavigator();
            try {
                if (navigate.getTargetPos() != null) {
                    return navigate.getTargetPos();
                } else if (navigate.getDestination() != null) {
                    return navigate.getDestination();
                } else if (navigate.getDesiredPos() != null) {
                    return navigate.getDesiredPos();
                } else {
                    return new BlockPos(dragon.flightManager.getFlightTarget());
                }
            } catch (Exception ignored) {

            }
        } else if (entity.getNavigator().getTargetPos() != null) {
            return entity.getNavigator().getTargetPos();
        }
        return null;
    }

    public static List<String> getAdditionalDragonDebugStrings(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return new ArrayList<>();
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        CompoundNBT compoundNBT = new CompoundNBT();
        DragonTongue.debugTarget.writeAdditional(compoundNBT);

        ICapTargetHolder capabilityInfoHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
        BlockPos targetPos = dragon.getNavigator().getTargetPos();

        return Arrays.asList(
                "Navigator target:" + getReachTarget(dragon).getCoordinatesAsString(),
                "FlightMgr:" + dragon.flightManager.getFlightTarget().toString() + "(" + util.getDistance(dragon.flightManager.getFlightTarget(), dragon.getPositionVec()) + ")",
                "NavType:" + String.valueOf(dragon.navigatorType),
                "Flying:" + compoundNBT.getByte("Flying"),
                "Hovering:" + dragon.isHovering(),
                "HoverTicks:" + dragon.hoverTicks,
                "TicksStill:" + dragon.ticksStill,
                "LookVec:" + dragon.getLookVec()
        );


    }

    public static boolean onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isRemote) {
            return false;
        }
        if (!isDragon(event.getTarget())) {
            return false;
        }

        EntityDragonBase dragon = (EntityDragonBase) event.getTarget();
        PlayerEntity playerEntity = (PlayerEntity) event.getEntityLiving();
        Hand hand = event.getHand();
        ItemStack itemStack = playerEntity.getHeldItem(hand);
        // Hijack the original dragon staff function in EntityDragonBase#1269
        if (itemStack.getItem() == IafItemRegistry.DRAGON_STAFF
                && playerEntity.getDistance(dragon) < 5) {
            playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty("Dragon staff used"), Util.DUMMY_UUID);
            if (dragon.hasHomePosition) {
                dragon.hasHomePosition = false;
                playerEntity.sendStatusMessage(new TranslationTextComponent("dragon.command.remove_home"), true);
                dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                    iCapTargetHolder.setHomePosition(null);
                });
            } else {
                BlockPos pos = dragon.getPosition();
                dragon.homePos = new HomePosition(pos, dragon.world);
                dragon.hasHomePosition = true;
                playerEntity.sendStatusMessage(new TranslationTextComponent("dragon.command.new_home", pos.getX(), pos.getY(), pos.getZ(), dragon.homePos.getDimension()), true);
            }
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
        }
        return true;
    }

}
