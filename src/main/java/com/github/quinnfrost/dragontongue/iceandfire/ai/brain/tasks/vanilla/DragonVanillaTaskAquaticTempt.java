package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public class DragonVanillaTaskAquaticTempt extends Behavior<EntityDragonBase> {
    private final double speed;
    private final Set<Item> temptItem;
    private final boolean scaredByPlayerMovement;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    private Player temptingPlayer;
    private int delayTemptCounter;
    private boolean isRunning;

    public DragonVanillaTaskAquaticTempt(double speedIn, Item temptItemIn, boolean scaredByPlayerMovementIn) {
        this(60, 60, speedIn, scaredByPlayerMovementIn, Sets.newHashSet(temptItemIn));
    }

    public DragonVanillaTaskAquaticTempt(int durationMinIn, int durationMaxIn, double speedIn, boolean scaredByPlayerMovementIn, Set<Item> temptItemIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
        this.speed = speedIn;
        this.temptItem = temptItemIn;
        this.scaredByPlayerMovement = scaredByPlayerMovementIn;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        } else {
            this.temptingPlayer = owner.level.getNearestPlayer(owner, 10.0D);

            if (this.temptingPlayer == null) {
                return false;
            } else {
                return this.isTempting(this.temptingPlayer.getMainHandItem()) || this.isTempting(this.temptingPlayer.getOffhandItem());
            }
        }
    }

    protected boolean isTempting(ItemStack stack) {
        return this.temptItem.contains(stack.getItem());
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (this.scaredByPlayerMovement) {
            if (entityIn.distanceToSqr(this.temptingPlayer) < 36.0D) {
                if (this.temptingPlayer.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs(this.temptingPlayer.xRot - this.pitch) > 5.0D
                        || Math.abs(this.temptingPlayer.yRot - this.yaw) > 5.0D) {
                    return false;
                }
            } else {
                this.targetX = this.temptingPlayer.getX();
                this.targetY = this.temptingPlayer.getY();
                this.targetZ = this.temptingPlayer.getZ();
            }

            this.pitch = this.temptingPlayer.xRot;
            this.yaw = this.temptingPlayer.yRot;
        }

        return this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.targetX = this.temptingPlayer.getX();
        this.targetY = this.temptingPlayer.getY();
        this.targetZ = this.temptingPlayer.getZ();
        this.isRunning = true;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.temptingPlayer = null;
        entityIn.getNavigation().stop();
        this.delayTemptCounter = 100;
        this.isRunning = false;
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        owner.getLookControl().setLookAt(this.temptingPlayer,
                owner.getMaxHeadYRot() + 20, owner.getMaxHeadXRot());

        if (owner.distanceToSqr(this.temptingPlayer) < 6.25D) {
            owner.getNavigation().stop();
        } else {
            owner.getNavigation().moveTo(this.temptingPlayer, this.speed);
        }
    }
}
