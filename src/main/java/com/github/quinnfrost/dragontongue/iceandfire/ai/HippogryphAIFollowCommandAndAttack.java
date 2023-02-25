package com.github.quinnfrost.dragontongue.iceandfire.ai;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class HippogryphAIFollowCommandAndAttack extends MeleeAttackGoal {
    EntityHippogryph hippogryph;
    ICapabilityInfoHolder capabilityInfoHolder;
    public HippogryphAIFollowCommandAndAttack(EntityHippogryph creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
        this.hippogryph = creature;
        this.capabilityInfoHolder = creature.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(creature));
    }

    // Todo: cleanup what's happening below
    @Override
    public boolean shouldExecute() {
        if (this.attacker.getAttackTarget() != null || capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            return true;
        }
        if (super.shouldExecute()) {
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
    public boolean shouldContinueExecuting() {
        if (this.attacker.getAttackTarget() != null || capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE) {
            return true;
        }
        if (super.shouldContinueExecuting()) {
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
    public void startExecuting() {
        if (hippogryph.getAttackTarget() != null) {
            super.startExecuting();
        }
    }

    @Override
    public void tick() {
        if (capabilityInfoHolder.getCommandStatus() != EnumCommandSettingType.CommandStatus.NONE
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
            hippogryph.getNavigator().tryMoveToEntityLiving(hippogryph.getAttackTarget(), 1.1f);
            super.tick();
        }
    }

}
