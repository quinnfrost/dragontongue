package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.EntityFireDragon;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.IDeadMob;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.ItemDragonsteelArmor;
import com.github.alexthe666.iceandfire.item.ItemScaleArmor;
import com.github.quinnfrost.dragontongue.client.render.RenderPath;
import com.github.quinnfrost.dragontongue.entity.ai.EntityBehaviorDebugger;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageSyncPath;
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageSyncPathReached;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Pathfinding;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.message.MessageClientDraw;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.*;

public class IafHelperClass {
    public static void startIafPathDebug(PlayerEntity playerEntity, LivingEntity livingEntity) {
        AbstractPathJob.trackingMap.put(playerEntity, livingEntity.getUniqueID());
    }

    public static void stopIafPathDebug() {
        Pathfinding.lastDebugNodesVisited = new HashSet<>();
        Pathfinding.lastDebugNodesNotVisited = new HashSet<>();
        Pathfinding.lastDebugNodesPath = new HashSet<>();

        AbstractPathJob.trackingMap.clear();
//        AbstractPathJob.trackingMap.remove(playerEntity);
        RegistryMessages.sendToAll(new MessageSyncPath(new HashSet<>(), new HashSet<>(), new HashSet<>()));
    }

    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        if (!Pathfinding.lastDebugNodesNotVisited.isEmpty() && !Pathfinding.lastDebugNodesPath.isEmpty() && !Pathfinding.lastDebugNodesVisited.isEmpty()) {
            RenderPath.debugDraw(event.getPartialTicks(), event.getMatrixStack());
        }
    }

    public static boolean isDragon(Entity dragonIn) {
        return DragonTongue.isIafPresent && dragonIn instanceof EntityDragonBase;
    }

    public static boolean isHippogryph(Entity hippogryphIn) {
        return DragonTongue.isIafPresent && hippogryphIn instanceof EntityHippogryph;
    }

    public static EntityDragonBase getDragon(Entity dragonIn) {
        if (DragonTongue.isIafPresent) {
            if (dragonIn instanceof EntityDragonBase) {
                return (EntityDragonBase) dragonIn;
            }
            if (dragonIn instanceof EntityDragonPart && ((EntityDragonPart) dragonIn).getParent() != null) {
                return (EntityDragonBase) ((EntityDragonPart) dragonIn).getParent();
            }
        }
        return null;
    }

    /**
     * Try to get an entity's target position
     *
     * @param entity
     * @return
     */
    public static BlockPos getReachTarget(MobEntity entity) {
        try {
            if (entity.getNavigator() instanceof AdvancedPathNavigate) {
                AdvancedPathNavigate navigate = (AdvancedPathNavigate) entity.getNavigator();
                // What is this?
                if (navigate.getTargetPos() != null) {
                    return navigate.getTargetPos();
                } else if (navigate.getDestination() != null) {
                    return navigate.getDestination();
                } else if (navigate.getDesiredPos() != null) {
                    return navigate.getDesiredPos();
                } else {
                    return null;
                }

            } else if (entity.getNavigator().getTargetPos() != null) {
                return entity.getNavigator().getTargetPos();
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public static List<String> getAdditionalDragonDebugStrings(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return new ArrayList<>();
        }
        if (dragonIn == null) {
            return new ArrayList<>();
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        IafAdvancedDragonPathNavigator navigator = (IafAdvancedDragonPathNavigator) dragon.getNavigator();

//        CompoundNBT compoundNBT = new CompoundNBT();
//        DragonTongue.debugTarget.writeAdditional(compoundNBT);

        ICapabilityInfoHolder capabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        BlockPos targetPos = getReachTarget(dragon);

        IafAdvancedDragonFlightManager flightManager = (IafAdvancedDragonFlightManager) dragon.flightManager;
        Vector3d currentFlightTarget = dragon.flightManager.getFlightTarget();
//        float distX = (float) (currentFlightTarget.x - dragon.getPosX());
//        float distY = (float) (currentFlightTarget.y - dragon.getPosY());
//        float distZ = (float) (currentFlightTarget.z - dragon.getPosZ());

        String reachDestString = "";
        if (navigator.pathResult == null) {
            reachDestString = "null";
        } else if (navigator.pathResult.isPathReachingDestination()) {
            reachDestString = "true";
        } else {
            reachDestString = "false";
        }
        String timeSinceLastPath = "";
        if (navigator.noPath()) {
            timeSinceLastPath = String.valueOf(dragon.world.getGameTime() - navigator.pathStartTime);
        }
        String ownerAttackTime = "";
        String ownerTickExisted = "";
        if (dragon.getOwner() != null) {
            ownerAttackTime = String.valueOf(dragon.getOwner().getLastAttackedEntityTime());
            ownerTickExisted = String.valueOf(dragon.getOwner().ticksExisted);
        }

        return Arrays.asList(
                String.format("%.4f - %.4f - %.4f - %.4f - %.4f", dragon.renderYawOffset, dragon.rotationYaw, dragon.rotationPitch, dragon.rotationYawHead, dragon.prevRotationYawHead),
                "HeadPos: " + EntityBehaviorDebugger.formatVector(dragon.getHeadPosition()),
                "AnimationTicks: " + dragon.getAnimationTick(),
                "Pitch: " + String.format("%.4f", dragon.getDragonPitch()),
                "Yaw: " + String.format("%.4f", dragon.rotationYaw),
                "PlaneDist: " + String.format("%.4f", (float) ((Math.abs(dragon.getMotion().x) + Math.abs(dragon.getMotion().z)) * 6F)),
                "NoPath? " + dragon.getNavigator().noPath(),
                "PathTime: " + String.valueOf(dragon.world.getGameTime() - ((IafAdvancedDragonPathNavigator) dragon.getNavigator()).pathStartTime),
                "Flying:" + dragon.isFlying(),
                "Hovering:" + dragon.isHovering(),
                "Render size:" + dragon.getRenderSize() + String.format("(%.2f)", dragon.getRenderScale()),
                "Flight height:" + IafDragonFlightUtil.getFlightHeight(dragon),
                "Navigator target:" + (targetPos != null ? targetPos : ""),
                "ReachDest? " + reachDestString,
                "TimeSince:" + timeSinceLastPath,
                "Speed:" + ((IafAdvancedDragonPathNavigator) dragon.getNavigator()).getSpeedFactor(),
                "AIMoveSpeed:" + dragon.getAIMoveSpeed(),
                "FlightCurrent:" + (flightManager.currentFlightTarget == null ? "" : flightManager.currentFlightTarget + "(" + util.getDistance(flightManager.currentFlightTarget, dragon.getPositionVec()) + ")"),
                "FlightFinal:" + (flightManager.finalFlightTarget == null ? "" : flightManager.finalFlightTarget + "(" + util.getDistance(flightManager.finalFlightTarget, dragon.getPositionVec()) + ")"),
                "FlightXZDistacne:" + util.getDistanceXZ(dragon.getPositionVec(), flightManager.finalFlightTarget),
                "FlightLevel:" + flightManager.flightLevel,
                "FlightPhase:" + flightManager.flightPhase,
                "TargetBlocked? " + dragon.isTargetBlocked(flightManager.finalFlightTarget),
                "NavType:" + dragon.navigatorType,
                "Command:" + dragon.getCommand(),
                "AirAttack:" + dragon.airAttack,
                "GroundAttack:" + dragon.groundAttack,
                "UseGroundAttack? " + dragon.usingGroundAttack,
                "LookingForRoost? " + dragon.lookingForRoostAIFlag,
                "OwnerAttackTime:" + ownerAttackTime,
                "OwnerTickExisted:" + ownerTickExisted
        );


    }

    public static boolean drawDragonFlightDestination(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        if (dragon.flightManager.getFlightTarget() == null) {
            return false;
        }

        RegistryMessages.sendToClient(new MessageClientDraw(
                -dragon.getEntityId(), dragon.flightManager.getFlightTarget(),
                dragon.getPositionVec()
        ), (ServerPlayerEntity) DragonTongue.debugger);

        double length = dragon.flightManager.getFlightTarget().distanceTo(dragon.getPositionVec());
        Vector3d direction = dragon.flightManager.getFlightTarget().subtract(dragon.getPositionVec()).normalize();
        Vector3d directionXZ = new Vector3d(direction.x, 0, direction.z).normalize();

        Vector3d central = dragon.getPositionVec();
        Vector3d leftWing = central.add(directionXZ.rotateYaw(90 * ((float) Math.PI / 180F)).scale(dragon.getRenderSize()));
        Vector3d rightWing = central.add(directionXZ.rotateYaw(-90 * ((float) Math.PI / 180F)).scale(dragon.getRenderSize()));

        Vector3d centralTarget = dragon.flightManager.getFlightTarget();
        Vector3d leftWingTarget = centralTarget.add(directionXZ.rotateYaw(90 * ((float) Math.PI / 180F)).scale(dragon.getRenderSize()));
        Vector3d rightWingTarget = centralTarget.add(directionXZ.rotateYaw(-90 * ((float) Math.PI / 180F)).scale(dragon.getRenderSize()));

        RegistryMessages.sendToClient(new MessageClientDraw(
                -dragon.getEntityId() * 10000, leftWingTarget, leftWing
        ), (ServerPlayerEntity) DragonTongue.debugger);
        RegistryMessages.sendToClient(new MessageClientDraw(
                -dragon.getEntityId() * 10000 + 1, rightWingTarget, rightWing
        ), (ServerPlayerEntity) DragonTongue.debugger);
        return true;
    }

    public static boolean isIafHostile(LivingEntity livingEntity) {
        if (livingEntity instanceof IDeadMob || !DragonUtils.isAlive(livingEntity)) {
            return false;
        }
        if (livingEntity instanceof EntityDragonBase && ((EntityDragonBase) livingEntity).isModelDead()) {
            return false;
        }
        // Todo: what hostiles does iaf have?
        return livingEntity instanceof EntityDragonBase
                ;
    }

    /**
     * Determine if player is wearing full set of dragon scale/steel set
     *
     * @param playerEntity
     * @return Empty string returned if not a valid set. For scale set, "ice","fire","lightning". For steel set, "dragonsteel_ice", "dragonsteel_fire", "dragonsteel_lightning".
     */
    public static String isFullSetOf(PlayerEntity playerEntity) {
        Item headItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();
        Item chestItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem();
        Item legItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem();
        Item feetItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.FEET).getItem();

        if (!(headItem instanceof ItemScaleArmor) && !(headItem instanceof ItemDragonsteelArmor)) {
            return "";
        }
        if (!(chestItem instanceof ItemScaleArmor) && !(chestItem instanceof ItemDragonsteelArmor)) {
            return "";
        }
        if (!(legItem instanceof ItemScaleArmor) && !(legItem instanceof ItemDragonsteelArmor)) {
            return "";
        }
        if (!(feetItem instanceof ItemScaleArmor) && !(feetItem instanceof ItemDragonsteelArmor)) {
            return "";
        }

        if (headItem instanceof ItemScaleArmor && chestItem instanceof ItemScaleArmor && legItem instanceof ItemScaleArmor && feetItem instanceof ItemScaleArmor) {
            ItemScaleArmor headScaleArmor = (ItemScaleArmor) headItem;
            ItemScaleArmor chestScaleArmor = (ItemScaleArmor) chestItem;
            ItemScaleArmor legScaleArmor = (ItemScaleArmor) legItem;
            ItemScaleArmor feetScaleArmor = (ItemScaleArmor) feetItem;

            if (headScaleArmor.eggType == chestScaleArmor.eggType && chestScaleArmor.eggType == legScaleArmor.eggType && legScaleArmor.eggType == feetScaleArmor.eggType) {
                return headScaleArmor.eggType.dragonType.getName();
            }
        }

        if (headItem instanceof ItemDragonsteelArmor && chestItem instanceof ItemDragonsteelArmor && legItem instanceof ItemDragonsteelArmor && feetItem instanceof ItemDragonsteelArmor) {
            ItemDragonsteelArmor headSteelArmor = (ItemDragonsteelArmor) headItem;
            ItemDragonsteelArmor chestSteelArmor = (ItemDragonsteelArmor) chestItem;
            ItemDragonsteelArmor legSteelArmor = (ItemDragonsteelArmor) legItem;
            ItemDragonsteelArmor feetSteelArmor = (ItemDragonsteelArmor) feetItem;

            if (headSteelArmor.getArmorMaterial() == chestSteelArmor.getArmorMaterial() && chestSteelArmor.getArmorMaterial() == legSteelArmor.getArmorMaterial() && legSteelArmor.getArmorMaterial() == feetSteelArmor.getArmorMaterial()) {
                if (headSteelArmor.getArmorMaterial() == IafItemRegistry.DRAGONSTEEL_ICE_ARMOR_MATERIAL) {
                    return "dragonsteel_ice";
                } else if (headSteelArmor.getArmorMaterial() == IafItemRegistry.DRAGONSTEEL_FIRE_ARMOR_MATERIAL) {
                    return "dragonsteel_fire";
                } else if (headSteelArmor.getArmorMaterial() == IafItemRegistry.DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL) {
                    return "dragonsteel_lightning";
                }

            }
        }

        return "";
    }

    public static boolean canSwimInLava(Entity entityIn) {
        if (!DragonTongue.isIafPresent) {
            return false;
        }
        if (entityIn instanceof PlayerEntity) {
            return isFullSetOf((PlayerEntity) entityIn).contains("fire") || isFullSetOf((PlayerEntity) entityIn).contains("dragonsteel");
        }
        if (entityIn instanceof EntityFireDragon) {
            return true;
        }
        return false;
    }

}
