package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathingStuckHandler;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.ai.*;
import com.github.quinnfrost.dragontongue.iceandfire.*;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.base.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

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

    @Shadow(remap = false)
    public abstract void updateParts();

    @Shadow(remap = false)
    public float prevDragonPitch;

    @Shadow(remap = false)
    public abstract float getDragonPitch();

    @Shadow(remap = false)
    private boolean isOverAir;

    @Shadow(remap = false)
    protected abstract boolean isOverAirLogic();

    @Shadow(remap = false)
    public IafDragonLogic logic;

    @Shadow(remap = false)
    public abstract void setBreathingFire(boolean breathing);

    @Shadow(remap = false)
    public abstract void setDragonPitch(float pitch);

    @Shadow(remap = false)
    public IafDragonFlightManager flightManager;

    @Shadow
    protected abstract boolean shouldTarget(Entity entity);

    @Shadow
    public int navigatorType;

    @Shadow
    public abstract void setFlying(boolean flying);

    @Shadow
    public abstract void setHovering(boolean hovering);

    @Shadow
    protected abstract PathingStuckHandler createStuckHandler();

    @Shadow
    protected abstract PathNavigator createNavigator(World worldIn, AdvancedPathNavigate.MovementType type);

    @Shadow
    protected abstract PathNavigator createNavigator(World worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler);

    @Shadow
    public abstract Animation getAnimation();

    @Shadow
    public static Animation ANIMATION_EPIC_ROAR;

    @Shadow
    public abstract void setAnimation(Animation animation);

    @Shadow
    public abstract SoundEvent getRoarSound();

    @Shadow
    protected abstract boolean isOwnersPet(LivingEntity living);

    @Shadow
    public static Animation ANIMATION_ROAR;

    @Shadow
    public abstract boolean hasFlightClearance();

    @Shadow protected int flyHovering;
    @Shadow protected int fireTicks;
    @Shadow public float sleepProgress;
    @Shadow public static Animation ANIMATION_SHAKEPREY;

    @Shadow public abstract int getArmorOrdinal(ItemStack stack);

    public ICapabilityInfoHolder cap = this.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(this));

    @Inject(
            method = "<init>",
            at = @At(value = "RETURN")
    )
    public void $EntityDragonBase(EntityType t, World world, DragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed, CallbackInfo ci) {
        this.flightManager = new IafAdvancedDragonFlightManager((EntityDragonBase) (Object) this);
        this.logic = new IafAdvancedDragonLogic((EntityDragonBase) (Object) this);

        this.setPathPriority(PathNodeType.FENCE, 0.0F);

    }

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
        this.goalSelector.addGoal(0, new DragonAIAsYouWish((EntityDragonBase) (Object) this));
        this.goalSelector.addGoal(0, new DragonAICalmLook((EntityDragonBase) (Object) this));
        this.targetSelector.addGoal(3, new DragonAIGuard<>((EntityDragonBase) (Object) this, LivingEntity.class, false, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
                return (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative())
                        && DragonUtils.canHostilesTarget(entity)
                        && DragonUtils.isAlive(entity)
                        && util.isHostile(entity);
            }
        }));

        this.goalSelector.addGoal(0, new DragonAIRide<>((EntityDragonBase) (Object) this));
        this.goalSelector.addGoal(1, new SitGoal(this));
        this.goalSelector.addGoal(2, new DragonAIMate((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(3, new DragonAIReturnToRoost((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(4, new DragonAIEscort((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(5, new DragonAIAttackMelee((EntityDragonBase) (Object) this, 1.5D, false));
        this.goalSelector.addGoal(6, new AquaticAITempt(this, 1.0D, IafItemRegistry.FIRE_STEW, false));
        this.goalSelector.addGoal(7, new DragonAIWander((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(8, new DragonAIWatchClosest(this, LivingEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new DragonAILookIdle((EntityDragonBase) (Object) this));

        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new DragonAITargetNonTamed((EntityDragonBase) (Object) this, LivingEntity.class, false, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
//                DragonTongue.LOGGER.debug("Getting inner class instance: " + this);
//                DragonTongue.LOGGER.debug("Getting outer class instance: " + MixinEntityDragonBase.this);
                return (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative()) && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
            }
        }));
        this.targetSelector.addGoal(5, new DragonAITarget((EntityDragonBase) (Object) this, LivingEntity.class, true, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
                return entity instanceof LivingEntity && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
            }
        }));
        this.targetSelector.addGoal(6, new DragonAITargetItems((EntityDragonBase) (Object) this, false));
    }

    @Inject(
            remap = false,
            method = "createNavigator(Lnet/minecraft/world/World;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate$MovementType;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/PathingStuckHandler;FF)Lnet/minecraft/pathfinding/PathNavigator;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $createNavigator(World worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler, float width, float height, CallbackInfoReturnable<PathNavigator> cir) {
        cir.setReturnValue(roadblock$createNavigator(worldIn, type, stuckHandler, width, height));
        cir.cancel();
    }

    protected PathNavigator roadblock$createNavigator(World worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler, float width, float height) {
        IafAdvancedDragonPathNavigator newNavigator = new IafAdvancedDragonPathNavigator(this, world, IafAdvancedDragonPathNavigator.MovementType.valueOf(type.name()), width, height);
        this.navigator = newNavigator;
        newNavigator.setCanSwim(true);
        newNavigator.getNodeProcessor().setCanOpenDoors(true);
        return newNavigator;
    }

    @Inject(
            remap = false,
            method = "switchNavigator(I)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $switchNavigator(int navigatorType, CallbackInfo ci) {
        roadblock$switchNavigator(navigatorType);
        ci.cancel();
    }

    protected void roadblock$switchNavigator(int navigatorType) {
        if (navigatorType == 0) {
            this.moveController = new IafAdvancedDragonMoveController.GroundMoveHelper(this);
            this.navigator = createNavigator(world, AdvancedPathNavigate.MovementType.WALKING, createStuckHandler().withTeleportSteps(5));
            this.navigatorType = 0;
            this.setFlying(false);
            this.setHovering(false);
        } else if (navigatorType == 1) {
            this.moveController = new IafAdvancedDragonMoveController.FlightMoveHelper((EntityDragonBase) (Object) this);
            this.navigator = createNavigator(world, AdvancedPathNavigate.MovementType.FLYING);
            this.navigatorType = 1;
        } else {
            this.moveController = new IafAdvancedDragonMoveController.PlayerFlightMoveHelper<>((EntityDragonBase) (Object) this);
            this.navigator = createNavigator(world, AdvancedPathNavigate.MovementType.FLYING);
            this.navigatorType = 2;
        }
    }

    @Inject(
            remap = false,
            method = "openGUI",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $openGUI(PlayerEntity playerEntity, CallbackInfo ci) {
        roadblock$openGUI(playerEntity);
        ci.cancel();
    }

    public void roadblock$openGUI(PlayerEntity playerEntity) {
        ContainerDragon.openGui(playerEntity, this);
    }

    @Inject(
            remap = false,
            method = "calculateArmorModifier",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$calculateArmorModifier(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(head$calculateArmorModifier());
        cir.cancel();
    }
    private double head$calculateArmorModifier() {
        double val = 1D;
        final EquipmentSlotType[] slots = {EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
        for (EquipmentSlotType slot : slots) {
            switch (getArmorOrdinal(getItemStackFromSlot(slot))) {
                case 1:
                    val += 2D;
                    break;
                case 2:
                case 4:
                    val += 3D;
                    break;
                case 3:
                    val += 5D;
                    break;
                case 5:
                case 6:
                case 8:
                    val += 10D;
                    break;
                case 7:
                    val += 1.5D;
                    break;
            }
        }
        return val;
    }

    @Inject(
            remap = false,
            method = "canMove",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$canMove(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(head$canMove());
        cir.cancel();
    }
    public boolean head$canMove() {
        return !this.isQueuedToSit()
                && !this.isSleeping()
                && this.getControllingPassenger() == null
                && !this.isPassenger()
                && !this.isModelDead()
                && sleepProgress == 0
                && this.getAnimation() != ANIMATION_SHAKEPREY;
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
            method = "roar()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $roar(CallbackInfo ci) {
        roadblock$roar();
        ci.cancel();
    }

    public void roadblock$roar() {
        if (EntityGorgon.isStoneMob(this) || this.isModelDead()) {
            return;
        }
        if (rand.nextBoolean()) {
            if (this.getAnimation() != ANIMATION_EPIC_ROAR) {
                this.setAnimation(ANIMATION_EPIC_ROAR);
                this.playSound(this.getRoarSound(), this.getSoundVolume() + 3 + Math.max(0, this.getDragonStage() - 2), this.getSoundPitch() * 0.7F);
            }
            if (this.getDragonStage() > 3) {
                final int size = (this.getDragonStage() - 3) * 30;
                final List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().expand(size, size, size));
                for (final Entity entity : entities) {
                    final boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                    if (entity instanceof LivingEntity && !isStrongerDragon) {
                        LivingEntity living = (LivingEntity) entity;
                        if (this.isOwner(living) || this.isOwnersPet(living)) {
                            living.addPotionEffect(new EffectInstance(Effects.STRENGTH, 50 * size, 0, false, false));
                        } else {
                            if (living.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() != IafItemRegistry.EARPLUGS) {
                                living.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 50 * size, 0, false, false));
                            }
                        }
                    }
                }
            }
        } else {
            if (this.getAnimation() != ANIMATION_ROAR) {
                this.setAnimation(ANIMATION_ROAR);
                this.playSound(this.getRoarSound(), this.getSoundVolume() + 2 + Math.max(0, this.getDragonStage() - 3), this.getSoundPitch());
            }
            if (this.getDragonStage() > 3) {
                final int size = (this.getDragonStage() - 3) * 30;
                final List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().expand(size, size, size));
                for (final Entity entity : entities) {
                    final boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                    if (entity instanceof LivingEntity && !isStrongerDragon) {
                        LivingEntity living = (LivingEntity) entity;
                        if (this.isOwner(living) || this.isOwnersPet(living)) {
                            living.addPotionEffect(new EffectInstance(Effects.STRENGTH, 30 * size, 0, false, false));
                        } else {
                            living.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 30 * size, 0, false, false));
                        }
                    }
                }
            }
        }
    }

    @Inject(
            remap = false,
            method = "isAllowedToTriggerFlight()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $isAllowedToTriggerFlight(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(roadblock$isAllowedToTriggerFlight());
        cir.cancel();
    }

    public boolean roadblock$isAllowedToTriggerFlight() {
        return (this.hasFlightClearance() && this.onGround || this.isInWater()) && !this.isQueuedToSit() && this.getPassengers().isEmpty() && !this.isChild() && !this.isSleeping() && this.canMove();
    }

}
