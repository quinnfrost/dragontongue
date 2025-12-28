package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.server.level.ServerLevel;

public class DragonTaskReturnRoost extends Behavior<EntityDragonBase> {
    public DragonTaskReturnRoost(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
            MemoryModuleType.HOME, MemoryStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
    }
    public DragonTaskReturnRoost() {
        this(60,60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        return owner.getTarget() == null && !isAtHome(owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return super.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.start(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        super.tick(worldIn, owner, gameTime);
    }

    private boolean isAtHome(EntityDragonBase dragonIn) {
        if (dragonIn.getBrain().getMemory(MemoryModuleType.HOME).isPresent()) {
            GlobalPos homePos = dragonIn.getBrain().getMemory(MemoryModuleType.HOME).get();
            if (dragonIn.level.dimension().equals(homePos.dimension())
            && util.hasArrived(dragonIn, homePos.pos(), dragonIn.getBoundingBox().getSize())) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }
}
