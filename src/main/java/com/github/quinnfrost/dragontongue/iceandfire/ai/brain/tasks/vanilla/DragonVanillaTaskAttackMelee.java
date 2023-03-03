package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

public class DragonVanillaTaskAttackMelee extends Task<EntityDragonBase> {
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
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        LivingEntity livingEntity = owner.getAttackTarget();
        if (!(owner.getNavigator() instanceof AdvancedPathNavigate)) {
            return false;
        }
        if (livingEntity == null) {
            return false;
        } else if (!livingEntity.isAlive()) {
            return false;
        } else if (!owner.canMove() || owner.isHovering() || owner.isFlying()) {
            return false;
        } else {
            ((AdvancedPathNavigate) owner.getNavigator()).moveToLivingEntity(livingEntity, speedTowardsTarget);
            return true;
        }
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (!(entityIn.getNavigator() instanceof AdvancedPathNavigate)) {
            return false;
        }
        LivingEntity livingEntity = entityIn.getAttackTarget();
        if (livingEntity != null && !livingEntity.isAlive()) {
            this.resetTask(worldIn, entityIn, gameTimeIn);
            return false;
        }

        return livingEntity != null && livingEntity.isAlive() && !entityIn.isFlying() && !entityIn.isHovering();
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.delayCounter = 0;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        LivingEntity LivingEntity = entityIn.getAttackTarget();
        if (LivingEntity instanceof PlayerEntity && (LivingEntity.isSpectator() || ((PlayerEntity) LivingEntity).isCreative())) {
            entityIn.setAttackTarget(null);
        }
        entityIn.getNavigator().clearPath();
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        LivingEntity entity = owner.getAttackTarget();
        if(delayCounter > 0){
            delayCounter--;
        }
        if (entity != null) {
            if (owner.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
                this.resetTask(worldIn, owner, gameTime);
                return;
            }

            ((AdvancedPathNavigate) owner.getNavigator()).moveToLivingEntity(entity, speedTowardsTarget);

            final double d0 = owner.getDistanceSq(entity.getPosX(), entity.getBoundingBox().minY, entity.getPosZ());
            final double d1 = this.getAttackReachSqr(owner, entity);
            --this.delayCounter;
            if ((this.longMemory || owner.getEntitySenses().canSee(entity)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entity.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || owner.getRNG().nextFloat() < 0.05F)) {
                this.targetX = entity.getPosX();
                this.targetY = entity.getBoundingBox().minY;
                this.targetZ = entity.getPosZ();
                this.delayCounter = 4 + owner.getRNG().nextInt(7);

                if (this.canPenalize) {
                    this.delayCounter += failedPathFindingPenalty;
                    if (owner.getNavigator().getPath() != null) {
                        net.minecraft.pathfinding.PathPoint finalPathPoint = owner.getNavigator().getPath().getFinalPathPoint();
                        if (finalPathPoint != null && entity.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
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
                owner.swingArm(Hand.MAIN_HAND);
                owner.attackEntityAsMob(entity);
            }
        }
    }
    protected double getAttackReachSqr(EntityDragonBase dragon, LivingEntity attackTarget) {
        return dragon.getWidth() * 2.0F * dragon.getWidth() * 2.0F + attackTarget.getWidth();
    }

}
