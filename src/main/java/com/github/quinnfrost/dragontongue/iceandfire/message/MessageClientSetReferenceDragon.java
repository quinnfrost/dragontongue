package com.github.quinnfrost.dragontongue.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.gui.ScreenDragon;
import com.github.quinnfrost.dragontongue.message.RegistryMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class MessageClientSetReferenceDragon {
    private int entityID;
    private Collection<MobEffectInstance> effects;

    public MessageClientSetReferenceDragon() {
        this.entityID = -1;
        this.effects = new ArrayList<>();
    }

    public MessageClientSetReferenceDragon(EntityDragonBase dragon) {
        this.entityID = dragon.getId();
        this.effects = dragon.getActiveEffects();
    }

    public MessageClientSetReferenceDragon(int entityID, Collection<MobEffectInstance> effects) {
        this.entityID = entityID;
        this.effects = effects;
    }


    public static MessageClientSetReferenceDragon decoder(FriendlyByteBuf buffer) {
        int entityID = buffer.readInt();
        Collection<MobEffectInstance> effects = buffer.readCollection(ArrayList::new, friendlyByteBuf -> {
            return MobEffectInstance.load(friendlyByteBuf.readNbt());
        });
        return new MessageClientSetReferenceDragon(entityID, effects);
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);
        buffer.writeCollection(effects, (friendlyByteBuf, mobEffectInstance) -> {
            friendlyByteBuf.writeNbt(mobEffectInstance.save(new CompoundTag()));
        });

    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityID);
                if (entity instanceof EntityDragonBase) {
                    ScreenDragon.referencedDragon = (EntityDragonBase) entity;
                    Iterator<MobEffectInstance> iterator = ScreenDragon.referencedDragon.getActiveEffects().iterator();

                    boolean flag;
                    for(flag = false; iterator.hasNext(); flag = true) {
                        MobEffectInstance effect = iterator.next();
                        iterator.remove();
                    }
                    for (MobEffectInstance effect :
                            effects) {
                        ScreenDragon.referencedDragon.forceAddEffect(effect, null);
                    }
                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                if (contextSupplier.get().getSender() != null) {
                    RegistryMessages.sendToClient(new MessageClientSetReferenceDragon(ScreenDragon.referencedDragon), contextSupplier.get().getSender());
                }
            }
        });
        return true;
    }

}
