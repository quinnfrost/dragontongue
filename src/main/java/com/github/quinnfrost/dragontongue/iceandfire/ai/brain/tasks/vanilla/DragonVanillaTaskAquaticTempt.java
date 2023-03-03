package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class DragonVanillaTaskAquaticTempt extends Task<EntityDragonBase> {
    private final double speed;
    private final Set<Item> temptItem;
    private final boolean scaredByPlayerMovement;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    private PlayerEntity temptingPlayer;
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
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        } else {
            this.temptingPlayer = owner.world.getClosestPlayer(owner, 10.0D);

            if (this.temptingPlayer == null) {
                return false;
            } else {
                return this.isTempting(this.temptingPlayer.getHeldItemMainhand()) || this.isTempting(this.temptingPlayer.getHeldItemOffhand());
            }
        }
    }

    protected boolean isTempting(ItemStack stack) {
        return this.temptItem.contains(stack.getItem());
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        if (this.scaredByPlayerMovement) {
            if (entityIn.getDistanceSq(this.temptingPlayer) < 36.0D) {
                if (this.temptingPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs(this.temptingPlayer.rotationPitch - this.pitch) > 5.0D
                        || Math.abs(this.temptingPlayer.rotationYaw - this.yaw) > 5.0D) {
                    return false;
                }
            } else {
                this.targetX = this.temptingPlayer.getPosX();
                this.targetY = this.temptingPlayer.getPosY();
                this.targetZ = this.temptingPlayer.getPosZ();
            }

            this.pitch = this.temptingPlayer.rotationPitch;
            this.yaw = this.temptingPlayer.rotationYaw;
        }

        return this.shouldExecute(worldIn, entityIn);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.targetX = this.temptingPlayer.getPosX();
        this.targetY = this.temptingPlayer.getPosY();
        this.targetZ = this.temptingPlayer.getPosZ();
        this.isRunning = true;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.temptingPlayer = null;
        entityIn.getNavigator().clearPath();
        this.delayTemptCounter = 100;
        this.isRunning = false;
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        owner.getLookController().setLookPositionWithEntity(this.temptingPlayer,
                owner.getHorizontalFaceSpeed() + 20, owner.getVerticalFaceSpeed());

        if (owner.getDistanceSq(this.temptingPlayer) < 6.25D) {
            owner.getNavigator().clearPath();
        } else {
            owner.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
        }
    }
}
