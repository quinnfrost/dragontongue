package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHippogryph.class)
public abstract class MixinEntityHippogryph extends TameableEntity {
    protected MixinEntityHippogryph(EntityType<? extends TameableEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(
            method = "Lcom/github/alexthe666/iceandfire/entity/EntityHippogryph;livingTick()V",
            at = @At(value = "HEAD")
    )
    public void inject$livingTick(CallbackInfo ci) {
        EntityHippogryph hippogryph = (EntityHippogryph) (Object) this;
        ICapabilityInfoHolder cap = this.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(this));
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.ALWAYS_HELP) {
            if (hippogryph.isTamed() && hippogryph.getCommand() == 1) {
                LivingEntity owner = hippogryph.getOwner();
                if (owner != null
                        && owner.getLastAttackedEntityTime() + 1 == owner.ticksExisted
                        && hippogryph.getAttackTarget() == null
                        && util.shouldAttack(hippogryph, owner.getLastAttackedEntity(), hippogryph.getAttributeValue(Attributes.FOLLOW_RANGE))
                ) {
                    cap.setDestination(hippogryph.getPosition());
                    hippogryph.setCommand(0);
                    hippogryph.setSitting(false);
                    hippogryph.setAttackTarget(owner.getLastAttackedEntity());
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
                }
            }
        }
    }
}
