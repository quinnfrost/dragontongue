package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.IFlyingMount;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public class DragonVanillaTaskRide<T extends Mob & IFlyingMount> extends Behavior<EntityDragonBase> {
    private Player player;
    public DragonVanillaTaskRide(int durationMinIn, int durationMaxIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        player = owner.getRidingPlayer();

        return player != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        entityIn.getNavigation().stop();
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        owner.getNavigation().stop();
        owner.setTarget(null);
        double x = owner.getX();
        double y = owner.getY();
        double z = owner.getZ();
        double speed = 1.8F * owner.getFlightSpeedModifier();
        Vec3 lookVec = player.getLookAngle();
        if (player.zza < 0) {
            lookVec = lookVec.yRot((float) Math.PI);
        } else if (player.xxa > 0) {
            lookVec = lookVec.yRot((float) Math.PI * 0.5f);
        } else if (player.xxa < 0) {
            lookVec = lookVec.yRot((float) Math.PI * -0.5f);
        }
        if (Math.abs(player.xxa) > 0.0) {
            speed *= 0.25D;
        }
        if (player.zza < 0.0) {
            speed *= 0.15D;
        }
        if (owner.isGoingUp()) {
            lookVec = lookVec.add(0, 1, 0);
        } else if (owner.isGoingDown()) {
            lookVec = lookVec.add(0, -1, 0);
        }
        if (player.xxa != 0 || player.zza != 0 || (owner.fliesLikeElytra())) {
            x += lookVec.x * 10;
            z += lookVec.z * 10;
        }
        if ((owner.isFlying() || hovering(owner)) && (owner.fliesLikeElytra() || owner.isGoingUp() || owner.isGoingDown())) {
            y += lookVec.y * owner.getYSpeedMod();
        }
        if (owner.fliesLikeElytra() && lookVec.y == -1 || !(owner.isFlying() || hovering(owner)) && !owner.isOnGround()) {
            y -= 1;
        }
        owner.getMoveControl().setWantedPosition(x, y, z, speed);

    }
    private boolean hovering(EntityDragonBase entityIn) {
        return entityIn.isHovering() || entityIn instanceof EntityDragonBase && ((EntityDragonBase) entityIn).useFlyingPathFinder();
    }
}
