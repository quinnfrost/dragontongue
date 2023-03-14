package com.github.quinnfrost.dragontongue.iceandfire.ai.custom;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.entity.ai.sensing.Sensing;

public class DragonSenses extends Sensing {
    EntityDragonBase dragon;
    public DragonSenses(EntityDragonBase dragonIn) {
        super(dragonIn);
        this.dragon = dragonIn;
    }
}
