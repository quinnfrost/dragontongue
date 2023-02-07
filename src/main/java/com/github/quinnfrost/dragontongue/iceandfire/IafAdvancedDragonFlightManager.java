package com.github.quinnfrost.dragontongue.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

public class IafAdvancedDragonFlightManager extends IafDragonFlightManager {
    private EntityDragonBase dragon;
    private ICapabilityInfoHolder cap;
    private Vector3d actualTarget;

    public IafAdvancedDragonFlightManager(EntityDragonBase dragon) {
        super(dragon);
        this.dragon = dragon;
        this.cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(dragon));
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
