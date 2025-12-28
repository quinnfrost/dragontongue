package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonBehaviorHelper;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class RegistryAI {
    public static void registerAI(Mob mobEntity) {
        try {
            if (!DragonTongue.isIafPresent ||
                    (!IafDragonBehaviorHelper.registerDragonAI(mobEntity)
                    && !IafDragonBehaviorHelper.registerHippogryphAI(mobEntity))
            ) {
                if (mobEntity instanceof Wolf || mobEntity instanceof Cat) {
                    mobEntity.goalSelector.addGoal(5, new FollowCommandAndAttackGoal((TamableAnimal) mobEntity, 1.0D, true));
                } else {
                    mobEntity.goalSelector.addGoal(0, new FollowCommandGoal(mobEntity));
                }

                if (mobEntity instanceof TamableAnimal) {
                    TamableAnimal tameableEntity = (TamableAnimal) mobEntity;
                    tameableEntity.targetSelector.addGoal(3, new GuardGoal<>(tameableEntity, LivingEntity.class, false, new Predicate<LivingEntity>() {
                        @Override
                        public boolean test(@Nullable LivingEntity entity) {
                            return (!(entity instanceof Player) || !((Player) entity).isCreative() || !entity.isSpectator())
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
