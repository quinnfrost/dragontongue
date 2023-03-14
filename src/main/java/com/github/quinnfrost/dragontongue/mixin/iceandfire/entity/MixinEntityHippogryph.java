package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHippogryph.class)
public abstract class MixinEntityHippogryph extends TamableAnimal {
    protected MixinEntityHippogryph(EntityType<? extends TamableAnimal> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(
            method = "aiStep",
            at = @At(value = "HEAD")
    )
    public void inject$livingTick(CallbackInfo ci) {
        EntityHippogryph hippogryph = (EntityHippogryph) (Object) this;
        ICapabilityInfoHolder cap = this.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(this));
        if (cap.getObjectSetting(EnumCommandSettingType.ATTACK_DECISION_TYPE) == EnumCommandSettingType.AttackDecisionType.ALWAYS_HELP) {
            if (hippogryph.isTame() && hippogryph.getCommand() == 1) {
                LivingEntity owner = hippogryph.getOwner();
                if (owner != null
                        && owner.getLastHurtMobTimestamp() + 1 == owner.tickCount
                        && hippogryph.getTarget() == null
                        && util.shouldAttack(hippogryph, owner.getLastHurtMob(), hippogryph.getAttributeValue(Attributes.FOLLOW_RANGE))
                ) {
                    cap.setDestination(hippogryph.blockPosition());
                    hippogryph.setCommand(0);
                    hippogryph.setOrderedToSit(false);
                    hippogryph.setTarget(owner.getLastHurtMob());
                    cap.setCommandStatus(EnumCommandSettingType.CommandStatus.ATTACK);
                }
            }
        }
    }
}
