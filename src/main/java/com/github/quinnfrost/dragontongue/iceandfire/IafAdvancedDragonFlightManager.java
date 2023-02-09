package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IafAdvancedDragonFlightManager extends IafDragonFlightManager {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    private Vector3d target;
    private IafDragonAttacks.Air prevAirAttack;
    private Vector3d startAttackVec;
    private Vector3d startPreyVec;
    private boolean hasStartedToScorch = false;
    private LivingEntity prevAttackTarget = null;

    public IafAdvancedDragonFlightManager(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
    }

    public static boolean applyDragonFlightManager(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.flightManager = new IafAdvancedDragonFlightManager(dragon);
        return true;
    }

    public static float approach(float number, float max, float min) {
        min = Math.abs(min);
        return number < max ? MathHelper.clamp(number + min, number, max) : MathHelper.clamp(number - min, max, number);
    }

    public static float approachDegrees(float number, float max, float min) {
        float add = MathHelper.wrapDegrees(max - number);
        return approach(number, number + add, min);
    }

    public static float degreesDifferenceAbs(float f1, float f2) {
        return Math.abs(MathHelper.wrapDegrees(f2 - f1));
    }

    public void update() {

        // Attack related
        if (dragon.getAttackTarget() != null && dragon.getAttackTarget().isAlive() ) {
            if (dragon instanceof EntityIceDragon && dragon.isInWater()) {
                if (dragon.getAttackTarget() == null) {
                    dragon.airAttack = IafDragonAttacks.Air.SCORCH_STREAM;
                } else {
                    dragon.airAttack = IafDragonAttacks.Air.TACKLE;
                }
            }
            LivingEntity entity = dragon.getAttackTarget();
            if (dragon.airAttack == IafDragonAttacks.Air.TACKLE) {
                target = new Vector3d(entity.getPosX(), entity.getPosY() + entity.getHeight(), entity.getPosZ());
            }
            if (dragon.airAttack == IafDragonAttacks.Air.HOVER_BLAST) {
                float distY = 5 + dragon.getDragonStage() * 2;
                int randomDist = 20;
                if (dragon.getDistanceSq(entity.getPosX(), dragon.getPosY(), entity.getPosZ()) < 16 || dragon.getDistanceSq(entity.getPosX(), dragon.getPosY(), entity.getPosZ()) > 900) {
                    target = new Vector3d(entity.getPosX() + dragon.getRNG().nextInt(randomDist) - randomDist / 2, entity.getPosY() + distY, entity.getPosZ() + dragon.getRNG().nextInt(randomDist) - randomDist / 2);
                }
                dragon.stimulateFire(entity.getPosX(), entity.getPosY(), entity.getPosZ(), 3);
            }
            if (dragon.airAttack == IafDragonAttacks.Air.SCORCH_STREAM && startPreyVec != null && startAttackVec != null) {
                float distX = (float) (startPreyVec.x - startAttackVec.x);
                float distY = 5 + dragon.getDragonStage() * 2;
                float distZ = (float) (startPreyVec.z - startAttackVec.z);
                target = new Vector3d(entity.getPosX() + distX, entity.getPosY() + distY, entity.getPosZ() + distZ);
                dragon.tryScorchTarget();
                hasStartedToScorch = true;
                if (target != null && dragon.getDistanceSq(target.x, target.y, target.z) < 100) {
                    target = new Vector3d(entity.getPosX() - distX, entity.getPosY() + distY, entity.getPosZ() - distZ);
                }
            }
//
        } else if (target == null || dragon.getDistanceSq(target.x, target.y, target.z) < 4 || !dragon.world.isAirBlock(new BlockPos(target)) && (dragon.isHovering() || dragon.isFlying()) || dragon.getCommand() == 2 && dragon.shouldTPtoOwner()) {
            BlockPos viewBlock = null;

            if (dragon instanceof EntityIceDragon && dragon.isInWater()) {
                viewBlock = IafDragonFlightUtil.getWaterBlockInView(dragon);
            }
            if (dragon.getCommand() == 2 && dragon.isFlying()) {
                viewBlock = IafDragonFlightUtil.getBlockInViewEscort(dragon);
            }else if(dragon.lookingForRoostAIFlag){
                double xDist = Math.abs(dragon.getPosX() - dragon.getHomePosition().getX() - 0.5F);
                double zDist = Math.abs(dragon.getPosZ() - dragon.getHomePosition().getZ() - 0.5F);
                double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);
                BlockPos upPos = dragon.getHomePosition();
                if(dragon.getDistanceSquared(Vector3d.copyCentered(dragon.getHomePosition())) > 200){
                    upPos = upPos.up(30);
                }
                viewBlock = upPos;

            }else if(viewBlock == null){
                viewBlock = IafDragonFlightUtil.getBlockInView(dragon);
            }
            if (viewBlock != null) {
                target = new Vector3d(viewBlock.getX() + 0.5, viewBlock.getY() + 0.5, viewBlock.getZ() + 0.5);
            }
        }
        if (target != null) {
            if (target.y > IafConfig.maxDragonFlight) {
                target = new Vector3d(target.x, IafConfig.maxDragonFlight, target.z);
            }
            if (target.y >= dragon.getPosY() && !dragon.isModelDead()) {
                dragon.setMotion(dragon.getMotion().add(0, 0.1D, 0));

            }
        }

        this.prevAirAttack = dragon.airAttack;


        // In DragonUtils#getBlockInView:65, dragon's flight height is a random value between 0~8, which is too little for elder dragons

        // In DragonUtils#getBlockInView, a random position is returned if dragon is airborne and nowhere to go
//        if (flightTarget != null && IafDragonBehaviorHelper.isDragonInAir(dragon)) {
//            switch (cap.getCommandStatus()) {
//                case REACH:
//                case STAY:
//                case HOVER:
//                    dragon.flightManager.setFlightTarget(flightTarget);
//                    break;
//            }
//        }
        // In IafDragonFlightManger#91, if flight target is on ground, a random position is used instead
        // This is solved by never set flight target to none air block

    }

    public Vector3d getFlightTarget() {
        return target == null ? Vector3d.ZERO : target;
    }

    public void setFlightTarget(Vector3d target){
        this.target = target;
    }

    private float getDistanceXZ(double x, double z) {
        float f = (float) (dragon.getPosX() - x);
        float f2 = (float) (dragon.getPosZ() - z);
        return f * f + f2 * f2;
    }

    public void onSetAttackTarget(@Nullable LivingEntity LivingEntityIn) {
        if (prevAttackTarget != LivingEntityIn) {
            if (LivingEntityIn != null) {
                startPreyVec = new Vector3d(LivingEntityIn.getPosX(), LivingEntityIn.getPosY(), LivingEntityIn.getPosZ());
            } else {
                startPreyVec = new Vector3d(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ());
            }
            startAttackVec = new Vector3d(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ());
        }
        prevAttackTarget = LivingEntityIn;
    }

}
