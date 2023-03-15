package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.*;

import net.minecraft.world.level.Level;

public class IafAdvancedDragonPathNavigator extends AdvancedPathNavigate {
    private final EntityDragonBase dragon;

    public IafAdvancedDragonPathNavigator(EntityDragonBase dragon, Level world, MovementType valueOf, float width, float height) {
        super(dragon, world, valueOf, width, height);
        this.dragon = dragon;
    }

}
