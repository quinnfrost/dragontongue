package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.server.ServerWorld;

public class DragonOwnerHurtByTarget extends TaskTarget{
    private LivingEntity attacker;
    private int timestamp;
    public DragonOwnerHurtByTarget(int durationMinIn, int durationMaxIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(durationMinIn, durationMaxIn, false, false);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, TameableEntity owner) {
        if (owner.isTamed() && !owner.isQueuedToSit()) {
            LivingEntity livingentity = owner.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.attacker = livingentity.getRevengeTarget();
                int i = livingentity.getRevengeTimer();
                return i != this.timestamp && this.isSuitableTarget(owner, this.attacker, EntityPredicate.DEFAULT) && owner.shouldAttackEntity(this.attacker, livingentity);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, TameableEntity entityIn, long gameTimeIn) {
        entityIn.setAttackTarget(this.attacker);
        LivingEntity livingentity = entityIn.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getRevengeTimer();
        }

        super.startExecuting(worldIn, entityIn, gameTimeIn);
    }
}
