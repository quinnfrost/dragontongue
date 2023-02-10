package com.github.quinnfrost.dragontongue.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonFlightUtil;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.github.alexthe666.iceandfire.entity.IafDragonFlightManager$FlightMoveHelper")
public abstract class MixinFlightMoveHelper extends MovementController {
    public MixinFlightMoveHelper(MobEntity mob) {
        super(mob);
    }

    /*
    Detouring state
    0: no detour
    1: climbing
    2: flying over the terrain
     */
    private int detourState;
    private Vector3d detourTarget;


    @Shadow(remap = false)
    private EntityDragonBase dragon;


    @Inject(
            remap = false,
            method = "<init>(Lcom/github/alexthe666/iceandfire/entity/EntityDragonBase;)V",
            at = @At(value = "RETURN")
    )
    public void FlightMoveHelper(EntityDragonBase dragonBase, CallbackInfo ci) {
        detourState = 0;
        detourTarget = null;
    }

    /**
     * @author
     * @reason Only for test use
     */
    @Overwrite(remap = false)
    public void tick() {
        ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl());
        EnumCommandSettingType.CommandStatus commandStatus = cap.getCommandStatus();
        Vector3d flightTarget = dragon.flightManager.getFlightTarget();
        float distToX = (float) (flightTarget.x - dragon.getPosX());
        float distToY = (float) (flightTarget.y - dragon.getPosY());
        float distToZ = (float) (flightTarget.z - dragon.getPosZ());

        // Try to avoid the CFIT issue
        // Every 1 second (or collided already) the dragon check if there is terrain between her and the target
        if ((detourState == 0 && dragon.world.getGameTime() % 20 == 0) || dragon.collidedHorizontally) {
            if (commandStatus != EnumCommandSettingType.CommandStatus.STAY && commandStatus != EnumCommandSettingType.CommandStatus.HOVER) {
                BlockRayTraceResult blockRayTraceResult = util.rayTraceBlock(dragon.world, dragon.getPositionVec(), dragon.flightManager.getFlightTarget());
                // If there is, she will find a higher place where the target is directly in her sight
                if (!dragon.world.isAirBlock(blockRayTraceResult.getPos())) {
                    BlockPos preferredFlightPos = IafDragonFlightUtil.getHighestBlockInRadius(dragon.world, blockRayTraceResult.getPos(), 10);
                    while (!dragon.world.isAirBlock(blockRayTraceResult.getPos()) && preferredFlightPos.getY() < dragon.world.getHeight()) {
                        preferredFlightPos = preferredFlightPos.add(0, dragon.getYNavSize() * 2, 0);
                        blockRayTraceResult = util.rayTraceBlock(dragon.world, Vector3d.copyCentered(preferredFlightPos), dragon.flightManager.getFlightTarget());
                    }
                    // And take a detour to reach her target
                    detourState = 1;
                    detourTarget = Vector3d.copyCentered(preferredFlightPos);
                }
            }
        }
        if (detourState != 0) {
            distToX = (float) (detourTarget.x - dragon.getPosX());
            distToY = (float) (detourTarget.y - dragon.getPosY());
            distToZ = (float) (detourTarget.z - dragon.getPosZ());
            // Detour state 1: try reach the top of the terrain
            if (detourState == 1 && detourTarget != null) {
                if (dragon.getPositionVec().y >= detourTarget.y
                        && util.hasArrived(dragon, new BlockPos(detourTarget), Double.valueOf(dragon.getYNavSize() * 2))) {
                    detourState = 2;
                    detourTarget = detourTarget.add(
                            (flightTarget.x - detourTarget.x) / 2,
                            dragon.getYNavSize(),
                            (flightTarget.z - detourTarget.z) / 2
                    );
                }
            }
            // Detour state 2: try fly over the terrain (by travel half of the distance in high air)
            if (detourState == 2 && detourTarget != null) {
                if (dragon.getPositionVec().y >= detourTarget.y
                        && util.hasArrived(dragon, new BlockPos(detourTarget), Double.valueOf(dragon.getYNavSize() * 2))) {
                    detourState = 0;
                    detourTarget = null;
                }
            }
        }

        // Following logic makes dragon actually fly to the target, it's not touched except the name
        // The shortest possible distance to the target plane (parallel to y)
        double xzPlaneDist = MathHelper.sqrt(distToX * distToX + distToZ * distToZ);
        // f = 1 - |0.7 * Y| / sqrt(X^2+Y^2)
        double yDistMod = 1.0D - (double) MathHelper.abs(distToY * 0.7F) / xzPlaneDist;
        distToX = (float) ((double) distToX * yDistMod);
        distToZ = (float) ((double) distToZ * yDistMod);
        xzPlaneDist = MathHelper.sqrt(distToX * distToX + distToZ * distToZ);
        double distToTarget = MathHelper.sqrt(distToX * distToX + distToZ * distToZ + distToY * distToY);
        if (distToTarget > 1.0F) {
            float oldYaw = dragon.rotationYaw;
            // Theta = atan2(y,x) - the angle of (x,y)
            float targetYaw = (float) MathHelper.atan2(distToZ, distToX);
            float currentYawTurn = MathHelper.wrapDegrees(dragon.rotationYaw + 90);
            // Radian to degree
            float targetYawDegrees = MathHelper.wrapDegrees(targetYaw * 57.295776F);
            dragon.rotationYaw = IafDragonFlightManager.approachDegrees(currentYawTurn, targetYawDegrees, dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getAttackTarget() != null ? 10 : 4.0F) - 90.0F;
            dragon.renderYawOffset = dragon.rotationYaw;
            if (IafDragonFlightManager.degreesDifferenceAbs(oldYaw, dragon.rotationYaw) < 3.0F) {
                speed = IafDragonFlightManager.approach((float) speed, 1.8F, 0.005F * (1.8F / (float) speed));
            } else {
                speed = IafDragonFlightManager.approach((float) speed, 0.2F, 0.025F);
                if (distToTarget < 100D && dragon.getAttackTarget() != null) {
                    speed = speed * (distToTarget / 100D);
                }
            }
            float finPitch = (float) (-(MathHelper.atan2(-distToY, xzPlaneDist) * 57.2957763671875D));
            dragon.rotationPitch = finPitch;
            float yawTurnHead = dragon.rotationYaw + 90.0F;
            speed *= dragon.getFlightSpeedModifier();
            speed *= detourState == 0
                    ? Math.min(1, distToTarget / 50 + 0.3)  //Make the dragon fly slower when close to target
                    : 1;    // Do not limit speed when detouring
            double lvt_16_1_ = speed * MathHelper.cos(yawTurnHead * 0.017453292F) * Math.abs((double) distToX / distToTarget);
            double lvt_18_1_ = speed * MathHelper.sin(yawTurnHead * 0.017453292F) * Math.abs((double) distToZ / distToTarget);
            double lvt_20_1_ = speed * MathHelper.sin(finPitch * 0.017453292F) * Math.abs((double) distToY / distToTarget);
            double motionCap = 0.2D;
            dragon.setMotion(dragon.getMotion().add(Math.min(lvt_16_1_ * 0.2D, motionCap), Math.min(lvt_20_1_ * 0.2D, motionCap), Math.min(lvt_18_1_ * 0.2D, motionCap)));
        }
    }

}
