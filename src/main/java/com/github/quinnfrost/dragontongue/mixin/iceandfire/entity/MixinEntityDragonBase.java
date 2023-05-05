package com.github.quinnfrost.dragontongue.mixin.iceandfire.entity;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.IFlyingMount;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathingStuckHandler;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.capability.CapabilityInfoHolderImpl;
import com.github.quinnfrost.dragontongue.capability.ICapabilityInfoHolder;
import com.github.quinnfrost.dragontongue.enums.EnumCommandSettingType;
import com.github.quinnfrost.dragontongue.iceandfire.ai.*;
import com.github.quinnfrost.dragontongue.iceandfire.*;
import com.github.quinnfrost.dragontongue.iceandfire.ai.brain.RegistryBrains;
import com.github.quinnfrost.dragontongue.iceandfire.container.ContainerDragon;
import com.github.quinnfrost.dragontongue.utils.util;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;

@Mixin(EntityDragonBase.class)
public abstract class MixinEntityDragonBase extends TamableAnimal {
    protected MixinEntityDragonBase(EntityType<? extends TamableAnimal> type, Level worldIn) {
        super(type, worldIn);
    }

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
    protected abstract PathNavigation createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type);

    @Shadow
    protected abstract PathNavigation createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler);

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

    @Shadow
    protected int flyHovering;
    @Shadow
    protected int fireTicks;
    @Shadow
    public float sleepProgress;
    @Shadow
    public static Animation ANIMATION_SHAKEPREY;

    @Shadow
    public abstract int getArmorOrdinal(ItemStack stack);

    @Shadow
    public String prevArmorResLoc;
    @Shadow
    public String armorResLoc;
    @Shadow
    public DragonType dragonType;
    @Shadow
    public double maximumHealth;
    @Shadow
    public double minimumHealth;
    @Shadow
    public double minimumDamage;
    @Shadow
    public double maximumDamage;
    @Shadow
    public double maximumSpeed;
    @Shadow
    public double maximumArmor;
    @Shadow
    public double minimumSpeed;
    @Shadow
    public double minimumArmor;
    @Shadow
    @Final
    private static UUID ARMOR_MODIFIER_UUID;

    @Shadow
    protected abstract double calculateArmorModifier();

    @Shadow
    public abstract void breakBlock();

    @Shadow
    public abstract BlockPos getRestrictCenter();

    @Shadow
    public abstract int getAgeInDays();

    @Shadow
    public boolean hasHomePosition;
    @Shadow
    public int flyTicks;

    @Shadow
    public abstract boolean isGoingDown();

    @Shadow
    public float flyProgress;

    @Shadow
    public abstract boolean isChained();

    @Shadow
    public abstract int getCommand();

    @Shadow
    public abstract boolean isHovering();

//    @Shadow
//    public abstract boolean useFlyingPathFinder();

    @Shadow
    public abstract boolean isGoingUp();

    @Shadow
    @Nullable
    public abstract Player getRidingPlayer();

    @Shadow
    public abstract double getFlightSpeedModifier();

    @Shadow
    protected abstract void setStateField(int i, boolean newState);

    @Shadow
    @Nullable
    public abstract Entity getControllingPassenger();

    @Shadow
    public abstract void travel(@NotNull Vec3 Vector3d);

    @Shadow
    public abstract void incrementDragonPitch(float pitch);

    @Shadow
    protected abstract boolean isOverAir();

    @Shadow
    public abstract void decrementDragonPitch(float pitch);

    @Shadow
    public float sitProgress;
    @Shadow
    public float modelDeadProgress;
    @Shadow
    public float hoverProgress;

    @Shadow
    public abstract float getRenderSize();

    @Shadow
    protected abstract void updatePreyInMouth(Entity prey);

    @Shadow
    public int ticksStill;
    @Shadow
    public int hoverTicks;

    @Shadow public abstract boolean isStriking();

    @Shadow public abstract boolean isTackling();

    @Shadow public abstract boolean isAttacking();

    @Shadow public abstract void setTackling(boolean tackling);

    @Shadow public abstract boolean isBreathingFire();

    public ICapabilityInfoHolder cap = this.getCapability(CapabilityInfoHolder.TARGET_HOLDER).orElse(new CapabilityInfoHolderImpl(this));

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,

            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,

            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN

    );
    private static final ImmutableList<SensorType<? extends Sensor<? super EntityDragonBase>>> SENSOR_TYPES = ImmutableList.of(
//            RegistryBrains.SENSOR_TEST,
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS

    );


    @Inject(
            method = "<init>",
            at = @At(value = "RETURN")
    )
    public void $EntityDragonBase(EntityType t, Level world, DragonType type, double minimumDamage, double maximumDamage, double minimumHealth, double maximumHealth, double minimumSpeed, double maximumSpeed, CallbackInfo ci) {
//        this.minimumSpeed = 0.18d;
//        this.maximumSpeed = 0.45d;
        this.minimumArmor = 1D;
        this.maximumArmor = 20D;

//        this.sensing = new Sensing(this);

        NbtOps nbtdynamicops = NbtOps.INSTANCE;
        this.brain = this.makeBrain(new Dynamic<>(nbtdynamicops, nbtdynamicops.createMap(ImmutableMap.of(nbtdynamicops.createString("memories"), nbtdynamicops.emptyMap()))));

        this.flightManager = new IafAdvancedDragonFlightManager((EntityDragonBase) (Object) this);
        this.logic = new IafAdvancedDragonLogic((EntityDragonBase) (Object) this);

        this.setPathfindingMalus(BlockPathTypes.FENCE, 0.0F);

    }

    @Override
    public void setDeltaMovement(Vec3 pMotion) {
        double deltaX = pMotion.x - this.getDeltaMovement().x;
        double deltaY = pMotion.y - this.getDeltaMovement().y;
        double deltaZ = pMotion.z - this.getDeltaMovement().z;
        if (this.getControllingPassenger() != null) {
            util.mixinDebugger(deltaX, deltaY, deltaZ, pMotion, this.getControllingPassenger());
        }
        super.setDeltaMovement(pMotion);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        breakBlock();

        if (this.hasHomePosition) {
            this.getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level.dimension(), this.getRestrictCenter()));
        }

        this.level.getProfiler().push("dragonBrain");
        this.getBrain().tick((ServerLevel) this.level, (EntityDragonBase) (Object) this);
        this.level.getProfiler().pop();
    }

    public Brain<EntityDragonBase> getBrain() {
        return (Brain<EntityDragonBase>) super.getBrain();
    }

    protected Brain.Provider<EntityDragonBase> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamicIn) {
        Brain<EntityDragonBase> brain = this.brainProvider().makeBrain(dynamicIn);
//        this.initBrain(brain);
        return brain;
    }

    private void initBrain(Brain<EntityDragonBase> dragonBrain) {
        dragonBrain.setSchedule(RegistryBrains.SCHEDULE_DEFAULT);

        // Core activity should be the very basic activity, handles the basic movement when the specific memory item is set
        dragonBrain.addActivity(Activity.CORE, RegistryBrains.core());
        // Other activities should only set correspond memory item base on condition
//        dragonBrain.registerActivity(RegistryBrains.ACTIVITY_DRAGON_DEFAULT, RegistryBrains.vanilla());
        dragonBrain.addActivity(RegistryBrains.ACTIVITY_IDLE, RegistryBrains.idle());
        dragonBrain.addActivity(RegistryBrains.ACTIVITY_ATTACK, RegistryBrains.attack());

        dragonBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        dragonBrain.setDefaultActivity(RegistryBrains.ACTIVITY_IDLE);
        dragonBrain.useDefaultActivity();

        dragonBrain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
    }

    @Override
    public boolean isPushable() {
        if (!super.isPushable()) {
            return false;
        }
        return this.getDragonStage() < 4;
    }

    @Override
    protected void pushEntities() {
        Vec3 oldMotion = this.getDeltaMovement();
        super.pushEntities();
        if (!isPushable()) {
            this.setDeltaMovement(oldMotion);
        }
    }

    @Override
    public boolean isInWater() {
//        return super.isInWater() && this.wasEyeInWater;
        return super.isInWater() && this.getFluidHeight(FluidTags.WATER) > Mth.floor(this.getDragonStage() / 2.0f);
//        return super.isInWater();
    }

    /**
     * @author
     * @reason Temp use for hovering dragons to update flying navigator, in order to remove the gap between the command and action
     *          Ice dragons override this method, so it should not be working for ice dragons according to mixin docs.
     *          Todo: remove this
     */
    @Overwrite(remap = false)
    public boolean useFlyingPathFinder() {
        return (this.isFlying() || this.isHovering()) && this.getControllingPassenger() == null;
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
                return (!(entity instanceof Player) || !((Player) entity).isCreative())
                        && DragonUtils.canHostilesTarget(entity)
                        && DragonUtils.isAlive(entity)
                        && util.isHostile(entity);
            }
        }));

//        this.goalSelector.addGoal(0, new DragonAIRide<>((EntityDragonBase) (Object) this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new DragonAIMate((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(3, new DragonAIReturnToRoost((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(4, new DragonAIEscort((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(5, new DragonAIAttackMelee((EntityDragonBase) (Object) this, 1.25D, false));
        this.goalSelector.addGoal(6, new AquaticAITempt((EntityDragonBase) (Object) this, 1.0D, IafItemRegistry.FIRE_STEW, false));
        this.goalSelector.addGoal(7, new DragonAIWander((EntityDragonBase) (Object) this, 1.0D));
        this.goalSelector.addGoal(8, new DragonAIWatchClosest(this, LivingEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new DragonAILookIdle((EntityDragonBase) (Object) this));

        this.targetSelector.addGoal(1, new DragonAIOwnerTarget((EntityDragonBase) (Object) this));
        this.targetSelector.addGoal(2, new DragonAIDefendOwner((EntityDragonBase) (Object) this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new DragonAITargetNonTamed<>((EntityDragonBase) (Object) this, LivingEntity.class, false, new Predicate<LivingEntity>() {
            @Override
            public boolean apply(@Nullable LivingEntity entity) {
//                DragonTongue.LOGGER.debug("Getting inner class instance: " + this);
//                DragonTongue.LOGGER.debug("Getting outer class instance: " + MixinEntityDragonBase.this);
                return (!(entity instanceof Player) || !((Player) entity).isCreative()) && DragonUtils.canHostilesTarget(entity) && entity.getType() != MixinEntityDragonBase.this.getType() && MixinEntityDragonBase.this.shouldTarget(entity) && DragonUtils.isAlive(entity);
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
            method = "createNavigator(Lnet/minecraft/world/level/Level;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/AdvancedPathNavigate$MovementType;Lcom/github/alexthe666/iceandfire/pathfinding/raycoms/PathingStuckHandler;FF)Lnet/minecraft/world/entity/ai/navigation/PathNavigation;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler, float width, float height, CallbackInfoReturnable<PathNavigation> cir) {
        cir.setReturnValue(roadblock$createNavigator(worldIn, type, stuckHandler, width, height));
        cir.cancel();
    }

    protected PathNavigation roadblock$createNavigator(Level worldIn, AdvancedPathNavigate.MovementType type, PathingStuckHandler stuckHandler, float width, float height) {
        IafAdvancedDragonPathNavigator newNavigator = new IafAdvancedDragonPathNavigator((EntityDragonBase) (Object) this, level, IafAdvancedDragonPathNavigator.MovementType.valueOf(type.name()), width, height);
        this.navigation = newNavigator;
        newNavigator.setCanFloat(true);

        newNavigator.getPathingOptions().withJumpCost(0.7).withSwimCost(0.7);

        newNavigator.getNodeEvaluator().setCanOpenDoors(true);
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
            this.moveControl = new IafAdvancedDragonMoveController.GroundMoveHelper(this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.WALKING, createStuckHandler().withTeleportSteps(5));
            this.navigatorType = 0;
            this.setFlying(false);
            this.setHovering(false);
        } else if (navigatorType == 1) {
            this.moveControl = new IafAdvancedDragonMoveController.FlightMoveHelper((EntityDragonBase) (Object) this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.FLYING);
            this.navigatorType = 1;
        } else {
            this.moveControl = new IafAdvancedDragonMoveController.PlayerFlightMoveHelper<>((EntityDragonBase) (Object) this);
            this.navigation = createNavigator(level, AdvancedPathNavigate.MovementType.FLYING);
            this.navigatorType = 2;
        }
    }

    @Inject(
            remap = false,
            method = "openInventory",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $openGUI(Player playerEntity, CallbackInfo ci) {
//        roadblock$openGUI(playerEntity);
//        ci.cancel();
    }

    public void roadblock$openGUI(Player playerEntity) {
        ContainerDragon.openGui(playerEntity, this);
    }

    @Inject(
            remap = false,
            method = "updateAttributes",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$updateAttributes(CallbackInfo ci) {
        head$updateAttributes();
        ci.cancel();
    }

    protected void head$updateAttributes() {
        prevArmorResLoc = armorResLoc;
        final int armorHead = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.HEAD));
        final int armorNeck = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.CHEST));
        final int armorLegs = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.LEGS));
        final int armorFeet = this.getArmorOrdinal(this.getItemBySlot(EquipmentSlot.FEET));
        armorResLoc = dragonType.getName() + "|" + armorHead + "|" + armorNeck + "|" + armorLegs + "|" + armorFeet;
        IceAndFire.PROXY.updateDragonArmorRender(armorResLoc);
        if (this.getAgeInDays() <= 125) {
            final double healthStep = (maximumHealth - minimumHealth) / 125F;
            final double attackStep = (maximumDamage - minimumDamage) / 125F;
            final double speedStep = (maximumSpeed - minimumSpeed) / 125F;
            final double armorStep = (maximumArmor - minimumArmor) / 125F;

            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.round(minimumHealth + (healthStep * this.getAgeInDays())));
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.round(minimumDamage + (attackStep * this.getAgeInDays())));
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(minimumSpeed + (speedStep * this.getAgeInDays()));
            final double baseValue = minimumArmor + (armorStep * this.getAgeInDays());
            this.getAttribute(Attributes.ARMOR).setBaseValue(baseValue);
            if (!this.level.isClientSide) {
                this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
                this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Dragon armor bonus", calculateArmorModifier(), AttributeModifier.Operation.ADDITION));
            }
        }
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(Math.min(2048, IafConfig.dragonTargetSearchLength));
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
        final EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            switch (getArmorOrdinal(getItemBySlot(slot))) {
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
            if ((this.blockBreakCounter == 0 || IafConfig.dragonBreakBlockCooldown == 0) && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
                if (IafConfig.dragonGriefing != 2 && (!this.isTame() || IafConfig.tamedDragonGriefing)) {
                    if (!isModelDead() && this.getDragonStage() >= 3 && (this.canMove() || this.getControllingPassenger() != null)) {
                        final int bounds = 1;//(int)Math.ceil(this.getRenderSize() * 0.1);
                        final int flightModifier =
                                (isFlying() && this.getTarget() != null) ? -1 : 1;
//                        final int yMinus = calculateDownY();
                        int yMinus = calculateDownY();
                        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
                            yMinus = 0;
                        }
                        BlockPos.betweenClosedStream(
                                (int) Math.floor(this.getBoundingBox().minX) - bounds,
                                (int) Math.floor(this.getBoundingBox().minY) + yMinus,
                                (int) Math.floor(this.getBoundingBox().minZ) - bounds,
                                (int) Math.floor(this.getBoundingBox().maxX) + bounds,
                                (int) Math.floor(this.getBoundingBox().maxY) + bounds + flightModifier,
                                (int) Math.floor(this.getBoundingBox().maxZ) + bounds
                        ).forEach(pos -> {
                            if (MinecraftForge.EVENT_BUS.post(new GenericGriefEvent(this, pos.getX(), pos.getY(), pos.getZ())))
                                return;
                            final BlockState state = level.getBlockState(pos);
                            final float hardness = IafConfig.dragonGriefing == 1 || this.getDragonStage() <= 3 ? 2.0F : 5.0F;
                            if (isBreakable(pos, state, hardness)) {
                                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6F, 1, 0.6F));
                                if (!level.isClientSide) {
                                    if (random.nextFloat() <= IafConfig.dragonBlockBreakingDropChance && DragonUtils.canDropFromDragonBlockBreak(state)) {
                                        level.destroyBlock(pos, true);
                                    } else {
                                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
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
            remap = false,
            method = "Lcom/github/alexthe666/iceandfire/entity/EntityDragonBase;doesWantToLand()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void roadblock$doesWantToLand(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(head$doesWantToLand());
        cir.cancel();
    }

    public boolean head$doesWantToLand() {
        return this.flyTicks > 6000 && cap.getCommandStatus() == EnumCommandSettingType.CommandStatus.NONE && this.getCommand() == 0 || isGoingDown() || flyTicks > 40 && this.flyProgress == 0 || this.isChained() && flyTicks > 100;
    }

    @Override
    public float getStepHeight() {
        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
            return 0.5F;
        } else {
            return Math.max(1.2F, 1.2F + (Math.min(this.getAgeInDays(), 125) - 25) * 1.8F / 100F);
        }
    }

//    @ModifyConstant(
//            method = "tick",
//            constant = @Constant(floatValue = 1.2f, ordinal = 0)
//    )
//    public float injectedStepHeight(float constant) {
//        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
//            return 0.5F;
//        } else {
//            return Math.max(1.2F, 1.2F + (Math.min(this.getAgeInDays(), 125) - 25) * 1.8F / 100F);
//        }
//    }

    @Inject(
            method = "tick()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void $tick(CallbackInfo ci) {
//        roadblock$tick();
//        ci.cancel();
    }

    public void roadblock$tick() {
        super.tick();
        refreshDimensions();
        updateParts();
        this.prevDragonPitch = getDragonPitch();
        level.getProfiler().push("dragonLogic");

        if (cap.getObjectSetting(EnumCommandSettingType.DESTROY_TYPE) == EnumCommandSettingType.DestroyType.DELIBERATE) {
            this.maxUpStep = 0.5F;
        } else {
            this.maxUpStep = Math.max(1.2F, 1.2F + (Math.min(this.getAgeInDays(), 125) - 25) * 1.8F / 100F);
        }

        isOverAir = isOverAirLogic();
        logic.updateDragonCommon();
        if (this.isModelDead()) {
            if (!level.isClientSide && level.isEmptyBlock(new BlockPos(this.getX(), this.getBoundingBox().minY, this.getZ())) && this.getY() > -1) {
                this.move(MoverType.SELF, new Vec3(0, -0.2F, 0));
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
            if (level.isClientSide) {
                logic.updateDragonClient();
            } else {
                logic.updateDragonServer();
                logic.updateDragonAttack();
            }
        }
        level.getProfiler().pop();
        level.getProfiler().push("dragonFlight");
        if (isControlledByLocalInstance() && useFlyingPathFinder() && !level.isClientSide) {
            this.flightManager.update();
        }
        level.getProfiler().pop();
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
        if (random.nextBoolean()) {
            if (this.getAnimation() != ANIMATION_EPIC_ROAR) {
                this.setAnimation(ANIMATION_EPIC_ROAR);
                this.playSound(this.getRoarSound(), this.getSoundVolume() + 3 + Math.max(0, this.getDragonStage() - 2), this.getVoicePitch() * 0.7F);
            }
            if (this.getDragonStage() > 3) {
                final int size = (this.getDragonStage() - 3) * 30;
                final List<Entity> entities = level.getEntities(this, this.getBoundingBox().expandTowards(size, size, size));
                for (final Entity entity : entities) {
                    final boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                    if (entity instanceof LivingEntity && !isStrongerDragon) {
                        LivingEntity living = (LivingEntity) entity;
                        if (this.isOwnedBy(living) || this.isOwnersPet(living)) {
                            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50 * size, 0, false, false));
                        } else {
                            if (living.getItemBySlot(EquipmentSlot.HEAD).getItem() != IafItemRegistry.EARPLUGS.get()) {
                                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50 * size, 0, false, false));
                            }
                        }
                    }
                }
            }
        } else {
            if (this.getAnimation() != ANIMATION_ROAR) {
                this.setAnimation(ANIMATION_ROAR);
                this.playSound(this.getRoarSound(), this.getSoundVolume() + 2 + Math.max(0, this.getDragonStage() - 3), this.getVoicePitch());
            }
            if (this.getDragonStage() > 3) {
                final int size = (this.getDragonStage() - 3) * 30;
                final List<Entity> entities = level.getEntities(this, this.getBoundingBox().expandTowards(size, size, size));
                for (final Entity entity : entities) {
                    final boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase) entity).getDragonStage() >= this.getDragonStage();
                    if (entity instanceof LivingEntity && !isStrongerDragon) {
                        LivingEntity living = (LivingEntity) entity;
                        if (this.isOwnedBy(living) || this.isOwnersPet(living)) {
                            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * size, 0, false, false));
                        } else {
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30 * size, 0, false, false));
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
//        cir.setReturnValue(roadblock$isAllowedToTriggerFlight());
//        cir.cancel();
    }

    public boolean roadblock$isAllowedToTriggerFlight() {
        return (this.hasFlightClearance() && this.onGround || this.isInWater()) && !this.isOrderedToSit() && this.getPassengers().isEmpty() && !this.isBaby() && !this.isSleeping() && this.canMove();
    }

    @Inject(
            method = "Lcom/github/alexthe666/iceandfire/entity/EntityDragonBase;isControlledByLocalInstance()Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void head$isControlledByLocalInstance(CallbackInfoReturnable<Boolean> cir) {
//        cir.setReturnValue(super.isControlledByLocalInstance());
//        cir.cancel();
    }

    @Inject(
            method = "travel",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void head$travel(Vec3 Vector3d, CallbackInfo ci) {
//        if (roadblock$travel(Vector3d)) {
//            ci.cancel();
//        }
    }

    private float glidingSpeedBonus = 0;
    public boolean roadblock$travel(Vec3 pTravelVector) {
        if (this.isNoGravity() && !this.isVehicle()) {
            this.setNoGravity(false);
        }
//        util.mixinDebugger(this.yo);
        if (this.isVehicle() && this.canBeControlledByRider()) {

            LivingEntity rider = (LivingEntity) this.getControllingPassenger();
            if (rider == null) {
                return false;
            }

            double forward = rider.zza;
            double strafing = rider.xxa;
            double vertical = 0;
            float speed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            // Try to match the old riding system's speed
            float groundSpeedModifier = 0.7f;
            float airSpeedModifier = (float) (1.8f * getFlightSpeedModifier() * 3);

            int controlMode = 0;
            if (rider.isSprinting()) {
                speed *= 1.5f;
                controlMode = 1;
            }

            if (isHovering() || useFlyingPathFinder()) {
                // In air control
                // Slower on going astern
                forward *= rider.zza > 0 ? 1.0f : 0.5f;
                // Slower on going sideways
                strafing *= 0.5f;
                // Apply speed mod
                speed *= airSpeedModifier;
                this.setNoGravity(true);
                // Set flag for logic and animation
                if (forward > 0) {
                    this.setFlying(true);
                    this.setHovering(false);
                }
                if (this.isAttacking() && this.getXRot() > -5) {
                    this.setTackling(true);
//                } else if (this.getXRot() > 10) {
                    // Todo: diving animation here
                } else {
                    this.setTackling(false);
                }

                switch (controlMode) {
                    case 0 -> {
                        glidingSpeedBonus = 0;
                        // Mouse controlled yaw
                        if (isGoingUp()) {
                            vertical = 0.5f;
                        } else if (isGoingDown()) {
                            vertical = -0.5f;
                        } else {
                            // Damp the vertical motion so the dragon's head is more responsive to the control
                            dampMotion(new Vec3(1f, 0.8f, 1f));
                        }

                        submitTravelVec(strafing, vertical, forward, speed, rider instanceof Player, null);
                    }
                    case 1 -> {
                        // Mouse controlled yaw and pitch
                        strafing *= 0.2f;
                        // Diving is faster
//                        speed += forward * Mth.map(this.getXRot(), -90f, 90f, 0f, 0.5f);
                        glidingSpeedBonus = (float) Mth.clamp(glidingSpeedBonus + this.getDeltaMovement().y * -0.05d, -0.8d, 1.5d);
                        speed += glidingSpeedBonus;
                        // Speed bonus damp
                        glidingSpeedBonus -= glidingSpeedBonus * 0.02d;
                        // Try to match the moving vector to the rider's look vector
                        forward = Mth.abs(Mth.cos(this.getXRot() * ((float) Math.PI / 180F)));
                        vertical = Mth.abs(Mth.sin(this.getXRot() * ((float) Math.PI / 180F)));
                        if (isGoingUp()) {
                            vertical = Math.max(vertical, 0.5);
                        } else if (isGoingDown()) {
                            vertical = Math.min(vertical, -0.5);
                        } else if (this.getXRot() < 0) {
                            vertical *= 1;
                        } else if (this.getXRot() > 0) {
                            vertical *= -1;
                        } else {
//                            dampMotion(new Vec3(1f, 0.65f, 1f));
                        }

                        submitTravelVec(strafing, vertical, forward, speed, rider instanceof Player, new Vec3(0.9, 0.9, 0.9));
                    }
                    // The old fashion ride controller
                    case 2 -> {
                        IFlyingMount mount = (IFlyingMount) this;

                        this.getNavigation().stop();
                        this.setTarget(null);
                        double x = this.getX();
                        double y = this.getY();
                        double z = this.getZ();
                        double _speed = 1.8F * this.getFlightSpeedModifier();
                        Vec3 lookVec = rider.getLookAngle();
                        if (rider.zza < 0) {
                            lookVec = lookVec.yRot((float) Math.PI);
                        } else if (rider.xxa > 0) {
                            lookVec = lookVec.yRot((float) Math.PI * 0.5f);
                        } else if (rider.xxa < 0) {
                            lookVec = lookVec.yRot((float) Math.PI * -0.5f);
                        }
                        if (Math.abs(rider.xxa) > 0.0) {
                            _speed *= 0.25D;
                        }
                        if (rider.zza < 0.0) {
                            _speed *= 0.15D;
                        }
                        if (this.isGoingUp()) {
                            lookVec = lookVec.add(0, 1, 0);
                        } else if (this.isGoingDown()) {
                            lookVec = lookVec.add(0, -1, 0);
                        }
                        if (rider.xxa != 0 || rider.zza != 0) {
                            x += lookVec.x * 10;
                            z += lookVec.z * 10;
                        }
                        if ((this.useFlyingPathFinder() || this.isHovering()) && (this.isGoingUp() || this.isGoingDown())) {
                            y += lookVec.y * ((IFlyingMount) this).getYSpeedMod();
                        }
                        if (!(this.useFlyingPathFinder() || this.isHovering()) && !this.isOnGround()) {
                            y -= 1;
                        }

                        double flySpeed = _speed * 1.5 * IafConfig.dragonFlightSpeedMod;;
                        Vec3 dragonVec = this.position();
                        Vec3 moveVec = new Vec3(x, y, z);
                        Vec3 normalized = moveVec.subtract(dragonVec).normalize();
                        double dist = dragonVec.distanceTo(moveVec);
                        this.setDeltaMovement(normalized.x * flySpeed, normalized.y * flySpeed, normalized.z * flySpeed);
                        if (dist > 2.5E-7) {
                            float yaw = (float) Math.toDegrees(Math.PI * 2 - Math.atan2(normalized.x, normalized.y));
                            this.setYRot(IafAdvancedDragonMoveController.rotlerp(this.getYRot(), yaw, 5));
                            this.setSpeed((float) (_speed));
                        }
                        this.move(MoverType.SELF, this.getDeltaMovement());
                    }


                }
            } else {
                // Walking control
                forward *= rider.zza > 0 ? 1.0f : 0.3f;
                strafing *= 0.1f;
                speed *= groundSpeedModifier;
                this.setNoGravity(false);

                // Inherit y motion for dropping
                vertical = pTravelVector.y;

                submitTravelVec(strafing, vertical, forward, speed, rider instanceof Player, null);
            }

            return true;
        }
        return false;
    }

    private void submitTravelVec(double strafing, double vertical, double forward, float speed, boolean zeroOnServer, Vec3 customFrictionVector) {
        // Speed travel in the air (not the speed of flight)
        this.flyingSpeed = speed * 0.1F;
        if (this.isControlledByLocalInstance()) {
            this.setSpeed(speed);

//            if ((EntityDragonBase)(Object) this instanceof EntityIceDragon && this.isInWater()) {
//                this.moveRelative(this.getSpeed(), new Vec3(strafing, vertical, forward));
//                this.move(MoverType.SELF, this.getDeltaMovement());
//                this.setDeltaMovement(this.getDeltaMovement().scale(0.4D));
//                if (this.getTarget() == null) {
//                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
//                }
//            } else {
//                super.travel(new Vec3(strafing, vertical, forward));
//            }
            super.travel(new Vec3(strafing, vertical, forward));
            if (customFrictionVector != null) {
                float f2 = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getFriction(level, this.getBlockPosBelowThatAffectsMyMovement(), this);
                float f3 = this.onGround ? f2 * 0.91F : 0.91F;
                Vec3 newMotion = this.getDeltaMovement();
                if (this.shouldDiscardFriction()) {
                    this.setDeltaMovement(newMotion.multiply(customFrictionVector));
                } else {
                    this.setDeltaMovement(new Vec3(newMotion.x / (double) f3, newMotion.y / 0.98d, newMotion.z / (double) f3).multiply(customFrictionVector));
                }
            }

        } else if (zeroOnServer) {
            this.setDeltaMovement(Vec3.ZERO);
        }
//            this.calculateEntityAnimation(this, false);
        this.tryCheckInsideBlocks();
        this.updatePitch();
    }

    private void submitMoveVec(Vec3 moveVector, boolean zeroOnServer) {
        if (this.isControlledByLocalInstance()) {
            this.setDeltaMovement(moveVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else if (zeroOnServer) {
            this.setDeltaMovement(Vec3.ZERO);
        }
//        this.calculateEntityAnimation(this, false);
        this.tryCheckInsideBlocks();
        this.updatePitch();
    }

    private void dampMotion(Vec3 factors) {
        this.setDeltaMovement(this.getDeltaMovement().multiply(factors));
    }

    private void submitMoveVec(double deltaX, double deltaY, double deltaZ, float speed, boolean zeroOnServer) {

    }

    private void updatePitch() {
        if (this.isOverAir() && !this.isPassenger()) {
            // For some reason yo does not work
            final double ydist = this.yOld - this.getY();//down 0.4 up -0.38
            if (!this.isHovering()) {
                this.incrementDragonPitch((float) (ydist) * 10);
            }
            this.setDragonPitch(Mth.clamp(this.getDragonPitch(), -60, 40));
            final float plateau = 2;
            final float planeDist = (float) ((Math.abs(this.getDeltaMovement().x) + Math.abs(this.getDeltaMovement().z)) * 6F);
            if (this.getDragonPitch() > plateau) {
                //down
                //this.motionY -= 0.2D;
                this.decrementDragonPitch(planeDist * Math.abs(this.getDragonPitch()) / 90);
            }
            if (this.getDragonPitch() < -plateau) {//-2
                //up
                this.incrementDragonPitch(planeDist * Math.abs(this.getDragonPitch()) / 90);
            }
            if (this.getDragonPitch() > 2F) {
                this.decrementDragonPitch(1);
            } else if (this.getDragonPitch() < -2F) {
                this.incrementDragonPitch(1);
            }
            if (this.getDragonPitch() < -45 && planeDist < 3) {
                if (this.isFlying() && !this.isHovering()) {
                    this.setHovering(true);
                }
            }
        } else {
            this.setDragonPitch(0);
        }
    }

    /**
     * @author
     * @reason
     */
//    @Overwrite
//    public @NotNull Vec3 handleRelativeFrictionAndCalculateMovement(@NotNull Vec3 pDeltaMovement, float pFriction) {
////        if (this.getControllingPassenger() != null)
////            return super.handleRelativeFrictionAndCalculateMovement(pDeltaMovement, 0.4f);
//        return super.handleRelativeFrictionAndCalculateMovement(pDeltaMovement, pFriction);
//    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public Vec3 getRiderPosition() {
        final float sitProg = this.sitProgress * 0.015F;
        final float deadProg = this.modelDeadProgress * -0.02F;
        final float hoverProg = this.hoverProgress * 0.03F;
        final float flyProg = this.flyProgress * 0.01F;
        final float sleepProg = this.sleepProgress * -0.025F;
        final float extraAgeScale = this.getScale() * 0.2F;
        float pitchX = 0F;
        float pitchY = 0F;
        final float dragonPitch = getDragonPitch();
        if (dragonPitch > 0) {
            pitchX = Math.min(dragonPitch / 90, 0.3F);
            pitchY = -(dragonPitch / 90) * 2F;
        } else if (dragonPitch < 0) {//going up
            pitchY = (dragonPitch / 90) * 0.1F;
            pitchX = Math.max(dragonPitch / 90, -0.7F);
        }
        final float xzMod = (0.15F + pitchX) * getRenderSize() + extraAgeScale;
        final float headPosX = (float) (getX() + (xzMod) * Mth.cos((float) ((getYRot() + 90) * Math.PI / 180)));
        final float headPosY = (float) (getY() + (0.7F + sitProg + hoverProg + deadProg + sleepProg + flyProg + pitchY) * getRenderSize() * (getAgeInDays() / 100.0f) * 0.3F + extraAgeScale);
        final float headPosZ = (float) (getZ() + (xzMod) * Mth.sin((float) ((getYRot() + 90) * Math.PI / 180)));
        return new Vec3(headPosX, headPosY, headPosZ);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public void positionRider(@NotNull Entity passenger) {
        super.positionRider(passenger);
        if (this.hasPassenger(passenger)) {
            if (this.getControllingPassenger() == null || !this.getControllingPassenger().getUUID().equals(passenger.getUUID())) {
                updatePreyInMouth(passenger);
            } else {
                if (this.isModelDead()) {
                    passenger.stopRiding();
                }

                if (passenger instanceof Player rider
                        && !this.isAttacking()
                        && !this.isBreathingFire()
                        && Mth.abs(rider.zza) < 0.05
                        && this.getDeltaMovement().x == 0.0f
                        && this.getDeltaMovement().z == 0.0f
                ) {
                } else {
                    this.setYRot(passenger.getYRot());
                    this.setYHeadRot(passenger.getYHeadRot());
                    this.setXRot(passenger.getXRot());
                }

                Vec3 riderPos = this.getRiderPosition();
                passenger.setPos(riderPos.x, riderPos.y + passenger.getBbHeight(), riderPos.z);
            }
        }
    }

    /**
     * @author
     * @reason
     */
//    @Overwrite(remap = false)
//    public boolean isDiving() {
//        return isFlying() && this.getXRot() > 10 && this.getControllingPassenger() != null;
//    }

}
