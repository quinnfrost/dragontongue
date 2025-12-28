package com.github.quinnfrost.dragontongue.iceandfire.ai.brain.tasks.vanilla;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonEgg;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

public class DragonVanillaTaskMate extends Behavior<EntityDragonBase> {
    private static final BlockState NEST = IafBlockRegistry.NEST.defaultBlockState();
    int spawnBabyDelay;
    double moveSpeed;
    private EntityDragonBase targetMate;
    public DragonVanillaTaskMate(int durationMinIn, int durationMaxIn, double speedIn) {
        super(ImmutableMap.of(

        ), durationMinIn, durationMaxIn);
        this.moveSpeed = speedIn;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityDragonBase owner) {
        if (!owner.isInLove() || !owner.canMove()) {
            return false;
        } else {
            this.targetMate = this.getNearbyMate(worldIn, owner);
            return this.targetMate != null;
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        return this.targetMate.isAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityDragonBase owner, long gameTime) {
        owner.getLookControl().setLookAt(this.targetMate, 10.0F, owner.getMaxHeadXRot());
        owner.getNavigation().moveTo(targetMate.getX(), targetMate.getY(), targetMate.getZ(), this.moveSpeed);
        owner.setFlying(false);
        owner.setHovering(false);
        ++this.spawnBabyDelay;
        if (this.spawnBabyDelay >= 60 && owner.distanceTo(this.targetMate) < 35) {
            this.spawnBaby(worldIn, owner);
        }
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityDragonBase entityIn, long gameTimeIn) {
        this.targetMate = null;
        this.spawnBabyDelay = 0;
    }

    /**
     * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
     * valid mate found.
     */
    private EntityDragonBase getNearbyMate(ServerLevel worldIn, EntityDragonBase entityIn) {
        List<EntityDragonBase> list = worldIn.getEntitiesOfClass(entityIn.getClass(), entityIn.getBoundingBox().inflate(180.0D, 180.0D, 180.0D));
        double d0 = Double.MAX_VALUE;
        EntityDragonBase mate = null;
        for (EntityDragonBase partner : list) {
            if (entityIn.canMate(partner)) {
                double d1 = entityIn.distanceToSqr(partner);
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
    private void spawnBaby(ServerLevel worldIn, EntityDragonBase entityIn) {

        EntityDragonEgg egg = entityIn.createEgg(this.targetMate);

        if (egg != null) {
//            PlayerEntity PlayerEntity = entityIn.getLoveCause();
//
//            if (PlayerEntity == null && this.targetMate.getLoveCause() != null) {
//                PlayerEntity = this.targetMate.getLoveCause();
//            }

            entityIn.setAge(6000);
            this.targetMate.setAge(6000);
            entityIn.resetLove();
            this.targetMate.resetLove();
            int nestX = (int) (entityIn.isMale() ? this.targetMate.getX() : entityIn.getX());
            int nestY = (int) (entityIn.isMale() ? this.targetMate.getY() : entityIn.getY()) - 1;
            int nestZ = (int) (entityIn.isMale() ? this.targetMate.getZ() : entityIn.getZ());

            egg.moveTo(nestX - 0.5F, nestY + 1F, nestZ - 0.5F, 0.0F, 0.0F);
            worldIn.addFreshEntity(egg);
            Random random = entityIn.getRandom();

            for (int i = 0; i < 17; ++i) {
                final double d0 = random.nextGaussian() * 0.02D;
                final double d1 = random.nextGaussian() * 0.02D;
                final double d2 = random.nextGaussian() * 0.02D;
                final double d3 = random.nextDouble() * entityIn.getBbWidth() * 2.0D - entityIn.getBbWidth();
                final double d4 = 0.5D + random.nextDouble() * entityIn.getBbHeight();
                final double d5 = random.nextDouble() * entityIn.getBbWidth() * 2.0D - entityIn.getBbWidth();
                worldIn.addParticle(ParticleTypes.HEART, entityIn.getX() + d3, entityIn.getY() + d4, entityIn.getZ() + d5, d0, d1, d2);
            }
            BlockPos eggPos = new BlockPos(nestX - 2, nestY, nestZ - 2);
            BlockPos dirtPos = eggPos.offset(1, 0, 1);

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos add = eggPos.offset(x, 0, z);
                    BlockState prevState = worldIn.getBlockState(add);
                    if (prevState.getMaterial().isReplaceable() || worldIn.getBlockState(add).getMaterial() == Material.DIRT || worldIn.getBlockState(add).getDestroySpeed(worldIn, add) < 5F || worldIn.getBlockState(add).getDestroySpeed(worldIn, add) >= 0F) {
                        worldIn.setBlockAndUpdate(add, NEST);
                    }
                }
            }
            if (worldIn.getBlockState(dirtPos).getMaterial().isReplaceable() || worldIn.getBlockState(dirtPos) == NEST) {
                worldIn.setBlockAndUpdate(dirtPos, Blocks.GRASS_PATH.defaultBlockState());
            }
            if (worldIn.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                worldIn.addFreshEntity(new ExperienceOrb(worldIn, entityIn.getX(), entityIn.getY(), entityIn.getZ(), random.nextInt(15) + 10));
            }
        }
    }
}
