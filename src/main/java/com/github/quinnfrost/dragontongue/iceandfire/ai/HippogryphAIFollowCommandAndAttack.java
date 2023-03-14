package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class HippogryphAIFollowCommandAndAttack extends MeleeAttackGoal {
    EntityHippogryph hippogryph;
    ICapabilityInfoHolder capabilityInfoHolder;
    public HippogryphAIFollowCommandAndAttack(EntityHippogryph creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
        this.hippogryph = creature;
        this.capabilityInfoHolder = creature.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(creature));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null || capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            return true;
        }
        if (super.canUse()) {
            return true;
        }
        if (capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD
                || capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public boolean canContinueToUse() {
        if (this.mob.getTarget() != null || capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            return true;
        }
        if (super.canContinueToUse()) {
            return true;
        }
        if (capabilityInfoHolder.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.GUARD
                || capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE)
        {
            return true;
        }
        return false;
    }
    @Override
    public void start() {
        if (hippogryph.getTarget() != null) {
            super.start();
        }
    }

    @Override
    public void tick() {
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
                && hippogryph.getTarget() == null) {

            capabilityInfoHolder.getDestination().ifPresent(blockPos -> {
                if (hippogryph.level.isEmptyBlock(blockPos) && hippogryph.level.isEmptyBlock(blockPos.below())) {
                    hippogryph.setFlying(true);
                    hippogryph.getMoveControl().setWantedPosition(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            1.1D
                    );
                } else {
                    if (!hippogryph.level.isEmptyBlock(hippogryph.blockPosition()) || !hippogryph.level.isEmptyBlock(hippogryph.blockPosition().below())) {
                        hippogryph.setFlying(false);
                    }
                    hippogryph.getNavigation().moveTo(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            1.1D
                    );
                }

            });
        }
        if (hippogryph.getTarget() != null) {
            hippogryph.getNavigation().moveTo(hippogryph.getTarget(), 1.1f);
            super.tick();
        }
    }

}
