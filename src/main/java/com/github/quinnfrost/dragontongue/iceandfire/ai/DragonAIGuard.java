package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class DragonAIGuard <T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    public DragonAIGuard(EntityDragonBase entityIn, Class<T> targetClassIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, targetChanceIn, checkSight, nearbyOnlyIn, targetPredicate);
        this.setMutexFlags(EnumSet.of(Flag.TARGET));
        this.dragon = entityIn;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
    }

    public DragonAIGuard(EntityDragonBase entityIn, Class<T> targetClassIn, boolean checkSight, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(entityIn, targetClassIn, 1, checkSight, false, null);
        this.setMutexFlags(EnumSet.of(Flag.TARGET));
        this.dragon = entityIn;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
    }

    @Override
    public boolean shouldExecute() {
        if (!dragon.isTamed() || dragon.getOwner() == null || dragon.getControllingPassenger() != null) {
            return false;
        }
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) != EnumCommandSettingType.AttackDecisionType.GUARD) {
            return false;
        }
        if (super.shouldExecute() && !nearestTarget.getClass().equals(this.dragon.getClass())) {

            final float dragonSize = Math.max(this.dragon.getWidth(), this.dragon.getWidth() * dragon.getRenderSize());
            if (dragonSize >= nearestTarget.getWidth()) {
                if (!dragon.isOwner(nearestTarget) && util.isHostile(nearestTarget)) {

                    if (nearestTarget instanceof EntityDragonBase) {
                        EntityDragonBase dragon = (EntityDragonBase) nearestTarget;
                        if (dragon.getOwner() == null) {
                            // Only attack wild dragons
                            return !dragon.isModelDead();
                        }
                    }

                    return DragonUtils.canTameDragonAttack(dragon, nearestTarget);
                }
                if (nearestTarget instanceof PlayerEntity && dragon.isTamed()) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.dragon.getBoundingBox().grow(targetDistance, targetDistance, targetDistance);
    }

    @Override
    protected double getTargetDistance() {
        ModifiableAttributeInstance iattributeinstance = this.goalOwner.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 128.0D : iattributeinstance.getValue();
    }

    public static double getTargetDistance(MobEntity mobEntity) {
        ModifiableAttributeInstance iattributeinstance = mobEntity.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 128.0D : iattributeinstance.getValue();
    }
    public static AxisAlignedBB getTargetableArea(MobEntity mobEntity, double targetDistance) {
        return mobEntity.getBoundingBox().grow(targetDistance, targetDistance, targetDistance);
    }
    public static LivingEntity findNearestTarget(MobEntity mobEntity) {
        LivingEntity nearestTarget;
        Predicate<LivingEntity> targetPredicate = new Predicate<LivingEntity>() {
            @Override
            public boolean test(@Nullable LivingEntity entity) {
                return (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative())
                        && DragonUtils.canHostilesTarget(entity)
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
