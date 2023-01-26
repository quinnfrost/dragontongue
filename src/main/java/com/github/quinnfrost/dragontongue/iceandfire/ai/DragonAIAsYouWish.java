package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class DragonAIAsYouWish extends Goal {
    private final EntityDragonBase dragon;
    private final ICapTargetHolder capabilityInfoHolder;
    private boolean isTargetAir;
    private BlockPos shouldStay;

    public DragonAIAsYouWish(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragonIn));
        // 不能直接用shouldHover,因为在onEntityJoinWorld的时候区块似乎还没有加载，isAir会导致读取存档时永远等下去
        this.isTargetAir = (dragon.isFlying() || dragon.isHovering());
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }
    @Override
    public boolean shouldExecute() {
        return (
                capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE
                && dragon.getAttackTarget() == null
        );
    }
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }
    @Override
    public void startExecuting() {
        BlockPos pos = capabilityInfoHolder.getDestination();
        this.shouldStay = this.dragon.getPosition();

    }

    @Override
    public void tick() {
        BlockPos targetPos = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(null).getDestination();

        switch (capabilityInfoHolder.getCommandStatus()) {
            case REACH:
                if (util.hasArrived(dragon, capabilityInfoHolder.getDestination())
                        && capabilityInfoHolder.getCommandStatus() == EnumCommandStatus.REACH) {
                    dragon.getNavigator().clearPath();
                    dragon.setMotion(0,0,0);
                    // TODO: 指令的目标应该是准星指着的那一格还是上一格
                    if (shouldHover(dragon)) {
                        capabilityInfoHolder.setCommandStatus(EnumCommandStatus.HOVER);
                    } else {
                        capabilityInfoHolder.setCommandStatus(EnumCommandStatus.STAY);
                    }
                } else {
                    this.shouldStay = targetPos;
                    IafHelperClass.setDragonFlightTarget(dragon, targetPos);
//                    dragon.flightManager.setFlightTarget(new Vector3d(targetPos.getX(), targetPos.getY(), targetPos.getZ()));
//                    dragon.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.1D);
                }
                break;
            case STAY:
                IafHelperClass.setDragonStay(dragon);
                break;
            case HOVER:
                IafHelperClass.setDragonHover(dragon, targetPos);
                break;
            case ATTACK:
                // TODO: 参照IafDragonAttacks.Air#HOVER_BLAST
                IafHelperClass.setDragonBreathTarget(dragon, targetPos);
                if (dragon.isFlying() || dragon.isHovering()) {
                    IafHelperClass.setDragonHover(dragon, shouldStay);
                }

                break;
        }
    }

    public boolean shouldHover(EntityDragonBase dragon) {
        BlockPos targetPos = capabilityInfoHolder.getDestination();

        return (dragon.world.getBlockState(targetPos).isAir()
                && dragon.world.getBlockState(targetPos.add(0,-1,0)).isAir());
    }
}
