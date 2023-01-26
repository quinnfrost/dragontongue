package com.github.quinnfrost.dragontongue.iceandfire.event;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.References;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumClientDisplay;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.message.MessageClientDisplay;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = References.MOD_ID)
public class IafServerEvents {

    @SubscribeEvent
    public static void updateClientMessage(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote || !DragonTongue.isIafPresent) {
            return;
        }

        if (DragonTongue.debugTarget != null && event.getEntity() == DragonTongue.debugTarget) {
            CompoundNBT compoundNBT = new CompoundNBT();
            DragonTongue.debugTarget.writeAdditional(compoundNBT);

            MobEntity target = DragonTongue.debugTarget;
            if (IafHelperClass.isDragon(target))
            {
                EntityDragonBase dragon = (EntityDragonBase) target;
                ICapabilityInfoHolder capabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null);

                RegistryMessages.sendToAll(
                        new MessageClientDisplay(
                                EnumClientDisplay.ENTITY_DEBUG, new ArrayList<>(
                                Arrays.asList(
                                        dragon.getEntityString(),
                                        "Pos:" + dragon.getPosition().getCoordinatesAsString(),
                                        "Motion:" + dragon.getMotion(),
                                        dragon.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                                        dragon.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                                        capabilityInfoHolder.getCommandStatus().toString(),
                                        String.valueOf(dragon.getNavigator().getPath() == null ? "NoPath" : dragon.getNavigator().getPath().reachesTarget()),
                                        capabilityInfoHolder.getDestination().toString() + "(" + util.getDistance(capabilityInfoHolder.getDestination(),dragon.getPosition()) + ")",
                                        "FlightMgr:" + dragon.flightManager.getFlightTarget().toString(),
                                        "Target:" + IafHelperClass.getReachTarget(dragon).toString(),
                                        "NavType:" + String.valueOf(dragon.navigatorType),
                                        "Flying:" + compoundNBT.getByte("Flying"),
                                        "Hovering:" + dragon.isHovering(),
                                        "HoverTicks:" + dragon.hoverTicks,
                                        "TacklingTicks:" + dragon.tacklingTicks,
                                        "TicksStill:" + dragon.ticksStill
                                )
                        )
                        )
                );
            } else {
                ICapabilityInfoHolder capabilityInfoHolder = target.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null);

                RegistryMessages.sendToAll(
                        new MessageClientDisplay(
                                EnumClientDisplay.ENTITY_DEBUG, new ArrayList<>(
                                Arrays.asList(
                                        target.getEntityString(),
                                        "Pos:" + target.getPosition().getCoordinatesAsString(),
                                        "Motion:" + target.getMotion(),
                                        target.goalSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                                        target.targetSelector.getRunningGoals().map(goal -> goal.getGoal().toString()).collect(Collectors.toList()).toString(),
                                        capabilityInfoHolder.getDestination().toString() + "(" + util.getDistance(capabilityInfoHolder.getDestination(),target.getPosition()) + ")",
                                        "Target:" + IafHelperClass.getReachTarget(target),
                                        "Flying:" + compoundNBT.getByte("Flying")
                                )
                        )
                        )
                );

            }
        }
    }

}
