package com.github.quinnfrost.dragontongue.mixin.iceandfire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafDragonAttacks;
import com.github.alexthe666.iceandfire.entity.IafDragonFlightManager;
import com.github.alexthe666.iceandfire.entity.IafDragonLogic;
import com.github.alexthe666.iceandfire.entity.ai.*;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.IafDragonFlightUtil;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.base.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(EntityDragonBase.class)
public abstract class MixinEntityDragonBase extends TameableEntity {
    protected MixinEntityDragonBase(EntityType<? extends TameableEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Shadow(remap = false)
    public abstract int getAgeInDays();



    @Shadow(remap = false)
    protected int blockBreakCounter;

    @Shadow(remap = false)
    protected abstract boolean isIceInWater();

    @Shadow(remap = false)
    public abstract boolean isModelDead();

    @Shadow(remap = false)
    public abstract int getDragonStage();

    @Shadow(remap = false)
    public abstract boolean canMove();

    @Shadow(remap = false)
    public abstract boolean isFlying();

    @Shadow(remap = false)
    protected abstract int calculateDownY();

    @Shadow(remap = false)
    protected abstract boolean isBreakable(BlockPos pos, BlockState state, float hardness);

    @Shadow public abstract void updateParts();

    @Shadow public float prevDragonPitch;

    @Shadow public abstract float getDragonPitch();

    @Shadow private boolean isOverAir;

    @Shadow protected abstract boolean isOverAirLogic();

    @Shadow public IafDragonLogic logic;

    @Shadow public abstract void setBreathingFire(boolean breathing);

    @Shadow public abstract void setDragonPitch(float pitch);

    @Shadow public IafDragonFlightManager flightManager;
    @Shadow public abstract boolean shouldTarget(Entity entity);
    public EntityDragonBase thisInstance;
    public ICapabilityInfoHolder cap = this.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(this));

    @Inject(
            method = "registerGoals()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $registerGoals(CallbackInfo ci) {
        roadblock$registerGoals();
        ci.cancel();
    }
    public void roadblock$registerGoals() {
        this.goalSelector.addGoal(0, new DragonAIRide<>((EntityDragonBase)(Object)this));
        this.goalSelector.addGoal(1, new SitGoal(this));
        this.goalSelector.addGoal(2, new DragonAIMate((EntityDragonBase)(Object)this, 1.0D));
        this.goalSelector.addGoal(3, new DragonAIReturnToRoost((EntityDragonBase)(Object)this, 1.0D));
        this.goalSelector.addGoal(4, new DragonAIEscort((EntityDragonBase)(Object)this, 1.0D));
        this.goalSelector.addGoal(5, new DragonAIAttackMelee((EntityDragonBase)(Object)this, 1.5D, false));
        this.goalSelector.addGoal(6, new AquaticAITempt(this, 1.0D, IafItemRegistry.FIRE_STEW, false));
        this.goalSelector.addGoal(7, new DragonAIWander((EntityDragonBase)(Object)this, 1.0D));
        this.goalSelector.addGoal(8, new DragonAIWatchClosest(this, LivingEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new DragonAILookIdle((EntityDragonBase)(Object)this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new DragonAITargetNonTamed((EntityDragonBase)(Object)this, LivingEntity.class, false, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
//                DragonTongue.LOGGER.debug("Getting inner class instance: " + this);
//                DragonTongue.LOGGER.debug("Getting outer class instance: " + MixinEntityDragonBase.this);
                return (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative()) && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
            }
        }));
        this.targetSelector.addGoal(5, new DragonAITarget((EntityDragonBase)(Object)this, LivingEntity.class, true, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
                return entity instanceof LivingEntity && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
            }
        }));
        this.targetSelector.addGoal(6, new DragonAITargetItems((EntityDragonBase)(Object)this, false));
    }

    @Inject(
            method = "tick()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $tick(CallbackInfo ci) {
        roadblock$tick();
        ci.cancel();
    }
    public void roadblock$tick() {
        super.tick();
        recalculateSize();
        updateParts();
        this.prevDragonPitch = getDragonPitch();
        world.getProfiler().startSection("dragonLogic");

        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
            this.stepHeight = 0.5F;
        } else {
            this.stepHeight = Math.max(1.2F, 1.2F + (Math.min(this.getAgeInDays(), 125) - 25) * 1.8F / 100F);
        }

        isOverAir = isOverAirLogic();
        logic.updateDragonCommon();
        if (this.isModelDead()) {
            if (!world.isRemote && world.isAirBlock(new BlockPos(this.getPosX(), this.getBoundingBox().minY, this.getPosZ())) && this.getPosY() > -1) {
                this.move(MoverType.SELF, new Vector3d(0, -0.2F, 0));
            }
            this.setBreathingFire(false);

            float dragonPitch = this.getDragonPitch();
            if (dragonPitch > 0) {
                dragonPitch = Math.min(0, dragonPitch - 5);
                this.setDragonPitch(dragonPitch);
            }
            if (dragonPitch < 0) {
                this.setDragonPitch(Math.max(0, dragonPitch + 5));
            }
        } else {
            if (world.isRemote) {
                logic.updateDragonClient();
            } else {
                logic.updateDragonServer();
                logic.updateDragonAttack();
            }
        }
        world.getProfiler().endSection();
        world.getProfiler().startSection("dragonFlight");
        if (isFlying() && !world.isRemote) {
            this.flightManager.update();
        }
        world.getProfiler().endSection();
    }

    @Inject(
            remap = false,
            method = "breakBlock()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $breakBlock(CallbackInfo ci) {
        roadblock$breakBlock();
        ci.cancel();
    }
    public void roadblock$breakBlock() {
        if (this.blockBreakCounter > 0 || IafConfig.dragonBreakBlockCooldown == 0) {
            --this.blockBreakCounter;
            if (!this.isIceInWater() && (this.blockBreakCounter == 0 || IafConfig.dragonBreakBlockCooldown == 0) && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
                if (IafConfig.dragonGriefing != 2 && (!this.isTamed() || IafConfig.tamedDragonGriefing)) {
                    if (!isModelDead() && this.getDragonStage() >= 3 && (this.canMove() || this.getControllingPassenger() != null)) {
                        final int bounds = 1;//(int)Math.ceil(this.getRenderSize() * 0.1);
                        final int flightModifier =
                                (isFlying() && this.getAttackTarget() != null) ? -1 : 1;
//                        final int yMinus = calculateDownY();
                        int yMinus = calculateDownY();
                        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
                            yMinus = 0;
                        }
                        BlockPos.getAllInBox(
                                (int) Math.floor(this.getBoundingBox().minX) - bounds,
                                (int) Math.floor(this.getBoundingBox().minY) + yMinus,
                                (int) Math.floor(this.getBoundingBox().minZ) - bounds,
                                (int) Math.floor(this.getBoundingBox().maxX) + bounds,
                                (int) Math.floor(this.getBoundingBox().maxY) + bounds + flightModifier,
                                (int) Math.floor(this.getBoundingBox().maxZ) + bounds
                        ).forEach(pos -> {
                            if (MinecraftForge.EVENT_BUS.post(new GenericGriefEvent(this, pos.getX(), pos.getY(), pos.getZ())))
                                return;
                            final BlockState state = world.getBlockState(pos);
                            final float hardness = IafConfig.dragonGriefing == 1 || this.getDragonStage() <= 3 ? 2.0F : 5.0F;
                            if (isBreakable(pos, state, hardness)) {
                                this.setMotion(this.getMotion().mul(0.6F, 1, 0.6F));
                                if (!world.isRemote) {
                                    if (rand.nextFloat() <= IafConfig.dragonBlockBreakingDropChance && DragonUtils.canDropFromDragonBlockBreak(state)) {
                                        world.destroyBlock(pos, true);
                                    } else {
                                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    @Mixin(targets = "com.github.alexthe666.iceandfire.entity.IafDragonFlightManager$FlightMoveHelper")
    public abstract static class MixinFlightMoveHelper extends MovementController {
        public MixinFlightMoveHelper(MobEntity mob) {
            super(mob);
        }

        /*
        Detouring state
        0: no detour
        1: climbing
        2: flying over the terrain
         */
        private int detourState;
        private Vector3d detourTarget;

        private EntityDragonBase dragon;


        @Inject(
                remap = false,
                method = "<init>(Lcom/github/alexthe666/iceandfire/entity/EntityDragonBase;)V",
                at = @At(value = "RETURN")
        )
        public void $EntityDragonBase(EntityDragonBase dragonBase, CallbackInfo ci) {
            detourState = 0;
            detourTarget = null;
        }

        @Inject(
                method = "tick",
                at = @At(value = "HEAD"),
                cancellable = true
        )
        public void $tick(CallbackInfo ci) {
            roadblock$tick();
            ci.cancel();
        }
        public void roadblock$tick() {
            util.mixinDebugger();
            ICapabilityInfoHolder cap = dragon.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl());
            EnumCommandSettingType.CommandStatus commandStatus = cap.getCommandStatus();
            Vector3d flightTarget = dragon.flightManager.getFlightTarget();

            float distToX = (float) (flightTarget.x - dragon.getPosX());
            float distToY = (float) (flightTarget.y - dragon.getPosY());
            float distToZ = (float) (flightTarget.z - dragon.getPosZ());

            // Try to avoid the CFIT issue
            // Every 1 second (or collided already) the dragon check if there is terrain between her and the target
            if ((detourState == 0 && dragon.world.getGameTime() % 20 == 0) || dragon.collidedHorizontally) {
                if (commandStatus != EnumCommandSettingType.CommandStatus.STAY && commandStatus != EnumCommandSettingType.CommandStatus.HOVER
                        && !util.hasArrived(dragon, new BlockPos(flightTarget), (double) dragon.getRenderSize())) {
                    BlockRayTraceResult blockRayTraceResult = util.rayTraceBlock(dragon.world, dragon.getPositionVec(), dragon.flightManager.getFlightTarget());
                    // If there is, she will find a higher place where the target is directly in her sight
                    if (!dragon.world.isAirBlock(blockRayTraceResult.getPos())) {
                        BlockPos preferredFlightPos = IafDragonFlightUtil.highestBlockOnPath(dragon.world, dragon.getPositionVec(), flightTarget, 0).add(0, 2 * dragon.getYNavSize(), 0);

                        // And take a detour to reach her target
                        detourState = 1;
                        if (preferredFlightPos.getY() <= 400) {
                            detourTarget = Vector3d.copyCentered(preferredFlightPos);
                        } else {
                            detourTarget = new Vector3d(preferredFlightPos.getX(), 400, preferredFlightPos.getZ());
                        }
                    }
                }
            }
            if (detourState != 0) {
                distToX = (float) (detourTarget.x - dragon.getPosX());
                distToY = (float) (detourTarget.y - dragon.getPosY());
                distToZ = (float) (detourTarget.z - dragon.getPosZ());
                // Detour state 1: try reach the top of the terrain
                if (detourState == 1 && detourTarget != null) {
                    if (dragon.getPositionVec().y >= detourTarget.y) {
                        detourState = 2;
                        detourTarget = detourTarget.add(
                                (flightTarget.x - detourTarget.x) / 2,
                                0,
                                (flightTarget.z - detourTarget.z) / 2
                        );
                    }
                }
                // Detour state 2: try fly over the terrain (by travel half of the distance in high air)
                if (detourState == 2 && detourTarget != null) {
                    if (dragon.getPositionVec().y >= detourTarget.y
                            && util.hasArrived(dragon, new BlockPos(detourTarget), (double) (dragon.getYNavSize() * 2))) {
                        detourState = 0;
                        detourTarget = null;
                    }
                }
            }

            // Following logic makes dragon actually fly to the target, it's not touched except the name
            // The shortest possible distance to the target plane (parallel to y)
            double xzPlaneDist = MathHelper.sqrt(distToX * distToX + distToZ * distToZ);
            // f = 1 - |0.7 * Y| / sqrt(X^2+Y^2)
            double yDistMod = 1.0D - (double) MathHelper.abs(distToY * 0.7F) / xzPlaneDist;
            distToX = (float) ((double) distToX * yDistMod);
            distToZ = (float) ((double) distToZ * yDistMod);
            xzPlaneDist = MathHelper.sqrt(distToX * distToX + distToZ * distToZ);
            double distToTarget = MathHelper.sqrt(distToX * distToX + distToZ * distToZ + distToY * distToY);
            if (distToTarget > 1.0F) {
                float oldYaw = dragon.rotationYaw;
                // Theta = atan2(y,x) - the angle of (x,y)
                float targetYaw = (float) MathHelper.atan2(distToZ, distToX);
                float currentYawTurn = MathHelper.wrapDegrees(dragon.rotationYaw + 90);
                // Radian to degree
                float targetYawDegrees = MathHelper.wrapDegrees(targetYaw * 57.295776F);
                dragon.rotationYaw = IafDragonFlightManager.approachDegrees(currentYawTurn, targetYawDegrees, dragon.airAttack == IafDragonAttacks.Air.TACKLE && dragon.getAttackTarget() != null ? 10 : 4.0F) - 90.0F;
                dragon.renderYawOffset = dragon.rotationYaw;
                if (IafDragonFlightManager.degreesDifferenceAbs(oldYaw, dragon.rotationYaw) < 3.0F) {
                    speed = IafDragonFlightManager.approach((float) speed, 1.8F, 0.005F * (1.8F / (float) speed));
                } else {
                    speed = IafDragonFlightManager.approach((float) speed, 0.2F, 0.025F);
                    if (distToTarget < 100D && dragon.getAttackTarget() != null) {
                        speed = speed * (distToTarget / 100D);
                    }
                }
                float finPitch = (float) (-(MathHelper.atan2(-distToY, xzPlaneDist) * 57.2957763671875D));
                dragon.rotationPitch = finPitch;
                float yawTurnHead = dragon.rotationYaw + 90.0F;
                speed *= dragon.getFlightSpeedModifier();
                speed *= detourState == 0
                        ? Math.min(1, distToTarget / 50 + 0.3)  //Make the dragon fly slower when close to target
                        : 1;    // Do not limit speed when detouring
                double lvt_16_1_ = speed * MathHelper.cos(yawTurnHead * 0.017453292F) * Math.abs((double) distToX / distToTarget);
                double lvt_18_1_ = speed * MathHelper.sin(yawTurnHead * 0.017453292F) * Math.abs((double) distToZ / distToTarget);
                double lvt_20_1_ = speed * MathHelper.sin(finPitch * 0.017453292F) * Math.abs((double) distToY / distToTarget);
                double motionCap = 0.2D;
                dragon.setMotion(dragon.getMotion().add(Math.min(lvt_16_1_ * 0.2D, motionCap), Math.min(lvt_20_1_ * 0.2D, motionCap), Math.min(lvt_18_1_ * 0.2D, motionCap)));
            }

        }
    }

}
