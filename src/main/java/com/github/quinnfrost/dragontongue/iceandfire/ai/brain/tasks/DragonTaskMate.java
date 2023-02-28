package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonEgg;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class DragonTaskMate extends Task<EntityDragonBase> {
    private static final BlockState NEST = IafBlockRegistry.NEST.getDefaultState();
    int spawnBabyDelay;
    double moveSpeed;
    private EntityDragonBase targetMate;
    public DragonTaskMate(int durationMinIn, int durationMaxIn, double speedIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
        this.moveSpeed = speedIn;
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, EntityDragonBase owner) {
        if (!owner.isInLove() || !owner.canMove()) {
            return false;
        } else {
            this.targetMate = this.getNearbyMate(worldIn, owner);
            return this.targetMate != null;
        }
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return this.targetMate.isAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
    }

    @Override
    protected void updateTask(ServerWorld worldIn, EntityDragonBase owner, long gameTime) {
        owner.getLookController().setLookPositionWithEntity(this.targetMate, 10.0F, owner.getVerticalFaceSpeed());
        owner.getNavigator().tryMoveToXYZ(targetMate.getPosX(), targetMate.getPosY(), targetMate.getPosZ(), this.moveSpeed);
        owner.setFlying(false);
        owner.setHovering(false);
        ++this.spawnBabyDelay;
        if (this.spawnBabyDelay >= 60 && owner.getDistance(this.targetMate) < 35) {
            this.spawnBaby(worldIn, owner);
        }
    }

    @Override
    protected void resetTask(ServerWorld worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.targetMate = null;
        this.spawnBabyDelay = 0;
    }

    /**
     * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
     * valid mate found.
     */
    private EntityDragonBase getNearbyMate(ServerWorld worldIn, EntityDragonBase entityIn) {
        List<EntityDragonBase> list = worldIn.getEntitiesWithinAABB(entityIn.getClass(), entityIn.getBoundingBox().grow(180.0D, 180.0D, 180.0D));
        double d0 = Double.MAX_VALUE;
        EntityDragonBase mate = null;
        for (EntityDragonBase partner : list) {
            if (entityIn.canMateWith(partner)) {
                double d1 = entityIn.getDistanceSq(partner);
                if (d1 < d0) { // find min distance
                    mate = partner;
                    d0 = d1;
                }
            }
        }
        return mate;
    }

    /**
     * Spawns a baby animal of the same type.
     */
    private void spawnBaby(ServerWorld worldIn, EntityDragonBase entityIn) {

        EntityDragonEgg egg = entityIn.createEgg(this.targetMate);

        if (egg != null) {
//            PlayerEntity PlayerEntity = entityIn.getLoveCause();
//
//            if (PlayerEntity == null && this.targetMate.getLoveCause() != null) {
//                PlayerEntity = this.targetMate.getLoveCause();
//            }

            entityIn.setGrowingAge(6000);
            this.targetMate.setGrowingAge(6000);
            entityIn.resetInLove();
            this.targetMate.resetInLove();
            int nestX = (int) (entityIn.isMale() ? this.targetMate.getPosX() : entityIn.getPosX());
            int nestY = (int) (entityIn.isMale() ? this.targetMate.getPosY() : entityIn.getPosY()) - 1;
            int nestZ = (int) (entityIn.isMale() ? this.targetMate.getPosZ() : entityIn.getPosZ());

            egg.setLocationAndAngles(nestX - 0.5F, nestY + 1F, nestZ - 0.5F, 0.0F, 0.0F);
            worldIn.addEntity(egg);
            Random random = entityIn.getRNG();

            for (int i = 0; i < 17; ++i) {
                final double d0 = random.nextGaussian() * 0.02D;
                final double d1 = random.nextGaussian() * 0.02D;
                final double d2 = random.nextGaussian() * 0.02D;
                final double d3 = random.nextDouble() * entityIn.getWidth() * 2.0D - entityIn.getWidth();
                final double d4 = 0.5D + random.nextDouble() * entityIn.getHeight();
                final double d5 = random.nextDouble() * entityIn.getWidth() * 2.0D - entityIn.getWidth();
                worldIn.addParticle(ParticleTypes.HEART, entityIn.getPosX() + d3, entityIn.getPosY() + d4, entityIn.getPosZ() + d5, d0, d1, d2);
            }
            BlockPos eggPos = new BlockPos(nestX - 2, nestY, nestZ - 2);
            BlockPos dirtPos = eggPos.add(1, 0, 1);

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos add = eggPos.add(x, 0, z);
                    BlockState prevState = worldIn.getBlockState(add);
                    if (prevState.getMaterial().isReplaceable() || worldIn.getBlockState(add).getMaterial() == Material.EARTH || worldIn.getBlockState(add).getBlockHardness(worldIn, add) < 5F || worldIn.getBlockState(add).getBlockHardness(worldIn, add) >= 0F) {
                        worldIn.setBlockState(add, NEST);
                    }
                }
            }
            if (worldIn.getBlockState(dirtPos).getMaterial().isReplaceable() || worldIn.getBlockState(dirtPos) == NEST) {
                worldIn.setBlockState(dirtPos, Blocks.GRASS_PATH.getDefaultState());
            }
            if (worldIn.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                worldIn.addEntity(new ExperienceOrbEntity(worldIn, entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ(), random.nextInt(15) + 10));
            }
        }
    }
}
