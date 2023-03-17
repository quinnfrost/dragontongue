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
import com.github.quinnfrost.dragontongue.iceandfire.message.MessageSyncPath;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.Pathfinding;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.*;

public class IafHelperClass {
    public static void startIafPathDebug(Player playerEntity, LivingEntity livingEntity) {
        AbstractPathJob.trackingMap.put(playerEntity, livingEntity.getUUID());
    }

    public static void stopIafPathDebug() {
        Pathfinding.lastDebugNodesVisited = new HashSet<>();
        Pathfinding.lastDebugNodesNotVisited = new HashSet<>();
        Pathfinding.lastDebugNodesPath = new HashSet<>();

        AbstractPathJob.trackingMap.clear();
//        AbstractPathJob.trackingMap.remove(playerEntity);
        RegistryMessages.sendToAll(new MessageSyncPath(new HashSet<>(), new HashSet<>(), new HashSet<>()));
    }

    public static void renderWorldLastEvent(RenderLevelLastEvent event) {
        if (!Pathfinding.lastDebugNodesNotVisited.isEmpty() && !Pathfinding.lastDebugNodesPath.isEmpty() && !Pathfinding.lastDebugNodesVisited.isEmpty()) {
            RenderPath.debugDraw(event.getPartialTick(), event.getPoseStack());
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
    public static BlockPos getReachTarget(Mob entity) {
        try {
            if (entity.getNavigation() instanceof AdvancedPathNavigate) {
                AdvancedPathNavigate navigate = (AdvancedPathNavigate) entity.getNavigation();
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

            } else if (entity.getNavigation().getTargetPos() != null) {
                return entity.getNavigation().getTargetPos();
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
        IafAdvancedDragonPathNavigator navigator = (IafAdvancedDragonPathNavigator) dragon.getNavigation();

        ICapabilityInfoHolder capabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        BlockPos targetPos = getReachTarget(dragon);

        IafAdvancedDragonFlightManager flightManager = (IafAdvancedDragonFlightManager) dragon.flightManager;
        Vec3 currentFlightTarget = dragon.flightManager.getFlightTarget();
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
        if (navigator.isDone()) {
            timeSinceLastPath = String.valueOf(dragon.level.getGameTime() - navigator.pathStartTime);
        }
        String ownerAttackTime = "";
        String ownerTickExisted = "";
        if (dragon.getOwner() != null) {
            ownerAttackTime = String.valueOf(dragon.getOwner().getLastHurtMobTimestamp());
            ownerTickExisted = String.valueOf(dragon.getOwner().tickCount);
        }

        return Arrays.asList(
//                String.format("%.4f - %.4f - %.4f - %.4f - %.4f", dragon.renderYawOffset, dragon.rotationYaw, dragon.rotationPitch, dragon.rotationYawHead, dragon.prevRotationYawHead),
//                "HeadPos: " + EntityBehaviorDebugger.formatVector(dragon.getHeadPosition()),
//                "AnimationTicks: " + dragon.getAnimationTick(),
                "Pitch: " + String.format("%.4f", dragon.getDragonPitch()),
                "Yaw: " + String.format("%.4f", dragon.yRot),
                "HorizontalCollide? " + dragon.horizontalCollision,
                "VerticalCollide? " + dragon.verticalCollision,
                "Flying | Hovering? " + dragon.isFlying() + "|" + dragon.isHovering(),
//                "PlaneDist: " + String.format("%.4f", (float) ((Math.abs(dragon.getMotion().x) + Math.abs(dragon.getMotion().z)) * 6F)),
//                "NoPath? " + dragon.getNavigator().noPath(),
//                "PathTime: " + String.valueOf(dragon.world.getGameTime() - ((IafAdvancedDragonPathNavigator) dragon.getNavigator()).pathStartTime),
//                "Flying:" + dragon.isFlying(),
//                "Hovering:" + dragon.isHovering(),
//                "Render size:" + dragon.getRenderSize() + String.format("(%.2f)", dragon.getRenderScale()),
                "Terrain height:" +  String.format("%d (%d)", IafDragonFlightUtil.getTerrainHeight(dragon), IafDragonFlightUtil.getGround(dragon).getY()),
                "Flight height:" + String.format("%.4f", IafDragonFlightUtil.getFlightHeight(dragon)),
                "CanSeeSky? " + dragon.level.canSeeSkyFromBelowWater(dragon.blockPosition()),
                "Navigator target:" + (targetPos != null ? targetPos : ""),
                "TimeSince:" + timeSinceLastPath,
                "Speed:" + ((IafAdvancedDragonPathNavigator) dragon.getNavigation()).getSpeedFactor(),
                "AIMoveSpeed:" + dragon.getSpeed(),
                "FlightCurrent:" + (flightManager.currentFlightTarget == null ? "" : flightManager.currentFlightTarget + "(" + util.getDistance(flightManager.currentFlightTarget, dragon.position()) + ")"),
                "FlightFinal:" + (flightManager.finalFlightTarget == null ? "" : flightManager.finalFlightTarget + "(" + util.getDistance(flightManager.finalFlightTarget, dragon.position()) + ")"),
                "FlightXZDistance:" + util.getDistanceXZ(dragon.position(), flightManager.finalFlightTarget),
                "FlightLevel:" + flightManager.flightLevel,
                "FlightPhase:" + flightManager.flightPhase,
                "TargetBlocked? " + dragon.isTargetBlocked(flightManager.finalFlightTarget),
                "NavType:" + dragon.navigatorType,
                "Command:" + dragon.getCommand()
//                "AirAttack:" + dragon.airAttack,
//                "GroundAttack:" + dragon.groundAttack,
//                "UseGroundAttack? " + dragon.usingGroundAttack,
//                "LookingForRoost? " + dragon.lookingForRoostAIFlag,
//                "OwnerAttackTime:" + ownerAttackTime,
//                "OwnerTickExisted:" + ownerTickExisted
        );


    }

    public static boolean isIafHostile(LivingEntity livingEntity) {
        if (livingEntity instanceof IDeadMob || !DragonUtils.isAlive(livingEntity)) {
            return false;
        }
        if (livingEntity instanceof EntityDragonBase && ((EntityDragonBase) livingEntity).isModelDead()) {
            return false;
        }
        return livingEntity instanceof EntityDragonBase
                ;
    }

    /**
     * Determine if player is wearing full set of dragon scale/steel set
     *
     * @param playerEntity
     * @return Empty string returned if not a valid set. For scale set, "ice","fire","lightning". For steel set, "dragonsteel_ice", "dragonsteel_fire", "dragonsteel_lightning".
     */
    public static String isFullSetOf(Player playerEntity) {
        Item headItem = playerEntity.getItemBySlot(EquipmentSlot.HEAD).getItem();
        Item chestItem = playerEntity.getItemBySlot(EquipmentSlot.CHEST).getItem();
        Item legItem = playerEntity.getItemBySlot(EquipmentSlot.LEGS).getItem();
        Item feetItem = playerEntity.getItemBySlot(EquipmentSlot.FEET).getItem();

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

            if (headSteelArmor.getMaterial() == chestSteelArmor.getMaterial() && chestSteelArmor.getMaterial() == legSteelArmor.getMaterial() && legSteelArmor.getMaterial() == feetSteelArmor.getMaterial()) {
                if (headSteelArmor.getMaterial() == IafItemRegistry.DRAGONSTEEL_ICE_ARMOR_MATERIAL) {
                    return "dragonsteel_ice";
                } else if (headSteelArmor.getMaterial() == IafItemRegistry.DRAGONSTEEL_FIRE_ARMOR_MATERIAL) {
                    return "dragonsteel_fire";
                } else if (headSteelArmor.getMaterial() == IafItemRegistry.DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL) {
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
        if (entityIn instanceof Player) {
            return isFullSetOf((Player) entityIn).contains("fire") || isFullSetOf((Player) entityIn).contains("dragonsteel");
        }
        if (entityIn instanceof EntityFireDragon) {
            return true;
        }
        return false;
    }

    public static float getXZDistanceSq(Vec3 startIn, Vec3 endIn) {
        float dx = (float) (startIn.x - endIn.x);
        float dz = (float) (startIn.z - endIn.z);
        return dx * dx + dz * dz;
    }

}
