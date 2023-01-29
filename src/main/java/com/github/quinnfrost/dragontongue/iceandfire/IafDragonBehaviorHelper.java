package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAIAsYouWish;
import com.github.quinnfrost.dragontongue.iceandfire.ai.DragonAICalmLook;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IafDragonBehaviorHelper {
    public static boolean registerDragonAI(MobEntity mobEntity) {
        if (IafHelperClass.isDragon(mobEntity)) {
            EntityDragonBase dragon = (EntityDragonBase) mobEntity;

            dragon.goalSelector.addGoal(0, new DragonAIAsYouWish(dragon));
            dragon.goalSelector.addGoal(0, new DragonAICalmLook(dragon));
            return true;
        }
        return false;
    }

    public static boolean setDragonTakeOff(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setQueuedToSit(false);
        dragon.setSitting(false);
        dragon.flyTicks = 0;

        return true;
    }

    public static boolean setDragonHover(MobEntity dragonIn, BlockPos hoverPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        dragon.setHovering(true);
        dragon.setFlying(false);
        // Force set hover ticks to prevent dragon from flying triggered by dragon logic
        dragon.hoverTicks = 10;
        dragon.ticksStill = 10;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(hoverPos));
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonStay(MobEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        ICapTargetHolder iCapTargetHolder = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(null);

        dragon.setHovering(false);
        dragon.setFlying(false);
        dragon.ticksStill = 10;
        dragon.getNavigator().clearPath();

        return true;
    }

    public static boolean setDragonAttackTarget(LivingEntity dragonIn, @Nullable LivingEntity target, @Nullable BlockPos breathPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        if (target == null && breathPos != null) {
            dragon.getCapability(CapTargetHolder.TARGET_HOLDER).ifPresent(iCapTargetHolder -> {
                iCapTargetHolder.setDestination(breathPos);
                iCapTargetHolder.setCommandStatus(EnumCommandStatus.BREATH);
            });
        } else {
            dragon.setAttackTarget(target);
        }
        return true;

    }

    public static boolean setDragonBreathTarget(LivingEntity dragonIn, @Nullable BlockPos breathPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

//        dragon.groundAttack = IafDragonAttacks.Ground.FIRE;
//        dragon.usingGroundAttack = true;

        if (breathPos != null) {
            dragon.burningTarget = breathPos;
            dragon.getLookController().setLookPosition(breathPos.getX() + 0.5D, breathPos.getY() + 0.5D, breathPos.getZ() + 0.5D, 180F, 180F);
            dragon.rotationYaw = dragon.renderYawOffset;
            if (dragon.getDragonStage() >= 3) {
                // SyncType=1 : no charge, SyncType=5 : with charge
                dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 5);
            } else {
                dragon.stimulateFire(breathPos.getX() + 0.5F, breathPos.getY() + 0.5F, breathPos.getZ() + 0.5F, 1);
            }
            dragon.setBreathingFire(true);
        } else {
            dragon.setBreathingFire(false);
        }
        return true;
    }

    public static boolean setDragonFlightTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.flightManager.setFlightTarget(Vector3d.copyCentered(blockPos));

        return true;
    }

    public static boolean setDragonReachTarget(LivingEntity dragonIn, BlockPos blockPos) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        if (dragon.getCommand() != 0) {
            dragon.setCommand(0);
        }

        dragon.getNavigator().tryMoveToXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0f);


        return true;
    }

    public static boolean resurrectDragon(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;

        if (dragon.getDeathStage() != 0 || !dragon.isModelDead()) {
            return false;
        }

        dragon.setHealth((float) Math.ceil(dragon.getMaxHealth() / 20.0f));
        dragon.clearActivePotions();
        dragon.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
//        dragon.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
        dragon.addPotionEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
        dragon.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 800, 0));
        dragon.world.setEntityState(dragon, (byte)35);

        dragon.setModelDead(false);
        dragon.setNoAI(false);

        return true;
    }
}
