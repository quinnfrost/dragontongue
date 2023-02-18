package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

public class IafAdvancedDragonPathNavigator extends AdvancedPathNavigate {
    public IafAdvancedDragonPathNavigator(final MobEntity entity, final World world, MovementType type, float width, float height) {
        super(entity, world, type, width, height);
    }
}
