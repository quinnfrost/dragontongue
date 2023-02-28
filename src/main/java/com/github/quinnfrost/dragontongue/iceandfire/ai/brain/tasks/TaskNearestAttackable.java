package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

//public class TaskNearestAttackable<T extends LivingEntity> extends TaskTarget {
//    protected final Class<T> targetClass;
//    protected final int targetChance;
//    protected LivingEntity nearestTarget;
//    protected EntityPredicate targetEntitySelector;
//    public TaskNearestAttackable(int durationMinIn, int durationMaxIn, Class<T> targetClassIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
//        super(durationMinIn, durationMaxIn, checkSight, nearbyOnlyIn);
//        this.targetClass = targetClassIn;
//        this.targetChance = targetChanceIn;
//        this.targetEntitySelector = (new EntityPredicate()).setDistance(this.getTargetDistance()).setCustomPredicate(targetPredicate);
//
//    }
//
//    @Override
//    protected boolean shouldExecute(ServerWorld worldIn, TameableEntity owner) {
//        if (this.targetChance > 0 && owner.getRNG().nextInt(this.targetChance) != 0) {
//            return false;
//        } else {
//            this.findNearestTarget();
//            return this.nearestTarget != null;
//        }
//    }
//
//    @Override
//    protected void startExecuting(ServerWorld worldIn, TameableEntity entityIn, long gameTimeIn) {
//        entityIn.setAttackTarget(this.nearestTarget);
//        super.startExecuting();
//    }
//
//    protected AxisAlignedBB getTargetableArea(TameableEntity owner, double targetDistance) {
//        return owner.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
//    }
//
//    protected void findNearestTarget(TameableEntity owner) {
//        if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
//            this.nearestTarget = owner.world.getClosestEntity(this.targetClass, this.targetEntitySelector, owner, owner.getPosX(), owner.getPosYEye(), owner.getPosZ(), this.getTargetableArea(this.getTargetDistance()));
//        } else {
//            this.nearestTarget = owner.world.getClosestPlayer(this.targetEntitySelector, owner, owner.getPosX(), owner.getPosYEye(), owner.getPosZ());
//        }
//
//    }
//    public void setNearestTarget(@Nullable LivingEntity target) {
//        this.nearestTarget = target;
//    }
//}
