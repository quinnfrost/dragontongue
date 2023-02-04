package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolder;
import com.github.quinnfrost.dragontongue.capability.CapTargetHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapTargetHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.enums.EnumCommandStatus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

public class IafAdvancedDragonFlightManager extends IafDragonFlightManager {
    private EntityDragonBase dragon;
    private ICapTargetHolder cap;
    private Vector3d actualTarget;

    public IafAdvancedDragonFlightManager(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
        this.cap = dragon.getCapability(CapTargetHolder.TARGET_HOLDER).orElse(new CapTargetHolderImpl(dragon));
    }

    public static boolean applyDragonFlightManager(LivingEntity dragonIn) {
        if (!IafHelperClass.isDragon(dragonIn)) {
            return false;
        }
        EntityDragonBase dragon = (EntityDragonBase) dragonIn;
        dragon.flightManager = new IafAdvancedDragonFlightManager(dragon);
        return true;
    }

    @Override
    public void update() {
        Vector3d flightTarget = dragon.flightManager.getFlightTarget();

        super.update();

        // In DragonUtils#getBlockInView, a random position is returned if dragon is airborne and nowhere to go
//        if (flightTarget != null && IafDragonBehaviorHelper.isDragonInAir(dragon)) {
//            switch (cap.getCommandStatus()) {
//                case REACH:
//                case STAY:
//                case HOVER:
//                    dragon.flightManager.setFlightTarget(flightTarget);
//                    break;
//            }
//        }
        // In IafDragonFlightManger#91, if flight target is on ground, a random position is used instead
        // This is solved by never set flight target to none air block

    }

}
