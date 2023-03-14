package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;

public class DragonTaskLookIdle extends Behavior<EntityDragonBase> {
    private double lookX;
    private double lookZ;
    private int idleTime;
    public DragonTaskLookIdle(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT
        ), durationMinIn, durationMaxIn);
    }
    public DragonTaskLookIdle() {
        this(60, 60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        if (!owner.canMove() || owner.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY || owner.isFuelingForge()) {
            return false;
        }
        return owner.getRandom().nextFloat() < 0.02F;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (!entityIn.canMove()) {
            return false;
        }
        return this.idleTime >= 0;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        final double d0 = (Math.PI * 2D) * entityIn.getRandom().nextDouble();
        this.lookX = Mth.cos((float) d0);
        this.lookZ = Mth.sin((float) d0);
        this.idleTime = 20 + entityIn.getRandom().nextInt(20);
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        if (this.idleTime > 0) {
            --this.idleTime;
        }
        BlockPos lookPos = new BlockPos(owner.getX() + this.lookX, owner.getY() + owner.getEyeHeight(), owner.getZ() + this.lookZ);
        owner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(lookPos));
    }
}
