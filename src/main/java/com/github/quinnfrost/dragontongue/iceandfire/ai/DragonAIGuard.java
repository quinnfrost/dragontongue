package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class DragonAIGuard<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    private BlockPos guardPosition;
    private double guardDistance = 16;
    private boolean isTooFar = false;

    public DragonAIGuard(EntityDragonBase entityIn, Class<T> targetClassIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, targetChanceIn, checkSight, nearbyOnlyIn, targetPredicate);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.dragon = entityIn;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
    }

    public DragonAIGuard(EntityDragonBase entityIn, Class<T> targetClassIn, boolean checkSight, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, 1, checkSight, false, null);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.dragon = entityIn;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
    }

    @Override
    public boolean canUse() {
        if (!dragon.isTame() || dragon.getOwner() == null || dragon.getControllingPassenger() != null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }
        if (super.canUse() && !target.getClass().equals(this.dragon.getClass())) {
            updateGuardPosition();
            if (target.position().distanceTo(Vec3.atBottomCenterOf(guardPosition)) > guardDistance) {
                return false;
            }
            final float dragonSize = Math.max(this.dragon.getBbWidth(), this.dragon.getBbWidth() * dragon.getRenderSize());
            if (dragonSize >= target.getBbWidth()) {
                if (!dragon.isOwnedBy(target) && util.isHostile(target)) {

                    if (target instanceof EntityDragonBase) {
                        EntityDragonBase dragon = (EntityDragonBase) target;
                        if (dragon.getOwner() == null) {
                            // Only attack wild dragons
                            return !dragon.isModelDead();
                        }
                    }

                    return DragonUtils.canTameDragonAttack(dragon, target);
                }
                if (target instanceof Player && dragon.isTame()) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!dragon.isTame() || dragon.getOwner() == null || dragon.getControllingPassenger() != null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }
        updateGuardPosition();
//        guardDistance = Math.min(owner.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(owner)).getCommandDistance(), dragon.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (dragon.position().distanceTo(Vec3.atBottomCenterOf(guardPosition)) > guardDistance || isTooFar) {
            if (!isTooFar) {
                dragon.setTarget(null);
                isTooFar = true;
            }
            // Waiting for current target eliminated
            if (dragon.getTarget() == null) {
                // Escort itself will do
                if (dragon.getCommand() != 2) {
                    IafDragonBehaviorHelper.setDragonWalkTarget(dragon, new BlockPos(guardPosition.getX(), guardPosition.getY(), guardPosition.getZ()));
                    IafDragonBehaviorHelper.setDragonFlightTarget(dragon, Vec3.atBottomCenterOf(guardPosition));
                }
            }
            if (dragon.position().distanceTo(Vec3.atBottomCenterOf(guardPosition)) < guardDistance / 2.0f) {
                isTooFar = false;
            }
            return true;
        } else {
            return super.canContinueToUse();
        }
    }

    private void updateGuardPosition() {
        if (dragon.getOwner() == null) {
            return;
        }
        guardDistance = dragon.getOwner().getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon.getOwner())).getSelectDistance();
        guardPosition = cap.getDestination().orElse(dragon.blockPosition());
        if (dragon.getCommand() == 2) {
            if (IafDragonBehaviorHelper.isDragonInAir(dragon) && dragon.flightManager.getFlightTarget() != null) {
                Vec3 flightTarget = dragon.flightManager.getFlightTarget();
                guardPosition = new BlockPos(dragon.getOwner().getX(), flightTarget.y, dragon.getOwner().getZ());
            } else {
                guardPosition = dragon.getOwner().blockPosition();
            }
        } else {
            cap.getDestination().ifPresent(blockPos -> {
                guardPosition = blockPos;
            });
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void start() {
        super.start();
        guardPosition = cap.getDestination().orElse(dragon.blockPosition());
    }

    @Override
    protected AABB getTargetSearchArea(double targetDistance) {
        return this.dragon.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
    }

    @Override
    protected double getFollowDistance() {
        AttributeInstance iattributeinstance = this.mob.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 128.0D : iattributeinstance.getValue();
    }

    public static double getTargetDistance(Mob mobEntity) {
        AttributeInstance iattributeinstance = mobEntity.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 128.0D : iattributeinstance.getValue();
    }

    public static AABB getTargetableArea(Mob mobEntity, double targetDistance) {
        return mobEntity.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
    }

    public static LivingEntity findNearestTarget(Mob mobEntity) {
        LivingEntity nearestTarget;
        Predicate<LivingEntity> targetPredicate = new Predicate<LivingEntity>() {
            @Override
            public boolean test(@Nullable LivingEntity entity) {
                return (!(entity instanceof Player) || !((Player) entity).isCreative())
                        && DragonUtils.canHostilesTarget(entity)
                        && util.isHostile(entity);
            }
        };
        Class entityClazz = LivingEntity.class;
        TargetingConditions targetEntitySelector = (new TargetingConditions()).range(getTargetDistance(mobEntity)).selector(targetPredicate);
        if (entityClazz != Player.class && entityClazz != ServerPlayer.class) {
            nearestTarget = mobEntity.level.getNearestLoadedEntity(LivingEntity.class, targetEntitySelector, mobEntity, mobEntity.getX(), mobEntity.getEyeY(), mobEntity.getZ(), getTargetableArea(mobEntity, getTargetDistance(mobEntity)));
        } else {
            nearestTarget = mobEntity.level.getNearestPlayer(targetEntitySelector, mobEntity, mobEntity.getX(), mobEntity.getEyeY(), mobEntity.getZ());
        }
        return nearestTarget;
    }

}
