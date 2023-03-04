package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class DragonAIOwnerTarget extends TargetGoal {
    private EntityPredicate predicate;
    private final TameableEntity tameable;
    private LivingEntity attacker;
    private int timestamp;

    public DragonAIOwnerTarget(TameableEntity theEntityTameableIn) {
        super(theEntityTameableIn, false);
        this.tameable = theEntityTameableIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));

        this.predicate = new EntityPredicate().setDistance(tameable.getAttribute(Attributes.FOLLOW_RANGE).getValue());
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean shouldExecute() {
        predicate.setDistance(Math.max(ICapabilityInfoHolder.getCapability(this.tameable).getSelectDistance(), this.tameable.getAttribute(Attributes.FOLLOW_RANGE).getValue()));

        if (this.tameable.isTamed() && !this.tameable.isQueuedToSit()) {
            LivingEntity livingentity = this.tameable.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.attacker = livingentity.getLastAttackedEntity();
                int i = livingentity.getLastAttackedEntityTime();
                return i != this.timestamp && this.isSuitableTarget(this.attacker, predicate) && this.tameable.shouldAttackEntity(this.attacker, livingentity);
            }
        } else {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.attacker);
        LivingEntity livingentity = this.tameable.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastAttackedEntityTime();
        }

        super.startExecuting();
    }
}
