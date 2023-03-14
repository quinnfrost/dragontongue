package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Optional;

public class DragonBehaviorAttack extends Behavior<EntityDragonBase> {
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
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.REGISTERED
        ), durationMinIn, durationMaxIn);
        this.speedTowardsTarget = speedTowardsTarget;
    }

    public DragonBehaviorAttack() {
        this(60, 60, 1.0f);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase dragon) {
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
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        return attackTarget != null && attackTarget.isAlive();
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        dragon.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        dragon.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        dragon.setTarget(null);
        attackTarget = null;

        dragon.getBrain().setDefaultActivity(RegistryBrains.ACTIVITY_IDLE);
        dragon.getBrain().updateActivityFromSchedule(worldIn.getDayTime(), gameTimeIn);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {

    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase dragon, long gameTime) {
        attackTarget = dragon.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (attackTarget != null) {
            if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
//                this.resetTask(worldIn, dragon, gameTime);
                return;
            }

            dragon.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(attackTarget.position(), speedTowardsTarget, 0));
            dragon.setTarget(attackTarget);

            final double distanceToTarget = dragon.distanceToSqr(attackTarget.getX(), attackTarget.getBoundingBox().minY, attackTarget.getZ());
            final double attackReachSqr = this.getAttackReachSqr(dragon, attackTarget);

            this.attackTick = Math.max(this.attackTick - 1, 0);
            if (distanceToTarget <= attackReachSqr && this.attackTick <= 0) {
                this.attackTick = 20;
                dragon.swing(InteractionHand.MAIN_HAND);
                dragon.doHurtTarget(attackTarget);
            }
        }
    }

    protected LivingEntity getAttackTarget(EntityDragonBase mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    protected double getAttackReachSqr(EntityDragonBase dragon, LivingEntity attackTarget) {
        return dragon.getBbWidth() * 2.0F * dragon.getBbWidth() * 2.0F + attackTarget.getBbWidth();
    }
}
