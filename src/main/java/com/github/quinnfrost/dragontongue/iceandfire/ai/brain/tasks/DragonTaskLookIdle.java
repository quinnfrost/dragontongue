package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public class DragonTaskLookIdle extends Task<EntityDragonBase> {
    private double lookX;
    private double lookZ;
    private int idleTime;
    public DragonTaskLookIdle(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT
        ), 60, 60);
    }
    public DragonTaskLookIdle() {
        this(60, 60);
    }
    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        if (!owner.canMove() || owner.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY || owner.isFuelingForge()) {
            return false;
        }
        return owner.getRNG().nextFloat() < 0.02F;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (!entityIn.canMove()) {
            return false;
        }
        return this.idleTime >= 0;
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        final double d0 = (Math.PI * 2D) * entityIn.getRNG().nextDouble();
        this.lookX = MathHelper.cos((float) d0);
        this.lookZ = MathHelper.sin((float) d0);
        this.idleTime = 20 + entityIn.getRNG().nextInt(20);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        if (this.idleTime > 0) {
            --this.idleTime;
        }
        BlockPos lookPos = new BlockPos(owner.getPosX() + this.lookX, owner.getPosY() + owner.getEyeHeight(), owner.getPosZ() + this.lookZ);
        owner.getLookController().setLookPosition(owner.getPosX() + this.lookX, owner.getPosY() + owner.getEyeHeight(), owner.getPosZ() + this.lookZ, owner.getHorizontalFaceSpeed(), owner.getVerticalFaceSpeed());
//        owner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(lookPos));
    }
}
