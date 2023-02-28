package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class DragonHurtByTarget extends TaskTarget{
    private static final EntityPredicate TARGET_ENTITY_SELECTOR = (new EntityPredicate()).setIgnoresLineOfSight().setUseInvisibilityCheck();

    private int revengeTimerOld;
    public DragonHurtByTarget(int durationMinIn, int durationMaxIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(durationMinIn, durationMaxIn, false, false);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, TameableEntity owner) {
        int i = owner.getRevengeTimer();
        LivingEntity livingentity = owner.getRevengeTarget();
        if (i != this.revengeTimerOld && livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && owner.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
                return false;
            } else {
                return this.isSuitableTarget(owner, livingentity, TARGET_ENTITY_SELECTOR);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, TameableEntity entityIn, long gameTimeIn) {
        entityIn.setAttackTarget(entityIn.getRevengeTarget());
        this.target = entityIn.getAttackTarget();
        this.revengeTimerOld = entityIn.getRevengeTimer();
        this.unseenMemoryTicks = 300;

        super.startExecuting(worldIn, entityIn, gameTimeIn);
    }


}
