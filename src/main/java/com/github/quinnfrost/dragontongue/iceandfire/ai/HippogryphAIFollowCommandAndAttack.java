package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.entity.ai.FollowCommandAndAttackGoal;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class HippogryphAIFollowCommandAndAttack extends MeleeAttackGoal {
    EntityHippogryph hippogryph;
    ICapTargetHolder capabilityInfoHolder;
    public HippogryphAIFollowCommandAndAttack(EntityHippogryph creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
        this.hippogryph = creature;
        this.capabilityInfoHolder = creature.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(creature));
    }

    // Todo: cleanup what's happening below
    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
            return true;
        }
        if (capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD
                || capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public boolean shouldContinueExecuting() {
        if (super.shouldContinueExecuting()) {
            return true;
        }
        if (capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD
                || capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public void startExecuting() {
        if (hippogryph.getAttackTarget() != null) {
            super.startExecuting();
        }
    }

    @Override
    public void tick() {
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandStatus.NONE
                && hippogryph.getAttackTarget() == null) {

            capabilityInfoHolder.getDestination().ifPresent(blockPos -> {
                if (hippogryph.world.isAirBlock(blockPos) && hippogryph.world.isAirBlock(blockPos.down())) {
                    hippogryph.setFlying(true);
                    hippogryph.getMoveHelper().setMoveTo(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            1.1D
                    );
                } else {
                    if (!hippogryph.world.isAirBlock(hippogryph.getPosition()) || !hippogryph.world.isAirBlock(hippogryph.getPosition().down())) {
                        hippogryph.setFlying(false);
                    }
                    hippogryph.getNavigator().tryMoveToXYZ(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            1.1D
                    );
                }

            });
        }
        if (hippogryph.getAttackTarget() != null) {
            super.tick();
        }
    }

}
