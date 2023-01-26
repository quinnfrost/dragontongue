package com.github.quinnfrost.dragontongue.entity.ai;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.iceandfire.IafHelperClass;
import net.minecraft.entity.MobEntity;

public class RegistryAI {
    public static void registerAI(MobEntity mobEntity) {
        try {
            if (IafHelperClass.isDragon(mobEntity)) {
                IafHelperClass.registerDragonAI(mobEntity);
            } else {
                mobEntity.goalSelector.addGoal(0, new FollowCommandGoal(mobEntity));
            }
        } catch (Exception e) {
            DragonTongue.LOGGER.error("Cannot register AI");
            e.printStackTrace();
        }
    }

}
