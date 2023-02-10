package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.IDeadMob;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.ItemDragonsteelArmor;
import com.github.alexthe666.iceandfire.item.ItemScaleArmor;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
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
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IafHelperClass {
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

    public static double getFlightHeight(Entity dragonIn) {
        if (!isDragon(dragonIn)) {
            return 0;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        BlockPos ground = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dragon.getPosition());
        return dragon.getPosY() - ground.getY();
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
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        AdvancedPathNavigate navigator = (AdvancedPathNavigate) dragon.getNavigator();

        CompoundNBT compoundNBT = new CompoundNBT();
        DragonTongue.debugTarget.writeAdditional(compoundNBT);

        ICapabilityInfoHolder capabilityInfoHolder = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
        BlockPos targetPos = getReachTarget(dragon);

        float distX = (float) (dragon.flightManager.getFlightTarget().x - dragon.getPosX());
        float distY = (float) (dragon.flightManager.getFlightTarget().y - dragon.getPosY());
        float distZ = (float) (dragon.flightManager.getFlightTarget().z - dragon.getPosZ());

        return Arrays.asList(
                "Flight height:" + IafHelperClass.getFlightHeight(dragon),
                "Navigator target:" + (targetPos != null ? targetPos : ""),
                "FlightMgr:" + dragon.flightManager.getFlightTarget().toString() + "(" + util.getDistance(dragon.flightManager.getFlightTarget(), dragon.getPositionVec()) + ")",
                "NavType:" + dragon.navigatorType,
                "Command:" + dragon.getCommand(),
//                "Flying:" + compoundNBT.getByte("Flying"),
//                "HoverTicks:" + dragon.hoverTicks,
//                "TicksStill:" + dragon.ticksStill,
//                "LookVec:" + dragon.getLookVec(),
                "NoPath? " + dragon.getNavigator().noPath(),
                "Hovering:" + dragon.isHovering(),
                "Pitch: " + dragon.getDragonPitch() + "|" + dragon.rotationPitch,
                "Yaw: " + dragon.rotationYaw,
                "PlaneDist:" + (double) MathHelper.sqrt(distX * distX + distZ * distZ),
                "Distance:" + (double) MathHelper.sqrt(distX * distX + distZ * distZ + distY * distY),
                "AirAttack:" + dragon.airAttack,
                "GroundAttack:" + dragon.groundAttack,
                "UseGroundAttack? " + dragon.usingGroundAttack,
                "LookingForRoost? " + dragon.lookingForRoostAIFlag
        );


    }

    public static boolean drawDragonFlightDestination(LivingEntity dragonIn) {
        if (!isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        RegistryMessages.sendToAll(new MessageClientDraw(
                -dragon.getEntityId(), dragon.flightManager.getFlightTarget(),
                dragon.getPositionVec()
        ));
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
     * @return For scale set, "ice","fire","lightning". For steel set, "dragonsteel_ice", "dragonsteel_fire", "dragonsteel_lightning".
     */
    @Nullable
    public static String isFullSetOf(PlayerEntity playerEntity) {
        Item headItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();
        Item chestItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem();
        Item legItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem();
        Item feetItem = playerEntity.getItemStackFromSlot(EquipmentSlotType.FEET).getItem();

        if (!(headItem instanceof ItemScaleArmor) && !(headItem instanceof ItemDragonsteelArmor)) {
            return null;
        }
        if (!(chestItem instanceof ItemScaleArmor) && !(chestItem instanceof ItemDragonsteelArmor)) {
            return null;
        }
        if (!(legItem instanceof ItemScaleArmor) && !(legItem instanceof ItemDragonsteelArmor)) {
            return null;
        }
        if (!(feetItem instanceof ItemScaleArmor) && !(feetItem instanceof ItemDragonsteelArmor)) {
            return null;
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

        return null;
    }

}
