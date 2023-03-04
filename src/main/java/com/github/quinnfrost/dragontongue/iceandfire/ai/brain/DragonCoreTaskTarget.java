package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class DragonCoreTaskTarget extends Task<EntityDragonBase> {
    private static final EntityPredicate TARGET_ENTITY_SELECTOR = (new EntityPredicate()).setIgnoresLineOfSight().setUseInvisibilityCheck();
    LivingEntity attacker;
    private long timestamp;
    private int revengeTimerOld;

    public DragonCoreTaskTarget(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    public DragonCoreTaskTarget() {
        this(60, 60);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase dragon) {
        if (dragon.isTamed() && !dragon.isQueuedToSit()) {
            LivingEntity owner = dragon.getOwner();

            if (owner != null) {
                // Owner hurt by target
                attacker = owner.getRevengeTarget();
                if (attacker != null
                        && owner.getRevengeTimer() != this.timestamp
                        && dragon.shouldAttackEntity(attacker, owner)) {
                    return true;
                }
                // Owner hurt target
                attacker = owner.getLastAttackedEntity();
                if (attacker != null
                        && owner.getLastAttackedEntityTime() != this.timestamp
                        && dragon.shouldAttackEntity(attacker, owner)) {
                    return true;
                }
            }
        }
        // Revenge
        attacker = dragon.getRevengeTarget();
        if (dragon.getRevengeTimer() != this.revengeTimerOld
                && attacker != null
                && TARGET_ENTITY_SELECTOR.canTarget(dragon, attacker)) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return false;
    }

    @Override
    protected boolean isTimedOut(long gameTime) {
        return false;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.resetTask(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        dragon.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);

        this.timestamp = gameTimeIn;

//        dragon.getBrain().setFallbackActivity(RegistryBrains.ACTIVITY_ATTACK);
//        dragon.getBrain().switchTo(RegistryBrains.ACTIVITY_ATTACK);
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase dragon, long gameTime) {

    }
}
