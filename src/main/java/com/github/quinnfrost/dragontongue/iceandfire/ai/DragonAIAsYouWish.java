package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImplementation;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import com.github.quinnfrost.dragontongue.iceandfire.IafTestClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;
import java.util.Optional;

public class DragonAIAsYouWish extends Goal {
    private final EntityDragonBase dragon;
    private final ICapabilityInfoHolder capabilityInfoHolder;
    private boolean isTargetAir;

    public DragonAIAsYouWish(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(new CapabilityInfoHolderImplementation());
        // 不能直接用shouldHover,因为在onEntityJoinWorld的时候区块似乎还没有加载，isAir会导致读取存档时永远等下去
        this.isTargetAir = (dragon.isFlying() || dragon.isHovering());
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }
    @Override
    public boolean shouldExecute() {
        return capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE;
    }
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }
    @Override
    public void startExecuting() {
        BlockPos pos = capabilityInfoHolder.getDestination();

    }

    @Override
    public void tick() {
        BlockPos targetPos = dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null).getDestination();

        if (util.hasArrived(dragon, capabilityInfoHolder.getDestination())
                && capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.HOVER) {
            dragon.getNavigator().clearPath();
            capabilityInfoHolder.setCommandStatus(EnumCommandStatus.HOVER);
            dragon.setMotion(0,0,0);
            // TODO: 指令的目标应该是准星指着的那一格还是上一格
            isTargetAir = shouldHover(dragon);
        }
        switch (capabilityInfoHolder.getCommandStatus()) {
            case REACH:
                dragon.flightManager.setFlightTarget(Vector3d.copyCentered(targetPos));
                dragon.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.1D);

                break;
            case HOVER:
                if (isTargetAir) {
                    IafTestClass.setDragonHover(dragon);
                } else {
                    IafTestClass.setDragonStay(dragon);
                }
                break;
            case CIRCLE:
                break;
        }
    }

    public boolean shouldHover(EntityDragonBase dragon) {
        BlockPos targetPos = capabilityInfoHolder.getDestination();

        return (dragon.world.getBlockState(targetPos).isAir()
                && dragon.world.getBlockState(targetPos.add(0,-1,0)).isAir());
    }
}
