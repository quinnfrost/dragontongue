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
    private Optional<Boolean> isOnGround;

    public DragonAIAsYouWish(EntityDragonBase dragonIn) {
        this.dragon = dragonIn;
        this.capabilityInfoHolder = dragonIn.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(new CapabilityInfoHolderImplementation());
        this.setMutexFlags(EnumSet.of(Flag.MOVE));

        this.isOnGround = Optional.empty();
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

    }

    @Override
    public void tick() {
        if (util.hasArrived(dragon, capabilityInfoHolder.getDestination())) {
            dragon.getNavigator().clearPath();
            capabilityInfoHolder.setCommandStatus(EnumCommandStatus.HOVER);
        }
        BlockPos targetPos = dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null).getDestination();
        switch (dragon.getCapability(CapabilityInfoHolder.ENTITY_DATA_STORAGE).orElse(null).getCommandStatus()) {
            case REACH:
                dragon.flightManager.setFlightTarget(Vector3d.copyCentered(targetPos));
                dragon.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.1D);
                isOnGround = Optional.of(dragon.isHovering() || dragon.isFlying());
                break;
            case HOVER:
                IafTestClass.setDragonHover(dragon, isOnGround.orElse(dragon.isFlying() || dragon.isHovering()));
                break;
            case CIRCLE:
                break;
        }
    }
}
