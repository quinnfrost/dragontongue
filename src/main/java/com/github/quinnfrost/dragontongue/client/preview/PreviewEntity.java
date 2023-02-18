package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;


import java.util.List;

public interface PreviewEntity<E extends Entity> {
    List<E> initializeEntities(PlayerEntity var1, ItemStack var2);

    void simulateShot(E var1);
}
