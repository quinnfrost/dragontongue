package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

public class DragonAIOwnerTarget extends TargetGoal {
    private TargetingConditions predicate;
    private double awareDistance = 1024;

    private final EntityDragonBase dragon;
    private LivingEntity attacker;
    private int timestamp;

    public DragonAIOwnerTarget(EntityDragonBase dragonIn) {
        super(dragonIn, false);
        this.dragon = dragonIn;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));

        this.predicate = TargetingConditions.DEFAULT.range(1024).ignoreLineOfSight();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (this.dragon.isTame() && !this.dragon.isOrderedToSit()) {
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
                    awareDistance = 32 + 16 * dragon.getDragonStage();
                }
                this.predicate = predicate.range(awareDistance);
                this.attacker = livingentity.getLastHurtMob();
                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.attacker, predicate) && this.dragon.wantsToAttack(this.attacker, livingentity);
            }
        } else {
            return false;
        }
    }

    @Override

    protected double getFollowDistance() {
        return this.awareDistance;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity livingentity = this.dragon.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
