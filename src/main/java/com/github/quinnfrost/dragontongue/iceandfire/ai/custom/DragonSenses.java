package com.github.quinnfrost.dragontongue.iceandfire.ai.custom;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.EntitySenses;

public class DragonSenses extends EntitySenses {
    EntityDragonBase dragon;
    public DragonSenses(EntityDragonBase dragonIn) {
        super(dragonIn);
        this.dragon = dragonIn;
    }
}
