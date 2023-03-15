package com.github.quinnfrost.dragontongue.iceandfire.ai.brain;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.server.level.ServerLevel;

public class DragonCoreTaskTarget extends Behavior<EntityDragonBase> {
    private static final TargetingConditions TARGET_ENTITY_SELECTOR = TargetingConditions.DEFAULT.ignoreLineOfSight().ignoreInvisibilityTesting();
    LivingEntity attacker;
    private long timestamp;
    private int revengeTimerOld;

    public DragonCoreTaskTarget(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    public DragonCoreTaskTarget() {
        this(60, 60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase dragon) {
        if (dragon.isTame() && !dragon.isOrderedToSit()) {
            LivingEntity owner = dragon.getOwner();

            if (owner != null) {
                // Owner hurt by target
                attacker = owner.getLastHurtByMob();
                if (attacker != null
                        && owner.getLastHurtByMobTimestamp() != this.timestamp
                        && dragon.wantsToAttack(attacker, owner)) {
                    return true;
                }
                // Owner hurt target
                attacker = owner.getLastHurtMob();
                if (attacker != null
                        && owner.getLastHurtMobTimestamp() != this.timestamp
                        && dragon.wantsToAttack(attacker, owner)) {
                    return true;
                }
            }
        }
        // Revenge
        attacker = dragon.getLastHurtByMob();
        if (dragon.getLastHurtByMobTimestamp() != this.revengeTimerOld
                && attacker != null
                && TARGET_ENTITY_SELECTOR.test(dragon, attacker)) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return false;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        super.stop(worldIn, entityIn, gameTimeIn);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase dragon, long gameTimeIn) {
        dragon.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);

        this.timestamp = gameTimeIn;

//        dragon.getBrain().setFallbackActivity(RegistryBrains.ACTIVITY_ATTACK);
//        dragon.getBrain().switchTo(RegistryBrains.ACTIVITY_ATTACK);
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase dragon, long gameTime) {

    }
}
