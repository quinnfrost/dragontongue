package com.github.quinnfrost.dragontongue.capability;

import com.github.quinnfrost.dragontongue.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class CapabilityInfoHolder {
    public static Capability<ICapabilityInfoHolder> TARGET_HOLDER = CapabilityManager.get(new CapabilityToken<ICapabilityInfoHolder>() {
        @Override
        public String toString() {
            return super.toString();
        }
    });

//    public static void register() {
//        CapabilityManager.INSTANCE.register(ICapabilityInfoHolder.class, new Storage(), CapabilityInfoHolderImpl::new);
//    }

    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            CapabilityProvider provider = new CapabilityProvider(event.getObject());
            event.addCapability(new ResourceLocation(References.MOD_ID, "extend_command_data"), provider);
            event.addListener(provider::invalidate);
        }
    }

}
