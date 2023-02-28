package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public abstract class TaskTarget extends Task<TameableEntity> {
    protected LivingEntity target;
    private int timestamp;
    protected final boolean shouldCheckSight;
    private final boolean nearbyOnly;
    private int targetSearchStatus;
    private int targetSearchDelay;
    private int targetUnseenTicks;
    protected int unseenMemoryTicks = 60;
    public TaskTarget(int durationMinIn, int durationMaxIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
        this.shouldCheckSight = checkSight;
        this.nearbyOnly = nearbyOnlyIn;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, TameableEntity entityIn, long gameTimeIn) {
        LivingEntity livingentity = entityIn.getAttackTarget();
        if (livingentity == null) {
            livingentity = this.target;
        }

        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else {
            Team team = entityIn.getTeam();
            Team team1 = livingentity.getTeam();
            if (team != null && team1 == team) {
                return false;
            } else {
                double d0 = this.getTargetDistance(entityIn);
                if (entityIn.getDistanceSq(livingentity) > d0 * d0) {
                    return false;
                } else {
                    if (this.shouldCheckSight) {
                        if (entityIn.getEntitySenses().canSee(livingentity)) {
                            this.targetUnseenTicks = 0;
                        } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {
                            return false;
                        }
                    }

                    if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).abilities.disableDamage) {
                        return false;
                    } else {
                        entityIn.setAttackTarget(livingentity);
                        return true;
                    }
                }
            }
        }
    }
    protected double getTargetDistance(TameableEntity entityIn) {
        return entityIn.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, TameableEntity entityIn, long gameTimeIn) {
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.targetUnseenTicks = 0;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, TameableEntity entityIn, long gameTimeIn) {
        entityIn.setAttackTarget((LivingEntity)null);
        this.target = null;
    }

    protected boolean isSuitableTarget(TameableEntity entityIn, @Nullable LivingEntity potentialTarget, EntityPredicate targetPredicate) {
        if (potentialTarget == null) {
            return false;
        } else if (!targetPredicate.canTarget(entityIn, potentialTarget)) {
            return false;
        } else if (!entityIn.isWithinHomeDistanceFromPosition(potentialTarget.getPosition())) {
            return false;
        } else {
            if (this.nearbyOnly) {
                if (--this.targetSearchDelay <= 0) {
                    this.targetSearchStatus = 0;
                }

                if (this.targetSearchStatus == 0) {
                    this.targetSearchStatus = this.canEasilyReach(entityIn, potentialTarget) ? 1 : 2;
                }

                if (this.targetSearchStatus == 2) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean canEasilyReach(TameableEntity entityIn, LivingEntity target) {
        this.targetSearchDelay = 10 + entityIn.getRNG().nextInt(5);
        Path path = entityIn.getNavigator().pathfind(target, 0);
        if (path == null) {
            return false;
        } else {
            PathPoint pathpoint = path.getFinalPathPoint();
            if (pathpoint == null) {
                return false;
            } else {
                int i = pathpoint.x - MathHelper.floor(target.getPosX());
                int j = pathpoint.z - MathHelper.floor(target.getPosZ());
                return (double)(i * i + j * j) <= 2.25D;
            }
        }
    }

    public void setUnseenMemoryTicks(int unseenMemoryTicksIn) {
        this.unseenMemoryTicks = unseenMemoryTicksIn;
    }
}
