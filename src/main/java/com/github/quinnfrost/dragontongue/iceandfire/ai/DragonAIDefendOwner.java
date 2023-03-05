package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class DragonAIDefendOwner extends TargetGoal {
    private EntityPredicate predicate;
    private double awareDistance = 1024;

    private final EntityDragonBase dragon;
    private LivingEntity attacker;
    private int timestamp;

    public DragonAIDefendOwner(EntityDragonBase dragonIn) {
        super(dragonIn, false);
        this.dragon = dragonIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));

        this.predicate = new EntityPredicate().setDistance(1024).setIgnoresLineOfSight();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean shouldExecute() {
        if (this.dragon.isTamed() && !this.dragon.isQueuedToSit()) {
            LivingEntity livingentity = this.dragon.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                awareDistance = 1024;
                if (!IafDragonBehaviorHelper.isDragonInAir(dragon)) {
                    awareDistance = 64 * dragon.getDragonStage();
//                    awareDistance = 64 * dragon.getHeight();
                }
                if (dragon.isSleeping() || dragon.getCommand() == 1) {
                    awareDistance = 32 * dragon.getDragonStage();
                }
                this.predicate = predicate.setDistance(awareDistance);
                this.attacker = livingentity.getRevengeTarget();
                int i = livingentity.getRevengeTimer();
                return i != this.timestamp && this.isSuitableTarget(this.attacker, predicate) && this.dragon.shouldAttackEntity(this.attacker, livingentity);
            }
        } else {
            return false;
        }
    }

    @Override
    protected double getTargetDistance() {
        return this.awareDistance;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        predicate.setDistance(Math.max(ICapabilityInfoHolder.getCapability(this.dragon).getSelectDistance(), this.dragon.getAttribute(Attributes.FOLLOW_RANGE).getValue()));

        this.goalOwner.setAttackTarget(this.attacker);
        LivingEntity livingentity = this.dragon.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getRevengeTimer();
        }

        super.startExecuting();
    }
}
