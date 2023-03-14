package com.github.quinnfrost.dragontongue.entity.ai;

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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class GuardGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private TamableAnimal tameableEntity;
    private ICapabilityInfoHolder cap;
    private BlockPos guardPosition;
    private double guardDistance = 16;


    public GuardGoal(TamableAnimal entityIn, Class<T> targetClassIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, targetChanceIn, checkSight, nearbyOnlyIn, targetPredicate);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.tameableEntity = entityIn;
        this.cap = tameableEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tameableEntity));
    }

    public GuardGoal(TamableAnimal entityIn, Class<T> targetClassIn, boolean checkSight, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, 1, checkSight, false, null);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.tameableEntity = entityIn;
        this.cap = tameableEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tameableEntity));
    }

    @Override
    public boolean canUse() {
        if (!tameableEntity.isTame() || tameableEntity.getOwner() == null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }
        updateGuardPosition();
        if (super.canUse() && !target.getClass().equals(this.tameableEntity.getClass())) {
            if (tameableEntity.position().distanceTo(Vec3.atBottomCenterOf(guardPosition)) > guardDistance) {
                return false;
            }
            return util.isHostile(target);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!tameableEntity.isTame() || tameableEntity.getOwner() == null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }

        updateGuardPosition();
        if (tameableEntity.position().distanceTo(Vec3.atBottomCenterOf(guardPosition)) > guardDistance) {
            // Waiting for current target eliminated
            if (tameableEntity.getTarget() == null) {
                tameableEntity.getNavigation().moveTo(guardPosition.getX(), guardPosition.getY(), guardPosition.getZ(), 1.0f);
            }
            return true;
        } else {
            return super.canContinueToUse();
        }
    }

    private void updateGuardPosition() {
        guardDistance = Math.min(tameableEntity.getOwner().getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tameableEntity.getOwner())).getSelectDistance(), tameableEntity.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (cap.getDestination().isPresent() && cap.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            guardPosition = cap.getDestination().get();
        } else {
            guardPosition = tameableEntity.getOwner().blockPosition();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (cap.getDestination().isPresent()) {
            cap.setCommandStatus(EnumCommandSettingType.CommandStatus.REACH);
        } else {
            cap.setCommandStatus(EnumCommandSettingType.CommandStatus.NONE);
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static double getTargetDistance(Mob mobEntity) {
        return mobEntity.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    public static AABB getTargetableArea(Mob mobEntity, double targetDistance) {
        return mobEntity.getBoundingBox().inflate(targetDistance, 4.0D, targetDistance);
    }

    public static LivingEntity findNearestTarget(Mob mobEntity) {
        LivingEntity nearestTarget;
        Predicate<LivingEntity> targetPredicate = new Predicate<LivingEntity>() {
            @Override
            public boolean test(@Nullable LivingEntity entity) {
                return (!(entity instanceof Player) || !((Player) entity).isCreative())
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
