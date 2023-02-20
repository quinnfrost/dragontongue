package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class GuardGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private TameableEntity tameableEntity;
    private ICapabilityInfoHolder cap;
    private BlockPos guardPosition;
    private double guardDistance = 16;


    public GuardGoal(TameableEntity entityIn, Class<T> targetClassIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, targetChanceIn, checkSight, nearbyOnlyIn, targetPredicate);
        this.setMutexFlags(EnumSet.of(Flag.TARGET));
        this.tameableEntity = entityIn;
        this.cap = tameableEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tameableEntity));
    }

    public GuardGoal(TameableEntity entityIn, Class<T> targetClassIn, boolean checkSight, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, 1, checkSight, false, null);
        this.setMutexFlags(EnumSet.of(Flag.TARGET));
        this.tameableEntity = entityIn;
        this.cap = tameableEntity.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tameableEntity));
    }

    @Override
    public boolean shouldExecute() {
        if (!tameableEntity.isTamed() || tameableEntity.getOwner() == null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }
        updateGuardPosition();
        if (super.shouldExecute() && !nearestTarget.getClass().equals(this.tameableEntity.getClass())) {
            if (tameableEntity.getPositionVec().distanceTo(Vector3d.copyCenteredHorizontally(guardPosition)) > guardDistance) {
                return false;
            }
            return util.isHostile(nearestTarget);
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (!tameableEntity.isTamed() || tameableEntity.getOwner() == null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }

        updateGuardPosition();
        if (tameableEntity.getPositionVec().distanceTo(Vector3d.copyCenteredHorizontally(guardPosition)) > guardDistance) {
            // Waiting for current target eliminated
            if (tameableEntity.getAttackTarget() == null) {
                tameableEntity.getNavigator().tryMoveToXYZ(guardPosition.getX(), guardPosition.getY(), guardPosition.getZ(), 1.0f);
            }
            return true;
        } else {
            return super.shouldContinueExecuting();
        }
    }

    private void updateGuardPosition() {
        guardDistance = Math.min(tameableEntity.getOwner().getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(tameableEntity.getOwner())).getSelectDistance(), tameableEntity.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (cap.getDestination().isPresent() && cap.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            guardPosition = cap.getDestination().get();
        } else {
            guardPosition = tameableEntity.getOwner().getPosition();
        }
    }

    @Override
    public void resetTask() {
        super.resetTask();
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

    public static double getTargetDistance(MobEntity mobEntity) {
        return mobEntity.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    public static AxisAlignedBB getTargetableArea(MobEntity mobEntity, double targetDistance) {
        return mobEntity.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    public static LivingEntity findNearestTarget(MobEntity mobEntity) {
        LivingEntity nearestTarget;
        Predicate<LivingEntity> targetPredicate = new Predicate<LivingEntity>() {
            @Override
            public boolean test(@Nullable LivingEntity entity) {
                return (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative())
                        && util.isHostile(entity);
            }
        };
        Class entityClazz = LivingEntity.class;
        EntityPredicate targetEntitySelector = (new EntityPredicate()).setDistance(getTargetDistance(mobEntity)).setCustomPredicate(targetPredicate);
        if (entityClazz != PlayerEntity.class && entityClazz != ServerPlayerEntity.class) {
            nearestTarget = mobEntity.world.getClosestEntity(LivingEntity.class, targetEntitySelector, mobEntity, mobEntity.getPosX(), mobEntity.getPosYEye(), mobEntity.getPosZ(), getTargetableArea(mobEntity, getTargetDistance(mobEntity)));
        } else {
            nearestTarget = mobEntity.world.getClosestPlayer(targetEntitySelector, mobEntity, mobEntity.getPosX(), mobEntity.getPosYEye(), mobEntity.getPosZ());
        }
        return nearestTarget;
    }

}
