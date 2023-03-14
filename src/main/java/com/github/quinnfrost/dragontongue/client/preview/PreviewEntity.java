package com.github.quinnfrost.dragontongue.client.preview;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.List;

public interface PreviewEntity<E extends Entity> {
    List<E> initializeEntities(Player var1, ItemStack var2);

    void simulateShot(E var1);
}
