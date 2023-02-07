package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class RegistryAI {
    public static void registerAI(MobEntity mobEntity) {
        try {
            if (!DragonTongue.isIafPresent ||
                    (!IafDragonBehaviorHelper.registerDragonAI(mobEntity)
                    && !IafDragonBehaviorHelper.registerHippogryphAI(mobEntity))
            ) {
                if (mobEntity instanceof WolfEntity || mobEntity instanceof CatEntity) {
                    mobEntity.goalSelector.addGoal(5, new FollowCommandAndAttackGoal((TameableEntity) mobEntity, 1.0D, true));
                } else {
                    mobEntity.goalSelector.addGoal(0, new FollowCommandGoal(mobEntity));
                }

                if (mobEntity instanceof TameableEntity) {
                    TameableEntity tameableEntity = (TameableEntity) mobEntity;
                    tameableEntity.targetSelector.addGoal(3, new GuardGoal<>(tameableEntity, LivingEntity.class, false, new Predicate<LivingEntity>() {
                        @Override
                        public boolean test(@Nullable LivingEntity entity) {
                            return (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative() || !entity.isSpectator())
                                    && util.isHostile(entity);
                        }
                    }));
                }
            }
        } catch (Exception e) {
            DragonTongue.LOGGER.error("Cannot register AI");
            e.printStackTrace();
        }
    }

}
