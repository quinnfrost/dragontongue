package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

public class DragonBehaviorAttack extends Task<EntityDragonBase> {
    @Nullable
    private LivingEntity attackTarget;
    private int attackTick;
    private boolean longMemory;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int failedPathFindingPenalty = 0;
    private boolean canPenalize = false;
    private float speedTowardsTarget;

    public DragonBehaviorAttack(int durationMinIn, int durationMaxIn, float speedTowardsTarget) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
        this.speedTowardsTarget = speedTowardsTarget;
    }

    public DragonBehaviorAttack() {
        this(60, 60, 1.0f);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase dragon) {
        LivingEntity target = this.getAttackTarget(dragon);
        if (!util.shouldAttack(dragon, target, dragon.getAttributeValue(Attributes.FOLLOW_RANGE))) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        } else if (!dragon.canMove()) {
            return false;
        }

        attackTarget = target;
        return true;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        return attackTarget != null && attackTarget.isAlive();
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {
        dragon.getBrain().removeMemory(MemoryModuleType.ATTACK_TARGET);
        dragon.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        dragon.setAttackTarget(null);
        attackTarget = null;

        dragon.getBrain().setFallbackActivity(RegistryBrains.ACTIVITY_IDLE);
        dragon.getBrain().updateActivity(worldIn.getDayTime(), gameTimeIn);
    }

    @Override
    protected boolean isTimedOut(long gameTime) {
        return false;
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase dragon, long gameTimeIn) {

    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase dragon, long gameTime) {
        attackTarget = dragon.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (attackTarget != null) {
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
//                this.resetTask(worldIn, dragon, gameTime);
                return;
            }

            dragon.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(attackTarget.getPositionVec(), speedTowardsTarget, 0));
            dragon.setAttackTarget(attackTarget);

            final double distanceToTarget = dragon.getDistanceSq(attackTarget.getPosX(), attackTarget.getBoundingBox().minY, attackTarget.getPosZ());
            final double attackReachSqr = this.getAttackReachSqr(dragon, attackTarget);

            this.attackTick = Math.max(this.attackTick - 1, 0);
            if (distanceToTarget <= attackReachSqr && this.attackTick <= 0) {
                this.attackTick = 20;
                dragon.swingArm(Hand.MAIN_HAND);
                dragon.attackEntityAsMob(attackTarget);
            }
        }
    }

    protected LivingEntity getAttackTarget(EntityDragonBase mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    protected double getAttackReachSqr(EntityDragonBase dragon, LivingEntity attackTarget) {
        return dragon.getWidth() * 2.0F * dragon.getWidth() * 2.0F + attackTarget.getWidth();
    }
}
