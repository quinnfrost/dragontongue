package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class DragonVanillaTaskAttackMelee extends Behavior<EntityDragonBase> {
    private int attackTick;
    private boolean longMemory;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int failedPathFindingPenalty = 0;
    private boolean canPenalize = false;
    private double speedTowardsTarget;
    public DragonVanillaTaskAttackMelee(int durationMinIn, int durationMaxIn, double speedIn, boolean useLongMemory) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
        this.longMemory = useLongMemory;
        this.speedTowardsTarget = speedIn;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        LivingEntity livingEntity = owner.getTarget();
        if (!(owner.getNavigation() instanceof AdvancedPathNavigate)) {
            return false;
        }
        if (livingEntity == null) {
            return false;
        } else if (!livingEntity.isAlive()) {
            return false;
        } else if (!owner.canMove() || owner.isHovering() || owner.isFlying()) {
            return false;
        } else {
            ((AdvancedPathNavigate) owner.getNavigation()).moveToLivingEntity(livingEntity, speedTowardsTarget);
            return true;
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (!(entityIn.getNavigation() instanceof AdvancedPathNavigate)) {
            return false;
        }
        LivingEntity livingEntity = entityIn.getTarget();
        if (livingEntity != null && !livingEntity.isAlive()) {
            this.stop(worldIn, entityIn, gameTimeIn);
            return false;
        }

        return livingEntity != null && livingEntity.isAlive() && !entityIn.isFlying() && !entityIn.isHovering();
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.delayCounter = 0;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        LivingEntity LivingEntity = entityIn.getTarget();
        if (LivingEntity instanceof Player && (LivingEntity.isSpectator() || ((Player) LivingEntity).isCreative())) {
            entityIn.setTarget(null);
        }
        entityIn.getNavigation().stop();
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        LivingEntity entity = owner.getTarget();
        if(delayCounter > 0){
            delayCounter--;
        }
        if (entity != null) {
            if (owner.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
                this.stop(worldIn, owner, gameTime);
                return;
            }

            ((AdvancedPathNavigate) owner.getNavigation()).moveToLivingEntity(entity, speedTowardsTarget);

            final double d0 = owner.distanceToSqr(entity.getX(), entity.getBoundingBox().minY, entity.getZ());
            final double d1 = this.getAttackReachSqr(owner, entity);
            --this.delayCounter;
            if ((this.longMemory || owner.getSensing().canSee(entity)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entity.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0D || owner.getRandom().nextFloat() < 0.05F)) {
                this.targetX = entity.getX();
                this.targetY = entity.getBoundingBox().minY;
                this.targetZ = entity.getZ();
                this.delayCounter = 4 + owner.getRandom().nextInt(7);

                if (this.canPenalize) {
                    this.delayCounter += failedPathFindingPenalty;
                    if (owner.getNavigation().getPath() != null) {
                        net.minecraft.world.level.pathfinder.Node finalPathPoint = owner.getNavigation().getPath().getEndNode();
                        if (finalPathPoint != null && entity.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                            failedPathFindingPenalty = 0;
                        else
                            failedPathFindingPenalty += 10;
                    } else {
                        failedPathFindingPenalty += 10;
                    }
                }

                if (d0 > 1024.0D) {
                    this.delayCounter += 10;
                } else if (d0 > 256.0D) {
                    this.delayCounter += 5;
                }
                if (owner.canMove()) {
                    this.delayCounter += 15;
                }
            }

            this.attackTick = Math.max(this.attackTick - 1, 0);

            if (d0 <= d1 && this.attackTick <= 0) {
                this.attackTick = 20;
                owner.swing(InteractionHand.MAIN_HAND);
                owner.doHurtTarget(entity);
            }
        }
    }
    protected double getAttackReachSqr(EntityDragonBase dragon, LivingEntity attackTarget) {
        return dragon.getBbWidth() * 2.0F * dragon.getBbWidth() * 2.0F + attackTarget.getBbWidth();
    }

}
